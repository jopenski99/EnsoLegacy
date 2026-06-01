package com.ensolegacy.mobile.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CareReminderDao {
    @Query("SELECT * FROM care_reminder WHERE bonsaiId = :bonsaiId ORDER BY nextDueAt ASC")
    fun observeForBonsai(bonsaiId: Long): Flow<List<CareReminderEntity>>

    @Insert
    suspend fun insertAll(reminders: List<CareReminderEntity>)

    @Query("DELETE FROM care_reminder WHERE bonsaiId = :bonsaiId")
    suspend fun deleteForBonsai(bonsaiId: Long)
}
