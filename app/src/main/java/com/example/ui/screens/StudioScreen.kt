package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.StudioViewModel
import com.example.ui.components.QrPreviewStage
import com.example.ui.screens.tabs.ContentTab
import com.example.ui.screens.tabs.DesignTab
import com.example.ui.screens.tabs.HistoryTab
import com.example.ui.screens.tabs.PhotoWeaveTab
import com.example.ui.screens.tabs.PresetsTab
import com.example.ui.theme.BgDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun StudioScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val payload by viewModel.payload.collectAsStateWithLifecycle()
    val style by viewModel.style.collectAsStateWithLifecycle()
    val photoBitmap by viewModel.photoBitmap.collectAsStateWithLifecycle()
    val customLogo by viewModel.customLogo.collectAsStateWithLifecycle()
    val qrBitmap by viewModel.qrBitmap.collectAsStateWithLifecycle()
    val scanResult by viewModel.scanResult.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeStudioTab.collectAsStateWithLifecycle()
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()

    val tabList = listOf(
        Pair("Content", Icons.Default.Tune),
        Pair("Presets", Icons.Default.AutoAwesome),
        Pair("Photo Art", Icons.Default.Image),
        Pair("Design", Icons.Default.FormatPaint),
        Pair("Saved (${historyList.size})", Icons.Default.Bookmark)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // App Header Brand
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.qrwho_logo),
                    contentDescription = "QRWho Logo",
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "QRWho Studio",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic
                    )
                    com.example.ui.components.RotatingTagline(
                        prefix = "QR codes that ",
                        words = listOf("actually scan", "pop", "stand out", "convert", "inspire", "dazzle"),
                        fontSize = 10.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(ElectricCyan.copy(alpha = 0.12f))
                    .border(1.dp, ElectricCyan.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "LEVEL H",
                    color = ElectricCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Live QR Preview Stage & Verification Meter
        QrPreviewStage(
            bitmap = qrBitmap,
            scanResult = scanResult,
            isGenerating = isGenerating,
            onAutoFix = { viewModel.autoFixScan() },
            onExportPng = { resPx -> viewModel.exportPng(context, resPx) },
            onExportSvg = { viewModel.exportSvg(context) },
            onCopySvg = {
                val svg = viewModel.getSvgString(1024)
                val cm = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                cm.setPrimaryClip(android.content.ClipData.newPlainText("QR SVG", svg))
            },
            onShare = { viewModel.shareQrCode(context) },
            onSaveHistory = { viewModel.saveToHistory() },
            payloadText = payload.toEncodedText(),
            photoBitmap = photoBitmap,
            onRemovePhoto = { viewModel.setPhoto(null) },
            modifier = Modifier.padding(bottom = 18.dp)
        )

        // Studio Navigation Tab Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabList.forEachIndexed { index, pair ->
                val isSelected = activeTab == index
                val bg = if (isSelected) ElectricCyan else SurfaceDark
                val textColor = if (isSelected) Color(0xFF0C0C0B) else TextSecondary
                val iconColor = if (isSelected) Color(0xFF0C0C0B) else TextMuted
                val borderColor = if (isSelected) ElectricCyan else CardBorder

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                        .clickable { viewModel.setStudioTab(index) }
                        .testTag("studio_tab_$index"),
                    color = bg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = pair.second,
                            contentDescription = pair.first,
                            tint = iconColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = pair.first,
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Active Tab Screen Content
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(20.dp)),
            color = CardDark
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                when (activeTab) {
                    0 -> ContentTab(
                        payload = payload,
                        onPayloadChange = { viewModel.updatePayload(it) }
                    )
                    1 -> PresetsTab(
                        currentStyle = style,
                        onPresetSelected = { viewModel.selectPreset(it) }
                    )
                    2 -> PhotoWeaveTab(
                        style = style,
                        photoBitmap = photoBitmap,
                        onStyleChange = { viewModel.updateStyle(it) },
                        onPhotoSelected = { viewModel.setPhoto(it) },
                        onSampleSelected = { viewModel.selectSamplePhoto(it) },
                        logoBitmap = customLogo,
                        onCustomLogoSelected = { viewModel.setCustomLogo(it) },
                        onBuiltInLogoSelected = { viewModel.selectBuiltInLogo(it) }
                    )
                    3 -> DesignTab(
                        style = style,
                        onStyleChange = { viewModel.updateStyle(it) }
                    )
                    4 -> HistoryTab(
                        historyList = historyList,
                        onLoadItem = { item ->
                            viewModel.updatePayload(payload.copy(kind = com.example.qr.engine.PayloadKind.TEXT, text = item.encodedText))
                        },
                        onDeleteItem = { viewModel.deleteHistoryItem(it) },
                        onClearAll = { viewModel.clearAllHistory() }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
