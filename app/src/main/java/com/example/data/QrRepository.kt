package com.example.data

import kotlinx.coroutines.flow.Flow

class QrRepository(private val dao: QrDao) {
    val allHistory: Flow<List<QrEntity>> = dao.getAllHistory()

    suspend fun saveQr(item: QrEntity): Long = dao.insertQr(item)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun clearAll() = dao.clearAll()
}
