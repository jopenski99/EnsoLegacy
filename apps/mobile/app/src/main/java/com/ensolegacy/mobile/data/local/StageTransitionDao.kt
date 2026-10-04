package com.ensolegacy.mobile.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StageTransitionDao {
    /** All stage changes for a tree, newest first. */
    @Query("SELECT * FROM stage_transition WHERE bonsaiId = :bonsaiId ORDER BY recordedAt DESC")
    fun observeForBonsai(bonsaiId: Long): Flow<List<StageTransitionEntity>>

    @Insert
    suspend fun insert(transition: StageTransitionEntity)

    /** One-shot snapshot of every transition (backup export). */
    @Query("SELECT * FROM stage_transition")
    suspend fun getAll(): List<StageTransitionEntity>

    /** Bulk insert for backup restore (rows keep their original ids). */
    @Insert
    suspend fun insertAll(transitions: List<StageTransitionEntity>)

    /** Wipes the table — backup restore does a full replace. */
    @Query("DELETE FROM stage_transition")
    suspend fun deleteAll()
}
