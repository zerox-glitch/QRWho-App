package com.example.ui.screens.tabs

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CustomPresetEntity
import com.example.qr.engine.QrGenerator
import com.example.qr.engine.QrPreset
import com.example.qr.engine.QrPresets
import com.example.qr.engine.QrStyle
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
fun PresetsTab(
    currentStyle: QrStyle,
    onPresetSelected: (QrPreset) -> Unit,
    customPresets: List<CustomPresetEntity> = emptyList(),
    favoriteIds: Set<String> = emptySet(),
    onToggleFavorite: (String) -> Unit = {},
    onDeleteCustomPreset: (Long) -> Unit = {},
    onSaveCurrentAsPreset: (String, String) -> Unit = { _, _ -> },
    initialCategory: String = "All",
    onCategorySelected: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var displayCount by remember { mutableIntStateOf(30) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var newPresetName by remember { mutableStateOf("") }
    var newPresetDesc by remember { mutableStateOf("") }

    androidx.compose.runtime.LaunchedEffect(initialCategory) {
        if (initialCategory.isNotEmpty()) {
            selectedCategory = initialCategory
        }
    }

    val categories = remember(favoriteIds.size, customPresets.size) {
        listOf(
            "All",
            "★ Favorites (${favoriteIds.size})",
            "✨ My Presets (${customPresets.size})"
        ) + QrPresets.categories.filter { it != "All" }
    }

    val customPresetObjects = remember(customPresets) {
        customPresets.map { it.toQrPreset() }
    }

    val filteredList = remember(searchQuery, selectedCategory, customPresetObjects, favoriteIds, QrPresets.list.size) {
        displayCount = 30
        val query = searchQuery.trim().lowercase()
        when {
            selectedCategory.startsWith("★ Favorites") -> {
                val allPossibilities = customPresetObjects + QrPresets.list
                allPossibilities.filter { favoriteIds.contains(it.id) && (query.isEmpty() || it.name.lowercase().contains(query) || it.category.lowercase().contains(query)) }
            }
            selectedCategory.startsWith("✨ My Presets") -> {
                if (query.isEmpty()) customPresetObjects else customPresetObjects.filter { it.name.lowercase().contains(query) || it.description.lowercase().contains(query) }
            }
            selectedCategory == "All" -> {
                val combined = customPresetObjects + QrPresets.list
                if (query.isEmpty()) combined else combined.filter { it.name.lowercase().contains(query) || it.category.lowercase().contains(query) || it.description.lowercase().contains(query) }
            }
            else -> {
                QrPresets.filter(searchQuery, selectedCategory)
            }
        }
    }

    val visibleList = remember(filteredList, displayCount) {
        filteredList.take(displayCount)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search Input Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search 335+ artistic presets...", color = TextMuted, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
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
                .testTag("preset_search_input")
        )

        // Category Horizontal Scroll Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isFavChip = cat.startsWith("★ Favorites")
                val isCustomChip = cat.startsWith("✨ My Presets")
                val isSelected = if (isFavChip) selectedCategory.startsWith("★ Favorites")
                    else if (isCustomChip) selectedCategory.startsWith("✨ My Presets")
                    else selectedCategory == cat

                val accentColor = when {
                    isFavChip -> Color(0xFFFFB800)
                    isCustomChip -> EmeraldGreen
                    else -> ElectricCyan
                }

                val bg = if (isSelected) accentColor.copy(alpha = 0.22f) else CardDark
                val border = if (isSelected) accentColor else if (isFavChip || isCustomChip) accentColor.copy(alpha = 0.45f) else CardBorder

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(bg)
                        .border(1.dp, border, RoundedCornerShape(20.dp))
                        .clickable {
                            selectedCategory = cat
                            onCategorySelected(cat)
                        }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) accentColor else if (isFavChip || isCustomChip) accentColor.copy(alpha = 0.85f) else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected || isFavChip || isCustomChip) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // Action banner: Quick Save Current Style as Preset
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, ElectricCyan.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
            color = SurfaceDark
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Swatch of current style
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(currentStyle.bgColor))
                            .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color(currentStyle.fgColor))
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Love your current look?",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Save this custom style to 'My Presets'",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                Button(
                    onClick = {
                        newPresetName = "My ${currentStyle.moduleShape.label} Style"
                        newPresetDesc = "Custom QR design"
                        showSaveDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan,
                        contentColor = Color(0xFF0C0C0B)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("save_as_preset_button")
                ) {
                    Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save Preset", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Status Count Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Showing ${visibleList.size} of ${filteredList.size} styles",
                color = TextMuted,
                fontSize = 11.sp
            )
            if (filteredList.isNotEmpty()) {
                Text(
                    text = "Tap to apply · Star to favorite",
                    color = ElectricCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Empty state for Favorites or My Presets
        if (visibleList.isEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp)),
                color = CardDark
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (selectedCategory.startsWith("★ Favorites")) Icons.Default.StarBorder else Icons.Default.BookmarkAdd,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (selectedCategory.startsWith("★ Favorites")) "No favorite presets yet" else if (selectedCategory.startsWith("✨ My Presets")) "No custom presets created yet" else "No styles match your search",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (selectedCategory.startsWith("★ Favorites")) {
                        Button(
                            onClick = {
                                selectedCategory = "All"
                                onCategorySelected("All")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB800), contentColor = Color(0xFF0C0C0B)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Browse All 335+ Presets", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    } else if (selectedCategory.startsWith("✨ My Presets")) {
                        Button(
                            onClick = {
                                newPresetName = "My ${currentStyle.moduleShape.label} Style"
                                newPresetDesc = "Custom QR design"
                                showSaveDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = Color(0xFF0C0C0B)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Current Style as Preset", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // List of Presets showing the ACTUAL rendered QR code
        visibleList.forEach { preset ->
            val isCurrent = currentStyle.moduleShape == preset.style.moduleShape &&
                    currentStyle.fgColor == preset.style.fgColor &&
                    currentStyle.bgColor == preset.style.bgColor
            val isFavorite = favoriteIds.contains(preset.id)
            val isCustom = preset.id.startsWith("custom_")
            val customId = if (isCustom) preset.id.removePrefix("custom_").toLongOrNull() else null

            // Generate/retrieve real miniature QR bitmap with caching
            val qrThumbnail = remember(preset.id, preset.style) {
                QrGenerator.getOrGenerateThumbnail(preset.style, sizePx = 120)
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        width = if (isCurrent) 2.dp else 1.dp,
                        color = if (isCurrent) ElectricCyan else CardBorder,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onPresetSelected(preset) }
                    .testTag("preset_card_${preset.id}"),
                color = CardDark
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Actual miniature QR Code preview
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(preset.style.bgColor))
                            .border(1.dp, CardBorder, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = qrThumbnail.asImageBitmap(),
                            contentDescription = "${preset.name} QR Preview",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = preset.name,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isCustom) EmeraldGreen.copy(alpha = 0.15f) else ElectricCyan.copy(alpha = 0.12f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isCustom) "My Preset" else preset.category,
                                    color = if (isCustom) EmeraldGreen else ElectricCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (preset.description.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = preset.description,
                                color = TextMuted,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "${preset.style.moduleShape.label} · ${preset.style.eyeShape.label}",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }

                    // Actions: Star Favorite + Active check / Delete
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onToggleFavorite(preset.id) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = if (isFavorite) "Favorited" else "Favorite",
                                tint = if (isFavorite) Color(0xFFFFB800) else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        if (isCustom && customId != null) {
                            IconButton(
                                onClick = { onDeleteCustomPreset(customId) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete Custom Preset",
                                    tint = BeaconRose,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        if (isCurrent) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Active",
                                tint = EmeraldGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Show more button if there are more presets available
        if (visibleList.size < filteredList.size) {
            Button(
                onClick = { displayCount += 30 },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurfaceDark,
                    contentColor = ElectricCyan
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Load More Presets (${filteredList.size - visibleList.size} remaining)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }

    // Dialog for saving current style as custom preset
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text("Save to My Presets", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Store your exact shape, colors, gradient, and frame for instant 1-tap reuse.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = newPresetName,
                        onValueChange = { newPresetName = it },
                        label = { Text("Preset Name", color = TextMuted) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPresetDesc,
                        onValueChange = { newPresetDesc = it },
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
                        if (newPresetName.isNotBlank()) {
                            onSaveCurrentAsPreset(newPresetName.trim(), newPresetDesc.trim())
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
