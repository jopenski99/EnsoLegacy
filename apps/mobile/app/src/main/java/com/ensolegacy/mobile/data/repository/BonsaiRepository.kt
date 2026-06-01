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

    suspend fun save(bonsai: BonsaiEntity): Long = dao.upsert(bonsai)

    suspend fun remove(id: Long) = dao.delete(id)
}
