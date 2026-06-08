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

    @Query("DELETE FROM milestone WHERE id = :id")
    suspend fun delete(id: Long)
}
