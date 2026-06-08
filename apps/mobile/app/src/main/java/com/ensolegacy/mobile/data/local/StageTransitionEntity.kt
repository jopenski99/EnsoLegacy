package com.ensolegacy.mobile.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Records a single stage change for a tree (e.g. Juvenile → In Training).
 * Deleting the tree cascades to its history; we never delete transitions
 * individually — the record is permanent once written.
 */
@Entity(
    tableName = "stage_transition",
    foreignKeys = [
        ForeignKey(
            entity = BonsaiEntity::class,
            parentColumns = ["id"],
            childColumns = ["bonsaiId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("bonsaiId")],
)
data class StageTransitionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bonsaiId: Long,
    val fromStage: String,
    val toStage: String,
    val notes: String? = null,
    val recordedAt: Long,
)
