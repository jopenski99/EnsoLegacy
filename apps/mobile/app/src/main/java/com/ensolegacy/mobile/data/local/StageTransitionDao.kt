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
}
