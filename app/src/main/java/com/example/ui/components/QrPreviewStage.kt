package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qr.engine.ScanCheckResult
import com.example.ui.theme.AmberWarning
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
fun QrPreviewStage(
    bitmap: Bitmap?,
    scanResult: ScanCheckResult?,
    isGenerating: Boolean,
    onAutoFix: () -> Unit,
    onExportPng: (Int) -> Unit,
    onExportSvg: () -> Unit,
    onCopySvg: () -> Unit,
    onShare: () -> Unit,
    onSaveHistory: () -> Unit,
    payloadText: String,
    modifier: Modifier = Modifier,
    photoBitmap: Bitmap? = null,
    onRemovePhoto: (() -> Unit)? = null,
    onSaveCustomPreset: (() -> Unit)? = null
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var showResolutionPicker by remember { mutableStateOf(false) }
    var selectedRes by remember { mutableStateOf(2048) }
    var justSavedHistory by remember { mutableStateOf(false) }
    var showKofiModal by remember { mutableStateOf(false) }

    LaunchedEffect(justSavedHistory) {
        if (justSavedHistory) {
            delay(2500)
            justSavedHistory = false
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(24.dp)),
        color = CardDark,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Scannability Status Bar - ALWAYS VISIBLE
            if (scanResult != null) {
                val isScannable = scanResult.isScannable
                val isHigh = scanResult.score >= 80
                val badgeColor = if (isHigh) EmeraldGreen else if (isScannable) AmberWarning else BeaconRose

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(badgeColor.copy(alpha = 0.12f))
                        .border(1.dp, badgeColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isHigh) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = "Scannability",
                            tint = badgeColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isHigh) "Verified Scannable (${scanResult.score}%)" else "Scan At Risk (${scanResult.score}%)",
                                color = badgeColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                            Text(
                                text = scanResult.feedback,
                                color = TextSecondary,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Fix Scan Button - ALWAYS prominently displayed
                    Button(
                        onClick = onAutoFix,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = Color(0xFF0C0C0B)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .defaultMinSize(minWidth = 1.dp, minHeight = 32.dp)
                            .testTag("fix_scan_button")
                    ) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (scanResult.score < 90) "Fix scan" else "Optimize",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ElectricCyan.copy(alpha = 0.10f))
                        .border(1.dp, ElectricCyan.copy(alpha = 0.30f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = ElectricCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Checking camera scannability...", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Evaluating contrast & lattice", color = TextSecondary, fontSize = 10.sp)
                        }
                    }

                    Button(
                        onClick = onAutoFix,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = Color(0xFF0C0C0B)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .defaultMinSize(minWidth = 1.dp, minHeight = 32.dp)
                            .testTag("fix_scan_button")
                    ) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Fix scan", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // The Rendered QR Code Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF0A0A0A))
                    .border(1.dp, CardBorder, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Active QR Artwork",
                        modifier = Modifier
                            .fillMaxWidth(0.94f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .testTag("qr_preview_image")
                    )
                } else {
                    CircularProgressIndicator(color = ElectricCyan)
                }
            }

            // Remove photo pill button under QR preview if a photo is woven in
            if (photoBitmap != null && onRemovePhoto != null) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onRemovePhoto,
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("stage_remove_photo_button"),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BeaconRose.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BeaconRose),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp), tint = BeaconRose)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Remove photo", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BeaconRose)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pro Export Suite Header Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PRO EXPORT SUITE",
                    color = ElectricCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    softWrap = false
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "512px–4096px · Vector SVG",
                    color = TextMuted,
                    fontSize = 10.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Export Actions: Row 1 (Primary Exports: Multi-Res PNG + Vector SVG)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showResolutionPicker = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("download_png_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Color(0xFF0C0C0B)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${selectedRes}px PNG", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                }

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onExportSvg()
                            showKofiModal = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("export_svg_button"),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Vector SVG", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = {
                            onCopySvg()
                            showKofiModal = true
                        },
                        modifier = Modifier
                            .width(44.dp)
                            .height(44.dp)
                            .testTag("copy_svg_button"),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy SVG Markup", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Export Actions: Row 2 (Secondary Actions: Share Artwork + Save Preset + Save Design)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("share_button"),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share", color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 11.sp, maxLines = 1)
                }

                if (onSaveCustomPreset != null) {
                    OutlinedButton(
                        onClick = onSaveCustomPreset,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(40.dp)
                            .testTag("save_custom_preset_stage_button"),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = EmeraldGreen.copy(alpha = 0.10f)),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Icon(Icons.Default.BookmarkAdd, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save as Preset", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                    }
                }

                OutlinedButton(
                    onClick = {
                        onSaveHistory()
                        justSavedHistory = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("save_history_button"),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (justSavedHistory) EmeraldGreen else CardBorder
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = if (justSavedHistory) {
                        ButtonDefaults.outlinedButtonColors(containerColor = EmeraldGreen.copy(alpha = 0.15f))
                    } else {
                        ButtonDefaults.outlinedButtonColors()
                    },
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Icon(
                        imageVector = if (justSavedHistory) Icons.Default.Check else Icons.Default.BookmarkAdd,
                        contentDescription = null,
                        tint = if (justSavedHistory) EmeraldGreen else TextPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (justSavedHistory) "Saved!" else "Save Design",
                        color = if (justSavedHistory) EmeraldGreen else TextPrimary,
                        fontWeight = if (justSavedHistory) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Copy Payload Text Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceDark)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = payloadText,
                    color = TextMuted,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(payloadText))
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextSecondary, modifier = Modifier.size(14.dp))
                }
            }
        }
    }

    if (showResolutionPicker) {
        AlertDialog(
            onDismissRequest = { showResolutionPicker = false },
            title = {
                Text("Select PNG Export Resolution", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Choose the image output dimensions for your artwork:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    listOf(
                        Triple(512, "512 × 512 px (Compact)", "Messaging & Social Avatars"),
                        Triple(1024, "1024 × 1024 px", "Web & Digital Displays"),
                        Triple(2048, "2048 × 2048 px (HD)", "Flyers, Menus & Displays (Recommended)"),
                        Triple(4096, "4096 × 4096 px (Ultra)", "300 DPI High-Res Posters & Print")
                    ).forEach { (res, label, desc) ->
                        val isSelected = selectedRes == res
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, if (isSelected) ElectricCyan else CardBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedRes = res
                                    onExportPng(res)
                                    showResolutionPicker = false
                                    showKofiModal = true
                                },
                            color = if (isSelected) ElectricCyan.copy(alpha = 0.15f) else SurfaceDark
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = label,
                                        color = if (isSelected) ElectricCyan else TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(text = desc, color = TextMuted, fontSize = 11.sp)
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showResolutionPicker = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = CardDark
        )
    }

    if (showKofiModal) {
        KofiDownloadPopup(onDismiss = { showKofiModal = false })
    }
}
