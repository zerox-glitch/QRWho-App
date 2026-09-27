package com.example.qr.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.LinearGradient
import android.graphics.Shader
import kotlin.math.cos
import kotlin.math.sin

data class LogoItem(
    val id: String,
    val name: String,
    val category: String,
    val brandColor: Int
)

object BuiltInLogos {
    val list = listOf(
        // Social & Messaging
        LogoItem("whatsapp", "WhatsApp", "Social", 0xFF25D366.toInt()),
        LogoItem("instagram", "Instagram", "Social", 0xFFE1306C.toInt()),
        LogoItem("youtube", "YouTube", "Social", 0xFFFF0000.toInt()),
        LogoItem("tiktok", "TikTok", "Social", 0xFF010101.toInt()),
        LogoItem("twitter", "X / Twitter", "Social", 0xFF000000.toInt()),
        LogoItem("facebook", "Facebook", "Social", 0xFF1877F2.toInt()),
        LogoItem("telegram", "Telegram", "Social", 0xFF24A1DE.toInt()),
        LogoItem("discord", "Discord", "Social", 0xFF5865F2.toInt()),
        LogoItem("spotify", "Spotify", "Social", 0xFF1DB954.toInt()),
        LogoItem("snapchat", "Snapchat", "Social", 0xFFFFFC00.toInt()),
        LogoItem("linkedin", "LinkedIn", "Social", 0xFF0A66C2.toInt()),
        LogoItem("pinterest", "Pinterest", "Social", 0xFFE60023.toInt()),
        LogoItem("reddit", "Reddit", "Social", 0xFFFF4500.toInt()),
        LogoItem("github", "GitHub", "Social", 0xFF24292E.toInt()),
        LogoItem("twitch", "Twitch", "Social", 0xFF9146FF.toInt()),

        // Daily Life & Business
        LogoItem("google", "Google", "Daily Life", 0xFF4285F4.toInt()),
        LogoItem("apple", "Apple", "Daily Life", 0xFF000000.toInt()),
        LogoItem("wifi", "Wi-Fi", "Daily Life", 0xFF00B4D8.toInt()),
        LogoItem("phone", "Phone", "Daily Life", 0xFF10B981.toInt()),
        LogoItem("mail", "Email", "Daily Life", 0xFFEA580C.toInt()),
        LogoItem("location", "Location", "Daily Life", 0xFFE11D48.toInt()),
        LogoItem("website", "Website", "Daily Life", 0xFF2563EB.toInt()),
        LogoItem("camera", "Camera", "Daily Life", 0xFF8B5CF6.toInt()),
        LogoItem("coffee", "Coffee", "Daily Life", 0xFF78350F.toInt()),
        LogoItem("restaurant", "Dining", "Daily Life", 0xFFD97706.toInt()),
        LogoItem("shopping", "Store", "Daily Life", 0xFF0D9488.toInt()),
        LogoItem("music", "Music", "Daily Life", 0xFFEC4899.toInt()),

        // Payments & Symbols
        LogoItem("paypal", "PayPal", "Payment", 0xFF003087.toInt()),
        LogoItem("bitcoin", "Bitcoin", "Payment", 0xFFF7931A.toInt()),
        LogoItem("ethereum", "Ethereum", "Payment", 0xFF627EEA.toInt()),
        LogoItem("heart", "Heart", "Symbols", 0xFFE11D48.toInt()),
        LogoItem("star", "Star", "Symbols", 0xFFF59E0B.toInt()),
        LogoItem("sparkle", "Beacon", "Symbols", 0xFF00F0FF.toInt())
    )

    private val logoCache = mutableMapOf<String, Bitmap>()

    fun createLogoBitmap(id: String, sizePx: Int = 200): Bitmap {
        val cacheKey = "${id}_$sizePx"
        logoCache[cacheKey]?.let { return it }

        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val s = sizePx.toFloat()
        val c = s / 2f
        val logo = list.find { it.id == id } ?: list.last()

        paint.color = logo.brandColor
        paint.style = Paint.Style.FILL

        when (id) {
            "whatsapp" -> {
                // WhatsApp green circle + chat bubble tail + white phone handset
                canvas.drawCircle(c, c, s * 0.46f, paint)
                val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFFFFFFFF.toInt()
                    style = Paint.Style.FILL
                }
                // Chat bubble
                val bubblePath = Path().apply {
                    addCircle(c, c, s * 0.32f, Path.Direction.CW)
                    moveTo(c - s * 0.20f, c + s * 0.20f)
                    lineTo(c - s * 0.35f, c + s * 0.35f)
                    lineTo(c - s * 0.12f, c + s * 0.30f)
                    close()
                }
                canvas.drawPath(bubblePath, whitePaint)

                // Phone icon inside in green
                val phonePath = Path().apply {
                    val pr = RectF(c - s * 0.18f, c - s * 0.18f, c + s * 0.18f, c + s * 0.18f)
                    addRoundRect(pr, s * 0.08f, s * 0.08f, Path.Direction.CW)
                }
                canvas.drawPath(phonePath, paint)
            }

            "instagram" -> {
                // Sunset Gradient rounded square + camera glyph
                val grad = LinearGradient(
                    0f, s, s, 0f,
                    intArrayOf(0xFFFFD600.toInt(), 0xFFFF0100.toInt(), 0xFFD800B9.toInt(), 0xFF7000FF.toInt()),
                    null,
                    Shader.TileMode.CLAMP
                )
                paint.shader = grad
                canvas.drawRoundRect(RectF(s * 0.06f, s * 0.06f, s * 0.94f, s * 0.94f), s * 0.24f, s * 0.24f, paint)
                paint.shader = null

                val whiteP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFFFFFFFF.toInt()
                    style = Paint.Style.STROKE
                    strokeWidth = s * 0.075f
                }
                canvas.drawRoundRect(RectF(s * 0.22f, s * 0.22f, s * 0.78f, s * 0.78f), s * 0.15f, s * 0.15f, whiteP)
                canvas.drawCircle(c, c, s * 0.14f, whiteP)
                whiteP.style = Paint.Style.FILL
                canvas.drawCircle(c + s * 0.18f, c - s * 0.18f, s * 0.038f, whiteP)
            }

            "youtube" -> {
                // Red rounded rectangle with play triangle
                val r = RectF(s * 0.08f, s * 0.18f, s * 0.92f, s * 0.82f)
                canvas.drawRoundRect(r, s * 0.20f, s * 0.20f, paint)
                val whiteP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFFFFFFFF.toInt()
                    style = Paint.Style.FILL
                }
                val tri = Path().apply {
                    moveTo(c - s * 0.12f, c - s * 0.15f)
                    lineTo(c + s * 0.18f, c)
                    lineTo(c - s * 0.12f, c + s * 0.15f)
                    close()
                }
                canvas.drawPath(tri, whiteP)
            }

            "tiktok" -> {
                canvas.drawRoundRect(RectF(s * 0.06f, s * 0.06f, s * 0.94f, s * 0.94f), s * 0.22f, s * 0.22f, paint)
                // Cyan music note shadow
                val cyanP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF25F4EE.toInt(); style = Paint.Style.FILL }
                val redP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFE2C55.toInt(); style = Paint.Style.FILL }
                val whiteP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt(); style = Paint.Style.FILL }

                fun drawNote(offsetX: Float, offsetY: Float, p: Paint) {
                    val nx = c + offsetX
                    val ny = c + offsetY
                    canvas.drawCircle(nx - s * 0.08f, ny + s * 0.15f, s * 0.11f, p)
                    canvas.drawRect(nx - s * 0.02f, ny - s * 0.25f, nx + s * 0.06f, ny + s * 0.15f, p)
                    val flag = Path().apply {
                        moveTo(nx + s * 0.06f, ny - s * 0.25f)
                        cubicTo(nx + s * 0.20f, ny - s * 0.25f, nx + s * 0.25f, ny - s * 0.15f, nx + s * 0.25f, ny - s * 0.05f)
                        lineTo(nx + s * 0.18f, ny - s * 0.05f)
                        cubicTo(nx + s * 0.18f, ny - s * 0.12f, nx + s * 0.14f, ny - s * 0.18f, nx + s * 0.06f, ny - s * 0.18f)
                        close()
                    }
                    canvas.drawPath(flag, p)
                }

                drawNote(-s * 0.025f, 0f, cyanP)
                drawNote(s * 0.025f, 0f, redP)
                drawNote(0f, 0f, whiteP)
            }

            "twitter" -> {
                // X logo
                canvas.drawRoundRect(RectF(s * 0.06f, s * 0.06f, s * 0.94f, s * 0.94f), s * 0.22f, s * 0.22f, paint)
                val whiteP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFFFFFFFF.toInt()
                    style = Paint.Style.STROKE
                    strokeWidth = s * 0.10f
                    strokeCap = Paint.Cap.ROUND
                }
                canvas.drawLine(c - s * 0.26f, c - s * 0.26f, c + s * 0.26f, c + s * 0.26f, whiteP)
                canvas.drawLine(c + s * 0.26f, c - s * 0.26f, c - s * 0.26f, c + s * 0.26f, whiteP)
            }

            "facebook" -> {
                canvas.drawCircle(c, c, s * 0.46f, paint)
                val whiteP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFFFFFFFF.toInt()
                    style = Paint.Style.FILL
                    textSize = s * 0.65f
                    textAlign = Paint.Align.CENTER
                    isFakeBoldText = true
                }
                canvas.drawText("f", c + s * 0.08f, c + s * 0.24f, whiteP)
            }

            "telegram" -> {
                canvas.drawCircle(c, c, s * 0.46f, paint)
                val whiteP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFFFFFFFF.toInt()
                    style = Paint.Style.FILL
                }
                val plane = Path().apply {
                    moveTo(c - s * 0.26f, c)
                    lineTo(c + s * 0.28f, c - s * 0.22f)
                    lineTo(c + s * 0.12f, c + s * 0.24f)
                    lineTo(c, c + s * 0.10f)
                    lineTo(c - s * 0.06f, c + s * 0.20f)
                    lineTo(c - s * 0.06f, c + s * 0.08f)
                    close()
                }
                canvas.drawPath(plane, whiteP)
            }

            "discord" -> {
                canvas.drawCircle(c, c, s * 0.46f, paint)
                val whiteP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt(); style = Paint.Style.FILL }
                val faceRect = RectF(c - s * 0.28f, c - s * 0.18f, c + s * 0.28f, c + s * 0.18f)
                canvas.drawRoundRect(faceRect, s * 0.14f, s * 0.14f, whiteP)
                // Eyes cutout
                val darkP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = logo.brandColor; style = Paint.Style.FILL }
                canvas.drawCircle(c - s * 0.12f, c, s * 0.055f, darkP)
                canvas.drawCircle(c + s * 0.12f, c, s * 0.055f, darkP)
            }

            "spotify" -> {
                canvas.drawCircle(c, c, s * 0.46f, paint)
                val whiteP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFFFFFFFF.toInt()
                    style = Paint.Style.STROKE
                    strokeCap = Paint.Cap.ROUND
                }
                whiteP.strokeWidth = s * 0.075f
                canvas.drawArc(RectF(c - s * 0.28f, c - s * 0.22f, c + s * 0.28f, c + s * 0.14f), 205f, 130f, false, whiteP)
                whiteP.strokeWidth = s * 0.060f
                canvas.drawArc(RectF(c - s * 0.22f, c - s * 0.10f, c + s * 0.22f, c + s * 0.20f), 205f, 130f, false, whiteP)
                whiteP.strokeWidth = s * 0.048f
                canvas.drawArc(RectF(c - s * 0.16f, c + s * 0.02f, c + s * 0.16f, c + s * 0.26f), 205f, 130f, false, whiteP)
            }

            "snapchat" -> {
                canvas.drawRoundRect(RectF(s * 0.06f, s * 0.06f, s * 0.94f, s * 0.94f), s * 0.22f, s * 0.22f, paint)
                val whiteP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt(); style = Paint.Style.FILL }
                val darkP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF000000.toInt(); style = Paint.Style.STROKE; strokeWidth = s * 0.03f }
                val ghost = Path().apply {
                    addCircle(c, c - s * 0.10f, s * 0.16f, Path.Direction.CW)
                    moveTo(c - s * 0.16f, c - s * 0.05f)
                    lineTo(c - s * 0.24f, c + s * 0.10f)
                    lineTo(c - s * 0.14f, c + s * 0.16f)
                    lineTo(c - s * 0.08f, c + s * 0.24f)
                    lineTo(c, c + s * 0.18f)
                    lineTo(c + s * 0.08f, c + s * 0.24f)
                    lineTo(c + s * 0.14f, c + s * 0.16f)
                    lineTo(c + s * 0.24f, c + s * 0.10f)
                    lineTo(c + s * 0.16f, c - s * 0.05f)
                    close()
                }
                canvas.drawPath(ghost, whiteP)
                canvas.drawPath(ghost, darkP)
            }

            "linkedin" -> {
                canvas.drawRoundRect(RectF(s * 0.06f, s * 0.06f, s * 0.94f, s * 0.94f), s * 0.22f, s * 0.22f, paint)
                val whiteP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFFFFFFFF.toInt()
                    style = Paint.Style.FILL
                    textSize = s * 0.44f
                    textAlign = Paint.Align.CENTER
                    isFakeBoldText = true
                }
                canvas.drawText("in", c, c + s * 0.15f, whiteP)
            }

            "pinterest" -> {
                canvas.drawCircle(c, c, s * 0.46f, paint)
                val whiteP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFFFFFFFF.toInt()
                    style = Paint.Style.FILL
                    textSize = s * 0.58f
                    textAlign = Paint.Align.CENTER
                    isFakeBoldText = true
                }
                canvas.drawText("P", c - s * 0.02f, c + s * 0.20f, whiteP)
            }

            "reddit" -> {
                canvas.drawCircle(c, c, s * 0.46f, paint)
                val whiteP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt(); style = Paint.Style.FILL }
                canvas.drawOval(RectF(c - s * 0.26f, c - s * 0.14f, c + s * 0.26f, c + s * 0.18f), whiteP)
                // Ears
                canvas.drawCircle(c - s * 0.24f, c - s * 0.08f, s * 0.07f, whiteP)
                canvas.drawCircle(c + s * 0.24f, c - s * 0.08f, s * 0.07f, whiteP)
                // Eyes
                val eyeP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFF4500.toInt(); style = Paint.Style.FILL }
                canvas.drawCircle(c - s * 0.10f, c + s * 0.02f, s * 0.045f, eyeP)
                canvas.drawCircle(c + s * 0.10f, c + s * 0.02f, s * 0.045f, eyeP)
            }

            "github" -> {
                canvas.drawCircle(c, c, s * 0.46f, paint)
                val whiteP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt(); style = Paint.Style.FILL }
                // Octocat head & ears
                val head = Path().apply {
                    addCircle(c, c + s * 0.04f, s * 0.24f, Path.Direction.CW)
                    moveTo(c - s * 0.20f, c - s * 0.04f)
                    lineTo(c - s * 0.24f, c - s * 0.24f)
                    lineTo(c - s * 0.06f, c - s * 0.14f)
                    close()
                    moveTo(c + s * 0.20f, c - s * 0.04f)
                    lineTo(c + s * 0.24f, c - s * 0.24f)
                    lineTo(c + s * 0.06f, c - s * 0.14f)
                    close()
                }
                canvas.drawPath(head, whiteP)
            }

            "twitch" -> {
                canvas.drawRoundRect(RectF(s * 0.06f, s * 0.06f, s * 0.94f, s * 0.94f), s * 0.22f, s * 0.22f, paint)
                val whiteP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt(); style = Paint.Style.FILL }
                val bubble = Path().apply {
                    addRoundRect(RectF(c - s * 0.26f, c - s * 0.24f, c + s * 0.26f, c + s * 0.18f), s * 0.08f, s * 0.08f, Path.Direction.CW)
                    moveTo(c - s * 0.10f, c + s * 0.18f)
                    lineTo(c - s * 0.18f, c + s * 0.32f)
                    lineTo(c - s * 0.02f, c + s * 0.18f)
                    close()
                }
                canvas.drawPath(bubble, whiteP)
                // Eyes
                val purpleP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = logo.brandColor; style = Paint.Style.FILL }
                canvas.drawRect(c - s * 0.12f, c - s * 0.10f, c - s * 0.05f, c + s * 0.06f, purpleP)
                canvas.drawRect(c + s * 0.05f, c - s * 0.10f, c + s * 0.12f, c + s * 0.06f, purpleP)
            }

            "google" -> {
                // White circle background + 4-color Google G
                canvas.drawCircle(c, c, s * 0.46f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt() })
                val gPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = s * 0.14f
                    strokeCap = Paint.Cap.BUTT
                }
                val gRect = RectF(c - s * 0.24f, c - s * 0.24f, c + s * 0.24f, c + s * 0.24f)
                // Red arc
                gPaint.color = 0xFFEA4335.toInt()
                canvas.drawArc(gRect, 200f, 110f, false, gPaint)
                // Yellow arc
                gPaint.color = 0xFFFBBC05.toInt()
                canvas.drawArc(gRect, 120f, 80f, false, gPaint)
                // Green arc
                gPaint.color = 0xFF34A853.toInt()
                canvas.drawArc(gRect, 45f, 75f, false, gPaint)
                // Blue arc and horizontal bar
                gPaint.color = 0xFF4285F4.toInt()
                canvas.drawArc(gRect, 320f, 85f, false, gPaint)
                canvas.drawLine(c, c, c + s * 0.24f, c, gPaint)
            }

            "apple" -> {
                canvas.drawCircle(c, c, s * 0.46f, paint)
                val whiteP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt(); style = Paint.Style.FILL }
                val applePath = Path().apply {
                    addCircle(c - s * 0.10f, c + s * 0.04f, s * 0.22f, Path.Direction.CW)
                    addCircle(c + s * 0.10f, c + s * 0.04f, s * 0.22f, Path.Direction.CW)
                }
                // Bite cutout
                val bite = Path().apply { addCircle(c + s * 0.28f, c - s * 0.02f, s * 0.14f, Path.Direction.CW) }
                applePath.op(bite, Path.Op.DIFFERENCE)
                // Leaf
                val leaf = Path().apply {
                    moveTo(c + s * 0.02f, c - s * 0.30f)
                    quadTo(c + s * 0.18f, c - s * 0.32f, c + s * 0.16f, c - s * 0.16f)
                    quadTo(c, c - s * 0.14f, c + s * 0.02f, c - s * 0.30f)
                    close()
                }
                applePath.op(leaf, Path.Op.UNION)
                canvas.drawPath(applePath, whiteP)
            }

            "wifi" -> {
                canvas.drawCircle(c, c, s * 0.46f, paint)
                val whiteP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFFFFFFFF.toInt()
                    style = Paint.Style.STROKE
                    strokeCap = Paint.Cap.ROUND
                    strokeWidth = s * 0.075f
                }
                canvas.drawArc(RectF(c - s * 0.30f, c - s * 0.22f, c + s * 0.30f, c + s * 0.38f), 215f, 110f, false, whiteP)
                canvas.drawArc(RectF(c - s * 0.18f, c - s * 0.10f, c + s * 0.18f, c + s * 0.26f), 215f, 110f, false, whiteP)
                whiteP.style = Paint.Style.FILL
                canvas.drawCircle(c, c + s * 0.22f, s * 0.055f, whiteP)
            }

            "phone" -> {
                canvas.drawCircle(c, c, s * 0.46f, paint)
                val whiteP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt(); style = Paint.Style.FILL }
                val handset = Path().apply {
                    moveTo(c - s * 0.14f, c - s * 0.22f)
                    quadTo(c - s * 0.26f, c - s * 0.08f, c - s * 0.08f, c + s * 0.18f)
                    quadTo(c + s * 0.08f, c + s * 0.26f, c + s * 0.22f, c + s * 0.14f)
                    lineTo(c + s * 0.14f, c + s * 0.04f)
                    lineTo(c + s * 0.06f, c + s * 0.08f)
                    quadTo(c - s * 0.02f, c - s * 0.02f, c - s * 0.08f, c - s * 0.06f)
                    lineTo(c - s * 0.04f, c - s * 0.14f)
                    close()
                }
                canvas.drawPath(handset, whiteP)
            }

            "mail" -> {
                canvas.drawCircle(c, c, s * 0.46f, paint)
                val whiteP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt(); style = Paint.Style.FILL }
                val envRect = RectF(c - s * 0.28f, c - s * 0.18f, c + s * 0.28f, c + s * 0.18f)
                canvas.drawRoundRect(envRect, s * 0.06f, s * 0.06f, whiteP)
                val lineP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = logo.brandColor
                    style = Paint.Style.STROKE
                    strokeWidth = s * 0.04f
                    strokeCap = Paint.Cap.ROUND
                }
                canvas.drawLine(c - s * 0.24f, c - s * 0.14f, c, c + s * 0.04f, lineP)
                canvas.drawLine(c + s * 0.24f, c - s * 0.14f, c, c + s * 0.04f, lineP)
            }

            "location" -> {
                canvas.drawCircle(c, c, s * 0.46f, paint)
                val whiteP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt(); style = Paint.Style.FILL }
                val pin = Path().apply {
                    addCircle(c, c - s * 0.08f, s * 0.18f, Path.Direction.CW)
                    moveTo(c - s * 0.16f, c - s * 0.04f)
                    lineTo(c, c + s * 0.28f)
                    lineTo(c + s * 0.16f, c - s * 0.04f)
                    close()
                }
                canvas.drawPath(pin, whiteP)
                val holeP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = logo.brandColor; style = Paint.Style.FILL }
                canvas.drawCircle(c, c - s * 0.08f, s * 0.075f, holeP)
            }

            "website" -> {
                canvas.drawCircle(c, c, s * 0.46f, paint)
                val whiteP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFFFFFFFF.toInt()
                    style = Paint.Style.STROKE
                    strokeWidth = s * 0.05f
                }
                canvas.drawCircle(c, c, s * 0.28f, whiteP)
                canvas.drawLine(c - s * 0.28f, c, c + s * 0.28f, c, whiteP)
                canvas.drawOval(RectF(c - s * 0.14f, c - s * 0.28f, c + s * 0.14f, c + s * 0.28f), whiteP)
            }

            "camera" -> {
                canvas.drawCircle(c, c, s * 0.46f, paint)
                val whiteP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt(); style = Paint.Style.FILL }
                canvas.drawRoundRect(RectF(c - s * 0.26f, c - s * 0.14f, c + s * 0.26f, c + s * 0.22f), s * 0.06f, s * 0.06f, whiteP)
                canvas.drawRoundRect(RectF(c - s * 0.12f, c - s * 0.22f, c + s * 0.12f, c - s * 0.12f), s * 0.04f, s * 0.04f, whiteP)
                val lensP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = logo.brandColor; style = Paint.Style.FILL }
                canvas.drawCircle(c, c + s * 0.04f, s * 0.10f, lensP)
                canvas.drawCircle(c + s * 0.18f, c - s * 0.06f, s * 0.03f, lensP)
            }

            "coffee" -> {
                canvas.drawCircle(c, c, s * 0.46f, paint)
                val whiteP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt(); style = Paint.Style.FILL }
                // Cup body
                canvas.drawRoundRect(RectF(c - s * 0.20f, c - s * 0.06f, c + s * 0.12f, c + s * 0.24f), s * 0.06f, s * 0.06f, whiteP)
                // Handle
                val handleP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFFFFFFFF.toInt()
                    style = Paint.Style.STROKE
                    strokeWidth = s * 0.05f
                }
                canvas.drawArc(RectF(c + s * 0.06f, c - s * 0.02f, c + s * 0.24f, c + s * 0.18f), -90f, 180f, false, handleP)
                // Steam waves
                val steamP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFFFFFFFF.toInt()
                    style = Paint.Style.STROKE
                    strokeWidth = s * 0.04f
                    strokeCap = Paint.Cap.ROUND
                }
                canvas.drawLine(c - s * 0.12f, c - s * 0.12f, c - s * 0.12f, c - s * 0.24f, steamP)
                canvas.drawLine(c - s * 0.02f, c - s * 0.12f, c - s * 0.02f, c - s * 0.26f, steamP)
            }

            "restaurant" -> {
                canvas.drawCircle(c, c, s * 0.46f, paint)
                val whiteP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFFFFFFFF.toInt()
                    style = Paint.Style.STROKE
                    strokeWidth = s * 0.06f
                    strokeCap = Paint.Cap.ROUND
                }
                // Crossed fork and knife
                canvas.drawLine(c - s * 0.22f, c - s * 0.22f, c + s * 0.22f, c + s * 0.22f, whiteP)
                canvas.drawLine(c + s * 0.22f, c - s * 0.22f, c - s * 0.22f, c + s * 0.22f, whiteP)
            }

            "shopping" -> {
                canvas.drawCircle(c, c, s * 0.46f, paint)
                val whiteP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt(); style = Paint.Style.FILL }
                // Bag
                canvas.drawRoundRect(RectF(c - s * 0.22f, c - s * 0.10f, c + s * 0.22f, c + s * 0.26f), s * 0.06f, s * 0.06f, whiteP)
                // Handle
                val handleP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFFFFFFFF.toInt()
                    style = Paint.Style.STROKE
                    strokeWidth = s * 0.05f
                    strokeCap = Paint.Cap.ROUND
                }
                canvas.drawArc(RectF(c - s * 0.12f, c - s * 0.24f, c + s * 0.12f, c), 180f, 180f, false, handleP)
            }

            "music" -> {
                canvas.drawCircle(c, c, s * 0.46f, paint)
                val whiteP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt(); style = Paint.Style.FILL }
                canvas.drawCircle(c - s * 0.14f, c + s * 0.14f, s * 0.10f, whiteP)
                canvas.drawCircle(c + s * 0.14f, c + s * 0.06f, s * 0.10f, whiteP)
                canvas.drawRect(c - s * 0.07f, c - s * 0.22f, c - s * 0.01f, c + s * 0.14f, whiteP)
                canvas.drawRect(c + s * 0.21f, c - s * 0.30f, c + s * 0.27f, c + s * 0.06f, whiteP)
                canvas.drawRect(c - s * 0.07f, c - s * 0.30f, c + s * 0.27f, c - s * 0.18f, whiteP)
            }

            "paypal" -> {
                canvas.drawCircle(c, c, s * 0.46f, paint)
                val whiteP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFFFFFFFF.toInt()
                    style = Paint.Style.FILL
                    textSize = s * 0.50f
                    textAlign = Paint.Align.CENTER
                    isFakeBoldText = true
                }
                canvas.drawText("P", c - s * 0.06f, c + s * 0.16f, whiteP)
                val cyanP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFF0079C1.toInt()
                    style = Paint.Style.FILL
                    textSize = s * 0.50f
                    textAlign = Paint.Align.CENTER
                    isFakeBoldText = true
                }
                canvas.drawText("P", c + s * 0.08f, c + s * 0.24f, cyanP)
            }

            "bitcoin" -> {
                canvas.drawCircle(c, c, s * 0.46f, paint)
                val whiteP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFFFFFFFF.toInt()
                    style = Paint.Style.FILL
                    textSize = s * 0.54f
                    textAlign = Paint.Align.CENTER
                    isFakeBoldText = true
                }
                canvas.drawText("₿", c, c + s * 0.19f, whiteP)
            }

            "ethereum" -> {
                canvas.drawCircle(c, c, s * 0.46f, paint)
                val whiteP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt(); style = Paint.Style.FILL }
                val topGem = Path().apply {
                    moveTo(c, c - s * 0.28f)
                    lineTo(c + s * 0.18f, c)
                    lineTo(c, c + s * 0.10f)
                    lineTo(c - s * 0.18f, c)
                    close()
                }
                canvas.drawPath(topGem, whiteP)
                val botGem = Path().apply {
                    moveTo(c, c + s * 0.15f)
                    lineTo(c + s * 0.18f, c + s * 0.05f)
                    lineTo(c, c + s * 0.30f)
                    lineTo(c - s * 0.18f, c + s * 0.05f)
                    close()
                }
                canvas.drawPath(botGem, whiteP)
            }

            "heart" -> {
                canvas.drawCircle(c, c, s * 0.46f, paint)
                val whiteP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt(); style = Paint.Style.FILL }
                val heartPath = Path().apply {
                    val r = s * 0.15f
                    addCircle(c - s * 0.11f, c - s * 0.06f, r, Path.Direction.CW)
                    addCircle(c + s * 0.11f, c - s * 0.06f, r, Path.Direction.CW)
                    val tri = Path().apply {
                        moveTo(c - s * 0.24f, c - s * 0.02f)
                        lineTo(c + s * 0.24f, c - s * 0.02f)
                        lineTo(c, c + s * 0.28f)
                        close()
                    }
                    op(tri, Path.Op.UNION)
                }
                canvas.drawPath(heartPath, whiteP)
            }

            "star" -> {
                canvas.drawCircle(c, c, s * 0.46f, paint)
                val whiteP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt(); style = Paint.Style.FILL }
                val path = Path()
                val points = 5
                val outer = s * 0.32f
                val inner = s * 0.14f
                for (i in 0 until points * 2) {
                    val angle = (Math.PI / points * i - Math.PI / 2).toFloat()
                    val rad = if (i % 2 == 0) outer else inner
                    val x = c + cos(angle.toDouble()).toFloat() * rad
                    val y = c + sin(angle.toDouble()).toFloat() * rad
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                canvas.drawPath(path, whiteP)
            }

            else -> {
                // Sparkle / Beacon logo
                canvas.drawCircle(c, c, s * 0.46f, paint)
                val darkP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF0C0C0B.toInt(); style = Paint.Style.FILL }
                val path = Path().apply {
                    moveTo(c, c - s * 0.32f)
                    quadTo(c + s * 0.04f, c - s * 0.04f, c + s * 0.32f, c)
                    quadTo(c + s * 0.04f, c + s * 0.04f, c, c + s * 0.32f)
                    quadTo(c - s * 0.04f, c + s * 0.04f, c - s * 0.32f, c)
                    quadTo(c - s * 0.04f, c - s * 0.04f, c, c - s * 0.32f)
                    close()
                }
                canvas.drawPath(path, darkP)
            }
        }

        logoCache[cacheKey] = bitmap
        return bitmap
    }
}
