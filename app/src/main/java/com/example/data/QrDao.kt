package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QrDao {
    // History
    @Query("SELECT * FROM qr_items ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<QrEntity>>

    @Query("SELECT * FROM qr_items WHERE isScanned = 1 ORDER BY timestamp DESC")
    fun getScannedHistory(): Flow<List<QrEntity>>

    @Query("SELECT * FROM qr_items WHERE isScanned = 0 ORDER BY timestamp DESC")
    fun getGeneratedHistory(): Flow<List<QrEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQr(item: QrEntity): Long

    @Query("DELETE FROM qr_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM qr_items")
    suspend fun clearAll()

    @Query("DELETE FROM qr_items WHERE isScanned = :isScanned")
    suspend fun clearByType(isScanned: Boolean)

    // Custom Presets
    @Query("SELECT * FROM custom_presets ORDER BY timestamp DESC")
    fun getCustomPresets(): Flow<List<CustomPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomPreset(preset: CustomPresetEntity): Long

    @Query("DELETE FROM custom_presets WHERE id = :id")
    suspend fun deleteCustomPreset(id: Long)

    // Favorites
    @Query("SELECT presetId FROM favorite_presets")
    fun getFavoriteIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoritePresetEntity)

    @Query("DELETE FROM favorite_presets WHERE presetId = :presetId")
    suspend fun removeFavorite(presetId: String)
}
