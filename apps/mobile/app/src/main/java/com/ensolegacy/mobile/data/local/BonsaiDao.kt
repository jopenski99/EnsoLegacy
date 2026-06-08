package com.ensolegacy.mobile.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BonsaiDao {
    @Query("SELECT * FROM bonsai ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<BonsaiEntity>>

    @Query("SELECT * FROM bonsai WHERE id = :id")
    fun observeById(id: Long): Flow<BonsaiEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(bonsai: BonsaiEntity): Long

    // Targeted column updates. These avoid the REPLACE insert above, which on a
    // PK conflict deletes the row first — cascading away the tree's reminders,
    // milestones, and photos before re-inserting it.
    @Query("UPDATE bonsai SET coverPhotoPath = :path, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateCoverPhoto(id: Long, path: String?, updatedAt: Long)

    @Query("UPDATE bonsai SET careScheduleSet = :set, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateCareScheduleSet(id: Long, set: Boolean, updatedAt: Long)

    @Query(
        "UPDATE bonsai SET name = :name, species = :species, stage = :stage, " +
            "health = :health, acquiredYear = :acquiredYear, " +
            "acquisitionSource = :acquisitionSource, placement = :placement, " +
            "origin = :origin, acquiredFrom = :acquiredFrom, " +
            "updatedAt = :updatedAt WHERE id = :id",
    )
    suspend fun updateDetails(
        id: Long,
        name: String,
        species: String,
        stage: String,
        health: String,
        acquiredYear: Int?,
        acquisitionSource: String?,
        placement: String?,
        origin: String?,
        acquiredFrom: String?,
        updatedAt: Long,
    )

    @Query("DELETE FROM bonsai WHERE id = :id")
    suspend fun delete(id: Long)
}
