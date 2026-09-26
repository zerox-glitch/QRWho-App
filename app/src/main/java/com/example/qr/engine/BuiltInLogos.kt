package com.example.qr.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF

data class LogoItem(
    val id: String,
    val name: String,
    val brandColor: Int
)

object BuiltInLogos {
    val list = listOf(
        LogoItem("apple", "Apple", 0xFF000000.toInt()),
        LogoItem("google", "Google", 0xFF4285F4.toInt()),
        LogoItem("spotify", "Spotify", 0xFF1DB954.toInt()),
        LogoItem("instagram", "Instagram", 0xFFE1306C.toInt()),
        LogoItem("youtube", "YouTube", 0xFFFF0000.toInt()),
        LogoItem("twitter", "X / Twitter", 0xFF000000.toInt()),
        LogoItem("whatsapp", "WhatsApp", 0xFF25D366.toInt()),
        LogoItem("github", "GitHub", 0xFF24292E.toInt()),
        LogoItem("linkedin", "LinkedIn", 0xFF0A66C2.toInt()),
        LogoItem("telegram", "Telegram", 0xFF229ED9.toInt()),
        LogoItem("discord", "Discord", 0xFF5865F2.toInt()),
        LogoItem("paypal", "PayPal", 0xFF003087.toInt()),
        LogoItem("bitcoin", "Bitcoin", 0xFFF7931A.toInt()),
        LogoItem("ethereum", "Ethereum", 0xFF627EEA.toInt()),
        LogoItem("wifi", "Wi-Fi", 0xFF00B4D8.toInt()),
        LogoItem("camera", "Camera", 0xFF7A5AF8.toInt()),
        LogoItem("coffee", "Coffee", 0xFF78350F.toInt()),
        LogoItem("heart", "Heart", 0xFFE11D48.toInt()),
        LogoItem("star", "Star", 0xFFF59E0B.toInt()),
        LogoItem("sparkle", "Beacon", 0xFF00F0FF.toInt())
    )

    fun createLogoBitmap(id: String, sizePx: Int = 200): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val s = sizePx.toFloat()
        val c = s / 2f
        val logo = list.find { it.id == id } ?: list.last()

        paint.color = logo.brandColor
        paint.style = Paint.Style.FILL

        when (id) {
            "apple" -> {
                // Apple body and leaf
                val path = Path()
                path.addCircle(c - s * 0.12f, c + s * 0.05f, s * 0.28f, Path.Direction.CW)
                path.addCircle(c + s * 0.12f, c + s * 0.05f, s * 0.28f, Path.Direction.CW)
                // cutout bite
                val bite = Path()
                bite.addCircle(c + s * 0.32f, c, s * 0.15f, Path.Direction.CW)
                path.op(bite, Path.Op.DIFFERENCE)
                // leaf
                val leaf = Path()
                leaf.addOval(RectF(c - s * 0.04f, c - s * 0.42f, c + s * 0.18f, c - s * 0.24f), Path.Direction.CW)
                path.op(leaf, Path.Op.UNION)
                canvas.drawPath(path, paint)
            }
            "spotify" -> {
                // Spotify circle with 3 curved sound waves
                canvas.drawCircle(c, c, s * 0.42f, paint)
                paint.color = 0xFFFFFFFF.toInt()
                paint.style = Paint.Style.STROKE
                paint.strokeCap = Paint.Cap.ROUND

                paint.strokeWidth = s * 0.075f
                val oval1 = RectF(c - s * 0.32f, c - s * 0.26f, c + s * 0.32f, c + s * 0.18f)
                canvas.drawArc(oval1, 205f, 130f, false, paint)

                paint.strokeWidth = s * 0.065f
                val oval2 = RectF(c - s * 0.26f, c - s * 0.12f, c + s * 0.26f, c + s * 0.24f)
                canvas.drawArc(oval2, 205f, 130f, false, paint)

                paint.strokeWidth = s * 0.055f
                val oval3 = RectF(c - s * 0.20f, c + s * 0.02f, c + s * 0.20f, c + s * 0.30f)
                canvas.drawArc(oval3, 205f, 130f, false, paint)
            }
            "youtube" -> {
                // Red rounded rectangle with play triangle
                val r = RectF(c - s * 0.42f, c - s * 0.28f, c + s * 0.42f, c + s * 0.28f)
                canvas.drawRoundRect(r, s * 0.12f, s * 0.12f, paint)
                paint.color = 0xFFFFFFFF.toInt()
                val tri = Path()
                tri.moveTo(c - s * 0.10f, c - s * 0.14f)
                tri.lineTo(c + s * 0.16f, c)
                tri.lineTo(c - s * 0.10f, c + s * 0.14f)
                tri.close()
                canvas.drawPath(tri, paint)
            }
            "instagram" -> {
                val r = RectF(c - s * 0.38f, c - s * 0.38f, c + s * 0.38f, c + s * 0.38f)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = s * 0.08f
                canvas.drawRoundRect(r, s * 0.18f, s * 0.18f, paint)
                canvas.drawCircle(c, c, s * 0.16f, paint)
                paint.style = Paint.Style.FILL
                canvas.drawCircle(c + s * 0.22f, c - s * 0.22f, s * 0.045f, paint)
            }
            "twitter" -> {
                // X logo
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = s * 0.10f
                paint.strokeCap = Paint.Cap.ROUND
                canvas.drawLine(c - s * 0.28f, c - s * 0.28f, c + s * 0.28f, c + s * 0.28f, paint)
                canvas.drawLine(c + s * 0.28f, c - s * 0.28f, c - s * 0.28f, c + s * 0.28f, paint)
            }
            "whatsapp" -> {
                canvas.drawCircle(c, c, s * 0.42f, paint)
                paint.color = 0xFFFFFFFF.toInt()
                paint.style = Paint.Style.FILL
                // phone receiver shape
                val path = Path()
                path.addCircle(c, c, s * 0.22f, Path.Direction.CW)
                canvas.drawPath(path, paint)
            }
            "bitcoin" -> {
                canvas.drawCircle(c, c, s * 0.42f, paint)
                paint.color = 0xFFFFFFFF.toInt()
                paint.textSize = s * 0.45f
                paint.textAlign = Paint.Align.CENTER
                paint.isFakeBoldText = true
                canvas.drawText("₿", c, c + s * 0.16f, paint)
            }
            "ethereum" -> {
                canvas.drawCircle(c, c, s * 0.42f, paint)
                paint.color = 0xFFFFFFFF.toInt()
                val path = Path()
                path.moveTo(c, c - s * 0.28f)
                path.lineTo(c + s * 0.18f, c)
                path.lineTo(c, c + s * 0.12f)
                path.lineTo(c - s * 0.18f, c)
                path.close()
                canvas.drawPath(path, paint)

                val bottom = Path()
                bottom.moveTo(c, c + s * 0.16f)
                bottom.lineTo(c + s * 0.18f, c + s * 0.04f)
                bottom.lineTo(c, c + s * 0.30f)
                bottom.lineTo(c - s * 0.18f, c + s * 0.04f)
                bottom.close()
                canvas.drawPath(bottom, paint)
            }
            "heart" -> {
                val path = Path()
                val r = s * 0.20f
                path.addCircle(c - s * 0.15f, c - s * 0.08f, r, Path.Direction.CW)
                path.addCircle(c + s * 0.15f, c - s * 0.08f, r, Path.Direction.CW)
                val tri = Path()
                tri.moveTo(c - s * 0.32f, c - s * 0.04f)
                tri.lineTo(c + s * 0.32f, c - s * 0.04f)
                tri.lineTo(c, c + s * 0.35f)
                tri.close()
                path.op(tri, Path.Op.UNION)
                canvas.drawPath(path, paint)
            }
            "star" -> {
                val path = Path()
                val points = 5
                val outer = s * 0.40f
                val inner = s * 0.18f
                for (i in 0 until points * 2) {
                    val angle = (Math.PI / points * i - Math.PI / 2).toFloat()
                    val rad = if (i % 2 == 0) outer else inner
                    val x = c + kotlin.math.cos(angle.toDouble()).toFloat() * rad
                    val y = c + kotlin.math.sin(angle.toDouble()).toFloat() * rad
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                canvas.drawPath(path, paint)
            }
            "wifi" -> {
                paint.style = Paint.Style.STROKE
                paint.strokeCap = Paint.Cap.ROUND
                paint.strokeWidth = s * 0.08f
                canvas.drawArc(RectF(c - s * 0.38f, c - s * 0.28f, c + s * 0.38f, c + s * 0.38f), 215f, 110f, false, paint)
                canvas.drawArc(RectF(c - s * 0.24f, c - s * 0.14f, c + s * 0.24f, c + s * 0.24f), 215f, 110f, false, paint)
                paint.style = Paint.Style.FILL
                canvas.drawCircle(c, c + s * 0.22f, s * 0.06f, paint)
            }
            else -> {
                // Sparkle / Beacon logo
                val path = Path()
                path.moveTo(c, c - s * 0.40f)
                path.quadTo(c + s * 0.05f, c - s * 0.05f, c + s * 0.40f, c)
                path.quadTo(c + s * 0.05f, c + s * 0.05f, c, c + s * 0.40f)
                path.quadTo(c - s * 0.05f, c + s * 0.05f, c - s * 0.40f, c)
                path.quadTo(c - s * 0.05f, c - s * 0.05f, c, c - s * 0.40f)
                path.close()
                canvas.drawPath(path, paint)
            }
        }
        return bitmap
    }
}
