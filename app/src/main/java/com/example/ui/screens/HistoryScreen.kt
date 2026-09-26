package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.QrEntity
import com.example.qr.engine.EyeShape
import com.example.qr.engine.FrameStyle
import com.example.qr.engine.GradientType
import com.example.qr.engine.ModuleShape
import com.example.qr.engine.QrGenerator
import com.example.qr.engine.QrStyle
import com.example.ui.StudioViewModel
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: StudioViewModel,
    onNavigateToStudio: () -> Unit,
    onNavigateToScanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    val dateFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var showClearDialog by remember { mutableStateOf(false) }

    val createdList = remember(historyList) { historyList.filter { !it.isScanned } }
    val scannedList = remember(historyList) { historyList.filter { it.isScanned } }

    val filteredList = remember(historyList, selectedFilter, searchQuery) {
        val query = searchQuery.trim().lowercase()
        val base = when (selectedFilter) {
            "Created" -> createdList
            "Scanned" -> scannedList
            else -> historyList
        }
        if (query.isEmpty()) {
            base
        } else {
            base.filter {
                it.title.lowercase().contains(query) ||
                it.encodedText.lowercase().contains(query) ||
                it.payloadKind.lowercase().contains(query)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(com.example.ui.theme.BgDark)
            .padding(16.dp)
    ) {
        // Screen Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ElectricCyan.copy(alpha = 0.15f))
                        .border(1.dp, ElectricCyan.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "History",
                        tint = ElectricCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "QR Vault & History",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${historyList.size} codes saved on device",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            if (historyList.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, BeaconRose.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .clickable { showClearDialog = true }
                        .testTag("history_clear_all_button"),
                    color = BeaconRose.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "Clear Vault",
                        color = BeaconRose,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search title, URL, or content...", color = TextMuted, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear search", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark,
                focusedBorderColor = ElectricCyan,
                unfocusedBorderColor = CardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
                .testTag("history_search_input")
        )

        // Filter Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Triple("All", historyList.size, ElectricCyan),
                Triple("Created", createdList.size, EmeraldGreen),
                Triple("Scanned", scannedList.size, NeonViolet)
            ).forEach { (label, count, accentColor) ->
                val isSelected = selectedFilter == label
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) accentColor.copy(alpha = 0.22f) else CardDark)
                        .border(1.dp, if (isSelected) accentColor else CardBorder, RoundedCornerShape(20.dp))
                        .clickable { selectedFilter = label }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$label ($count)",
                        color = if (isSelected) accentColor else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // Content Area
        if (filteredList.isEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
                color = CardDark
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(SurfaceDark)
                            .border(1.dp, CardBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (selectedFilter) {
                                "Scanned" -> Icons.Default.QrCodeScanner
                                "Created" -> Icons.Default.BookmarkAdd
                                else -> Icons.Default.History
                            },
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = when {
                            searchQuery.isNotEmpty() -> "No matches for \"$searchQuery\""
                            selectedFilter == "Scanned" -> "No camera-scanned QR codes yet"
                            selectedFilter == "Created" -> "No saved studio creations yet"
                            else -> "Your QR History Vault is empty"
                        },
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = when {
                            searchQuery.isNotEmpty() -> "Try searching for a different keyword or clear the search query."
                            selectedFilter == "Scanned" -> "Scan QR codes in restaurants, packages, or books to view and restyle them here."
                            selectedFilter == "Created" -> "Design artistic QR codes in Studio and tap 'Save Design' to save them here!"
                            else -> "Save your custom creations from Studio or scan codes with your camera to access them anytime."
                        },
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Helpful Direct Action Buttons
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = onNavigateToStudio,
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Color(0xFF0C0C0B)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open Studio to Create QR", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onNavigateToScanner,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scan a QR Code with Camera", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        }

                        // Instant test button: saves active studio look right into history
                        TextButton(
                            onClick = {
                                viewModel.saveToHistory()
                            }
                        ) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Current Studio QR to Vault Now", color = EmeraldGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList, key = { it.id }) { item ->
                    HistoryItemCard(
                        item = item,
                        dateFormat = dateFormat,
                        onOpenInStudio = {
                            viewModel.restoreFromHistory(item)
                            onNavigateToStudio()
                        },
                        onShare = {
                            viewModel.shareHistoryItem(context, item)
                        },
                        onDelete = {
                            viewModel.deleteHistoryItem(item.id)
                        }
                    )
                }
            }
        }
    }

    // Clear confirmation dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text("Clear QR History Vault", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Select what you want to remove from your on-device vault:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Button(
                        onClick = {
                            viewModel.clearHistoryByType(true)
                            showClearDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = NeonViolet),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear Only Scanned Codes (${scannedList.size})", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.clearHistoryByType(false)
                            showClearDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = EmeraldGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear Only Created Designs (${createdList.size})", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.clearAllHistory()
                            showClearDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BeaconRose.copy(alpha = 0.15f), contentColor = BeaconRose),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear Entire History (${historyList.size})", fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = CardDark
        )
    }
}

@Composable
fun HistoryItemCard(
    item: QrEntity,
    dateFormat: SimpleDateFormat,
    onOpenInStudio: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    // Generate mini preview bitmap
    val itemThumbnail: Bitmap = remember(item.id, item.encodedText, item.moduleShape, item.fgColor, item.bgColor) {
        try {
            val style = QrStyle(
                moduleShape = try { ModuleShape.valueOf(item.moduleShape) } catch (_: Exception) { ModuleShape.Rounded },
                eyeShape = try { EyeShape.valueOf(item.eyeShape) } catch (_: Exception) { EyeShape.Rounded },
                ballShape = try { EyeShape.valueOf(item.ballShape) } catch (_: Exception) { EyeShape.Circle },
                fgColor = item.fgColor,
                bgColor = item.bgColor,
                eyeColor = item.eyeColor,
                ballColor = item.ballColor,
                gradientType = try { GradientType.valueOf(item.gradientType) } catch (_: Exception) { GradientType.None },
                gradientTo = item.gradientTo,
                frameStyle = try { FrameStyle.valueOf(item.frameStyle) } catch (_: Exception) { FrameStyle.None },
                frameCaption = item.frameCaption,
                quietZone = item.quietZone
            )
            QrGenerator.generateQrBitmap(
                payload = item.encodedText.ifBlank { "QRWHO" },
                qrStyle = style,
                sizePx = 140
            )
        } catch (_: Exception) {
            Bitmap.createBitmap(140, 140, Bitmap.Config.ARGB_8888)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onOpenInStudio)
            .testTag("vault_card_${item.id}"),
        color = CardDark
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Miniature QR Thumbnail
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(item.bgColor))
                        .border(1.dp, CardBorder, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = itemThumbnail.asImageBitmap(),
                        contentDescription = "Thumbnail",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (item.isScanned) NeonViolet.copy(alpha = 0.15f) else EmeraldGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (item.isScanned) "SCANNED" else "CREATED",
                                color = if (item.isScanned) NeonViolet else EmeraldGreen,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(ElectricCyan.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = item.payloadKind,
                                color = ElectricCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (!item.isScanned && item.scanScore > 0) {
                            Text(
                                text = "${item.scanScore}% verified",
                                color = EmeraldGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = dateFormat.format(Date(item.timestamp)),
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Raw Encoded Text Preview
            Text(
                text = item.encodedText,
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(SurfaceDark)
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Card Bottom Actions: Open in Studio + Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onOpenInStudio,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Color(0xFF0C0C0B)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open in Studio", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onShare,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Artwork", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                }
            }
        }
    }
}
