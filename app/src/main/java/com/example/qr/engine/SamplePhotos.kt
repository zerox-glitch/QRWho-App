package com.example.qr.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader

data class SamplePhotoItem(
    val id: String,
    val name: String,
    val description: String,
    val assetPath: String
)

object SamplePhotos {
    val list = listOf(
        SamplePhotoItem("mountain", "Summit", "Mountain peak against crisp alpine horizon", "samples/mountain.jpg"),
        SamplePhotoItem("lake", "Lake", "Tranquil crystal waters and subtle reflection", "samples/lake.jpg"),
        SamplePhotoItem("peony", "Peony", "Lush floral petal folds and rich velvet tones", "samples/peony.jpg"),
        SamplePhotoItem("ink", "Ink", "Deep Japanese sumi-e ink dispersion on washi paper", "samples/ink.jpg"),
        SamplePhotoItem("dunes", "Dunes", "Warm desert sand ridges and soft shadows", "samples/dunes.jpg"),
        SamplePhotoItem("dusk", "Dusk", "Golden hour sunset fading to indigo night", "samples/dusk.jpg"),
        SamplePhotoItem("mist", "Mist", "Ethereal morning forest vapor and quiet fog", "samples/mist.jpg"),
        SamplePhotoItem("aurora", "Aurora", "Bioluminescent arctic sky with polar radiance", "samples/aurora.jpg"),
        SamplePhotoItem("silk", "Silk", "Flowing draped silk textile folds", "samples/silk.jpg"),
        SamplePhotoItem("blossom", "Blossom", "Delicate spring cherry blossom branch", "samples/blossom.jpg"),
        SamplePhotoItem("marble", "Marble", "Polished white carrara marble stone veins", "samples/marble.jpg"),
        SamplePhotoItem("tide", "Tide", "Ocean swell foam and turquoise sea wash", "samples/tide.jpg")
    )

    private val sampleThumbnails = java.util.concurrent.ConcurrentHashMap<String, Bitmap>()

    fun getThumbnail(context: Context, id: String, sizePx: Int = 160): Bitmap {
        return sampleThumbnails.getOrPut(id) {
            loadSampleBitmap(context, id, sizePx)
        }
    }

    fun loadSampleBitmap(context: Context, id: String, sizePx: Int = 1024): Bitmap {
        val item = list.find { it.id == id } ?: list.first()
        try {
            // Step 1: Decode image dimensions with bounds check
            val options = BitmapFactory.Options()
            context.assets.open(item.assetPath).use { rawStream ->
                java.io.BufferedInputStream(rawStream).use { stream ->
                    options.inJustDecodeBounds = true
                    BitmapFactory.decodeStream(stream, null, options)
                }
            }

            // Step 2: Compute inSampleSize based on requested sizePx
            val origW = options.outWidth
            val origH = options.outHeight
            var sampleSize = 1
            if (origW > sizePx || origH > sizePx) {
                val halfW = origW / 2
                val halfH = origH / 2
                while ((halfW / sampleSize) >= sizePx && (halfH / sampleSize) >= sizePx) {
                    sampleSize *= 2
                }
            }

            // Step 3: Decode scaled bitmap
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = if (sizePx <= 240) Bitmap.Config.RGB_565 else Bitmap.Config.ARGB_8888
            }

            val decoded = context.assets.open(item.assetPath).use { rawStream ->
                java.io.BufferedInputStream(rawStream).use { stream ->
                    BitmapFactory.decodeStream(stream, null, decodeOptions)
                }
            }

            if (decoded != null) {
                return if (sizePx <= 240 && (decoded.width > sizePx || decoded.height > sizePx)) {
                    Bitmap.createScaledBitmap(decoded, sizePx, sizePx, true)
                } else {
                    decoded
                }
            }
        } catch (_: Exception) {}

        // Fallback synthetic painterly canvas
        return createFallbackBitmap(id, sizePx)
    }

    private fun createFallbackBitmap(id: String, sizePx: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val s = sizePx.toFloat()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        when (id) {
            "mountain", "dusk" -> {
                paint.shader = LinearGradient(0f, 0f, 0f, s, 0xFFFF7E5F.toInt(), 0xFFFEB47B.toInt(), Shader.TileMode.CLAMP)
                canvas.drawRect(0f, 0f, s, s, paint)
                val sunPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFF1C5.toInt() }
                canvas.drawCircle(s * 0.5f, s * 0.45f, s * 0.16f, sunPaint)
                val mtnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF2D142C.toInt() }
                val path = Path().apply {
                    moveTo(0f, s)
                    lineTo(0f, s * 0.7f)
                    lineTo(s * 0.35f, s * 0.45f)
                    lineTo(s * 0.65f, s * 0.62f)
                    lineTo(s * 0.85f, s * 0.5f)
                    lineTo(s, s * 0.68f)
                    lineTo(s, s)
                    close()
                }
                canvas.drawPath(path, mtnPaint)
            }
            "lake" -> {
                paint.shader = LinearGradient(0f, 0f, 0f, s, 0xFF1A365D.toInt(), 0xFF319795.toInt(), Shader.TileMode.CLAMP)
                canvas.drawRect(0f, 0f, s, s, paint)
                val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFE6FFFA.toInt(); alpha = 160 }
                canvas.drawCircle(s * 0.5f, s * 0.85f, s * 0.45f, wavePaint)
            }
            "peony", "blossom" -> {
                paint.shader = LinearGradient(0f, 0f, s, s, 0xFF831843.toInt(), 0xFFF472B6.toInt(), Shader.TileMode.CLAMP)
                canvas.drawRect(0f, 0f, s, s, paint)
                val petalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFCE7F3.toInt(); alpha = 200 }
                canvas.drawCircle(s * 0.5f, s * 0.5f, s * 0.3f, petalPaint)
            }
            "ink" -> {
                paint.shader = LinearGradient(0f, 0f, s, s, 0xFFF4EFE6.toInt(), 0xFFE2D9C8.toInt(), Shader.TileMode.CLAMP)
                canvas.drawRect(0f, 0f, s, s, paint)
                val inkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF111827.toInt(); alpha = 230 }
                canvas.drawCircle(s * 0.45f, s * 0.5f, s * 0.28f, inkPaint)
                canvas.drawCircle(s * 0.65f, s * 0.4f, s * 0.18f, inkPaint)
            }
            "dunes" -> {
                paint.shader = LinearGradient(0f, 0f, 0f, s, 0xFFB45309.toInt(), 0xFFFDE68A.toInt(), Shader.TileMode.CLAMP)
                canvas.drawRect(0f, 0f, s, s, paint)
            }
            "mist" -> {
                paint.shader = LinearGradient(0f, 0f, 0f, s, 0xFF374151.toInt(), 0xFF9CA3AF.toInt(), Shader.TileMode.CLAMP)
                canvas.drawRect(0f, 0f, s, s, paint)
            }
            "aurora" -> {
                paint.shader = LinearGradient(0f, 0f, 0f, s, 0xFF050B14.toInt(), 0xFF0D253A.toInt(), Shader.TileMode.CLAMP)
                canvas.drawRect(0f, 0f, s, s, paint)
                val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = RadialGradient(s * 0.5f, s * 0.4f, s * 0.6f, 0xFF00FF88.toInt(), 0x00000000, Shader.TileMode.CLAMP)
                    alpha = 180
                }
                canvas.drawCircle(s * 0.5f, s * 0.4f, s * 0.6f, wavePaint)
            }
            "silk" -> {
                paint.shader = LinearGradient(0f, 0f, s, s, 0xFF4A044E.toInt(), 0xFFC084FC.toInt(), Shader.TileMode.CLAMP)
                canvas.drawRect(0f, 0f, s, s, paint)
            }
            "marble" -> {
                paint.shader = LinearGradient(0f, 0f, s, s, 0xFFE2E8F0.toInt(), 0xFFFFFFFF.toInt(), Shader.TileMode.CLAMP)
                canvas.drawRect(0f, 0f, s, s, paint)
            }
            "tide" -> {
                paint.shader = LinearGradient(0f, 0f, 0f, s, 0xFF0C4A6E.toInt(), 0xFF38BDF8.toInt(), Shader.TileMode.CLAMP)
                canvas.drawRect(0f, 0f, s, s, paint)
            }
            else -> {
                paint.shader = LinearGradient(0f, 0f, s, s, 0xFF2C3E50.toInt(), 0xFF3498DB.toInt(), Shader.TileMode.CLAMP)
                canvas.drawRect(0f, 0f, s, s, paint)
            }
        }
        return bitmap
    }
}
