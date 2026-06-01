package com.ensolegacy.mobile.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A recurring care reminder for one tree. Belongs to a [BonsaiEntity]; deleting
 * the tree cascades to its reminders. `nextDueAt` is recomputed (now + interval)
 * each time the task is completed — completion handling lands with notifications.
 */
@Entity(
    tableName = "care_reminder",
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
data class CareReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bonsaiId: Long,
    val type: String,
    val intervalDays: Int,
    val nextDueAt: Long,
    val createdAt: Long,
)
