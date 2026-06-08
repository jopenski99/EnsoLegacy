package com.ensolegacy.mobile.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One stored photo for a tree. A single table backs both surfaces:
 *  - **Photo vault** — loose diary photos, where `milestoneId` is null.
 *  - **Milestone photos** — attached to a milestone via `milestoneId` (the
 *    10-photo flow), ordered by `orderIndex`.
 *
 * `path` is an ImageStore-relative path (the file lives in app storage).
 * Deleting the tree or the milestone cascades to its photos' rows; the files
 * themselves are cleaned up by the repository.
 */
@Entity(
    tableName = "photo",
    foreignKeys = [
        ForeignKey(
            entity = BonsaiEntity::class,
            parentColumns = ["id"],
            childColumns = ["bonsaiId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = MilestoneEntity::class,
            parentColumns = ["id"],
            childColumns = ["milestoneId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("bonsaiId"), Index("milestoneId")],
)
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bonsaiId: Long,
    /** Null for loose vault photos; set when the photo belongs to a milestone. */
    val milestoneId: Long? = null,
    val path: String,
    val caption: String? = null,
    /** Position within a milestone's photo set (0-based); 0 for vault photos. */
    val orderIndex: Int = 0,
    val createdAt: Long,
)
