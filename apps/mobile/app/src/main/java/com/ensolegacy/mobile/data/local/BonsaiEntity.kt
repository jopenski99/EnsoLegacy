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
    val acquiredYear: Int? = null,
    val createdAt: Long,
    val updatedAt: Long,
)
