package com.ensolegacy.mobile.data

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.room.withTransaction
import com.ensolegacy.mobile.data.local.BonsaiEntity
import com.ensolegacy.mobile.data.local.CareReminderEntity
import com.ensolegacy.mobile.data.local.EnsoDatabase
import com.ensolegacy.mobile.data.local.MilestoneEntity
import com.ensolegacy.mobile.data.local.PhotoEntity
import com.ensolegacy.mobile.data.local.StageTransitionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Manual local backup: the whole record — all five tables plus every image
 * file — as a single zip in shared storage, so it survives uninstall and can
 * be restored after a reinstall or on a new device.
 *
 * Export writes `enso-backup-<timestamp>.zip` to `Documents/Enso/` via
 * MediaStore. Zip layout:
 *   manifest.json    format version, export time, row counts
 *   data.json        every row of every table
 *   images/<uuid>.jpg  the ImageStore files, under their storage-relative names
 *
 * Restore is a **full replace**: current data is wiped, then rows are
 * re-inserted with their original ids so cross-references (bonsaiId,
 * milestoneId, photo paths) stay intact, and the images are rewritten.
 *
 * MediaStore shared-storage access needs API 29+. Detection is two-tier:
 * [findBackups] (MediaStore, zero-permission) only sees backups written by the
 * current install — ownership is wiped with the app's data — while
 * [findBackupsDirect] (direct file scan, needs the "All files access" grant
 * on API 30+) sees everything, including backups from a previous install.
 * The SAF file picker in Settings is the zero-permission fallback for files
 * neither path can read.
 */
class BackupManager(
    private val context: Context,
    private val db: EnsoDatabase,
    private val imageStore: ImageStore,
) {

    /** A backup zip found on the device. */
    data class BackupFileInfo(val uri: Uri, val displayName: String, val dateAdded: Long)

    /** What a finished export wrote. */
    data class ExportResult(val displayName: String, val trees: Int, val photos: Int)

    /** Row/file counts from a finished restore, for the confirmation message. */
    data class ImportSummary(
        val trees: Int,
        val milestones: Int,
        val photos: Int,
        val reminders: Int,
        val transitions: Int,
    )

    /**
     * Every enso-backup zip in Documents/Enso, newest first. Empty below API 29.
     * The name filter runs in SQL; the folder is matched client-side because
     * path-based WHERE clauses can be mangled by selection rewriting for files
     * the app doesn't own.
     */
    suspend fun findBackups(): List<BackupFileInfo> = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return@withContext emptyList()
        val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.MediaColumns.RELATIVE_PATH,
        )
        val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ?"
        val args = arrayOf("$FILE_PREFIX%")
        val sort = "${MediaStore.MediaColumns.DATE_ADDED} DESC"
        val found = mutableListOf<BackupFileInfo>()
        context.contentResolver.query(collection, projection, selection, args, sort)?.use { c ->
            Log.d(TAG, "findBackups: ${c.count} name-matched row(s)")
            while (c.moveToNext()) {
                val relPath = c.getString(3) ?: continue
                if (!relPath.startsWith("$BACKUP_DIR/")) continue
                found += BackupFileInfo(
                    uri = ContentUris.withAppendedId(collection, c.getLong(0)),
                    displayName = c.getString(1),
                    dateAdded = c.getLong(2),
                )
            }
        }
        Log.d(TAG, "findBackups: ${found.size} in $BACKUP_DIR")
        found
    }

    /**
     * True if [uri] can actually be opened by us. MediaStore ownership only
     * lasts while the app's data does — after a reinstall or data wipe, old
     * backups become invisible/unreadable unless all-files access is granted
     * (see [findBackupsDirect]). The SAF file picker is the zero-permission
     * fallback.
     */
    suspend fun isReadable(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            if (uri.scheme == "file") {
                val f = File(requireNotNull(uri.path))
                f.canRead() && f.inputStream().use { it.read() } != -1
            } else {
                context.contentResolver.openInputStream(uri)?.use { it.read() } != null
            }
        }.getOrDefault(false)
    }

    // --- All-files access (API 30+) ------------------------------------------

    /**
     * Whether the user granted "All files access". This is the only way to
     * see backups left behind by a previous install: scoped storage hides
     * non-media files this app didn't create from [findBackups], and ownership
     * doesn't survive uninstall or a data clear.
     */
    fun hasAllFilesAccess(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()

    /**
     * Direct filesystem scan of the backup folder. Only useful when
     * [hasAllFilesAccess] is true; otherwise the directory reads as empty.
     */
    suspend fun findBackupsDirect(): List<BackupFileInfo> = withContext(Dispatchers.IO) {
        if (!hasAllFilesAccess()) return@withContext emptyList()
        val dir = File(Environment.getExternalStorageDirectory(), BACKUP_DIR)
        val files = dir.listFiles { f -> f.isFile && f.name.startsWith(FILE_PREFIX) && f.name.endsWith(".zip") }
            .orEmpty()
        Log.d(TAG, "findBackupsDirect: ${files.size} file(s) in ${dir.path}")
        files
            .sortedByDescending { it.lastModified() }
            .map { BackupFileInfo(Uri.fromFile(it), it.name, it.lastModified() / 1000) }
    }

    /** Write the full backup zip. Returns what was written. Requires API 29+. */
    suspend fun export(now: Long = System.currentTimeMillis()): ExportResult =
        withContext(Dispatchers.IO) {
            require(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                "Backup export requires Android 10 or newer"
            }
            val bonsai = db.bonsaiDao().getAll()
            val reminders = db.careReminderDao().getAll()
            val milestones = db.milestoneDao().getAll()
            val photos = db.photoDao().getAll()
            val transitions = db.stageTransitionDao().getAll()
            val images = imageStore.listAll()

            val data = JSONObject().apply {
                put("bonsai", bonsai.toJsonArray { it.toJson() })
                put("careReminders", reminders.toJsonArray { it.toJson() })
                put("milestones", milestones.toJsonArray { it.toJson() })
                put("photos", photos.toJsonArray { it.toJson() })
                put("stageTransitions", transitions.toJsonArray { it.toJson() })
            }
            val manifest = JSONObject().apply {
                put("version", BACKUP_VERSION)
                put("package", context.packageName)
                put("exportedAt", now)
                put("counts", JSONObject().apply {
                    put("trees", bonsai.size)
                    put("careReminders", reminders.size)
                    put("milestones", milestones.size)
                    put("photos", photos.size)
                    put("stageTransitions", transitions.size)
                })
            }

            val displayName = "$FILE_PREFIX${fileStamp(now)}.zip"
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/zip")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "$BACKUP_DIR/")
            }
            val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val uri = context.contentResolver.insert(collection, values)
                ?: error("Couldn't create the backup file")

            context.contentResolver.openOutputStream(uri, "w")!!.use { out ->
                ZipOutputStream(out.buffered()).use { zip ->
                    zip.putNextEntry(ZipEntry(MANIFEST_ENTRY))
                    zip.write(manifest.toString().toByteArray(Charsets.UTF_8))
                    zip.closeEntry()
                    zip.putNextEntry(ZipEntry(DATA_ENTRY))
                    zip.write(data.toString().toByteArray(Charsets.UTF_8))
                    zip.closeEntry()
                    images.forEach { file ->
                        zip.putNextEntry(ZipEntry("${ImageStore.DIR}/${file.name}"))
                        file.inputStream().use { it.copyTo(zip) }
                        zip.closeEntry()
                    }
                }
            }
            ExportResult(displayName = displayName, trees = bonsai.size, photos = images.size)
        }

    /**
     * Full-replace restore from a backup zip. The JSON is parsed and validated
     * before anything is wiped; the DB swap runs in one transaction; image
     * files are streamed in afterwards (per-file failures are tolerated — a
     * missing file leaves its row pointing nowhere, which Coil renders as
     * blank rather than crashing).
     */
    /** Opens a backup zip whether it came from MediaStore (content://) or a direct scan (file://). */
    private fun openBackup(uri: Uri): java.io.InputStream =
        if (uri.scheme == "file") {
            File(requireNotNull(uri.path)).inputStream()
        } else {
            context.contentResolver.openInputStream(uri)
                ?: error("Couldn't open the backup file")
        }

    suspend fun restoreFrom(uri: Uri): ImportSummary = withContext(Dispatchers.IO) {
        // Pass 1: read the small JSON entries and validate before touching data.
        var manifest: JSONObject? = null
        var data: JSONObject? = null
        openBackup(uri).use { input ->
            ZipInputStream(input.buffered()).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    when (entry.name) {
                        MANIFEST_ENTRY -> manifest = JSONObject(zip.readBytes().toString(Charsets.UTF_8))
                        DATA_ENTRY -> data = JSONObject(zip.readBytes().toString(Charsets.UTF_8))
                    }
                    zip.closeEntry()
                }
            }
        }
        val version = manifest?.optInt("version") ?: BACKUP_VERSION
        require(version <= BACKUP_VERSION) { "This backup is from a newer version of the app" }
        val json = requireNotNull(data) { "Not an Ensō backup (data.json missing)" }

        val bonsai = json.getJSONArray("bonsai").toList { bonsaiFromJson(it) }
        val reminders = json.getJSONArray("careReminders").toList { reminderFromJson(it) }
        val milestones = json.getJSONArray("milestones").toList { milestoneFromJson(it) }
        val photos = json.getJSONArray("photos").toList { photoFromJson(it) }
        val transitions = json.getJSONArray("stageTransitions").toList { transitionFromJson(it) }

        // Parents before children — photo rows reference bonsai + milestone ids.
        db.withTransaction {
            db.photoDao().deleteAll()
            db.careReminderDao().deleteAll()
            db.milestoneDao().deleteAll()
            db.stageTransitionDao().deleteAll()
            db.bonsaiDao().deleteAll()
            db.bonsaiDao().insertAll(bonsai)
            db.milestoneDao().insertAll(milestones)
            db.careReminderDao().insertAll(reminders)
            db.stageTransitionDao().insertAll(transitions)
            db.photoDao().insertAll(photos)
        }

        // Pass 2: stream the image files in, replacing current storage.
        imageStore.deleteAll()
        var restoredImages = 0
        openBackup(uri).use { input ->
            ZipInputStream(input.buffered()).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    if (entry.name.startsWith("${ImageStore.DIR}/") && !entry.isDirectory) {
                        runCatching {
                            val target = File(context.filesDir, entry.name)
                            target.parentFile?.mkdirs()
                            target.outputStream().use { zip.copyTo(it) }
                            restoredImages++
                        }
                    }
                    zip.closeEntry()
                }
            }
        }
        ImportSummary(
            trees = bonsai.size,
            milestones = milestones.size,
            photos = restoredImages,
            reminders = reminders.size,
            transitions = transitions.size,
        )
    }

    // --- JSON mapping --------------------------------------------------------

    private fun <T> List<T>.toJsonArray(map: (T) -> JSONObject): JSONArray =
        JSONArray().also { arr -> forEach { arr.put(map(it)) } }

    private fun <T> JSONArray.toList(map: (JSONObject) -> T): List<T> =
        (0 until length()).map { map(getJSONObject(it)) }

    private fun JSONObject.strOrNull(key: String): String? = if (isNull(key)) null else getString(key)
    private fun JSONObject.intOrNull(key: String): Int? = if (isNull(key)) null else getInt(key)
    private fun JSONObject.longOrNull(key: String): Long? = if (isNull(key)) null else getLong(key)

    private fun BonsaiEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("species", species)
        put("stage", stage)
        put("health", health)
        put("careScheduleSet", careScheduleSet)
        put("coverPhotoPath", coverPhotoPath ?: JSONObject.NULL)
        put("acquiredYear", acquiredYear ?: JSONObject.NULL)
        put("acquisitionSource", acquisitionSource ?: JSONObject.NULL)
        put("placement", placement ?: JSONObject.NULL)
        put("origin", origin ?: JSONObject.NULL)
        put("acquiredFrom", acquiredFrom ?: JSONObject.NULL)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
    }

    private fun bonsaiFromJson(o: JSONObject) = BonsaiEntity(
        id = o.getLong("id"),
        name = o.getString("name"),
        species = o.getString("species"),
        stage = o.getString("stage"),
        health = o.optString("health", "healthy"),
        careScheduleSet = o.optBoolean("careScheduleSet", false),
        coverPhotoPath = o.strOrNull("coverPhotoPath"),
        acquiredYear = o.intOrNull("acquiredYear"),
        acquisitionSource = o.strOrNull("acquisitionSource"),
        placement = o.strOrNull("placement"),
        origin = o.strOrNull("origin"),
        acquiredFrom = o.strOrNull("acquiredFrom"),
        createdAt = o.getLong("createdAt"),
        updatedAt = o.getLong("updatedAt"),
    )

    private fun CareReminderEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("bonsaiId", bonsaiId)
        put("type", type)
        put("intervalDays", intervalDays)
        put("nextDueAt", nextDueAt)
        put("createdAt", createdAt)
    }

    private fun reminderFromJson(o: JSONObject) = CareReminderEntity(
        id = o.getLong("id"),
        bonsaiId = o.getLong("bonsaiId"),
        type = o.getString("type"),
        intervalDays = o.getInt("intervalDays"),
        nextDueAt = o.getLong("nextDueAt"),
        createdAt = o.getLong("createdAt"),
    )

    private fun MilestoneEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("bonsaiId", bonsaiId)
        put("title", title)
        put("notes", notes ?: JSONObject.NULL)
        put("occurredAt", occurredAt)
        put("createdAt", createdAt)
    }

    private fun milestoneFromJson(o: JSONObject) = MilestoneEntity(
        id = o.getLong("id"),
        bonsaiId = o.getLong("bonsaiId"),
        title = o.getString("title"),
        notes = o.strOrNull("notes"),
        occurredAt = o.getLong("occurredAt"),
        createdAt = o.getLong("createdAt"),
    )

    private fun PhotoEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("bonsaiId", bonsaiId)
        put("milestoneId", milestoneId ?: JSONObject.NULL)
        put("path", path)
        put("caption", caption ?: JSONObject.NULL)
        put("orderIndex", orderIndex)
        put("createdAt", createdAt)
    }

    private fun photoFromJson(o: JSONObject) = PhotoEntity(
        id = o.getLong("id"),
        bonsaiId = o.getLong("bonsaiId"),
        milestoneId = o.longOrNull("milestoneId"),
        path = o.getString("path"),
        caption = o.strOrNull("caption"),
        orderIndex = o.optInt("orderIndex", 0),
        createdAt = o.getLong("createdAt"),
    )

    private fun StageTransitionEntity.toJson() = JSONObject().apply {
        put("id", id)
        put("bonsaiId", bonsaiId)
        put("fromStage", fromStage)
        put("toStage", toStage)
        put("notes", notes ?: JSONObject.NULL)
        put("recordedAt", recordedAt)
    }

    private fun transitionFromJson(o: JSONObject) = StageTransitionEntity(
        id = o.getLong("id"),
        bonsaiId = o.getLong("bonsaiId"),
        fromStage = o.getString("fromStage"),
        toStage = o.getString("toStage"),
        notes = o.strOrNull("notes"),
        recordedAt = o.getLong("recordedAt"),
    )

    companion object {
        /** Backup format version — bump when the zip layout or JSON shape changes. */
        const val BACKUP_VERSION = 1
        const val BACKUP_DIR = "Documents/Enso"
        private const val TAG = "BackupManager"
        private const val FILE_PREFIX = "enso-backup-"
        private const val MANIFEST_ENTRY = "manifest.json"
        private const val DATA_ENTRY = "data.json"

        // Readable date+time so users can tell backups apart at a glance;
        // seconds included to avoid same-minute name collisions.
        private fun fileStamp(epochMillis: Long): String =
            SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date(epochMillis))
    }
}
