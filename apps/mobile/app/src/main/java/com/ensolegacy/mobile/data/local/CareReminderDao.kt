package com.ensolegacy.mobile.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CareReminderDao {
    @Query("SELECT * FROM care_reminder WHERE bonsaiId = :bonsaiId ORDER BY nextDueAt ASC")
    fun observeForBonsai(bonsaiId: Long): Flow<List<CareReminderEntity>>

    @Query(
        "SELECT care_reminder.*, bonsai.name as bonsaiName " +
            "FROM care_reminder JOIN bonsai ON care_reminder.bonsaiId = bonsai.id " +
            "ORDER BY care_reminder.nextDueAt ASC",
    )
    fun observeAllWithBonsai(): Flow<List<ReminderWithBonsai>>

    @Query(
        "SELECT care_reminder.*, bonsai.name as bonsaiName " +
            "FROM care_reminder JOIN bonsai ON care_reminder.bonsaiId = bonsai.id " +
            "WHERE care_reminder.nextDueAt <= :now " +
            "ORDER BY care_reminder.nextDueAt ASC",
    )
    suspend fun getOverdueWithBonsai(now: Long): List<ReminderWithBonsai>

    @Query("UPDATE care_reminder SET nextDueAt = :nextDueAt WHERE id = :id")
    suspend fun updateNextDue(id: Long, nextDueAt: Long)

    @Insert
    suspend fun insertAll(reminders: List<CareReminderEntity>)

    @Query("DELETE FROM care_reminder WHERE bonsaiId = :bonsaiId")
    suspend fun deleteForBonsai(bonsaiId: Long)
}
