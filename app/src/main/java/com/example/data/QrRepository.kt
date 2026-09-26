package com.example.data

import kotlinx.coroutines.flow.Flow

class QrRepository(private val dao: QrDao) {
    val allHistory: Flow<List<QrEntity>> = dao.getAllHistory()
    val scannedHistory: Flow<List<QrEntity>> = dao.getScannedHistory()
    val generatedHistory: Flow<List<QrEntity>> = dao.getGeneratedHistory()
    val customPresets: Flow<List<CustomPresetEntity>> = dao.getCustomPresets()
    val favoriteIds: Flow<List<String>> = dao.getFavoriteIds()

    suspend fun saveQr(item: QrEntity): Long = dao.insertQr(item)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun clearAll() = dao.clearAll()

    suspend fun clearByType(isScanned: Boolean) = dao.clearByType(isScanned)

    suspend fun saveCustomPreset(preset: CustomPresetEntity): Long = dao.insertCustomPreset(preset)

    suspend fun deleteCustomPreset(id: Long) = dao.deleteCustomPreset(id)

    suspend fun addFavorite(presetId: String) {
        dao.addFavorite(FavoritePresetEntity(presetId = presetId))
    }

    suspend fun removeFavorite(presetId: String) {
        dao.removeFavorite(presetId)
    }
}
