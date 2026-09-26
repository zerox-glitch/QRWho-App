package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    onRemovePhoto: (() -> Unit)? = null
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

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

            // Export Actions: Row 1 (Primary Exports: 2048px PNG + Vector SVG)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onExportPng(2048) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("download_png_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Color(0xFF0C0C0B)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("2048px PNG", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                }

                OutlinedButton(
                    onClick = onExportSvg,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("export_svg_button"),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Code, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Vector SVG", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Export Actions: Row 2 (Secondary Actions: Share Artwork + Save Design)
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
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Artwork", color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 11.sp, maxLines = 1)
                }

                OutlinedButton(
                    onClick = onSaveHistory,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("save_history_button"),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.BookmarkAdd, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Design", color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 11.sp, maxLines = 1)
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
}
