package com.ensolegacy.mobile.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    /** Every photo for a tree (vault + milestone), newest first. */
    @Query("SELECT * FROM photo WHERE bonsaiId = :bonsaiId ORDER BY createdAt DESC")
    fun observeForBonsai(bonsaiId: Long): Flow<List<PhotoEntity>>

    @Insert
    suspend fun insert(photo: PhotoEntity): Long

    @Insert
    suspend fun insertAll(photos: List<PhotoEntity>)

    @Query("SELECT * FROM photo WHERE id = :id")
    suspend fun getById(id: Long): PhotoEntity?

    /** Snapshot of a milestone's photos, used to delete their files on cascade. */
    @Query("SELECT * FROM photo WHERE milestoneId = :milestoneId")
    suspend fun listForMilestone(milestoneId: Long): List<PhotoEntity>

    /** Snapshot of all of a tree's photos, used to delete their files on cascade. */
    @Query("SELECT * FROM photo WHERE bonsaiId = :bonsaiId")
    suspend fun listForBonsai(bonsaiId: Long): List<PhotoEntity>

    @Query("DELETE FROM photo WHERE id = :id")
    suspend fun delete(id: Long)
}
