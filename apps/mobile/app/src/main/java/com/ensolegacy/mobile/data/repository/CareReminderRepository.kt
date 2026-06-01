package com.ensolegacy.mobile.data.repository

import com.ensolegacy.mobile.data.local.CareReminderDao
import com.ensolegacy.mobile.data.local.CareReminderEntity
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for a tree's care reminders. The ViewModel layer talks
 * only to the repository, never to the DAO directly.
 */
class CareReminderRepository(private val dao: CareReminderDao) {

    fun observeForBonsai(bonsaiId: Long): Flow<List<CareReminderEntity>> =
        dao.observeForBonsai(bonsaiId)

    /** Replace a tree's reminders with [reminders] (the set-up flow is all-or-nothing). */
    suspend fun replaceForBonsai(bonsaiId: Long, reminders: List<CareReminderEntity>) {
        dao.deleteForBonsai(bonsaiId)
        dao.insertAll(reminders)
    }
}
