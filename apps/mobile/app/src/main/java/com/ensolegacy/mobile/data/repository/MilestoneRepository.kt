package com.ensolegacy.mobile.data.repository

import com.ensolegacy.mobile.data.ImageStore
import com.ensolegacy.mobile.data.local.MilestoneDao
import com.ensolegacy.mobile.data.local.MilestoneEntity
import com.ensolegacy.mobile.data.local.PhotoDao
import com.ensolegacy.mobile.data.local.PhotoEntity
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for a tree's timeline (milestones) and its photos
 * (milestone photos + loose vault photos). Owns the side effect Room can't do
 * on its own: deleting the underlying image files when their rows go away.
 */
class MilestoneRepository(
    private val milestoneDao: MilestoneDao,
    private val photoDao: PhotoDao,
    private val imageStore: ImageStore,
) {

    fun observeMilestones(bonsaiId: Long): Flow<List<MilestoneEntity>> =
        milestoneDao.observeForBonsai(bonsaiId)

    fun observePhotos(bonsaiId: Long): Flow<List<PhotoEntity>> =
        photoDao.observeForBonsai(bonsaiId)

    /**
     * Create a milestone and attach [photoPaths] (already saved to storage) to
     * it in order. Returns the new milestone id.
     */
    suspend fun addMilestone(
        bonsaiId: Long,
        title: String,
        notes: String?,
        occurredAt: Long,
        photoPaths: List<String>,
        now: Long,
    ): Long {
        val milestoneId = milestoneDao.insert(
            MilestoneEntity(
                bonsaiId = bonsaiId,
                title = title,
                notes = notes,
                occurredAt = occurredAt,
                createdAt = now,
            ),
        )
        if (photoPaths.isNotEmpty()) {
            photoDao.insertAll(
                photoPaths.mapIndexed { index, path ->
                    PhotoEntity(
                        bonsaiId = bonsaiId,
                        milestoneId = milestoneId,
                        path = path,
                        orderIndex = index,
                        createdAt = now,
                    )
                },
            )
        }
        return milestoneId
    }

    /** Add a loose vault photo (already saved to storage) for a tree. */
    suspend fun addVaultPhoto(bonsaiId: Long, path: String, now: Long) {
        photoDao.insert(
            PhotoEntity(bonsaiId = bonsaiId, milestoneId = null, path = path, createdAt = now),
        )
    }

    /** Delete a milestone and its photos (rows cascade; files removed here). */
    suspend fun deleteMilestone(id: Long) {
        photoDao.listForMilestone(id).forEach { imageStore.delete(it.path) }
        milestoneDao.delete(id)
    }

    /** Delete a single photo and its file. */
    suspend fun deletePhoto(id: Long) {
        val photo = photoDao.getById(id)
        photoDao.delete(id)
        imageStore.delete(photo?.path)
    }

    /**
     * Delete the image files for every photo of a tree. Call before removing the
     * tree, whose row delete cascades the photo rows away (but not their files).
     */
    suspend fun deleteAllFilesFor(bonsaiId: Long) {
        photoDao.listForBonsai(bonsaiId).forEach { imageStore.delete(it.path) }
    }

    /** Discard staged-but-unsaved capture files (e.g. a cancelled milestone). */
    fun discardStaged(paths: List<String>) {
        paths.forEach { imageStore.delete(it) }
    }
}
