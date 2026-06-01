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

    @Query("DELETE FROM bonsai WHERE id = :id")
    suspend fun delete(id: Long)
}
