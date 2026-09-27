package com.example.qr.engine

import android.graphics.Bitmap
import android.graphics.Color

data class LogoItem(
    val id: String,
    val name: String,
    val category: String = "Custom",
    val brandColor: Int = Color.BLACK
)

object BuiltInLogos {
    // All pre-added logos removed
    val list: List<LogoItem> = emptyList()

    fun createLogoBitmap(id: String, size: Int): Bitmap {
        val s = size.coerceAtLeast(64)
        return Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888)
    }
}
