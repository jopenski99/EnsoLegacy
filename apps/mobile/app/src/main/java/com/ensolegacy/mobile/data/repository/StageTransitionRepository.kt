package com.ensolegacy.mobile.data.repository

import com.ensolegacy.mobile.data.local.StageTransitionDao
import com.ensolegacy.mobile.data.local.StageTransitionEntity
import kotlinx.coroutines.flow.Flow

class StageTransitionRepository(private val dao: StageTransitionDao) {

    fun observeForBonsai(bonsaiId: Long): Flow<List<StageTransitionEntity>> =
        dao.observeForBonsai(bonsaiId)

    suspend fun recordTransition(
        bonsaiId: Long,
        fromStage: String,
        toStage: String,
        recordedAt: Long,
    ) {
        dao.insert(
            StageTransitionEntity(
                bonsaiId = bonsaiId,
                fromStage = fromStage,
                toStage = toStage,
                recordedAt = recordedAt,
            ),
        )
    }
}
