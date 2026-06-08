package com.ensolegacy.mobile.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for a tree in the local collection.
 * Mirrors the shared `Bonsai` type in packages/shared.
 */
@Entity(tableName = "bonsai")
data class BonsaiEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val species: String,
    val stage: String,
    val health: String = "healthy",
    val careScheduleSet: Boolean = false,
    /** Cover photo, as an ImageStore-relative path. Null = monogram placeholder. */
    val coverPhotoPath: String? = null,
    val acquiredYear: Int? = null,
    // Acquisition context (item 9) — stable string values mirrored from AcquisitionSource / Placement
    val acquisitionSource: String? = null,
    val placement: String? = null,
    // Provenance (item 8)
    val origin: String? = null,
    val acquiredFrom: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)
