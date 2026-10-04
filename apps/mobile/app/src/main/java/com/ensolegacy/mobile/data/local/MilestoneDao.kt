package com.ensolegacy.mobile.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MilestoneDao {
    /** Most-recent event first — the timeline reads top-down. */
    @Query("SELECT * FROM milestone WHERE bonsaiId = :bonsaiId ORDER BY occurredAt DESC")
    fun observeForBonsai(bonsaiId: Long): Flow<List<MilestoneEntity>>

    @Insert
    suspend fun insert(milestone: MilestoneEntity): Long

    /** One-shot snapshot of every milestone (backup export). */
    @Query("SELECT * FROM milestone")
    suspend fun getAll(): List<MilestoneEntity>

    /** Bulk insert for backup restore (rows keep their original ids). */
    @Insert
    suspend fun insertAll(milestones: List<MilestoneEntity>)

    /** Wipes the table — backup restore does a full replace. */
    @Query("DELETE FROM milestone")
    suspend fun deleteAll()

    @Query("DELETE FROM milestone WHERE id = :id")
    suspend fun delete(id: Long)
}
