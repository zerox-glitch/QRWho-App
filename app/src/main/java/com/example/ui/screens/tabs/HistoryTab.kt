package com.example.ui.screens.tabs

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.QrEntity
import com.example.ui.theme.BeaconRose
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryTab(
    historyList: List<QrEntity>,
    onLoadItem: (QrEntity) -> Unit,
    onShareItem: (QrEntity) -> Unit = {},
    onDeleteItem: (Long) -> Unit,
    onClearAll: () -> Unit,
    onClearByType: (Boolean) -> Unit = {},
    initialFilter: String = "All",
    onFilterSelected: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    var selectedFilter by remember { mutableStateOf(initialFilter) }
    var showClearDialog by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(initialFilter) {
        if (initialFilter.isNotEmpty()) {
            selectedFilter = initialFilter
        }
    }

    val createdList = remember(historyList) { historyList.filter { !it.isScanned } }
    val scannedList = remember(historyList) { historyList.filter { it.isScanned } }

    val filteredList = remember(historyList, selectedFilter) {
        when (selectedFilter) {
            "Created" -> createdList
            "Scanned" -> scannedList
            else -> historyList
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Top Filter Bar & Clear Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Filter Tabs
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    Pair("All", historyList.size),
                    Pair("Created", createdList.size),
                    Pair("Scanned", scannedList.size)
                ).forEach { (label, count) ->
                    val isSelected = selectedFilter == label
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) ElectricCyan.copy(alpha = 0.2f) else CardDark)
                            .border(1.dp, if (isSelected) ElectricCyan else CardBorder, RoundedCornerShape(16.dp))
                            .clickable { selectedFilter = label }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$label ($count)",
                            color = if (isSelected) ElectricCyan else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            if (historyList.isNotEmpty()) {
                Text(
                    text = "Clear...",
                    color = BeaconRose,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { showClearDialog = true }
                        .padding(start = 8.dp)
                        .testTag("clear_history_button")
                )
            }
        }

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (selectedFilter == "Scanned") Icons.Default.QrCodeScanner else Icons.Default.History,
                        contentDescription = "No history",
                        tint = TextMuted,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = when (selectedFilter) {
                            "Scanned" -> "No scanned QR codes yet"
                            "Created" -> "No saved creations yet"
                            else -> "No QR history yet"
                        },
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (selectedFilter) {
                            "Scanned" -> "Scan camera or gallery QR codes to access them here anytime."
                            "Created" -> "Tap 'Save Design' on the stage to save your custom creations!"
                            else -> "Create custom artistic QR codes or scan external codes to build your history."
                        },
                        color = TextMuted,
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredList.forEach { item ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                            .clickable { onLoadItem(item) }
                            .testTag("history_item_${item.id}"),
                        color = CardDark
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Swatch or Scanner Icon
                            if (item.isScanned) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(ElectricCyan.copy(alpha = 0.15f))
                                        .border(1.dp, ElectricCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = "Scanned QR",
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(item.bgColor))
                                        .border(1.dp, CardBorder, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(Color(item.fgColor))
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (item.isScanned) ElectricCyan.copy(alpha = 0.12f) else EmeraldGreen.copy(alpha = 0.15f))
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (item.isScanned) "SCANNED" else "CREATED",
                                            color = if (item.isScanned) ElectricCyan else EmeraldGreen,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Text(
                                        text = item.payloadKind,
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    if (!item.isScanned && item.scanScore > 0) {
                                        Text("•", color = TextMuted, fontSize = 9.sp)
                                        Text(
                                            text = "${item.scanScore}% verified",
                                            color = EmeraldGreen,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Text("•", color = TextMuted, fontSize = 9.sp)
                                    Text(
                                        text = dateFormat.format(Date(item.timestamp)),
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            // Quick Action Buttons
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onShareItem(item) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { onDeleteItem(item.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Clear Options Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text("Clear History", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Choose which items to remove from your local history:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Button(
                        onClick = {
                            onClearByType(true)
                            showClearDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = ElectricCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear Scanned QR History (${scannedList.size} items)", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            onClearByType(false)
                            showClearDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = EmeraldGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear Saved Creations (${createdList.size} items)", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            onClearAll()
                            showClearDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BeaconRose.copy(alpha = 0.2f), contentColor = BeaconRose),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear All History (${historyList.size} items)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
