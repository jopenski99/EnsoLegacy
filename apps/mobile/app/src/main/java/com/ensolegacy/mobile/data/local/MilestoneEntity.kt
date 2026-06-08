package com.ensolegacy.mobile.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A documented event in a tree's life — the heart of the Tree Passport timeline.
 * Mirrors the shared `Milestone` type in packages/shared. Belongs to a
 * [BonsaiEntity]; deleting the tree cascades to its milestones. Its photos live
 * in [PhotoEntity] (up to `MAX_MILESTONE_PHOTOS`), linked by `milestoneId`.
 */
@Entity(
    tableName = "milestone",
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
data class MilestoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bonsaiId: Long,
    val title: String,
    val notes: String? = null,
    /** When the event happened, epoch millis (may predate when it was logged). */
    val occurredAt: Long,
    val createdAt: Long,
)
