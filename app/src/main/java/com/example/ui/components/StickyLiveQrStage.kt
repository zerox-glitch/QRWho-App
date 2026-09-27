package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qr.engine.QrStyle
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
import kotlinx.coroutines.delay

@Composable
fun StickyLiveQrStage(
    visible: Boolean,
    bitmap: Bitmap?,
    scanResult: ScanCheckResult?,
    style: QrStyle,
    isGenerating: Boolean,
    onScrollToTop: () -> Unit,
    onSaveHistory: () -> Unit,
    onAutoFix: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMinimized by remember { mutableStateOf(false) }
    var justSaved by remember { mutableStateOf(false) }

    LaunchedEffect(justSaved) {
        if (justSaved) {
            delay(2000)
            justSaved = false
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        if (isMinimized) {
            // Minimized Floating QR Badge (Compact circle on top edge)
            Surface(
                modifier = Modifier
                    .shadow(elevation = 12.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .border(2.dp, ElectricCyan, CircleShape)
                    .clickable { isMinimized = false }
                    .testTag("sticky_qr_minimized_badge"),
                color = CardDark
            ) {
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Live Sticky QR Preview",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        CircularProgressIndicator(
                            color = ElectricCyan,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Expand indicator badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan)
                            .border(1.dp, Color.Black, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.UnfoldMore,
                            contentDescription = "Expand Sticky Preview",
                            tint = Color.Black,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        } else {
            // Expanded Sticky Live Preview Card
            Surface(
                modifier = Modifier
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .border(
                        width = 1.5.dp,
                        brush = Brush.horizontalGradient(
                            listOf(ElectricCyan, ElectricCyan.copy(alpha = 0.6f))
                        ),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .testTag("sticky_live_qr_card"),
                color = CardDark.copy(alpha = 0.96f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Live QR Code Image Thumbnail (Clickable to jump to full stage)
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(style.bgColor))
                            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                            .clickable(onClick = onScrollToTop)
                            .testTag("sticky_qr_thumbnail"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Sticky Live QR Code",
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        }

                        if (isGenerating) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.35f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = ElectricCyan,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Info & Scannability Column
                    Column(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Live Preview",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Scannability Score Badge
                            if (scanResult != null) {
                                val isScannable = scanResult.isScannable && scanResult.score >= 80
                                val badgeColor = if (isScannable) EmeraldGreen else if (scanResult.score >= 50) AmberWarning else BeaconRose
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(badgeColor.copy(alpha = 0.20f))
                                        .border(1.dp, badgeColor.copy(alpha = 0.40f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "${scanResult.score}%",
                                        color = badgeColor,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = "Updates live as you design",
                            color = TextMuted,
                            fontSize = 10.sp,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Quick Action Buttons (Save & Scroll to Top)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Quick Save
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (justSaved) EmeraldGreen.copy(alpha = 0.2f) else SurfaceDark)
                                    .border(1.dp, if (justSaved) EmeraldGreen else CardBorder, RoundedCornerShape(6.dp))
                                    .clickable {
                                        onSaveHistory()
                                        justSaved = true
                                    }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                                    .testTag("sticky_quick_save_btn"),
                                color = Color.Transparent
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (justSaved) Icons.Default.Check else Icons.Default.BookmarkAdd,
                                        contentDescription = "Save",
                                        tint = if (justSaved) EmeraldGreen else ElectricCyan,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (justSaved) "Saved!" else "Save",
                                        color = if (justSaved) EmeraldGreen else ElectricCyan,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Quick Jump to Top Stage
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceDark)
                                    .border(1.dp, CardBorder, RoundedCornerShape(6.dp))
                                    .clickable(onClick = onScrollToTop)
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                                    .testTag("sticky_jump_top_btn"),
                                color = Color.Transparent
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowUp,
                                        contentDescription = "Jump to Top",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "Top Stage",
                                        color = TextSecondary,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Minimize Pill Button
                    IconButton(
                        onClick = { isMinimized = true },
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(SurfaceDark)
                            .testTag("sticky_minimize_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.UnfoldLess,
                            contentDescription = "Minimize Preview",
                            tint = TextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}
