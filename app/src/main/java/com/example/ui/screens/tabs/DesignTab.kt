package com.example.ui.screens.tabs

import android.graphics.Bitmap
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qr.engine.BuiltInLogos
import com.example.qr.engine.EyeShape
import com.example.qr.engine.FrameStyle
import com.example.qr.engine.GradientType
import com.example.qr.engine.ModuleShape
import com.example.qr.engine.QrStyle
import com.example.ui.components.CustomColorSection
import com.example.ui.components.EyeBallVisualTile
import com.example.ui.components.EyeShapeVisualTile
import com.example.ui.components.ModuleShapeVisualTile
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
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

        // 6. Color Gradient Flow
        Column {
            Text("Color Gradient Type", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GradientType.values().forEach { gt ->
                    val isSelected = style.gradientType == gt
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) ElectricCyan.copy(alpha = 0.2f) else CardDark)
                            .border(1.dp, if (isSelected) ElectricCyan else CardBorder, RoundedCornerShape(10.dp))
                            .clickable { onStyleChange(style.copy(gradientType = gt)) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (gt) {
                                GradientType.None -> "Solid"
                                GradientType.Linear -> "Linear"
                                GradientType.Radial -> "Radial"
                                GradientType.Diagonal -> "Diagonal"
                            },
                            color = if (isSelected) ElectricCyan else TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

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
