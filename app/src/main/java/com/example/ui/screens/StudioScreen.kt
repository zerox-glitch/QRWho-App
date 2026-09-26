package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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

import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.theme.EmeraldGreen

import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun StudioScreen(
    viewModel: StudioViewModel,
    onNavigateToHistory: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val payload by viewModel.payload.collectAsStateWithLifecycle()
    val style by viewModel.style.collectAsStateWithLifecycle()
    val photoBitmap by viewModel.photoBitmap.collectAsStateWithLifecycle()
    val customLogo by viewModel.customLogo.collectAsStateWithLifecycle()
    val qrBitmap by viewModel.qrBitmap.collectAsStateWithLifecycle()
    val scanResult by viewModel.scanResult.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeStudioTab.collectAsStateWithLifecycle()
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    val customPresets by viewModel.customPresets.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val presetFilterCategory by viewModel.presetFilterCategory.collectAsStateWithLifecycle()
    val historyFilter by viewModel.historyFilter.collectAsStateWithLifecycle()

    var showSavePresetDialog by remember { mutableStateOf(false) }
    var newPresetName by remember { mutableStateOf("") }
    var newPresetDesc by remember { mutableStateOf("") }

    val tabList = listOf(
        Pair("Content", Icons.Default.Tune),
        Pair("Presets", Icons.Default.AutoAwesome),
        Pair("Photo Art", Icons.Default.Image),
        Pair("Design", Icons.Default.FormatPaint),
        Pair("Vault (${historyList.size})", Icons.Default.Bookmark)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // App Header Brand
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.qrwho_logo),
                    contentDescription = "QRWho Logo",
                    modifier = Modifier
                        .size(49.dp)
                        .clip(RoundedCornerShape(11.dp))
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ko-fi.com/qrwho"))
                            context.startActivity(intent)
                        } catch (e: Exception) {}
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = com.example.ui.components.KofiRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(28.dp)
                        .testTag("studio_top_kofi_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalCafe,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Support ☕", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }

                Spacer(modifier = Modifier.width(6.dp))

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
        }

        // Pro Features Quick Access Bar (Always visible for instant discoverability)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Quick 1: My Presets Shortcut Chip
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, EmeraldGreen.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .clickable {
                        viewModel.navigateToMyPresets()
                        coroutineScope.launch { scrollState.animateScrollTo(680) }
                    }
                    .testTag("quick_bar_my_presets"),
                color = EmeraldGreen.copy(alpha = 0.14f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("✨ My Presets (${customPresets.size})", color = EmeraldGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Quick 2: Favorites Shortcut Chip
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, Color(0xFFFFB800).copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .clickable {
                        viewModel.navigateToFavorites()
                        coroutineScope.launch { scrollState.animateScrollTo(680) }
                    }
                    .testTag("quick_bar_favorites"),
                color = Color(0xFFFFB800).copy(alpha = 0.14f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB800), modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("★ Favorites (${favoriteIds.size})", color = Color(0xFFFFB800), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Quick 3: Save Look as Preset Button
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .clickable {
                        newPresetName = "My ${style.moduleShape.label} Preset"
                        newPresetDesc = "Custom QR style"
                        showSavePresetDialog = true
                    }
                    .testTag("quick_bar_save_preset"),
                color = ElectricCyan.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.BookmarkAdd, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("➕ Save Look to Presets", color = ElectricCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Quick 4: Saved / History Shortcut Chip
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                    .clickable {
                        if (onNavigateToHistory != null) {
                            onNavigateToHistory()
                        } else {
                            viewModel.navigateToHistoryCreated()
                            coroutineScope.launch { scrollState.animateScrollTo(680) }
                        }
                    }
                    .testTag("quick_bar_history"),
                color = SurfaceDark
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Bookmark, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Vault (${historyList.size})", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
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
            onSaveCustomPreset = {
                newPresetName = "My ${style.moduleShape.label} Preset"
                newPresetDesc = "Custom QR style"
                showSavePresetDialog = true
            },
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
                        onPresetSelected = { viewModel.selectPreset(it) },
                        customPresets = customPresets,
                        favoriteIds = favoriteIds,
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onDeleteCustomPreset = { viewModel.deleteCustomPreset(it) },
                        onSaveCurrentAsPreset = { name, desc -> viewModel.saveCustomPreset(name, desc) },
                        initialCategory = presetFilterCategory,
                        onCategorySelected = { viewModel.setPresetCategory(it) }
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
                        onStyleChange = { viewModel.updateStyle(it) },
                        customLogo = customLogo,
                        onCustomLogoSelected = { viewModel.setCustomLogo(it) },
                        onSaveCustomPreset = { name, desc -> viewModel.saveCustomPreset(name, desc) }
                    )
                    4 -> HistoryTab(
                        historyList = historyList,
                        onLoadItem = { item -> viewModel.restoreFromHistory(item) },
                        onShareItem = { item -> viewModel.shareHistoryItem(context, item) },
                        onDeleteItem = { viewModel.deleteHistoryItem(it) },
                        onClearAll = { viewModel.clearAllHistory() },
                        onClearByType = { isScanned -> viewModel.clearHistoryByType(isScanned) },
                        initialFilter = historyFilter,
                        onFilterSelected = { viewModel.setHistoryFilter(it) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showSavePresetDialog) {
        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.BookmarkAdd, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save to My Presets", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Save this custom look (shape, colors, gradients, frames) to your personal presets library.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    OutlinedTextField(
                        value = newPresetName,
                        onValueChange = { newPresetName = it },
                        label = { Text("Preset Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = ElectricCyan
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newPresetDesc,
                        onValueChange = { newPresetDesc = it },
                        label = { Text("Description (Optional)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = ElectricCyan
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = newPresetName.ifBlank { "My ${style.moduleShape.label} Style" }
                        val desc = newPresetDesc.ifBlank { "Custom QR style" }
                        viewModel.saveCustomPreset(name, desc)
                        showSavePresetDialog = false
                        newPresetName = ""
                        newPresetDesc = ""
                        coroutineScope.launch { scrollState.animateScrollTo(680) }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan,
                        contentColor = Color(0xFF0C0C0B)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Preset", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePresetDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = CardDark
        )
    }
}
