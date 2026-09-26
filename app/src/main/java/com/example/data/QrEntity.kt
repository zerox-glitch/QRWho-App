package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "qr_items")
data class QrEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val payloadKind: String,
    val payloadRaw: String = "",
    val encodedText: String,
    val presetName: String? = null,
    val moduleShape: String = "Rounded",
    val eyeShape: String = "Rounded",
    val ballShape: String = "Circle",
    val fgColor: Int = 0xFF0F172A.toInt(),
    val bgColor: Int = 0xFFFFFFFF.toInt(),
    val eyeColor: Int = 0xFF0F172A.toInt(),
    val ballColor: Int = 0xFF0F172A.toInt(),
    val gradientType: String = "None",
    val gradientTo: Int = 0xFF7A5AF8.toInt(),
    val frameStyle: String = "None",
    val frameCaption: String = "SCAN ME",
    val quietZone: Int = 3,
    val moduleGap: Float = 0.04f,
    val dotScale: Float = 0.88f,
    val contrast: Float = 1.0f,
    val imageMode: String = "None",
    val imageOpacity: Float = 0.86f,
    val ecc: String = "H",
    val scanScore: Int = 98,
    val isScanned: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
