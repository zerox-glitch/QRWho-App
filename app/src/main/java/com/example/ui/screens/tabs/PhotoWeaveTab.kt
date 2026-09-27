package com.example.ui.screens.tabs

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qr.engine.ImageMode
import com.example.qr.engine.PhotoKernel
import com.example.qr.engine.QrStyle
import com.example.qr.engine.SamplePhotos
import com.example.qr.engine.WeavePresets
import com.example.ui.theme.BeaconRose
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PhotoWeaveTab(
    style: QrStyle,
    photoBitmap: Bitmap?,
    onStyleChange: (QrStyle) -> Unit,
    onPhotoSelected: (Bitmap?) -> Unit,
    onSampleSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    logoBitmap: Bitmap? = null,
    onCustomLogoSelected: (Bitmap?) -> Unit = {},
    onBuiltInLogoSelected: (String?) -> Unit = {}
) {
    val context = LocalContext.current
    var showAdvancedDetails by remember { mutableStateOf(false) }

    // Android zero-permission Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val src = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(src) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                onPhotoSelected(bitmap)
                onStyleChange(style.copy(imageMode = ImageMode.Clean))
            } catch (_: Exception) {}
        }
    }

    // Logo Picker
    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val src = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(src) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                onCustomLogoSelected(bitmap)
            } catch (_: Exception) {}
        }
    }

    val isPictured = photoBitmap != null && style.imageMode != ImageMode.None && style.imageMode != ImageMode.Logo

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Upload a picture Card / Button
        Column {
            Text("Upload a picture", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, if (photoBitmap != null) ElectricCyan else CardBorder, RoundedCornerShape(16.dp))
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                    .testTag("upload_photo_card"),
                color = SurfaceDark
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (photoBitmap != null) {
                        Image(
                            bitmap = photoBitmap.asImageBitmap(),
                            contentDescription = "Current Photo",
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CardDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(28.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (photoBitmap != null) "Change Photo" else "Drop a picture or browse",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Woven into the QR on this device — no server upload, no AI models.",
                            color = TextMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // Remove picture button if active
            if (photoBitmap != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = {
                            onPhotoSelected(null)
                            onStyleChange(style.copy(imageMode = ImageMode.None))
                        },
                        border = androidx.compose.foundation.BorderStroke(1.dp, BeaconRose.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("remove_picture_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = BeaconRose, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Remove picture", color = BeaconRose, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Or try a sample (12 Curated Authentic Photos)
        Column {
            Text("Or try a sample", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SamplePhotos.list.forEach { sample ->
                    val sampleThumb = remember(sample.id) {
                        SamplePhotos.getThumbnail(context, sample.id, 160)
                    }
                    Card(
                        modifier = Modifier
                            .width(104.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                onSampleSelected(sample.id)
                                onStyleChange(style.copy(imageMode = ImageMode.Clean))
                            }
                            .testTag("sample_photo_${sample.id}"),
                        colors = CardDefaults.cardColors(containerColor = CardDark)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = sampleThumb.asImageBitmap(),
                                    contentDescription = sample.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = sample.name,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // 3. Center Logo (Custom PNG Logo Upload)
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Center Logo (PNG)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                if (logoBitmap != null) {
                    Text(
                        text = "Remove logo ✕",
                        color = BeaconRose,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                onCustomLogoSelected(null)
                                onBuiltInLogoSelected(null)
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Option 1: None Tile
                val isNoneSelected = logoBitmap == null
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isNoneSelected) ElectricCyan.copy(alpha = 0.12f) else CardDark)
                        .clickable {
                            onCustomLogoSelected(null)
                            onBuiltInLogoSelected(null)
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = "No Logo",
                            tint = if (isNoneSelected) ElectricCyan else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "No Logo",
                            color = if (isNoneSelected) ElectricCyan else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isNoneSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                // Option 2: Upload Custom Logo Tile
                val isCustomSelected = logoBitmap != null
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isCustomSelected) ElectricCyan.copy(alpha = 0.12f) else CardDark)
                        .clickable {
                            logoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (logoBitmap != null) {
                            Image(
                                bitmap = logoBitmap.asImageBitmap(),
                                contentDescription = "Custom Logo",
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Upload Logo",
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = if (isCustomSelected) "Custom Added" else "+ Upload Logo",
                            color = if (isCustomSelected) ElectricCyan else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = if (isCustomSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            // Logo scale slider if logo is active
            if (logoBitmap != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Center Logo Scale", color = TextSecondary, fontSize = 11.sp)
                    Text("${(style.logoScale * 100).toInt()}%", color = ElectricCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = style.logoScale,
                    onValueChange = { onStyleChange(style.copy(logoScale = it)) },
                    valueRange = 0.12f..0.30f,
                    colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan)
                )
            }
        }

        // 4. Weave Modes
        Column {
            Text("Weave", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            val modes = listOf(
                ImageMode.Paint,
                ImageMode.Clean,
                ImageMode.Mosaic,
                ImageMode.Halftone,
                ImageMode.Duotone,
                ImageMode.Mono,
                ImageMode.None
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                modes.forEach { mode ->
                    val isSelected = style.imageMode == mode
                    val bg = if (isSelected) ElectricCyan else CardDark
                    val textColor = if (isSelected) Color(0xFF0C0C0B) else TextPrimary
                    val border = if (isSelected) ElectricCyan else CardBorder

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(bg)
                            .border(1.dp, border, RoundedCornerShape(10.dp))
                            .clickable { onStyleChange(style.copy(imageMode = mode)) }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.label,
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = style.imageMode.desc,
                color = TextMuted,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }



        // 9. Fine Tuning & Advanced Settings Collapsible Section
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(14.dp)),
            color = CardDark
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAdvancedDetails = !showAdvancedDetails },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Fine Tuning & Geometry Details", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Icon(
                        imageVector = if (showAdvancedDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = TextMuted
                    )
                }

                AnimatedVisibility(
                    visible = showAdvancedDetails,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(modifier = Modifier.padding(top = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Dot Size (dotScale)
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Dot size", color = TextSecondary, fontSize = 11.sp)
                                Text("${(style.dotScale * 100).toInt()}%", color = ElectricCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = style.dotScale,
                                onValueChange = { onStyleChange(style.copy(dotScale = it)) },
                                valueRange = 0.30f..1.0f,
                                colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan)
                            )
                        }

                        // Photo Size / Zoom (photoZoom)
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Photo size / Zoom", color = TextSecondary, fontSize = 11.sp)
                                Text("${(style.photoZoom * 100).toInt()}%", color = ElectricCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = style.photoZoom,
                                onValueChange = { onStyleChange(style.copy(photoZoom = it)) },
                                valueRange = 0.50f..2.0f,
                                colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan)
                            )
                        }

                        // Photo Color / Opacity (imageOpacity)
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Photo color / Inks", color = TextSecondary, fontSize = 11.sp)
                                Text("${(style.imageOpacity * 100).toInt()}%", color = ElectricCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = style.imageOpacity,
                                onValueChange = { onStyleChange(style.copy(imageOpacity = it)) },
                                valueRange = 0.20f..1.0f,
                                colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan)
                            )
                        }

                        // Contrast
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Contrast", color = TextSecondary, fontSize = 11.sp)
                                Text(String.format("%.2fx", style.contrast), color = ElectricCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = style.contrast,
                                onValueChange = { onStyleChange(style.copy(contrast = it)) },
                                valueRange = 0.30f..1.50f,
                                colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan)
                            )
                        }

                        // Quiet zone
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Quiet zone", color = TextSecondary, fontSize = 11.sp)
                                Text("${style.quietZone} modules", color = ElectricCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = style.quietZone.toFloat(),
                                onValueChange = { onStyleChange(style.copy(quietZone = it.toInt())) },
                                valueRange = 2f..6f,
                                steps = 3,
                                colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan)
                            )
                        }

                        // Grid Detail / Min Version
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Grid detail (Min Version)", color = TextSecondary, fontSize = 11.sp)
                                Text("Version ${style.minVersion}", color = ElectricCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = style.minVersion.toFloat(),
                                onValueChange = { onStyleChange(style.copy(minVersion = it.toInt())) },
                                valueRange = 5f..12f,
                                steps = 6,
                                colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan)
                            )
                        }
                    }
                }
            }
        }
    }
}
