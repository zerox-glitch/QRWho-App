package com.example.ui.screens.tabs

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qr.engine.EyeShape
import com.example.qr.engine.FrameStyle
import com.example.qr.engine.GradientType
import com.example.qr.engine.ModuleShape
import com.example.qr.engine.QrStyle
import com.example.ui.components.ColorTarget
import com.example.ui.components.CustomColorSection
import com.example.ui.components.EyeBallVisualTile
import com.example.ui.components.EyeShapeVisualTile
import com.example.ui.components.ModuleShapeVisualTile
import com.example.ui.theme.BeaconRose
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DesignTab(
    style: QrStyle,
    onStyleChange: (QrStyle) -> Unit,
    onSaveCustomPreset: (String, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeColorTarget by remember { mutableStateOf(com.example.ui.components.ColorTarget.Foreground) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var presetName by remember { mutableStateOf("") }
    var presetDesc by remember { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Save as Custom Preset Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(14.dp)),
            color = SurfaceDark
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Save this Custom Look", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Save shape, colors & frames into 'My Presets'", color = TextMuted, fontSize = 11.sp)
                }
                Button(
                    onClick = {
                        presetName = "My ${style.moduleShape.label} Preset"
                        presetDesc = "Custom style"
                        showSaveDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Color(0xFF0C0C0B)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save Preset", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        // 1. Module Shape: VISUAL TILES ONLY (No text names)
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Module Dot Shapes", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDark)
                        .border(1.dp, if (activeColorTarget == com.example.ui.components.ColorTarget.Foreground) ElectricCyan else CardBorder, RoundedCornerShape(12.dp))
                        .clickable { activeColorTarget = com.example.ui.components.ColorTarget.Foreground }
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(style.fgColor))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Dot Color", color = if (activeColorTarget == com.example.ui.components.ColorTarget.Foreground) ElectricCyan else TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModuleShape.values().forEach { shape ->
                    val isSelected = style.moduleShape == shape
                    ModuleShapeVisualTile(
                        shape = shape,
                        isSelected = isSelected,
                        onClick = { onStyleChange(style.copy(moduleShape = shape)) }
                    )
                }
            }
        }

        // 2. Eye Corner Shape: VISUAL TILES ONLY (No text names)
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Eye Frame Shapes", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDark)
                        .border(1.dp, if (activeColorTarget == com.example.ui.components.ColorTarget.EyeFrame) ElectricCyan else CardBorder, RoundedCornerShape(12.dp))
                        .clickable { activeColorTarget = com.example.ui.components.ColorTarget.EyeFrame }
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(style.eyeColor))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Frame Color", color = if (activeColorTarget == com.example.ui.components.ColorTarget.EyeFrame) ElectricCyan else TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EyeShape.values().forEach { shape ->
                    val isSelected = style.eyeShape == shape
                    EyeShapeVisualTile(
                        shape = shape,
                        isSelected = isSelected,
                        onClick = { onStyleChange(style.copy(eyeShape = shape)) }
                    )
                }
            }
        }

        // 3. Eye Pupil / Ball Shape: VISUAL TILES ONLY (No text names)
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Eye Pupil Shapes", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDark)
                        .border(1.dp, if (activeColorTarget == com.example.ui.components.ColorTarget.EyePupil) ElectricCyan else CardBorder, RoundedCornerShape(12.dp))
                        .clickable { activeColorTarget = com.example.ui.components.ColorTarget.EyePupil }
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(style.ballColor))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Pupil Color", color = if (activeColorTarget == com.example.ui.components.ColorTarget.EyePupil) ElectricCyan else TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EyeShape.values().forEach { shape ->
                    val isSelected = style.ballShape == shape
                    EyeBallVisualTile(
                        shape = shape,
                        isSelected = isSelected,
                        onClick = { onStyleChange(style.copy(ballShape = shape)) }
                    )
                }
            }
        }

        // 4. Custom Color Picker (Full customization for Foreground, Background, Eyes, Gradient)
        CustomColorSection(
            style = style,
            onStyleChange = onStyleChange,
            currentTarget = activeColorTarget,
            onTargetChange = { activeColorTarget = it }
        )

        // 5. Preset Color Themes & Quick Accents
        Column {
            Text("Preset Color Themes", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            val presetPalettes = listOf(
                Triple("Neon Cyan", 0xFF00F0FF.toInt(), 0xFF7000FF.toInt()),
                Triple("Obsidian Gold", 0xFFD4AF37.toInt(), 0xFFF59E0B.toInt()),
                Triple("Sakura Rose", 0xFFDB2777.toInt(), 0xFFFB7185.toInt()),
                Triple("Deep Forest", 0xFF064E3B.toInt(), 0xFF10B981.toInt()),
                Triple("Cyber Violet", 0xFF8B5CF6.toInt(), 0xFFC084FC.toInt()),
                Triple("Sunset Orange", 0xFFF97316.toInt(), 0xFFEC4899.toInt()),
                Triple("Pure Ink", 0xFF000000.toInt(), 0xFF334155.toInt()),
                Triple("Ocean Cobalt", 0xFF0284C7.toInt(), 0xFF00F0FF.toInt())
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                presetPalettes.forEach { (name, fg, grad) ->
                    val isSelected = style.fgColor == fg
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onStyleChange(
                                    style.copy(
                                        fgColor = fg,
                                        eyeColor = fg,
                                        ballColor = grad,
                                        gradientTo = grad
                                    )
                                )
                            }
                            .border(if (isSelected) 2.dp else 1.dp, if (isSelected) ElectricCyan else CardBorder, RoundedCornerShape(12.dp)),
                        color = CardDark
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(Color(fg))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(Color(grad))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(name, color = TextPrimary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // 6. Color Gradient Studio (Complete with interactive color picker, preview & presets)
        GradientStudioSection(
            style = style,
            onStyleChange = onStyleChange,
            activeColorTarget = activeColorTarget,
            onColorTargetChange = { activeColorTarget = it }
        )

        // 7. Custom Frames & Badges
        Column {
            Text("Custom Frames & Border", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FrameStyle.values().forEach { f ->
                    val isSelected = style.frameStyle == f
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) ElectricCyan.copy(alpha = 0.2f) else CardDark)
                            .border(1.dp, if (isSelected) ElectricCyan else CardBorder, RoundedCornerShape(10.dp))
                            .clickable { onStyleChange(style.copy(frameStyle = f)) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = f.label,
                            color = if (isSelected) ElectricCyan else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            if (style.frameStyle != FrameStyle.None) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = style.frameCaption,
                    onValueChange = { onStyleChange(style.copy(frameCaption = it)) },
                    label = { Text("Frame Caption", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text("Save Custom Preset", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Name your custom style to save it permanently in 'My Presets'.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = presetName,
                        onValueChange = { presetName = it },
                        label = { Text("Preset Name", color = TextMuted) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = presetDesc,
                        onValueChange = { presetDesc = it },
                        label = { Text("Description (Optional)", color = TextMuted) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (presetName.isNotBlank()) {
                            onSaveCustomPreset(presetName.trim(), presetDesc.trim())
                            showSaveDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Color(0xFF0C0C0B))
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = CardDark
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GradientStudioSection(
    style: QrStyle,
    onStyleChange: (QrStyle) -> Unit,
    activeColorTarget: ColorTarget,
    onColorTargetChange: (ColorTarget) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val isGradientActive = style.gradientType != GradientType.None
    
    // Track which gradient stop is being edited in the color picker: 0 = Start (fgColor), 1 = End (gradientTo)
    var activeGradientStop by remember { mutableStateOf(if (activeColorTarget == ColorTarget.GradientEnd) 1 else 0) }
    
    val currentStopColorInt = if (activeGradientStop == 0) style.fgColor else style.gradientTo

    // HSV representation for the active stop
    val initialHsv = remember(currentStopColorInt, activeGradientStop) {
        val hsv = FloatArray(3)
        AndroidColor.colorToHSV(currentStopColorInt, hsv)
        hsv
    }

    var hue by remember(currentStopColorInt, activeGradientStop) { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember(currentStopColorInt, activeGradientStop) { mutableFloatStateOf(initialHsv[1]) }
    var value by remember(currentStopColorInt, activeGradientStop) { mutableFloatStateOf(initialHsv[2]) }

    var hexInput by remember(currentStopColorInt, activeGradientStop) {
        val hex = String.format("%06X", 0xFFFFFF and currentStopColorInt)
        mutableStateOf("#$hex")
    }

    fun applyStopColor(colorInt: Int) {
        if (activeGradientStop == 0) {
            onStyleChange(style.copy(fgColor = colorInt))
        } else {
            // If currently Solid, auto-activate Diagonal gradient
            if (style.gradientType == GradientType.None) {
                onStyleChange(style.copy(gradientTo = colorInt, gradientType = GradientType.Diagonal))
            } else {
                onStyleChange(style.copy(gradientTo = colorInt))
            }
        }
    }

    fun updateHsv(h: Float, s: Float, v: Float) {
        hue = h
        saturation = s
        value = v
        val colorInt = AndroidColor.HSVToColor(floatArrayOf(h, s, v))
        hexInput = String.format("#%06X", 0xFFFFFF and colorInt)
        applyStopColor(colorInt)
    }

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

    val quickSwatches = listOf(
        0xFF00F0FF.toInt(), // Cyan
        0xFF7000FF.toInt(), // Violet
        0xFFEC4899.toInt(), // Pink
        0xFFF97316.toInt(), // Orange
        0xFF10B981.toInt(), // Emerald
        0xFFF59E0B.toInt(), // Amber
        0xFF8B5CF6.toInt(), // Purple
        0xFF0284C7.toInt(), // Cobalt
        0xFFEF4444.toInt(), // Red
        0xFF84CC16.toInt(), // Lime
        0xFF14B8A6.toInt(), // Teal
        0xFF000000.toInt(), // Black
        0xFF1E293B.toInt(), // Slate
        0xFFFFFFFF.toInt(), // White
        0xFFD946EF.toInt(), // Fuchsia
        0xFF6366F1.toInt()  // Indigo
    )

    val curatedGradientPresets = listOf(
        Triple("Neon Cyber", Pair(0xFF00F0FF.toInt(), 0xFF7000FF.toInt()), GradientType.Diagonal),
        Triple("Sunset Flame", Pair(0xFFF97316.toInt(), 0xFFEC4899.toInt()), GradientType.Diagonal),
        Triple("Obsidian Gold", Pair(0xFF1E293B.toInt(), 0xFFF59E0B.toInt()), GradientType.Diagonal),
        Triple("Emerald Aurora", Pair(0xFF064E3B.toInt(), 0xFF10B981.toInt()), GradientType.Linear),
        Triple("Electric Violet", Pair(0xFF4F46E5.toInt(), 0xFFC084FC.toInt()), GradientType.Vertical),
        Triple("Ocean Horizon", Pair(0xFF0284C7.toInt(), 0xFF00F0FF.toInt()), GradientType.Linear),
        Triple("Cherry Crimson", Pair(0xFF831843.toInt(), 0xFFF43F5E.toInt()), GradientType.Diagonal),
        Triple("Solar Fire", Pair(0xFFDC2626.toInt(), 0xFFF59E0B.toInt()), GradientType.Linear),
        Triple("Deep Nebula", Pair(0xFF1E3A8A.toInt(), 0xFFA855F7.toInt()), GradientType.Radial),
        Triple("Pure Steel", Pair(0xFF000000.toInt(), 0xFF475569.toInt()), GradientType.Diagonal)
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
        color = CardDark
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Color Gradient Studio",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Status Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isGradientActive) ElectricCyan.copy(alpha = 0.18f) else SurfaceDark)
                        .border(1.dp, if (isGradientActive) ElectricCyan else CardBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isGradientActive) ElectricCyan else TextMuted)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (isGradientActive) style.gradientType.label else "Solid (Off)",
                        color = if (isGradientActive) ElectricCyan else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Gradient Type Selectors (5 buttons)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GradientType.values().forEach { gt ->
                    val isSelected = style.gradientType == gt
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) ElectricCyan.copy(alpha = 0.2f) else SurfaceDark)
                            .border(1.dp, if (isSelected) ElectricCyan else CardBorder, RoundedCornerShape(10.dp))
                            .clickable {
                                if (gt == GradientType.None) {
                                    onStyleChange(style.copy(gradientType = GradientType.None))
                                } else {
                                    // If gradientTo is same as fgColor or was none, set a vibrant default
                                    val safeGradientTo = if (style.gradientTo == style.fgColor || style.gradientType == GradientType.None) {
                                        if (style.fgColor == 0xFF00F0FF.toInt()) 0xFF7000FF.toInt() else 0xFF00F0FF.toInt()
                                    } else {
                                        style.gradientTo
                                    }
                                    onStyleChange(style.copy(gradientType = gt, gradientTo = safeGradientTo))
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (gt) {
                                GradientType.None -> "Solid"
                                GradientType.Linear -> "Horizontal"
                                GradientType.Vertical -> "Vertical"
                                GradientType.Diagonal -> "Diagonal"
                                GradientType.Radial -> "Radial"
                            },
                            color = if (isSelected) ElectricCyan else TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Live Visual Gradient Preview Strip
            val previewBrush = remember(style.fgColor, style.gradientTo, style.gradientType) {
                val start = Color(style.fgColor)
                val end = Color(if (style.gradientType == GradientType.None) style.fgColor else style.gradientTo)
                when (style.gradientType) {
                    GradientType.Radial -> Brush.radialGradient(listOf(start, end))
                    GradientType.Vertical -> Brush.verticalGradient(listOf(start, end))
                    else -> Brush.horizontalGradient(listOf(start, end))
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(previewBrush)
                    .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Start: #${String.format("%06X", 0xFFFFFF and style.fgColor)}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isGradientActive) "→ Flow →" else "Solid Fill",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "End: #${String.format("%06X", 0xFFFFFF and style.gradientTo)}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Dual Stop Cards (Start Color vs End Color) + Swap Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Start Stop Card
                val isStartActive = activeGradientStop == 0
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isStartActive) ElectricCyan.copy(alpha = 0.15f) else SurfaceDark)
                        .border(if (isStartActive) 2.dp else 1.dp, if (isStartActive) ElectricCyan else CardBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            activeGradientStop = 0
                            onColorTargetChange(ColorTarget.Foreground)
                        },
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color(style.fgColor))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Start Color", color = TextMuted, fontSize = 10.sp)
                            Text(
                                text = "#${String.format("%06X", 0xFFFFFF and style.fgColor)}",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Swap Button
                IconButton(
                    onClick = {
                        val newFg = style.gradientTo
                        val newGrad = style.fgColor
                        onStyleChange(style.copy(fgColor = newFg, gradientTo = newGrad))
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SurfaceDark)
                        .border(1.dp, CardBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Swap Colors",
                        tint = ElectricCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // End Stop Card
                val isEndActive = activeGradientStop == 1
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isEndActive) ElectricCyan.copy(alpha = 0.15f) else SurfaceDark)
                        .border(if (isEndActive) 2.dp else 1.dp, if (isEndActive) ElectricCyan else CardBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            activeGradientStop = 1
                            onColorTargetChange(ColorTarget.GradientEnd)
                        },
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color(style.gradientTo))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("End Color", color = TextMuted, fontSize = 10.sp)
                            Text(
                                text = "#${String.format("%06X", 0xFFFFFF and style.gradientTo)}",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Interactive Color Picker for the Selected Stop
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceDark)
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "Pick ${if (activeGradientStop == 0) "Start (Dot) Color" else "End (Gradient) Color"}",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Hue Slider
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
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Brush.horizontalGradient(rainbowColors))
                )
                Slider(
                    value = hue,
                    onValueChange = { updateHsv(it, saturation, value) },
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
                    onValueChange = { updateHsv(hue, it, value) },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricCyan,
                        activeTrackColor = ElectricCyan,
                        inactiveTrackColor = CardBorder
                    ),
                    modifier = Modifier.height(28.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Brightness Slider
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
                    onValueChange = { updateHsv(hue, saturation, it) },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricCyan,
                        activeTrackColor = ElectricCyan,
                        inactiveTrackColor = CardBorder
                    ),
                    modifier = Modifier.height(28.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Hex Input & Copy/Paste
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
                                    applyStopColor(parsed)
                                } catch (_: Exception) {}
                            }
                        },
                        label = { Text("Hex Code (#RRGGBB)", fontSize = 10.sp, color = TextMuted) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CardDark,
                            unfocusedContainerColor = CardDark,
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f).height(52.dp)
                    )

                    IconButton(
                        onClick = {
                            val clipText = clipboardManager.getText()?.text?.trim() ?: ""
                            val clean = clipText.removePrefix("#")
                            if (clean.length == 6) {
                                try {
                                    val parsed = AndroidColor.parseColor("#$clean")
                                    hexInput = "#$clean"
                                    applyStopColor(parsed)
                                } catch (_: Exception) {}
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CardDark)
                            .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Paste Hex",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(hexInput))
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CardDark)
                            .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Hex",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Color Swatches Grid
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickSwatches.forEach { col ->
                        val isCurrent = (col and 0xFFFFFF) == (currentStopColorInt and 0xFFFFFF)
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(col))
                                .border(
                                    width = if (isCurrent) 2.dp else 1.dp,
                                    color = if (isCurrent) ElectricCyan else Color.White.copy(alpha = 0.25f),
                                    shape = CircleShape
                                )
                                .clickable {
                                    applyStopColor(col)
                                    hexInput = String.format("#%06X", 0xFFFFFF and col)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCurrent) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = if (Color(col).luminance() > 0.5f) Color.Black else Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. 1-Tap Popular Gradient Presets
            Text(
                text = "1-Tap Popular Gradients",
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                curatedGradientPresets.forEach { (name, colors, gt) ->
                    val (fg, grad) = colors
                    val isSelected = style.fgColor == fg && style.gradientTo == grad && style.gradientType == gt
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onStyleChange(
                                    style.copy(
                                        fgColor = fg,
                                        gradientTo = grad,
                                        gradientType = gt
                                    )
                                )
                            }
                            .border(if (isSelected) 2.dp else 1.dp, if (isSelected) ElectricCyan else CardBorder, RoundedCornerShape(12.dp)),
                        color = SurfaceDark
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(fg))
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(grad))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(name, color = TextPrimary, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
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
