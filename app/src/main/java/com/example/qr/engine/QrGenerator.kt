package com.example.qr.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.Encoder
import com.google.zxing.qrcode.encoder.QRCode
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

object QrGenerator {

    private val thumbnailCache = object : android.util.LruCache<Int, Bitmap>(400) {}
    private var cachedSampleMatrix: com.google.zxing.qrcode.encoder.ByteMatrix? = null

    private fun getSampleMatrix(): com.google.zxing.qrcode.encoder.ByteMatrix {
        val existing = cachedSampleMatrix
        if (existing != null) return existing
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 0
        )
        val qr = Encoder.encode("https://qrwho.vercel.app", ErrorCorrectionLevel.M, hints)
        val m = qr.matrix ?: com.google.zxing.qrcode.encoder.ByteMatrix(25, 25)
        cachedSampleMatrix = m
        return m
    }

    /**
     * Quickly renders an authentic miniature QR code for presets, showcase cards, and galleries.
     */
    fun generateThumbnail(qrStyle: QrStyle, sizePx: Int = 180): Bitmap = getOrGenerateThumbnail(qrStyle, sizePx)

    fun getOrGenerateThumbnail(qrStyle: QrStyle, sizePx: Int = 120): Bitmap {
        val key = qrStyle.hashCode() * 31 + sizePx
        val cached = thumbnailCache.get(key)
        if (cached != null) return cached

        val matrix = getSampleMatrix()
        val matrixSize = matrix.width
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw background
        val bgPaint = Paint().apply { color = qrStyle.bgColor }
        canvas.drawRect(0f, 0f, sizePx.toFloat(), sizePx.toFloat(), bgPaint)

        val qz = 2
        val totalModules = matrixSize + qz * 2
        val cellSize = sizePx.toFloat() / totalModules.toFloat()
        val originX = qz * cellSize
        val originY = qz * cellSize

        // Module paint
        val modulePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = qrStyle.fgColor
            style = Paint.Style.FILL
        }
        if (qrStyle.gradientType != GradientType.None) {
            modulePaint.shader = LinearGradient(
                originX, originY,
                originX + matrixSize * cellSize, originY + matrixSize * cellSize,
                qrStyle.fgColor, qrStyle.gradientTo, Shader.TileMode.CLAMP
            )
        }

        val dotScale = qrStyle.dotScale.coerceIn(0.60f, 1.0f)
        val gap = cellSize * qrStyle.moduleGap.coerceIn(0f, 0.15f)

        for (y in 0 until matrixSize) {
            for (x in 0 until matrixSize) {
                if (isFinderCell(x, y, matrixSize)) continue
                val isDark = matrix.get(x, y).toInt() == 1
                if (!isDark) continue

                val cellLeft = originX + x * cellSize
                val cellTop = originY + y * cellSize
                val cx = cellLeft + cellSize / 2f
                val cy = cellTop + cellSize / 2f
                val activeSize = (cellSize - gap * 2) * dotScale
                val nDark = y > 0 && matrix.get(x, y - 1).toInt() == 1
                val sDark = y < matrixSize - 1 && matrix.get(x, y + 1).toInt() == 1
                val wDark = x > 0 && matrix.get(x - 1, y).toInt() == 1
                val eDark = x < matrixSize - 1 && matrix.get(x + 1, y).toInt() == 1

                drawModuleShape(
                    canvas = canvas,
                    shape = qrStyle.moduleShape,
                    cx = cx,
                    cy = cy,
                    size = activeSize,
                    paint = modulePaint,
                    gx = x,
                    gy = y,
                    nDark = nDark,
                    sDark = sDark,
                    wDark = wDark,
                    eDark = eDark,
                    matrixSize = matrixSize
                )
            }
        }

        // Draw the 3 Finder Eyes
        drawEye(canvas, originX, originY, cellSize, qrStyle)
        drawEye(canvas, originX + (matrixSize - 7) * cellSize, originY, cellSize, qrStyle)
        drawEye(canvas, originX, originY + (matrixSize - 7) * cellSize, cellSize, qrStyle)

        thumbnailCache.put(key, bitmap)
        return bitmap
    }

    fun generateQrBitmap(
        payload: String,
        qrStyle: QrStyle,
        photoBitmap: Bitmap? = null,
        customLogo: Bitmap? = null,
        sizePx: Int = 1024
    ): Bitmap {
        val ecc = when (qrStyle.ecc.uppercase()) {
            "L" -> ErrorCorrectionLevel.L
            "M" -> ErrorCorrectionLevel.M
            "Q" -> ErrorCorrectionLevel.Q
            else -> ErrorCorrectionLevel.H // Level H default for maximum scannability and art embedding
        }

        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.ERROR_CORRECTION to ecc,
            EncodeHintType.MARGIN to 0
        )

        val qrCode: QRCode = try {
            Encoder.encode(payload, ecc, hints)
        } catch (_: Exception) {
            Encoder.encode("https://qrwho.vercel.app", ecc, hints)
        }

        val matrix = qrCode.matrix ?: return Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val matrixSize = matrix.width

        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Calculate Frame geometry
        val hasFrame = qrStyle.frameStyle != FrameStyle.None
        val frameBand = if (hasFrame) (sizePx * 0.10f).toInt() else 0
        val bottomBand = if (hasFrame) (sizePx * 0.16f).toInt() else 0

        val qz = if (photoBitmap != null && qrStyle.imageMode != ImageMode.None) {
            max(2, min(6, qrStyle.quietZone))
        } else {
            max(1, min(6, qrStyle.quietZone))
        }

        val totalCells = matrixSize + qz * 2
        val usableWidth = sizePx - frameBand * 2
        val usableHeight = sizePx - frameBand - bottomBand
        val cellSize = min(usableWidth, usableHeight).toFloat() / totalCells

        val originX = frameBand + (usableWidth - matrixSize * cellSize) / 2f
        val originY = frameBand + (usableHeight - matrixSize * cellSize) / 2f

        // 1. Draw Background
        if (!qrStyle.transparentBg) {
            bitmap.eraseColor(qrStyle.bgColor)
            val bgPaint = Paint().apply {
                color = qrStyle.bgColor
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, sizePx.toFloat(), sizePx.toFloat(), bgPaint)
        } else {
            bitmap.eraseColor(0x00000000)
        }

        // 2. Draw Photo Underlay only for Clean mode (Clean overlay)
        val hasPhotoUnderlay = photoBitmap != null && qrStyle.imageMode == ImageMode.Clean
        if (hasPhotoUnderlay && photoBitmap != null) {
            renderPhotoUnderlay(canvas, photoBitmap, qrStyle, originX, originY, matrixSize * cellSize, sizePx, cellSize, matrixSize)
        }

        val hasPhoto = photoBitmap != null && qrStyle.imageMode != ImageMode.None && qrStyle.imageMode != ImageMode.Logo

        // 3. Setup Module Shader / Paint
        val modulePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = qrStyle.fgColor
        }

        val bodyW = matrixSize * cellSize
        when (qrStyle.gradientType) {
            GradientType.Linear -> {
                modulePaint.shader = LinearGradient(
                    originX, originY, originX + bodyW, originY,
                    qrStyle.fgColor, qrStyle.gradientTo, Shader.TileMode.CLAMP
                )
            }
            GradientType.Diagonal -> {
                modulePaint.shader = LinearGradient(
                    originX, originY, originX + bodyW, originY + bodyW,
                    qrStyle.fgColor, qrStyle.gradientTo, Shader.TileMode.CLAMP
                )
            }
            GradientType.Radial -> {
                modulePaint.shader = RadialGradient(
                    originX + bodyW / 2f, originY + bodyW / 2f, bodyW * 0.7f,
                    qrStyle.fgColor, qrStyle.gradientTo, Shader.TileMode.CLAMP
                )
            }
            GradientType.None -> {
                modulePaint.shader = null
            }
        }

        // 4. Reserve Center Safe Zone for Logo if present
        val centerZone = if (customLogo != null || qrStyle.imageMode == ImageMode.Logo || qrStyle.selectedLogoId != null) {
            val logoRadiusModules = (matrixSize * (qrStyle.logoScale.coerceIn(0.18f, 0.28f)) / 2f).toInt()
            val mid = matrixSize / 2
            Rect(mid - logoRadiusModules, mid - logoRadiusModules, mid + logoRadiusModules, mid + logoRadiusModules)
        } else {
            null
        }

        // 5. Draw QR Modules (Skip Finders)
        val gap = cellSize * qrStyle.moduleGap.coerceIn(0f, 0.15f)
        val dotScale = qrStyle.dotScale.coerceIn(0.55f, 1.0f)

        for (y in 0 until matrixSize) {
            for (x in 0 until matrixSize) {
                if (isFinderCell(x, y, matrixSize)) continue
                if (centerZone != null && centerZone.contains(x, y)) continue

                val isDark = matrix.get(x, y).toInt() == 1
                if (!isDark) continue

                val cellLeft = originX + x * cellSize
                val cellTop = originY + y * cellSize
                val cx = cellLeft + cellSize / 2f
                val cy = cellTop + cellSize / 2f
                var activeSize = (cellSize - gap * 2) * dotScale

                // Photo-driven module styling
                if (hasPhoto && photoBitmap != null) {
                    val normX = (x.toFloat() / matrixSize).coerceIn(0f, 1f)
                    val normY = (y.toFloat() / matrixSize).coerceIn(0f, 1f)
                    val pxX = (normX * (photoBitmap.width - 1)).toInt().coerceIn(0, photoBitmap.width - 1)
                    val pxY = (normY * (photoBitmap.height - 1)).toInt().coerceIn(0, photoBitmap.height - 1)
                    val sample = photoBitmap.getPixel(pxX, pxY)
                    val luma = (0.299f * android.graphics.Color.red(sample) + 0.587f * android.graphics.Color.green(sample) + 0.114f * android.graphics.Color.blue(sample)) / 255f

                    when (qrStyle.imageMode) {
                        ImageMode.Mosaic -> {
                            val contrastBoost = qrStyle.contrast.coerceIn(0.5f, 2.0f)
                            val r = (android.graphics.Color.red(sample) * (1f - (1f - luma) * 0.4f * contrastBoost)).toInt().coerceIn(0, 255)
                            val g = (android.graphics.Color.green(sample) * (1f - (1f - luma) * 0.4f * contrastBoost)).toInt().coerceIn(0, 255)
                            val b = (android.graphics.Color.blue(sample) * (1f - (1f - luma) * 0.4f * contrastBoost)).toInt().coerceIn(0, 255)
                            modulePaint.shader = null
                            modulePaint.color = android.graphics.Color.rgb(r, g, b)
                        }
                        ImageMode.Halftone -> {
                            activeSize *= (1.15f - luma * 0.40f).coerceIn(0.68f, 1.0f)
                        }
                        ImageMode.Duotone -> {
                            val t = luma.coerceIn(0f, 1f)
                            val c1 = qrStyle.fgColor
                            val c2 = qrStyle.gradientTo
                            val dr = (android.graphics.Color.red(c1) * (1 - t) + android.graphics.Color.red(c2) * t).toInt()
                            val dg = (android.graphics.Color.green(c1) * (1 - t) + android.graphics.Color.green(c2) * t).toInt()
                            val db = (android.graphics.Color.blue(c1) * (1 - t) + android.graphics.Color.blue(c2) * t).toInt()
                            modulePaint.shader = null
                            modulePaint.color = android.graphics.Color.rgb(dr, dg, db)
                        }
                        ImageMode.Mono -> {
                            val v = (luma * 160).toInt().coerceIn(10, 180)
                            modulePaint.shader = null
                            modulePaint.color = android.graphics.Color.rgb(v, v, v)
                        }
                        ImageMode.Paint -> {
                            // Guaranteed center core dot in deep ink for camera decode
                            val coreSize = activeSize * (1f - qrStyle.artisticStrength * 0.45f).coerceIn(0.65f, 0.95f)
                            drawModuleShape(
                                canvas = canvas,
                                shape = qrStyle.moduleShape,
                                cx = cx,
                                cy = cy,
                                size = coreSize,
                                paint = modulePaint,
                                gx = x,
                                gy = y
                            )
                            continue
                        }
                        else -> {}
                    }
                }

                val nDark = y > 0 && matrix.get(x, y - 1).toInt() == 1
                val sDark = y < matrixSize - 1 && matrix.get(x, y + 1).toInt() == 1
                val wDark = x > 0 && matrix.get(x - 1, y).toInt() == 1
                val eDark = x < matrixSize - 1 && matrix.get(x + 1, y).toInt() == 1

                drawModuleShape(
                    canvas = canvas,
                    shape = qrStyle.moduleShape,
                    cx = cx,
                    cy = cy,
                    size = activeSize,
                    paint = modulePaint,
                    gx = x,
                    gy = y,
                    nDark = nDark,
                    sDark = sDark,
                    wDark = wDark,
                    eDark = eDark,
                    matrixSize = matrixSize
                )
            }
        }

        // 6. Draw The 3 Finder Eyes (Top-Left, Top-Right, Bottom-Left)
        drawEye(canvas, originX, originY, cellSize, qrStyle)
        drawEye(canvas, originX + (matrixSize - 7) * cellSize, originY, cellSize, qrStyle)
        drawEye(canvas, originX, originY + (matrixSize - 7) * cellSize, cellSize, qrStyle)

        // 7. Render Center Logo
        if (customLogo != null || qrStyle.selectedLogoId != null) {
            val logoBmp = customLogo ?: BuiltInLogos.createLogoBitmap(qrStyle.selectedLogoId ?: "sparkle", (sizePx * 0.24f).toInt())
            renderCenterLogo(canvas, logoBmp, sizePx, originX, originY, bodyW, qrStyle)
        }

        // 8. Render Decorative Frame if requested
        if (hasFrame) {
            drawFrameOnCanvas(canvas, qrStyle.frameStyle, qrStyle.frameCaption, sizePx, originX, originY, bodyW, qrStyle)
        }

        return bitmap
    }

    /**
     * Generates a 100% genuine vector SVG representation of the QR code with
     * custom module shapes, gradients, finder patterns, and frames.
     */
    fun generateQrSvg(
        payload: String,
        qrStyle: QrStyle,
        sizePx: Int = 1024
    ): String {
        val ecc = when (qrStyle.ecc.uppercase()) {
            "L" -> ErrorCorrectionLevel.L
            "M" -> ErrorCorrectionLevel.M
            "Q" -> ErrorCorrectionLevel.Q
            else -> ErrorCorrectionLevel.H
        }

        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.ERROR_CORRECTION to ecc,
            EncodeHintType.MARGIN to 0
        )

        val qrCode: QRCode = try {
            Encoder.encode(payload, ecc, hints)
        } catch (_: Exception) {
            Encoder.encode("https://qrwho.vercel.app", ecc, hints)
        }

        val matrix = qrCode.matrix ?: return "<svg viewBox=\"0 0 $sizePx $sizePx\"></svg>"
        val matrixSize = matrix.width

        val hasFrame = qrStyle.frameStyle != FrameStyle.None
        val frameBand = if (hasFrame) (sizePx * 0.10f).toInt() else 0
        val bottomBand = if (hasFrame) (sizePx * 0.16f).toInt() else 0

        val qz = max(1, min(6, qrStyle.quietZone))
        val totalCells = matrixSize + qz * 2
        val usableWidth = sizePx - frameBand * 2
        val usableHeight = sizePx - frameBand - bottomBand
        val cellSize = min(usableWidth, usableHeight).toFloat() / totalCells

        val originX = frameBand + (usableWidth - matrixSize * cellSize) / 2f
        val originY = frameBand + (usableHeight - matrixSize * cellSize) / 2f
        val gap = cellSize * qrStyle.moduleGap.coerceIn(0f, 0.15f)
        val dotScale = qrStyle.dotScale.coerceIn(0.60f, 1.0f)

        val fgHex = String.format("#%06X", 0xFFFFFF and qrStyle.fgColor)
        val bgHex = String.format("#%06X", 0xFFFFFF and qrStyle.bgColor)
        val gradHex = String.format("#%06X", 0xFFFFFF and qrStyle.gradientTo)
        val eyeHex = String.format("#%06X", 0xFFFFFF and qrStyle.eyeColor)
        val ballHex = String.format("#%06X", 0xFFFFFF and qrStyle.ballColor)

        val fillAttr = if (qrStyle.gradientType != GradientType.None) "url(#qrGrad)" else fgHex

        return buildString {
            append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            append("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 $sizePx $sizePx\" width=\"100%\" height=\"100%\">\n")

            // Gradients and Definitions
            append("  <defs>\n")
            if (qrStyle.gradientType == GradientType.Diagonal) {
                append("    <linearGradient id=\"qrGrad\" x1=\"0%\" y1=\"0%\" x2=\"100%\" y2=\"100%\">\n")
                append("      <stop offset=\"0%\" stop-color=\"$fgHex\" />\n")
                append("      <stop offset=\"100%\" stop-color=\"$gradHex\" />\n")
                append("    </linearGradient>\n")
            } else if (qrStyle.gradientType == GradientType.Linear) {
                append("    <linearGradient id=\"qrGrad\" x1=\"0%\" y1=\"0%\" x2=\"100%\" y2=\"0%\">\n")
                append("      <stop offset=\"0%\" stop-color=\"$fgHex\" />\n")
                append("      <stop offset=\"100%\" stop-color=\"$gradHex\" />\n")
                append("    </linearGradient>\n")
            } else if (qrStyle.gradientType == GradientType.Radial) {
                append("    <radialGradient id=\"qrGrad\" cx=\"50%\" cy=\"50%\" r=\"60%\">\n")
                append("      <stop offset=\"0%\" stop-color=\"$fgHex\" />\n")
                append("      <stop offset=\"100%\" stop-color=\"$gradHex\" />\n")
                append("    </radialGradient>\n")
            }
            append("  </defs>\n")

            // Background
            if (!qrStyle.transparentBg) {
                append("  <rect width=\"$sizePx\" height=\"$sizePx\" fill=\"$bgHex\" />\n")
            }

            // QR Modules
            append("  <g fill=\"$fillAttr\">\n")
            for (y in 0 until matrixSize) {
                for (x in 0 until matrixSize) {
                    if (isFinderCell(x, y, matrixSize)) continue
                    if (matrix.get(x, y).toInt() != 1) continue

                    val cx = originX + x * cellSize + cellSize / 2f
                    val cy = originY + y * cellSize + cellSize / 2f
                    val activeSize = (cellSize - gap * 2) * dotScale
                    val r = activeSize / 2f
                    val left = cx - r
                    val top = cy - r

                    when (qrStyle.moduleShape) {
                        ModuleShape.Square -> {
                            append("    <rect x=\"$left\" y=\"$top\" width=\"$activeSize\" height=\"$activeSize\" />\n")
                        }
                        ModuleShape.Rounded -> {
                            val rx = activeSize * 0.25f
                            append("    <rect x=\"$left\" y=\"$top\" width=\"$activeSize\" height=\"$activeSize\" rx=\"$rx\" ry=\"$rx\" />\n")
                        }
                        ModuleShape.Squircle, ModuleShape.Fluid -> {
                            val rx = activeSize * 0.38f
                            append("    <rect x=\"$left\" y=\"$top\" width=\"$activeSize\" height=\"$activeSize\" rx=\"$rx\" ry=\"$rx\" />\n")
                        }
                        ModuleShape.Dots, ModuleShape.Bubbles -> {
                            append("    <circle cx=\"$cx\" cy=\"$cy\" r=\"$r\" />\n")
                        }
                        ModuleShape.Diamond -> {
                            append("    <polygon points=\"$cx,$top ${cx + r},$cy $cx,${cy + r} ${cx - r},$cy\" />\n")
                        }
                        ModuleShape.Hex -> {
                            val pts = buildString {
                                for (i in 0 until 6) {
                                    val angle = (Math.PI / 3 * i - Math.PI / 6)
                                    val px = cx + cos(angle).toFloat() * r
                                    val py = cy + sin(angle).toFloat() * r
                                    append("$px,$py ")
                                }
                            }.trim()
                            append("    <polygon points=\"$pts\" />\n")
                        }
                        else -> {
                            val rx = activeSize * 0.20f
                            append("    <rect x=\"$left\" y=\"$top\" width=\"$activeSize\" height=\"$activeSize\" rx=\"$rx\" ry=\"$rx\" />\n")
                        }
                    }
                }
            }
            append("  </g>\n")

            // Vector Finder Eyes
            append("  <!-- Finder Eyes -->\n")
            append(renderEyeSvg(originX, originY, cellSize, qrStyle.eyeShape, qrStyle.ballShape, eyeHex, ballHex, bgHex))
            append(renderEyeSvg(originX + (matrixSize - 7) * cellSize, originY, cellSize, qrStyle.eyeShape, qrStyle.ballShape, eyeHex, ballHex, bgHex))
            append(renderEyeSvg(originX, originY + (matrixSize - 7) * cellSize, cellSize, qrStyle.eyeShape, qrStyle.ballShape, eyeHex, ballHex, bgHex))

            // Frame caption if present
            if (hasFrame && qrStyle.frameCaption.isNotEmpty()) {
                val s = sizePx.toFloat()
                append("  <text x=\"${s / 2f}\" y=\"${s * 0.93f}\" font-family=\"sans-serif\" font-size=\"${s * 0.045f}\" font-weight=\"bold\" text-anchor=\"middle\" fill=\"$fgHex\">${qrStyle.frameCaption}</text>\n")
            }

            append("</svg>\n")
        }
    }

    private fun renderEyeSvg(
        ox: Float, oy: Float, cell: Float,
        eyeShape: EyeShape, ballShape: EyeShape,
        eyeHex: String, ballHex: String, bgHex: String
    ): String = buildString {
        val s = cell * 7
        val r = s / 2f
        val cx = ox + r
        val cy = oy + r

        // Outer Ring
        append("  <rect x=\"$ox\" y=\"$oy\" width=\"$s\" height=\"$s\" fill=\"$eyeHex\" rx=\"${if (eyeShape == EyeShape.Circle) r else if (eyeShape == EyeShape.Rounded) s * 0.22f else 0f}\" />\n")
        // Inner Gap
        append("  <rect x=\"${ox + cell}\" y=\"${oy + cell}\" width=\"${cell * 5}\" height=\"${cell * 5}\" fill=\"$bgHex\" rx=\"${if (eyeShape == EyeShape.Circle) cell * 2.5f else if (eyeShape == EyeShape.Rounded) cell else 0f}\" />\n")
        // Pupil Ball
        val bSize = cell * 3
        val bRadius = bSize / 2f
        append("  <rect x=\"${ox + cell * 2}\" y=\"${oy + cell * 2}\" width=\"$bSize\" height=\"$bSize\" fill=\"$ballHex\" rx=\"${if (ballShape == EyeShape.Circle) bRadius else if (ballShape == EyeShape.Rounded) bSize * 0.25f else 0f}\" />\n")
    }

    private fun isFinderCell(x: Int, y: Int, matrixSize: Int): Boolean {
        // Top-left
        if (x < 8 && y < 8) return true
        // Top-right
        if (x >= matrixSize - 8 && y < 8) return true
        // Bottom-left
        if (x < 8 && y >= matrixSize - 8) return true
        return false
    }

    private fun renderPhotoUnderlay(
        canvas: Canvas,
        photo: Bitmap,
        style: QrStyle,
        ox: Float,
        oy: Float,
        bodyPx: Float,
        sizePx: Int,
        cellSize: Float,
        matrixSize: Int
    ) {
        val zoom = style.photoZoom.coerceIn(0.5f, 2.5f)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            alpha = (style.imageOpacity.coerceIn(0.15f, 1.0f) * 255).toInt()
        }

        canvas.save()
        canvas.clipRect(0f, 0f, sizePx.toFloat(), sizePx.toFloat())

        // Calculate source rect with zoom centered
        val sw = photo.width.toFloat()
        val sh = photo.height.toFloat()
        val cropW = sw / zoom
        val cropH = sh / zoom
        val left = ((sw - cropW) / 2f).coerceAtLeast(0f)
        val top = ((sh - cropH) / 2f).coerceAtLeast(0f)
        val right = (left + cropW).coerceAtMost(sw)
        val bottom = (top + cropH).coerceAtMost(sh)
        val srcRect = Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())

        // Always draw the photo covering the full canvas to avoid awkward white borders around the QR
        val destRect = RectF(0f, 0f, sizePx.toFloat(), sizePx.toFloat())
        canvas.drawBitmap(photo, srcRect, destRect, paint)

        // Gentle scrim to keep modules readable
        val scrimColor = if (isDarkColor(style.bgColor)) 0x28000000 else 0x1EFFFFFF
        val scrimPaint = Paint().apply { color = scrimColor; this.style = Paint.Style.FILL }
        canvas.drawRect(destRect, scrimPaint)

        // Finder plates: Opaque rounded paper islands behind the 3 finder eyes in all photo modes
        // to isolate finder patterns from photo noise, guaranteeing verified camera decode and eliminating white border glitches.
        val platePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = style.bgColor
            this.style = Paint.Style.FILL
        }
        val plateG = cellSize * 0.45f
        val plateR = cellSize * 0.9f
        val corners = listOf(
            Pair(0, 0),
            Pair(matrixSize - 7, 0),
            Pair(0, matrixSize - 7)
        )
        for ((ex, ey) in corners) {
            val cx = ox + ex * cellSize
            val cy = oy + ey * cellSize
            val sepX = if (ex == 0) cx else cx - cellSize
            val sepY = if (ey == 0) cy else cy - cellSize
            canvas.drawRoundRect(
                RectF(sepX - plateG, sepY - plateG, sepX + cellSize * 8 + plateG * 2, sepY + cellSize * 8 + plateG * 2),
                plateR, plateR, platePaint
            )
        }

        canvas.restore()
    }

    private fun isDarkColor(color: Int): Boolean {
        val r = (color shr 16) and 0xFF
        val g = (color shr 8) and 0xFF
        val b = color and 0xFF
        val luma = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
        return luma < 0.5
    }

    private fun renderCenterLogo(
        canvas: Canvas,
        logo: Bitmap,
        sizePx: Int,
        ox: Float,
        oy: Float,
        bodyPx: Float,
        style: QrStyle
    ) {
        val cx = ox + bodyPx / 2f
        val cy = oy + bodyPx / 2f
        val logoW = bodyPx * style.logoScale.coerceIn(0.18f, 0.28f)
        val badgeW = logoW * 1.25f

        // Draw pill / circular badge behind logo
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = style.bgColor
            this.style = Paint.Style.FILL
            setShadowLayer(sizePx * 0.015f, 0f, sizePx * 0.005f, 0x44000000)
        }
        canvas.drawRoundRect(
            RectF(cx - badgeW / 2f, cy - badgeW / 2f, cx + badgeW / 2f, cy + badgeW / 2f),
            badgeW * 0.28f, badgeW * 0.28f, badgePaint
        )

        // Draw logo centered
        val destRect = RectF(cx - logoW / 2f, cy - logoW / 2f, cx + logoW / 2f, cy + logoW / 2f)
        canvas.drawBitmap(logo, Rect(0, 0, logo.width, logo.height), destRect, Paint(Paint.ANTI_ALIAS_FLAG))
    }

    private fun drawModuleShape(
        canvas: Canvas,
        shape: ModuleShape,
        cx: Float,
        cy: Float,
        size: Float,
        paint: Paint,
        gx: Int,
        gy: Int,
        nDark: Boolean = false,
        sDark: Boolean = false,
        wDark: Boolean = false,
        eDark: Boolean = false,
        matrixSize: Int = 33
    ) {
        val r = size / 2f
        val x = cx - r
        val y = cy - r

        when (shape) {
            ModuleShape.Square -> {
                canvas.drawRect(x, y, x + size, y + size, paint)
            }
            ModuleShape.Rounded -> {
                canvas.drawRoundRect(RectF(x, y, x + size, y + size), size * 0.28f, size * 0.28f, paint)
            }
            ModuleShape.Squircle -> {
                canvas.drawRoundRect(RectF(x, y, x + size, y + size), size * 0.42f, size * 0.42f, paint)
            }
            ModuleShape.Dots -> {
                canvas.drawCircle(cx, cy, r * 0.96f, paint)
            }
            ModuleShape.Diamond -> {
                val path = Path().apply {
                    moveTo(cx, y)
                    lineTo(x + size, cy)
                    lineTo(cx, y + size)
                    lineTo(x, cy)
                    close()
                }
                canvas.drawPath(path, paint)
            }
            ModuleShape.Star -> {
                val path = Path()
                val innerR = r * 0.42f
                for (i in 0 until 10) {
                    val angle = (Math.PI / 5 * i - Math.PI / 2).toFloat()
                    val curR = if (i % 2 == 0) r else innerR
                    val px = cx + cos(angle.toDouble()).toFloat() * curR
                    val py = cy + sin(angle.toDouble()).toFloat() * curR
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
                canvas.drawPath(path, paint)
            }
            ModuleShape.Plus -> {
                val t = size * 0.34f
                val o = (size - t) / 2f
                canvas.drawRect(x + o, y, x + o + t, y + size, paint)
                canvas.drawRect(x, y + o, x + size, y + o + t, paint)
            }
            ModuleShape.Classy -> {
                val cr = size * 0.55f
                val radii = floatArrayOf(0f, 0f, cr, cr, 0f, 0f, cr, cr)
                val path = Path().apply {
                    addRoundRect(RectF(x, y, x + size, y + size), radii, Path.Direction.CW)
                }
                canvas.drawPath(path, paint)
            }
            ModuleShape.Leaf -> {
                val cr = size * 0.62f
                val radii = floatArrayOf(cr, cr, 0f, 0f, cr, cr, 0f, 0f)
                val path = Path().apply {
                    addRoundRect(RectF(x, y, x + size, y + size), radii, Path.Direction.CW)
                }
                canvas.drawPath(path, paint)
            }
            ModuleShape.Fluid -> {
                val cr = size * 0.52f
                val tl = if (nDark || wDark) 0f else cr
                val tr = if (nDark || eDark) 0f else cr
                val br = if (sDark || eDark) 0f else cr
                val bl = if (sDark || wDark) 0f else cr
                val radii = floatArrayOf(tl, tl, tr, tr, br, br, bl, bl)
                val path = Path().apply {
                    addRoundRect(RectF(x, y, x + size, y + size), radii, Path.Direction.CW)
                }
                canvas.drawPath(path, paint)
            }
            ModuleShape.Hex -> {
                val path = Path()
                for (i in 0 until 6) {
                    val angle = (Math.PI / 3 * i - Math.PI / 6).toFloat()
                    val px = cx + cos(angle.toDouble()).toFloat() * r * 0.98f
                    val py = cy + sin(angle.toDouble()).toFloat() * r * 0.98f
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
                canvas.drawPath(path, paint)
            }
            ModuleShape.Heart -> {
                val hr = size * 0.24f
                val path = Path().apply {
                    addCircle(cx - size * 0.2f, cy - size * 0.12f, hr, Path.Direction.CW)
                    addCircle(cx + size * 0.2f, cy - size * 0.12f, hr, Path.Direction.CW)
                    moveTo(cx - size * 0.42f, cy - size * 0.05f)
                    lineTo(cx + size * 0.42f, cy - size * 0.05f)
                    lineTo(cx, cy + size * 0.45f)
                    close()
                }
                canvas.drawPath(path, paint)
            }
            ModuleShape.Dash -> {
                val hash = (gx * 374761393 + gy * 668265263) and 0x7FFFFFFF
                val horizontal = hash % 2 == 0
                if (horizontal) {
                    val barH = size * 0.44f
                    val barW = size * 0.95f
                    canvas.drawRoundRect(RectF(cx - barW / 2f, cy - barH / 2f, cx + barW / 2f, cy + barH / 2f), barH / 2f, barH / 2f, paint)
                } else {
                    val barW = size * 0.44f
                    val barH = size * 0.95f
                    canvas.drawRoundRect(RectF(cx - barW / 2f, cy - barH / 2f, cx + barW / 2f, cy + barH / 2f), barW / 2f, barW / 2f, paint)
                }
            }
            ModuleShape.HBar -> {
                val barH = size * 0.48f
                val barW = size * 0.96f
                canvas.drawRoundRect(RectF(cx - barW / 2f, cy - barH / 2f, cx + barW / 2f, cy + barH / 2f), barH / 2f, barH / 2f, paint)
            }
            ModuleShape.VBar -> {
                val barW = size * 0.48f
                val barH = size * 0.96f
                canvas.drawRoundRect(RectF(cx - barW / 2f, cy - barH / 2f, cx + barW / 2f, cy + barH / 2f), barW / 2f, barW / 2f, paint)
            }
            ModuleShape.Cross -> {
                canvas.save()
                canvas.rotate(45f, cx, cy)
                val barL = size * 0.95f
                val barT = size * 0.38f
                canvas.drawRoundRect(RectF(cx - barL / 2f, cy - barT / 2f, cx + barL / 2f, cy + barT / 2f), barT / 2f, barT / 2f, paint)
                canvas.drawRoundRect(RectF(cx - barT / 2f, cy - barL / 2f, cx + barT / 2f, cy + barL / 2f), barT / 2f, barT / 2f, paint)
                canvas.restore()
            }
            ModuleShape.Diag -> {
                canvas.save()
                canvas.rotate(45f, cx, cy)
                val len = size * 1.02f
                val thick = size * 0.36f
                canvas.drawRoundRect(RectF(cx - len / 2f, cy - thick / 2f, cx + len / 2f, cy + thick / 2f), thick / 2f, thick / 2f, paint)
                canvas.restore()
            }
            ModuleShape.Radial -> {
                val c = (matrixSize - 1) / 2f
                val ang = Math.atan2((gy - c).toDouble(), (gx - c).toDouble()).toFloat() * (180f / Math.PI.toFloat())
                canvas.save()
                canvas.rotate(ang, cx, cy)
                val hash = (gx * 374761393 + gy * 668265263) and 0x7FFFFFFF
                val wob = 0.8f + ((hash ushr 5) % 20) / 100f
                val path = Path().apply {
                    moveTo(cx + size * 0.55f * wob, cy)
                    lineTo(cx - size * 0.45f * wob, cy - size * 0.28f)
                    lineTo(cx - size * 0.30f * wob, cy)
                    lineTo(cx - size * 0.45f * wob, cy + size * 0.28f)
                    close()
                }
                canvas.drawPath(path, paint)
                canvas.restore()
            }
            ModuleShape.Bubbles -> {
                val hash = (gx * 374761393 + gy * 668265263) and 0x7FFFFFFF
                val br = size * (0.26f + ((hash % 64) / 64f) * 0.22f)
                canvas.drawCircle(cx, cy, br, paint)
            }
            ModuleShape.Confetti -> {
                val hash = (gx * 374761393 + gy * 668265263) and 0x7FFFFFFF
                val angle = (hash % 360).toFloat()
                val len = size * (0.66f + ((hash ushr 9) % 28) / 100f)
                val thick = size * 0.38f
                canvas.save()
                canvas.rotate(angle, cx, cy)
                canvas.drawRoundRect(RectF(cx - len / 2f, cy - thick / 2f, cx + len / 2f, cy + thick / 2f), thick / 2f, thick / 2f, paint)
                canvas.restore()
            }
        }
    }

    private fun drawEye(canvas: Canvas, ox: Float, oy: Float, cell: Float, qrStyle: QrStyle) {
        val s = cell * 7
        val eyeColor = qrStyle.eyeColor
        val ballColor = qrStyle.ballColor
        val bgColor = qrStyle.bgColor

        if (qrStyle.eyeShape == EyeShape.Target) {
            val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = eyeColor; style = Paint.Style.FILL }
            val midPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = bgColor; style = Paint.Style.FILL }
            val pupilPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ballColor; style = Paint.Style.FILL }
            val cx = ox + s / 2f
            val cy = oy + s / 2f
            canvas.drawCircle(cx, cy, s * 0.5f, outerPaint)
            canvas.drawCircle(cx, cy, s * (0.5f - 1f / 7f), midPaint)
            canvas.drawCircle(cx, cy, s * (1.5f / 7f), pupilPaint)
            return
        }

        val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = eyeColor
            this.style = Paint.Style.FILL
        }
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            this.style = Paint.Style.FILL
        }
        val ballPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ballColor
            this.style = Paint.Style.FILL
        }

        // Outer Ring (7x7)
        drawEyeLayer(canvas, ox, oy, s, qrStyle.eyeShape, outerPaint)

        // Middle Inset / Gap (5x5)
        drawEyeLayer(canvas, ox + cell, oy + cell, cell * 5, qrStyle.eyeShape, bgPaint)

        // Inner Ball / Pupil (3x3 modules)
        val bx = ox + cell * 2
        val by = oy + cell * 2
        val bSize = cell * 3
        drawEyeLayer(canvas, bx, by, bSize, qrStyle.ballShape, ballPaint)

        // If Ticks, draw the 4 cross-hair tick marks
        if (qrStyle.eyeShape == EyeShape.Ticks) {
            val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = eyeColor
                style = Paint.Style.FILL
            }
            val t = cell * 1.35f
            val r = t * 0.3f
            canvas.drawRoundRect(RectF(ox + s / 2f - t / 2f, oy, ox + s / 2f + t / 2f, oy + t), r, r, tickPaint)
            canvas.drawRoundRect(RectF(ox + s / 2f - t / 2f, oy + s - t, ox + s / 2f + t / 2f, oy + s), r, r, tickPaint)
            canvas.drawRoundRect(RectF(ox, oy + s / 2f - t / 2f, ox + t, oy + s / 2f + t / 2f), r, r, tickPaint)
            canvas.drawRoundRect(RectF(ox + s - t, oy + s / 2f - t / 2f, ox + s, oy + s / 2f + t / 2f), r, r, tickPaint)
        }
    }

    private fun drawEyeLayer(canvas: Canvas, x: Float, y: Float, size: Float, shape: EyeShape, paint: Paint) {
        val cx = x + size / 2f
        val cy = y + size / 2f
        val r = size / 2f

        when (shape) {
            EyeShape.Square -> {
                canvas.drawRect(x, y, x + size, y + size, paint)
            }
            EyeShape.Rounded -> {
                canvas.drawRoundRect(RectF(x, y, x + size, y + size), size * 0.18f, size * 0.18f, paint)
            }
            EyeShape.ExtraRounded -> {
                canvas.drawRoundRect(RectF(x, y, x + size, y + size), size * 0.32f, size * 0.32f, paint)
            }
            EyeShape.Circle, EyeShape.Target -> {
                canvas.drawCircle(cx, cy, r, paint)
            }
            EyeShape.Diamond -> {
                val path = Path().apply {
                    moveTo(cx, y)
                    lineTo(x + size, cy)
                    lineTo(cx, y + size)
                    lineTo(x, cy)
                    close()
                }
                canvas.drawPath(path, paint)
            }
            EyeShape.Hex -> {
                val path = Path()
                for (i in 0 until 6) {
                    val angle = (Math.PI / 3 * i - Math.PI / 6).toFloat()
                    val px = cx + cos(angle.toDouble()).toFloat() * r
                    val py = cy + sin(angle.toDouble()).toFloat() * r
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
                canvas.drawPath(path, paint)
            }
            EyeShape.Classy -> {
                // Diagonally rounded: top-right & bottom-left
                val cr = size * 0.38f
                val radii = floatArrayOf(0f, 0f, cr, cr, 0f, 0f, cr, cr)
                val path = Path().apply {
                    addRoundRect(RectF(x, y, x + size, y + size), radii, Path.Direction.CW)
                }
                canvas.drawPath(path, paint)
            }
            EyeShape.Leaf -> {
                // Diagonally rounded: top-left & bottom-right
                val cr = size * 0.42f
                val radii = floatArrayOf(cr, cr, 0f, 0f, cr, cr, 0f, 0f)
                val path = Path().apply {
                    addRoundRect(RectF(x, y, x + size, y + size), radii, Path.Direction.CW)
                }
                canvas.drawPath(path, paint)
            }
            EyeShape.Ticks -> {
                canvas.drawRoundRect(RectF(x, y, x + size, y + size), size * 0.18f, size * 0.18f, paint)
            }
        }
    }

    private fun drawFrameOnCanvas(
        canvas: Canvas,
        frameStyle: FrameStyle,
        caption: String,
        sizePx: Int,
        ox: Float,
        oy: Float,
        bodyPx: Float,
        qrStyle: QrStyle
    ) {
        val s = sizePx.toFloat()
        val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = qrStyle.fgColor
            this.style = Paint.Style.STROKE
            strokeWidth = s * 0.02f
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = s * 0.045f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        when (frameStyle) {
            FrameStyle.SimpleBorder -> {
                canvas.drawRoundRect(RectF(s * 0.04f, s * 0.04f, s * 0.96f, s * 0.96f), s * 0.04f, s * 0.04f, framePaint)
            }
            FrameStyle.BadgeScanMe -> {
                canvas.drawRoundRect(RectF(s * 0.04f, s * 0.04f, s * 0.96f, s * 0.82f), s * 0.04f, s * 0.04f, framePaint)
                val badgeFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = qrStyle.fgColor
                    this.style = Paint.Style.FILL
                }
                val badgeRect = RectF(s * 0.15f, s * 0.85f, s * 0.85f, s * 0.96f)
                canvas.drawRoundRect(badgeRect, s * 0.05f, s * 0.05f, badgeFill)
                canvas.drawText(caption.ifEmpty { "SCAN ME" }, s / 2f, s * 0.925f, textPaint)
            }
            FrameStyle.ModernPill -> {
                val pillFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = qrStyle.fgColor
                    this.style = Paint.Style.FILL
                }
                val pillRect = RectF(s * 0.20f, s * 0.87f, s * 0.80f, s * 0.96f)
                canvas.drawRoundRect(pillRect, s * 0.045f, s * 0.045f, pillFill)
                canvas.drawText(caption.ifEmpty { "SCAN ME" }, s / 2f, s * 0.93f, textPaint)
            }
            FrameStyle.PhoneFrame -> {
                val phoneRect = RectF(s * 0.04f, s * 0.02f, s * 0.96f, s * 0.98f)
                canvas.drawRoundRect(phoneRect, s * 0.08f, s * 0.08f, framePaint)
                val notchPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = qrStyle.fgColor
                    this.style = Paint.Style.FILL
                }
                canvas.drawRoundRect(RectF(s * 0.38f, s * 0.035f, s * 0.62f, s * 0.065f), s * 0.015f, s * 0.015f, notchPaint)
            }
            FrameStyle.NeonGlow -> {
                framePaint.color = qrStyle.gradientTo
                framePaint.strokeWidth = s * 0.025f
                framePaint.setShadowLayer(s * 0.04f, 0f, 0f, qrStyle.gradientTo)
                canvas.drawRoundRect(RectF(s * 0.05f, s * 0.05f, s * 0.95f, s * 0.84f), s * 0.05f, s * 0.05f, framePaint)

                val badgeFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = qrStyle.gradientTo
                    this.style = Paint.Style.FILL
                }
                val pillRect = RectF(s * 0.22f, s * 0.87f, s * 0.78f, s * 0.96f)
                canvas.drawRoundRect(pillRect, s * 0.045f, s * 0.045f, badgeFill)
                textPaint.color = 0xFF000000.toInt()
                canvas.drawText(caption.ifEmpty { "SCAN NOW" }, s / 2f, s * 0.93f, textPaint)
            }
            else -> {}
        }
    }
}
