package com.ensolegacy.mobile.data.repository

import com.ensolegacy.mobile.data.local.BonsaiDao
import com.ensolegacy.mobile.data.local.BonsaiEntity
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for bonsai data. The ViewModel layer talks only to
 * the repository, never to the DAO directly.
 */
class BonsaiRepository(private val dao: BonsaiDao) {

    fun observeCollection(): Flow<List<BonsaiEntity>> = dao.observeAll()

    fun observe(id: Long): Flow<BonsaiEntity?> = dao.observeById(id)

    suspend fun save(bonsai: BonsaiEntity): Long = dao.upsert(bonsai)

    /** Set (or clear) the tree's cover photo without touching its other columns. */
    suspend fun setCoverPhoto(id: Long, path: String?, updatedAt: Long) =
        dao.updateCoverPhoto(id, path, updatedAt)

    /** Mark the tree's care schedule as set up, leaving its other columns intact. */
    suspend fun markCareScheduleSet(id: Long, updatedAt: Long) =
        dao.updateCareScheduleSet(id, true, updatedAt)

    /** Edit a tree's core details in place — no REPLACE, so children are safe. */
    suspend fun updateDetails(
        id: Long,
        name: String,
        species: String,
        stage: String,
        health: String,
        acquiredYear: Int?,
        acquisitionSource: String?,
        placement: String?,
        origin: String?,
        acquiredFrom: String?,
        updatedAt: Long,
    ) = dao.updateDetails(
        id, name, species, stage, health, acquiredYear,
        acquisitionSource, placement, origin, acquiredFrom, updatedAt,
    )

    suspend fun remove(id: Long) = dao.delete(id)
}
