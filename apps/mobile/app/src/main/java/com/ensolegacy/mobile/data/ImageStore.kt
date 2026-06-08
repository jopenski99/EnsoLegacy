package com.ensolegacy.mobile.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * Owns the app's photo files. Every cover/milestone/vault image is copied into
 * the app's internal storage (`filesDir/images`) so the tree's record survives
 * the source photo being deleted from the gallery — the "forever" promise.
 *
 * Rows in Room store only the [StoredImage.relativePath] (e.g. `images/<uuid>.jpg`);
 * [resolve] turns that back into an absolute [File] for Coil to load.
 */
class ImageStore(private val context: Context) {

    private val imagesDir: File
        get() = File(context.filesDir, DIR).apply { if (!exists()) mkdirs() }

    /**
     * Allocate a fresh, empty file for a new image and return it with its
     * storage-relative path. CameraX writes the captured frame straight into
     * [StoredImage.file]; persist [StoredImage.relativePath] in the database.
     */
    fun newImageFile(): StoredImage {
        val name = "${UUID.randomUUID()}.jpg"
        return StoredImage(file = File(imagesDir, name), relativePath = "$DIR/$name")
    }

    /**
     * Copy an external gallery [uri] into app storage off the main thread.
     * Returns the new relative path, or null if the source couldn't be read.
     */
    suspend fun importFromUri(uri: Uri): String? = withContext(Dispatchers.IO) {
        val target = newImageFile()
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                target.file.outputStream().use { output -> input.copyTo(output) }
            } ?: return@runCatching null
            target.relativePath
        }.getOrNull()
    }

    /** Absolute file for a stored [relativePath], for loading/deleting. */
    fun resolve(relativePath: String): File = File(context.filesDir, relativePath)

    /** Best-effort delete of a stored image; no-op for null/missing files. */
    fun delete(relativePath: String?) {
        relativePath ?: return
        runCatching { resolve(relativePath).delete() }
    }

    companion object {
        private const val DIR = "images"
    }
}

/** A file allocated by [ImageStore] together with the path to persist for it. */
data class StoredImage(val file: File, val relativePath: String)
