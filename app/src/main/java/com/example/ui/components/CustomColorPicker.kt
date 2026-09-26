package com.example.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qr.engine.QrStyle
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class ColorTarget(val label: String, val shortLabel: String) {
    Foreground("Modules / Dots", "Dots"),
    EyeFrame("Eye Frame", "Eyes"),
    EyePupil("Eye Pupil / Ball", "Pupils"),
    Background("Background", "Background"),
    GradientEnd("Gradient Flow", "Gradient")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomColorSection(
    style: QrStyle,
    onStyleChange: (QrStyle) -> Unit,
    modifier: Modifier = Modifier,
    currentTarget: ColorTarget? = null,
    onTargetChange: (ColorTarget) -> Unit = {}
) {
    var internalTarget by remember { mutableStateOf(ColorTarget.Foreground) }
    val selectedTarget = currentTarget ?: internalTarget

    fun setTarget(t: ColorTarget) {
        internalTarget = t
        onTargetChange(t)
    }

    val clipboardManager = LocalClipboardManager.current

    val currentColorInt = when (selectedTarget) {
        ColorTarget.Foreground -> style.fgColor
        ColorTarget.EyeFrame -> style.eyeColor
        ColorTarget.EyePupil -> style.ballColor
        ColorTarget.Background -> style.bgColor
        ColorTarget.GradientEnd -> style.gradientTo
    }

    // Convert currentColorInt to HSV
    val initialHsv = remember(currentColorInt) {
        val hsv = FloatArray(3)
        AndroidColor.colorToHSV(currentColorInt, hsv)
        hsv
    }

    var hue by remember(currentColorInt, selectedTarget) { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember(currentColorInt, selectedTarget) { mutableFloatStateOf(initialHsv[1]) }
    var value by remember(currentColorInt, selectedTarget) { mutableFloatStateOf(initialHsv[2]) }

    var hexInput by remember(currentColorInt, selectedTarget) {
        val hex = String.format("%06X", 0xFFFFFF and currentColorInt)
        mutableStateOf("#$hex")
    }

    fun applyColor(colorInt: Int) {
        val updated = when (selectedTarget) {
            ColorTarget.Foreground -> style.copy(fgColor = colorInt)
            ColorTarget.EyeFrame -> style.copy(eyeColor = colorInt)
            ColorTarget.EyePupil -> style.copy(ballColor = colorInt)
            ColorTarget.Background -> style.copy(bgColor = colorInt)
            ColorTarget.GradientEnd -> style.copy(gradientTo = colorInt)
        }
        onStyleChange(updated)
    }

    fun updateFromHsv(h: Float, s: Float, v: Float) {
        hue = h
        saturation = s
        value = v
        val colorInt = AndroidColor.HSVToColor(floatArrayOf(h, s, v))
        hexInput = String.format("#%06X", 0xFFFFFF and colorInt)
        applyColor(colorInt)
    }

    // Rainbow gradient for Hue Slider
    val rainbowColors = remember {
        listOf(
            Color.Red,
            Color.Yellow,
            Color.Green,
            Color.Cyan,
            Color.Blue,
            Color.Magenta,
            Color.Red
        )
    }

    // Spectrum of 32 curated designer hex values
    val paletteSwatches = listOf(
        0xFF000000.toInt(), // Pure Black
        0xFF0F172A.toInt(), // Midnight Slate
        0xFF1E293B.toInt(), // Dark Slate
        0xFF334155.toInt(), // Charcoal
        0xFFFFFFFF.toInt(), // Pure White
        0xFFF8FAFC.toInt(), // Off White
        0xFFE2E8F0.toInt(), // Light Slate
        0xFF00F0FF.toInt(), // Cyan
        0xFF06B6D4.toInt(), // Teal Cyan
        0xFF0284C7.toInt(), // Cobalt Blue
        0xFF2563EB.toInt(), // Royal Blue
        0xFF4F46E5.toInt(), // Indigo
        0xFF7C3AED.toInt(), // Deep Violet
        0xFF8B5CF6.toInt(), // Neon Violet
        0xFFA855F7.toInt(), // Purple
        0xFFD946EF.toInt(), // Fuchsia
        0xFFEC4899.toInt(), // Pink
        0xFFF43F5E.toInt(), // Rose
        0xFFEF4444.toInt(), // Crimson Red
        0xFFDC2626.toInt(), // Deep Red
        0xFFF97316.toInt(), // Orange
        0xFFEA580C.toInt(), // Rust
        0xFFF59E0B.toInt(), // Amber
        0xFFEAB308.toInt(), // Gold
        0xFF84CC16.toInt(), // Lime
        0xFF10B981.toInt(), // Emerald
        0xFF059669.toInt(), // Dark Jade
        0xFF0D9488.toInt(), // Dark Teal
        0xFF1E3A8A.toInt(), // Navy Blue
        0xFF4A044E.toInt(), // Deep Plum
        0xFF831843.toInt(), // Berry
        0xFF78350F.toInt()  // Warm Bronze
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
        color = CardDark
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with target badge and hex swatch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ColorLens,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Color Studio",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Current Active Color Swatch Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceDark)
                        .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(currentColorInt))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = String.format("#%06X", 0xFFFFFF and currentColorInt),
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Target selector tabs (Modules/Dots, Eye Frame, Eye Pupil/Ball, Background, Gradient)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ColorTarget.values().forEach { target ->
                    val isSelected = selectedTarget == target
                    val targetColorInt = when (target) {
                        ColorTarget.Foreground -> style.fgColor
                        ColorTarget.EyeFrame -> style.eyeColor
                        ColorTarget.EyePupil -> style.ballColor
                        ColorTarget.Background -> style.bgColor
                        ColorTarget.GradientEnd -> style.gradientTo
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) ElectricCyan.copy(alpha = 0.2f) else SurfaceDark)
                            .border(1.dp, if (isSelected) ElectricCyan else CardBorder, RoundedCornerShape(10.dp))
                            .clickable { setTarget(target) }
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color(targetColorInt))
                                    .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = target.label,
                                color = if (isSelected) ElectricCyan else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // VISUAL INTERACTIVE COLOR PICKER (HUE SLIDER with RAINBOW)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceDark)
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                // Hue Spectrum Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Hue Spectrum", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("${hue.toInt()}°", color = ElectricCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(Brush.horizontalGradient(rainbowColors))
                )
                Slider(
                    value = hue,
                    onValueChange = { updateFromHsv(it, saturation, value) },
                    valueRange = 0f..360f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    ),
                    modifier = Modifier.height(28.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Saturation Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Saturation / Vibrancy", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("${(saturation * 100).toInt()}%", color = ElectricCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = saturation,
                    onValueChange = { updateFromHsv(hue, it, value) },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricCyan,
                        activeTrackColor = ElectricCyan,
                        inactiveTrackColor = CardBorder
                    ),
                    modifier = Modifier.height(28.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Value / Brightness Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Brightness / Shade", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("${(value * 100).toInt()}%", color = ElectricCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = value,
                    onValueChange = { updateFromHsv(hue, saturation, it) },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricCyan,
                        activeTrackColor = ElectricCyan,
                        inactiveTrackColor = CardBorder
                    ),
                    modifier = Modifier.height(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Sync Actions (Match Eyes to Dots, Match Pupil to Frame)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        // Match Eyes and Pupils to Dots
                        onStyleChange(style.copy(eyeColor = style.fgColor, ballColor = style.fgColor))
                    },
                    modifier = Modifier.weight(1f).height(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(13.dp), tint = ElectricCyan)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sync Eyes to Dots", fontSize = 10.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = {
                        // Swap Foreground and Background
                        onStyleChange(style.copy(fgColor = style.bgColor, bgColor = style.fgColor))
                    },
                    modifier = Modifier.weight(1f).height(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Text("Invert Colors", fontSize = 10.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Hex Input Field & Quick Apply
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { input ->
                        hexInput = input
                        val clean = input.trim().removePrefix("#")
                        if (clean.length == 6) {
                            try {
                                val parsed = AndroidColor.parseColor("#$clean")
                                applyColor(parsed)
                            } catch (_: Exception) {}
                        }
                    },
                    label = { Text("Hex Code (#RRGGBB)", fontSize = 11.sp, color = TextMuted) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark,
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .testTag("hex_color_input")
                )

                // Quick Paste button
                IconButton(
                    onClick = {
                        val clipText = clipboardManager.getText()?.text?.trim() ?: ""
                        val clean = clipText.removePrefix("#")
                        if (clean.length == 6) {
                            try {
                                val parsed = AndroidColor.parseColor("#$clean")
                                hexInput = "#$clean"
                                applyColor(parsed)
                            } catch (_: Exception) {}
                        }
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceDark)
                        .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "Paste Hex",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Quick Copy button
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(hexInput))
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceDark)
                        .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Hex",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Designer Swatches Grid (32 colors)
            Text(
                text = "Preset Color Swatches",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                paletteSwatches.forEach { col ->
                    val isCurrent = (col and 0xFFFFFF) == (currentColorInt and 0xFFFFFF)
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(col))
                            .border(
                                width = if (isCurrent) 2.5.dp else 1.dp,
                                color = if (isCurrent) ElectricCyan else Color.White.copy(alpha = 0.25f),
                                shape = CircleShape
                            )
                            .clickable {
                                applyColor(col)
                                hexInput = String.format("#%06X", 0xFFFFFF and col)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCurrent) {
                            val checkTint = if (Color(col).luminance() > 0.5f) Color.Black else Color.White
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = checkTint,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun Color.luminance(): Float {
    return 0.299f * red + 0.587f * green + 0.114f * blue
}
