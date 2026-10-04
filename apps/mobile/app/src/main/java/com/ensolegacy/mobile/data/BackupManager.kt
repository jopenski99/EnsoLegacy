package com.ensolegacy.mobile.data

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
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
import java.io.InputStream
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/** The PIN entered at restore didn't match the one the backup was exported with. */
class WrongBackupPinException : Exception("PIN does not match this backup")

/**
 * Manual local backup: the whole record — all five tables plus every image
 * file — as a single zip in shared storage, so it survives uninstall and can
 * be restored after a reinstall or on a new device.
 *
 * Export writes `enso-backup-<date_time>.zip` to `Documents/Enso/` via
 * MediaStore. The owner sets a 4–6 digit PIN at export time (format v2):
 * the PIN stretches through PBKDF2 into an AES-256-GCM key, and `data.json`
 * plus every image is encrypted with it. Only `manifest.json` stays in
 * plaintext — format version, KDF salt/iterations, and a key verifier, so a
 * restore can check the PIN before decrypting anything. (A short PIN is a
 * casual-theft gate, not strong cryptography: it blocks the "restore first,
 * claim the collection" race and keeps the contents out of sight, but the
 * verifier is offline-brute-forceable by a determined attacker.)
 *
 * Restore is a **full replace**: current data is wiped, then rows are
 * re-inserted with their original ids so cross-references (bonsaiId,
 * milestoneId, photo paths) stay intact, and the images are rewritten.
 * Version-1 (unencrypted) backups still restore without a PIN.
 *
 * Detection is two-tier: [findBackups] (MediaStore, zero-permission) only
 * sees backups written by the current install — ownership is wiped with the
 * app's data — while [findBackupsDirect] (direct file scan, needs the "All
 * files access" grant on API 30+) sees everything, including backups from a
 * previous install. The SAF file picker in Settings is the zero-permission
 * fallback for files neither path can read.
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

    // --- Detection -----------------------------------------------------------

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

    /**
     * True if [uri] can actually be opened by us — MediaStore ownership for
     * current-install files, a live All-files grant for file URIs.
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

    /** Format version of a backup (1 = legacy plaintext, 2 = PIN-encrypted). */
    suspend fun peekVersion(uri: Uri): Int = withContext(Dispatchers.IO) {
        runCatching {
            openBackup(uri).use { input ->
                ZipInputStream(input.buffered()).use { zip ->
                    var version = LEGACY_VERSION
                    while (true) {
                        val entry = zip.nextEntry ?: break
                        if (entry.name == MANIFEST_ENTRY) {
                            version = JSONObject(zip.readBytes().toString(Charsets.UTF_8))
                                .optInt("version", LEGACY_VERSION)
                            break
                        }
                        zip.closeEntry()
                    }
                    version
                }
            }
        }.getOrDefault(LEGACY_VERSION)
    }

    // --- Export --------------------------------------------------------------

    /** Write the full backup zip, encrypted with [pin] (4–6 digits). Requires API 29+. */
    suspend fun export(pin: String, now: Long = System.currentTimeMillis()): ExportResult =
        withContext(Dispatchers.IO) {
            require(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                "Backup export requires Android 10 or newer"
            }
            require(pin.length in 4..6 && pin.all { it.isDigit() }) {
                "PIN must be 4–6 digits"
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

            val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
            val key = deriveKey(pin, salt, KDF_ITERATIONS)
            val manifest = JSONObject().apply {
                put("version", BACKUP_VERSION)
                put("package", context.packageName)
                put("exportedAt", now)
                put("kdf", KDF_ALGO)
                put("iterations", KDF_ITERATIONS)
                put("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
                put("verifier", Base64.encodeToString(verifierFor(key), Base64.NO_WRAP))
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
                    zip.write(encrypt(key, data.toString().toByteArray(Charsets.UTF_8)))
                    zip.closeEntry()
                    images.forEach { file ->
                        zip.putNextEntry(ZipEntry("${ImageStore.DIR}/${file.name}"))
                        zip.write(encrypt(key, file.readBytes()))
                        zip.closeEntry()
                    }
                }
            }
            ExportResult(displayName = displayName, trees = bonsai.size, photos = images.size)
        }

    // --- Restore -------------------------------------------------------------

    /**
     * Full-replace restore from a backup zip. The JSON is parsed and validated
     * (and for v2, the PIN verified) before anything is wiped; the DB swap
     * runs in one transaction; image files are rewritten afterwards (per-file
     * failures are tolerated — a missing file leaves its row pointing nowhere,
     * which Coil renders as blank rather than crashing).
     *
     * Throws [WrongBackupPinException] for v2 backups when [pin] is absent or
     * wrong. Version-1 backups restore without a PIN.
     */
    suspend fun restoreFrom(uri: Uri, pin: String?): ImportSummary = withContext(Dispatchers.IO) {
        // Pass 1: manifest (always plaintext), then the data payload.
        var manifest: JSONObject? = null
        var dataBytes: ByteArray? = null
        openBackup(uri).use { input ->
            ZipInputStream(input.buffered()).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    when (entry.name) {
                        MANIFEST_ENTRY -> manifest = JSONObject(zip.readBytes().toString(Charsets.UTF_8))
                        DATA_ENTRY -> dataBytes = zip.readBytes()
                    }
                    zip.closeEntry()
                }
            }
        }
        val m = requireNotNull(manifest) { "Not an Ensō backup (manifest.json missing)" }
        val version = m.optInt("version", LEGACY_VERSION)
        require(version <= BACKUP_VERSION) { "This backup is from a newer version of the app" }

        // v2: derive the key from the PIN and check it against the verifier
        // before decrypting anything.
        var key: SecretKey? = null
        if (version >= 2) {
            if (pin.isNullOrEmpty()) throw WrongBackupPinException()
            val salt = Base64.decode(m.getString("salt"), Base64.NO_WRAP)
            val iterations = m.optInt("iterations", KDF_ITERATIONS)
            val expected = Base64.decode(m.getString("verifier"), Base64.NO_WRAP)
            key = deriveKey(pin, salt, iterations)
            if (!verifierFor(key).contentEquals(expected)) throw WrongBackupPinException()
        }

        val rawData = requireNotNull(dataBytes) { "Not an Ensō backup (data.json missing)" }
        val json = JSONObject(
            (key?.let { decrypt(it, rawData) } ?: rawData).toString(Charsets.UTF_8),
        )

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

        // Pass 2: stream the image files in (decrypting for v2), replacing storage.
        imageStore.deleteAll()
        var restoredImages = 0
        openBackup(uri).use { input ->
            ZipInputStream(input.buffered()).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    if (entry.name.startsWith("${ImageStore.DIR}/") && !entry.isDirectory) {
                        runCatching {
                            val bytes = zip.readBytes()
                            val target = File(context.filesDir, entry.name)
                            target.parentFile?.mkdirs()
                            target.writeBytes(key?.let { decrypt(it, bytes) } ?: bytes)
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

    // --- Crypto (format v2) ----------------------------------------------------

    private fun deriveKey(pin: String, salt: ByteArray, iterations: Int): SecretKey {
        val spec = PBEKeySpec(pin.toCharArray(), salt, iterations, KEY_BITS)
        val bytes = SecretKeyFactory.getInstance(KDF_ALGO).generateSecret(spec).encoded
        return SecretKeySpec(bytes, "AES")
    }

    /** Key-check value: proves the PIN-derived key is right before decrypting. */
    private fun verifierFor(key: SecretKey): ByteArray =
        Mac.getInstance("HmacSHA256").apply { init(key) }
            .doFinal(VERIFIER_MESSAGE.toByteArray(Charsets.UTF_8))

    /** AES-GCM with a fresh random IV, prepended to the ciphertext. */
    private fun encrypt(key: SecretKey, plain: ByteArray): ByteArray {
        val iv = ByteArray(GCM_IV_BYTES).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        return iv + cipher.doFinal(plain)
    }

    private fun decrypt(key: SecretKey, bytes: ByteArray): ByteArray {
        val iv = bytes.copyOfRange(0, GCM_IV_BYTES)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        return cipher.doFinal(bytes, GCM_IV_BYTES, bytes.size - GCM_IV_BYTES)
    }

    /** Opens a backup zip whether it came from MediaStore (content://) or a direct scan (file://). */
    private fun openBackup(uri: Uri): InputStream =
        if (uri.scheme == "file") {
            File(requireNotNull(uri.path)).inputStream()
        } else {
            context.contentResolver.openInputStream(uri)
                ?: error("Couldn't open the backup file")
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
        /**
         * Backup format version — bump when the zip layout or JSON shape changes.
         * v1: plaintext. v2: PIN-derived AES-GCM encryption of data + images.
         */
        const val BACKUP_VERSION = 2
        private const val LEGACY_VERSION = 1
        const val BACKUP_DIR = "Documents/Enso"
        private const val TAG = "BackupManager"
        private const val FILE_PREFIX = "enso-backup-"
        private const val MANIFEST_ENTRY = "manifest.json"
        private const val DATA_ENTRY = "data.json"

        private const val KDF_ALGO = "PBKDF2WithHmacSHA256"
        private const val KDF_ITERATIONS = 120_000
        private const val KEY_BITS = 256
        private const val SALT_BYTES = 16
        private const val GCM_IV_BYTES = 12
        private const val GCM_TAG_BITS = 128
        private const val VERIFIER_MESSAGE = "enso-backup-verify"

        // Readable date+time so users can tell backups apart at a glance;
        // seconds included to avoid same-minute name collisions.
        private fun fileStamp(epochMillis: Long): String =
            SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date(epochMillis))
    }
}
