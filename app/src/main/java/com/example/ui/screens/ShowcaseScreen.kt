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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qr.engine.QrGenerator
import com.example.qr.engine.QrPreset
import com.example.qr.engine.QrPresets
import com.example.ui.StudioViewModel
import com.example.ui.theme.BgDark
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
fun ShowcaseScreen(
    viewModel: StudioViewModel,
    onNavigateToStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var displayCount by remember { mutableIntStateOf(30) }

    val allPresets = QrPresets.list
    val categories = remember(favoriteIds.size) {
        listOf("All", "★ Favorites (${favoriteIds.size})") + QrPresets.categories.filter { it != "All" }
    }

    val filteredPresets = remember(searchQuery, selectedCategory, favoriteIds, allPresets.size) {
        displayCount = 30
        val query = searchQuery.trim().lowercase()
        when {
            selectedCategory.startsWith("★ Favorites") -> {
                allPresets.filter {
                    favoriteIds.contains(it.id) &&
                    (query.isEmpty() || it.name.lowercase().contains(query) || it.category.lowercase().contains(query))
                }
            }
            selectedCategory == "All" -> {
                if (query.isEmpty()) allPresets else allPresets.filter {
                    it.name.lowercase().contains(query) || it.category.lowercase().contains(query)
                }
            }
            else -> {
                QrPresets.filter(searchQuery, selectedCategory)
            }
        }
    }

    val visiblePresets = remember(filteredPresets, displayCount) {
        filteredPresets.take(displayCount)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(NeonViolet.copy(alpha = 0.2f))
                    .border(1.dp, NeonViolet, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Palette, contentDescription = "Showcase", tint = NeonViolet, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Art Gallery & Showcase", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                Text(
                    text = "Complete gallery of ${allPresets.size} camera-verified artistic presets",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // Search Input Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search ${allPresets.size}+ designer presets...", color = TextMuted, fontSize = 13.sp) },
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
                .testTag("showcase_search_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Horizontal Scroll Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isFavChip = cat.startsWith("★ Favorites")
                val isSelected = if (isFavChip) selectedCategory.startsWith("★ Favorites") else selectedCategory == cat
                val accentColor = if (isFavChip) Color(0xFFFFB800) else ElectricCyan
                val bg = if (isSelected) accentColor.copy(alpha = 0.22f) else CardDark
                val border = if (isSelected) accentColor else if (isFavChip) accentColor.copy(alpha = 0.45f) else CardBorder

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(bg)
                        .border(1.dp, border, RoundedCornerShape(20.dp))
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) accentColor else if (isFavChip) accentColor.copy(alpha = 0.85f) else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected || isFavChip) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Status Count Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Showing ${visiblePresets.size} of ${filteredPresets.size} styles",
                color = TextMuted,
                fontSize = 11.sp
            )
            if (filteredPresets.isNotEmpty()) {
                Text(
                    text = "Tap any style to load in Studio",
                    color = ElectricCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Complete Gallery Cards List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (visiblePresets.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
                        color = CardDark
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (selectedCategory.startsWith("★ Favorites")) "No favorite presets yet" else "No styles found",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (selectedCategory.startsWith("★ Favorites")) "Tap the star icon on any preset to save it to your favorites for fast 1-tap access!" else "Try a different search query or choose another category above.",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    selectedCategory = "All"
                                    searchQuery = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Color(0xFF0C0C0B)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Browse All 335+ Presets", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            items(visiblePresets, key = { it.id }) { preset ->
                ShowcasePresetCard(
                    preset = preset,
                    isFavorite = favoriteIds.contains(preset.id),
                    onToggleFavorite = { viewModel.toggleFavorite(preset.id) },
                    onSelect = {
                        viewModel.selectPreset(preset)
                        onNavigateToStudio()
                    }
                )
            }

            if (visiblePresets.size < filteredPresets.size) {
                item {
                    Button(
                        onClick = { displayCount += 30 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CardDark,
                            contentColor = ElectricCyan
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            text = "Load more styles (${filteredPresets.size - visiblePresets.size} remaining)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShowcasePresetCard(
    preset: QrPreset,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    onSelect: () -> Unit
) {
    val qrThumb: Bitmap = remember(preset.id) {
        QrGenerator.getOrGenerateThumbnail(preset.style, 360)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onSelect)
            .testTag("showcase_card_${preset.id}"),
        color = CardDark
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 1. Header: Style Name, Badges, and Apply Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    Text(
                        text = preset.name,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ElectricCyan.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = preset.category,
                                color = ElectricCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SurfaceDark)
                                .border(1.dp, CardBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Shape: ${preset.style.moduleShape.label}",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = if (isFavorite) "Favorited" else "Favorite",
                            tint = if (isFavorite) Color(0xFFFFB800) else TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(
                        onClick = onSelect,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = Color(0xFF0C0C0B)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("Apply", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Large Hero QR Code Stage (Shows actual designer QR prominently!)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceDark)
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(preset.style.bgColor))
                        .border(1.dp, CardBorder, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = qrThumb.asImageBitmap(),
                        contentDescription = preset.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                }
            }

            // 3. Preset Description
            if (preset.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = preset.description,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Footer: Palette Swatches and Verified Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color Palette Indicators
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Palette:", color = TextMuted, fontSize = 11.sp)
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color(preset.style.fgColor))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color(preset.style.eyeColor))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color(preset.style.gradientTo))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color(preset.style.bgColor))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                    )
                }

                // Verified Scannable Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen)
                    )
                    Text(
                        text = "Camera Verified",
                        color = EmeraldGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
