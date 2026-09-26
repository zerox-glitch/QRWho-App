package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.qr.engine.EyeShape
import com.example.qr.engine.ModuleShape
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonViolet
import kotlin.math.cos
import kotlin.math.sin

/**
 * Renders a visual representation of a QR Module Dot Shape (e.g. 2x2 grid of shapes)
 * without displaying text names, giving the user direct visual feedback.
 */
@Composable
fun ModuleShapeVisualTile(
    shape: ModuleShape,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val bg = if (isSelected) ElectricCyan.copy(alpha = 0.2f) else CardDark
    val border = if (isSelected) ElectricCyan else CardBorder
    val shapeColor = if (isSelected) ElectricCyan else Color(0xFFD4D4D8)

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(if (isSelected) 2.dp else 1.dp, border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
            .testTag("module_shape_${shape.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size - 16.dp)) {
            val w = this.size.width
            val cell = w / 2f
            val dotRadius = cell * 0.40f

            // Draw a 2x2 cluster showing the dot shape pattern
            val centers = listOf(
                Offset(cell * 0.5f, cell * 0.5f),
                Offset(cell * 1.5f, cell * 0.5f),
                Offset(cell * 0.5f, cell * 1.5f),
                Offset(cell * 1.5f, cell * 1.5f)
            )

            for (c in centers) {
                when (shape) {
                    ModuleShape.Square -> {
                        drawRect(
                            color = shapeColor,
                            topLeft = Offset(c.x - dotRadius, c.y - dotRadius),
                            size = Size(dotRadius * 2f, dotRadius * 2f)
                        )
                    }
                    ModuleShape.Rounded -> {
                        drawRoundRect(
                            color = shapeColor,
                            topLeft = Offset(c.x - dotRadius, c.y - dotRadius),
                            size = Size(dotRadius * 2f, dotRadius * 2f),
                            cornerRadius = CornerRadius(dotRadius * 0.45f, dotRadius * 0.45f)
                        )
                    }
                    ModuleShape.Squircle -> {
                        drawRoundRect(
                            color = shapeColor,
                            topLeft = Offset(c.x - dotRadius, c.y - dotRadius),
                            size = Size(dotRadius * 2f, dotRadius * 2f),
                            cornerRadius = CornerRadius(dotRadius * 0.70f, dotRadius * 0.70f)
                        )
                    }
                    ModuleShape.Dots, ModuleShape.Bubbles, ModuleShape.Fluid -> {
                        drawCircle(
                            color = shapeColor,
                            radius = dotRadius,
                            center = c
                        )
                    }
                    ModuleShape.Classy -> {
                        val path = Path().apply {
                            val r = dotRadius
                            addRoundRect(
                                RoundRect(
                                    rect = Rect(c.x - r, c.y - r, c.x + r, c.y + r),
                                    topRight = CornerRadius(r * 0.9f, r * 0.9f),
                                    bottomLeft = CornerRadius(r * 0.9f, r * 0.9f),
                                    topLeft = CornerRadius(0f, 0f),
                                    bottomRight = CornerRadius(0f, 0f)
                                )
                            )
                        }
                        drawPath(path, shapeColor)
                    }
                    ModuleShape.Leaf -> {
                        val path = Path().apply {
                            val r = dotRadius
                            addRoundRect(
                                RoundRect(
                                    rect = Rect(c.x - r, c.y - r, c.x + r, c.y + r),
                                    topLeft = CornerRadius(r * 0.9f, r * 0.9f),
                                    bottomRight = CornerRadius(r * 0.9f, r * 0.9f),
                                    topRight = CornerRadius(0f, 0f),
                                    bottomLeft = CornerRadius(0f, 0f)
                                )
                            )
                        }
                        drawPath(path, shapeColor)
                    }
                    ModuleShape.Diamond -> {
                        val path = Path().apply {
                            val r = dotRadius * 1.05f
                            moveTo(c.x, c.y - r)
                            lineTo(c.x + r, c.y)
                            lineTo(c.x, c.y + r)
                            lineTo(c.x - r, c.y)
                            close()
                        }
                        drawPath(path, shapeColor)
                    }
                    ModuleShape.Star -> {
                        val path = Path().apply {
                            val outerR = dotRadius * 1.1f
                            val innerR = outerR * 0.45f
                            for (i in 0 until 8) {
                                val r = if (i % 2 == 0) outerR else innerR
                                val angle = i * Math.PI / 4.0 - Math.PI / 2.0
                                val px = (c.x + r * cos(angle)).toFloat()
                                val py = (c.y + r * sin(angle)).toFloat()
                                if (i == 0) moveTo(px, py) else lineTo(px, py)
                            }
                            close()
                        }
                        drawPath(path, shapeColor)
                    }
                    ModuleShape.Heart -> {
                        val path = Path().apply {
                            val r = dotRadius * 0.95f
                            val top = c.y - r
                            val bottom = c.y + r
                            moveTo(c.x, top + r * 0.6f)
                            cubicTo(c.x - r, top, c.x - r * 1.2f, top + r * 0.9f, c.x, bottom)
                            cubicTo(c.x + r * 1.2f, top + r * 0.9f, c.x + r, top, c.x, top + r * 0.6f)
                            close()
                        }
                        drawPath(path, shapeColor)
                    }
                    ModuleShape.Plus, ModuleShape.Cross -> {
                        val path = Path().apply {
                            val r = dotRadius
                            val t = r * 0.4f
                            // Horizontal
                            addRect(Rect(c.x - r, c.y - t, c.x + r, c.y + t))
                            // Vertical
                            addRect(Rect(c.x - t, c.y - r, c.x + t, c.y + r))
                        }
                        drawPath(path, shapeColor)
                    }
                    ModuleShape.Hex -> {
                        val path = Path().apply {
                            val r = dotRadius * 1.05f
                            for (i in 0 until 6) {
                                val angle = i * Math.PI / 3.0
                                val px = (c.x + r * cos(angle)).toFloat()
                                val py = (c.y + r * sin(angle)).toFloat()
                                if (i == 0) moveTo(px, py) else lineTo(px, py)
                            }
                            close()
                        }
                        drawPath(path, shapeColor)
                    }
                    ModuleShape.Dash, ModuleShape.HBar -> {
                        drawRoundRect(
                            color = shapeColor,
                            topLeft = Offset(c.x - dotRadius * 1.1f, c.y - dotRadius * 0.5f),
                            size = Size(dotRadius * 2.2f, dotRadius * 1.0f),
                            cornerRadius = CornerRadius(dotRadius * 0.3f, dotRadius * 0.3f)
                        )
                    }
                    ModuleShape.VBar -> {
                        drawRoundRect(
                            color = shapeColor,
                            topLeft = Offset(c.x - dotRadius * 0.5f, c.y - dotRadius * 1.1f),
                            size = Size(dotRadius * 1.0f, dotRadius * 2.2f),
                            cornerRadius = CornerRadius(dotRadius * 0.3f, dotRadius * 0.3f)
                        )
                    }
                    ModuleShape.Diag -> {
                        val path = Path().apply {
                            val r = dotRadius * 0.95f
                            val w = dotRadius * 0.36f
                            moveTo(c.x - r + w, c.y - r)
                            lineTo(c.x + r, c.y + r - w)
                            lineTo(c.x + r - w, c.y + r)
                            lineTo(c.x - r, c.y - r + w)
                            close()
                        }
                        drawPath(path, shapeColor)
                    }
                    ModuleShape.Radial -> {
                        val path = Path().apply {
                            val r = dotRadius * 0.95f
                            moveTo(c.x + r * 0.65f, c.y)
                            lineTo(c.x - r * 0.5f, c.y - r * 0.32f)
                            lineTo(c.x - r * 0.28f, c.y)
                            lineTo(c.x - r * 0.5f, c.y + r * 0.32f)
                            close()
                        }
                        drawPath(path, shapeColor)
                    }
                    ModuleShape.Confetti -> {
                        val path = Path().apply {
                            val r = dotRadius * 0.8f
                            moveTo(c.x - r, c.y - r * 0.5f)
                            lineTo(c.x + r * 0.5f, c.y - r)
                            lineTo(c.x + r, c.y + r * 0.5f)
                            lineTo(c.x - r * 0.5f, c.y + r)
                            close()
                        }
                        drawPath(path, shapeColor)
                    }
                }
            }
        }
    }
}

/**
 * Renders a visual representation of the Outer Eye Finder Frame
 * without displaying text names.
 */
@Composable
fun EyeShapeVisualTile(
    shape: EyeShape,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val bg = if (isSelected) NeonViolet.copy(alpha = 0.2f) else CardDark
    val border = if (isSelected) NeonViolet else CardBorder
    val shapeColor = if (isSelected) NeonViolet else Color(0xFFD4D4D8)

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(if (isSelected) 2.dp else 1.dp, border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
            .testTag("eye_shape_${shape.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size - 16.dp)) {
            val w = this.size.width
            val h = this.size.height
            val strokeW = w * 0.22f
            val halfStroke = strokeW / 2f
            val rect = Rect(halfStroke, halfStroke, w - halfStroke, h - halfStroke)

            when (shape) {
                EyeShape.Square -> {
                    drawRect(
                        color = shapeColor,
                        topLeft = Offset(halfStroke, halfStroke),
                        size = Size(w - strokeW, h - strokeW),
                        style = Stroke(width = strokeW)
                    )
                }
                EyeShape.Rounded -> {
                    drawRoundRect(
                        color = shapeColor,
                        topLeft = Offset(halfStroke, halfStroke),
                        size = Size(w - strokeW, h - strokeW),
                        cornerRadius = CornerRadius(w * 0.20f, w * 0.20f),
                        style = Stroke(width = strokeW)
                    )
                }
                EyeShape.ExtraRounded -> {
                    drawRoundRect(
                        color = shapeColor,
                        topLeft = Offset(halfStroke, halfStroke),
                        size = Size(w - strokeW, h - strokeW),
                        cornerRadius = CornerRadius(w * 0.38f, w * 0.38f),
                        style = Stroke(width = strokeW)
                    )
                }
                EyeShape.Circle -> {
                    drawCircle(
                        color = shapeColor,
                        radius = (w - strokeW) / 2f,
                        center = Offset(w / 2f, h / 2f),
                        style = Stroke(width = strokeW)
                    )
                }
                EyeShape.Leaf -> {
                    val path = Path().apply {
                        addRoundRect(
                            RoundRect(
                                rect = rect,
                                topLeft = CornerRadius(w * 0.42f, w * 0.42f),
                                bottomRight = CornerRadius(w * 0.42f, w * 0.42f),
                                topRight = CornerRadius(0f, 0f),
                                bottomLeft = CornerRadius(0f, 0f)
                            )
                        )
                    }
                    drawPath(path, shapeColor, style = Stroke(width = strokeW))
                }
                EyeShape.Diamond -> {
                    val cx = w / 2f
                    val cy = h / 2f
                    val r = cx - halfStroke
                    val path = Path().apply {
                        moveTo(cx, cy - r)
                        lineTo(cx + r, cy)
                        lineTo(cx, cy + r)
                        lineTo(cx - r, cy)
                        close()
                    }
                    drawPath(path, shapeColor, style = Stroke(width = strokeW))
                }
                EyeShape.Hex -> {
                    val cx = w / 2f
                    val cy = h / 2f
                    val r = cx - halfStroke
                    val path = Path().apply {
                        for (i in 0 until 6) {
                            val angle = i * Math.PI / 3.0
                            val px = (cx + r * cos(angle)).toFloat()
                            val py = (cy + r * sin(angle)).toFloat()
                            if (i == 0) moveTo(px, py) else lineTo(px, py)
                        }
                        close()
                    }
                    drawPath(path, shapeColor, style = Stroke(width = strokeW))
                }
                EyeShape.Classy -> {
                    val path = Path().apply {
                        addRoundRect(
                            RoundRect(
                                rect = rect,
                                topLeft = CornerRadius(w * 0.35f, w * 0.35f),
                                bottomRight = CornerRadius(w * 0.35f, w * 0.35f),
                                topRight = CornerRadius(w * 0.1f, w * 0.1f),
                                bottomLeft = CornerRadius(w * 0.1f, w * 0.1f)
                            )
                        )
                    }
                    drawPath(path, shapeColor, style = Stroke(width = strokeW))
                }
                EyeShape.Target -> {
                    drawCircle(
                        color = shapeColor,
                        radius = (w - strokeW) / 2f,
                        center = Offset(w / 2f, h / 2f),
                        style = Stroke(width = strokeW * 0.7f)
                    )
                    drawCircle(
                        color = shapeColor,
                        radius = (w - strokeW) / 4f,
                        center = Offset(w / 2f, h / 2f),
                        style = Stroke(width = strokeW * 0.5f)
                    )
                }
                EyeShape.Ticks -> {
                    drawRect(
                        color = shapeColor,
                        topLeft = Offset(halfStroke, halfStroke),
                        size = Size(w - strokeW, h - strokeW),
                        style = Stroke(width = strokeW * 0.8f)
                    )
                    // Tick accents
                    drawLine(shapeColor, Offset(0f, h / 2f), Offset(strokeW, h / 2f), strokeWidth = 2f)
                    drawLine(shapeColor, Offset(w - strokeW, h / 2f), Offset(w, h / 2f), strokeWidth = 2f)
                }
            }
        }
    }
}

/**
 * Renders a visual representation of the Inner Eye Pupil / Eyeball Shape
 * without displaying text names.
 */
@Composable
fun EyeBallVisualTile(
    shape: EyeShape,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val bg = if (isSelected) ElectricCyan.copy(alpha = 0.2f) else CardDark
    val border = if (isSelected) ElectricCyan else CardBorder
    val shapeColor = if (isSelected) ElectricCyan else Color(0xFFD4D4D8)

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(if (isSelected) 2.dp else 1.dp, border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
            .testTag("eyeball_shape_${shape.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size - 16.dp)) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f

            // Faint outer finder frame hint
            drawRoundRect(
                color = shapeColor.copy(alpha = 0.25f),
                topLeft = Offset(1f, 1f),
                size = Size(w - 2f, h - 2f),
                cornerRadius = CornerRadius(4f, 4f),
                style = Stroke(width = 1.5f)
            )

            // Inner solid pupil shape
            val pupilR = w * 0.28f

            when (shape) {
                EyeShape.Circle, EyeShape.Target -> {
                    drawCircle(
                        color = shapeColor,
                        radius = pupilR,
                        center = Offset(cx, cy)
                    )
                }
                EyeShape.Square, EyeShape.Ticks -> {
                    drawRect(
                        color = shapeColor,
                        topLeft = Offset(cx - pupilR, cy - pupilR),
                        size = Size(pupilR * 2f, pupilR * 2f)
                    )
                }
                EyeShape.Rounded -> {
                    drawRoundRect(
                        color = shapeColor,
                        topLeft = Offset(cx - pupilR, cy - pupilR),
                        size = Size(pupilR * 2f, pupilR * 2f),
                        cornerRadius = CornerRadius(pupilR * 0.5f, pupilR * 0.5f)
                    )
                }
                EyeShape.ExtraRounded -> {
                    drawRoundRect(
                        color = shapeColor,
                        topLeft = Offset(cx - pupilR, cy - pupilR),
                        size = Size(pupilR * 2f, pupilR * 2f),
                        cornerRadius = CornerRadius(pupilR * 0.85f, pupilR * 0.85f)
                    )
                }
                EyeShape.Leaf, EyeShape.Classy -> {
                    val path = Path().apply {
                        addRoundRect(
                            RoundRect(
                                rect = Rect(cx - pupilR, cy - pupilR, cx + pupilR, cy + pupilR),
                                topLeft = CornerRadius(pupilR * 0.9f, pupilR * 0.9f),
                                bottomRight = CornerRadius(pupilR * 0.9f, pupilR * 0.9f),
                                topRight = CornerRadius(1f, 1f),
                                bottomLeft = CornerRadius(1f, 1f)
                            )
                        )
                    }
                    drawPath(path, shapeColor)
                }
                EyeShape.Diamond -> {
                    val path = Path().apply {
                        val r = pupilR * 1.2f
                        moveTo(cx, cy - r)
                        lineTo(cx + r, cy)
                        lineTo(cx, cy + r)
                        lineTo(cx - r, cy)
                        close()
                    }
                    drawPath(path, shapeColor)
                }
                EyeShape.Hex -> {
                    val path = Path().apply {
                        val r = pupilR * 1.15f
                        for (i in 0 until 6) {
                            val angle = i * Math.PI / 3.0
                            val px = (cx + r * cos(angle)).toFloat()
                            val py = (cy + r * sin(angle)).toFloat()
                            if (i == 0) moveTo(px, py) else lineTo(px, py)
                        }
                        close()
                    }
                    drawPath(path, shapeColor)
                }
            }
        }
    }
}
