package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.qr.engine.EyeShape
import com.example.qr.engine.FrameStyle
import com.example.qr.engine.GradientType
import com.example.qr.engine.ModuleShape
import com.example.qr.engine.QrPreset
import com.example.qr.engine.QrStyle

@Entity(tableName = "custom_presets")
data class CustomPresetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String = "My Presets",
    val description: String = "Custom user preset",
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
    val ecc: String = "H",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toQrPreset(): QrPreset {
        val style = QrStyle(
            moduleShape = try { ModuleShape.valueOf(moduleShape) } catch (_: Exception) { ModuleShape.Rounded },
            eyeShape = try { EyeShape.valueOf(eyeShape) } catch (_: Exception) { EyeShape.Rounded },
            ballShape = try { EyeShape.valueOf(ballShape) } catch (_: Exception) { EyeShape.Circle },
            fgColor = fgColor,
            bgColor = bgColor,
            eyeColor = eyeColor,
            ballColor = ballColor,
            gradientType = try { GradientType.valueOf(gradientType) } catch (_: Exception) { GradientType.None },
            gradientTo = gradientTo,
            frameStyle = try { FrameStyle.valueOf(frameStyle) } catch (_: Exception) { FrameStyle.None },
            frameCaption = frameCaption,
            quietZone = quietZone,
            moduleGap = moduleGap,
            dotScale = dotScale,
            contrast = contrast,
            ecc = ecc
        )
        return QrPreset(
            id = "custom_$id",
            name = name,
            category = "My Presets",
            description = description,
            style = style,
            featured = true
        )
    }
}
