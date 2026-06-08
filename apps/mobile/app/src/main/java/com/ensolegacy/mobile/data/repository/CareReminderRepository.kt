package com.ensolegacy.mobile.data.repository

import com.ensolegacy.mobile.data.local.CareReminderDao
import com.ensolegacy.mobile.data.local.CareReminderEntity
import com.ensolegacy.mobile.data.local.ReminderWithBonsai
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for a tree's care reminders. The ViewModel layer talks
 * only to the repository, never to the DAO directly.
 */
class CareReminderRepository(private val dao: CareReminderDao) {

    fun observeForBonsai(bonsaiId: Long): Flow<List<CareReminderEntity>> =
        dao.observeForBonsai(bonsaiId)

    fun observeAllWithBonsai(): Flow<List<ReminderWithBonsai>> =
        dao.observeAllWithBonsai()

    /** Replace a tree's reminders with [reminders] (the set-up flow is all-or-nothing). */
    suspend fun replaceForBonsai(bonsaiId: Long, reminders: List<CareReminderEntity>) {
        dao.deleteForBonsai(bonsaiId)
        dao.insertAll(reminders)
    }

    /** Mark a task done: advance nextDueAt by one full interval from now. */
    suspend fun completeTask(id: Long, intervalDays: Int, now: Long) {
        dao.updateNextDue(id, now + intervalDays * MILLIS_PER_DAY)
    }

    /** Reschedule a task to an explicit date chosen by the owner. */
    suspend fun reschedule(id: Long, newDueAt: Long) {
        dao.updateNextDue(id, newDueAt)
    }

    /** Overdue reminders with their tree names, for the notification worker. */
    suspend fun getOverdueWithBonsai(now: Long): List<ReminderWithBonsai> =
        dao.getOverdueWithBonsai(now)

    companion object {
        private const val MILLIS_PER_DAY = 24L * 60 * 60 * 1000
    }
}
