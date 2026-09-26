package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QrDao {
    @Query("SELECT * FROM qr_items ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<QrEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQr(item: QrEntity): Long

    @Query("DELETE FROM qr_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM qr_items")
    suspend fun clearAll()
}
