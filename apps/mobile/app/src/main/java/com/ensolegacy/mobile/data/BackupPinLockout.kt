package com.ensolegacy.mobile.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest

/**
 * In-app brute-force guard for PIN-protected backups. Tracks failed PIN
 * attempts per backup *content* — a SHA-256 fingerprint of the zip, so
 * renaming the file doesn't dodge it — and locks a file after
 * [MAX_FAILURES] wrong tries.
 *
 * The record lives in SharedPreferences: clearing app data (or reinstalling)
 * resets the counters, so this deters casual in-app guessing. The actual
 * protection of the contents is the backup encryption itself.
 */
class BackupPinLockout(private val context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    data class Status(val failures: Int, val locked: Boolean)

    /** SHA-256 fingerprint of the backup file — its rename-proof identity. */
    suspend fun fingerprintOf(uri: Uri): String = withContext(Dispatchers.IO) {
        val digest = MessageDigest.getInstance("SHA-256")
        open(uri).use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        digest.digest().joinToString("") { "%02x".format(it) }
    }

    fun statusFor(fingerprint: String): Status {
        val record = prefs.getString(fingerprint, null)
            ?.let { runCatching { JSONObject(it) }.getOrNull() }
        return Status(
            failures = record?.optInt("failures") ?: 0,
            locked = record?.optBoolean("locked") ?: false,
        )
    }

    /** Record a wrong PIN ([displayName] kept for support/debugging); returns the new status. */
    fun recordFailure(fingerprint: String, displayName: String): Status {
        val failures = statusFor(fingerprint).failures + 1
        val locked = failures >= MAX_FAILURES
        prefs.edit().putString(
            fingerprint,
            JSONObject()
                .put("name", displayName)
                .put("failures", failures)
                .put("locked", locked)
                .toString(),
        ).apply()
        return Status(failures, locked)
    }

    /** A successful restore clears the file's record. */
    fun reset(fingerprint: String) {
        prefs.edit().remove(fingerprint).apply()
    }

    private fun open(uri: Uri) =
        if (uri.scheme == "file") {
            File(requireNotNull(uri.path)).inputStream()
        } else {
            context.contentResolver.openInputStream(uri)!!
        }

    companion object {
        const val MAX_FAILURES = 3
        private const val PREFS_NAME = "enso_backup_lockout"
    }
}
