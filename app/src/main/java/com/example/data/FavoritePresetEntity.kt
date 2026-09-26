package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_presets")
data class FavoritePresetEntity(
    @PrimaryKey
    val presetId: String,
    val timestamp: Long = System.currentTimeMillis()
)
