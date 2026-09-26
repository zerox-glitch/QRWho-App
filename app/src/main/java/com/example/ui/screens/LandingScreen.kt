package com.example.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.qr.engine.QrGenerator
import com.example.qr.engine.QrPresets
import com.example.qr.engine.ShowcaseDesigns
import com.example.qr.engine.ShowcaseItem
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
fun LandingScreen(
    viewModel: StudioViewModel,
    onNavigateToStudio: () -> Unit,
    onNavigateToScanner: () -> Unit,
    onNavigateToHistory: (() -> Unit)? = null,
    onNavigateToShowcase: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    val customPresets by viewModel.customPresets.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Navigation Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.qrwho_logo),
                    contentDescription = "QRWho Logo",
                    modifier = Modifier
                        .size(55.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "QRWho",
                        color = TextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic
                    )
                    com.example.ui.components.RotatingTagline(
                        prefix = "QR that ",
                        words = listOf("actually scans", "pops", "stands out", "converts", "inspires"),
                        fontSize = 10.sp
                    )
                }
            }

            Button(
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ko-fi.com/qrwho"))
                        context.startActivity(intent)
                    } catch (e: Exception) {}
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF5E5B),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("landing_top_kofi_button")
            ) {
                Icon(
                    imageVector = Icons.Default.LocalCafe,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Support ☕", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Hero Section
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(24.dp)),
            color = SurfaceDark
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Trust Pill Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(ElectricCyan.copy(alpha = 0.12f))
                        .border(1.dp, ElectricCyan.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "100% Free · On-Device · Zero Watermarks",
                        color = ElectricCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Artistic QR codes that",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    com.example.ui.components.RotatingTagline(
                        prefix = "",
                        words = listOf("actually scan", "pop", "stand out", "convert", "inspire", "dazzle"),
                        fontSize = 22.sp,
                        highlightColor = ElectricCyan
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Transform URLs, Wi-Fi profiles, contact cards, and photos into camera-grade designer QR art with real-time verification and vector SVG & 2048px PNG export.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Hero Live QR Art Preview
                val heroPreset = remember { QrPresets.findById("art-cyberpunk") ?: QrPresets.fallbackList.first() }
                val heroQrBitmap: Bitmap = remember(heroPreset.id) {
                    QrGenerator.getOrGenerateThumbnail(heroPreset.style, 360)
                }

                Surface(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.5.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .clickable { onNavigateToStudio() },
                    color = Color(heroPreset.style.bgColor),
                    tonalElevation = 6.dp
                ) {
                    Box(modifier = Modifier.fillMaxSize().padding(10.dp), contentAlignment = Alignment.Center) {
                        Image(
                            bitmap = heroQrBitmap.asImageBitmap(),
                            contentDescription = "Featured Designer QR Code",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Verified Scannable · Tap to Edit in Studio", color = ElectricCyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onNavigateToStudio,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("hero_launch_studio"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = Color(0xFF0C0C0B)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(17.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Launch Studio",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onNavigateToScanner,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("hero_launch_scanner"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(17.dp), tint = TextPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Scan QR",
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Pro Features & Creator Suite Section
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = "Pro Creator Suite",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Powerful tools built directly into the Studio",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ElectricCyan.copy(alpha = 0.12f))
                        .border(1.dp, ElectricCyan.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("4 NEW TOOLS", color = ElectricCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Card 1: My Presets Library
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, EmeraldGreen.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                color = CardDark
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(EmeraldGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.BookmarkAdd, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("✨ My Presets Library", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Personal Database of Custom Styles", color = EmeraldGreen, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.navigateToMyPresets()
                                onNavigateToStudio()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = Color(0xFF0C0C0B)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Open", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Customize dot shapes, eye corners, brand colors, frames, and gradients, then 1-tap save into your persistent on-device library.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // Card 2: Preset Favoriting System
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFFFB800).copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                color = CardDark
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFFB800).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB800), modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("★ Preset Favorites", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Fast Access to 335+ Curated Styles", color = Color(0xFFFFB800), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.navigateToFavorites()
                                if (onNavigateToShowcase != null) {
                                    onNavigateToShowcase()
                                } else {
                                    onNavigateToStudio()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB800), contentColor = Color(0xFF0C0C0B)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("View", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Star any design in the Presets tab or Showcase. Your favorited styles instantly sync to dedicated '★ Favorites' filter chips.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // Card 3: Pro Multi-Resolution PNG & Vector SVG Export
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                color = CardDark
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ElectricCyan.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("📐 Pro Export Suite", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("512px to 4096px Ultra HD & Vector SVG", color = ElectricCyan, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Button(
                            onClick = onNavigateToStudio,
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Color(0xFF0C0C0B)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Export", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Export production print-ready PNG files (up to 4096px 300 DPI) or copy clean vector SVG code directly to your clipboard for Figma & Adobe Illustrator.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // Card 4: Smart Categorized History & Style Restore
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
                color = CardDark
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SurfaceDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.History, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("🕒 Smart History & Vault", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${historyList.size} codes saved on device", color = TextMuted, fontSize = 10.sp)
                            }
                        }

                        Button(
                            onClick = {
                                if (onNavigateToHistory != null) {
                                    onNavigateToHistory()
                                } else {
                                    viewModel.navigateToHistoryCreated()
                                    onNavigateToStudio()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Color(0xFF0C0C0B)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Open Vault", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Keep every created artwork and camera-scanned QR organized in separate tabs with 1-tap visual style restoration and sharing.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Featured Art Showcase Section (9 Flagship Styles)
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = "Curated Showcase",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Explore 300+ artistic styles with verified camera decoding",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CardDark)
                        .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("335 Styles", color = EmeraldGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Grid of Showcase items with 1-tap Favoriting
            ShowcaseDesigns.items.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { item ->
                        ShowcaseCard(
                            item = item,
                            isFavorite = favoriteIds.contains(item.presetId),
                            onToggleFavorite = { viewModel.toggleFavorite(item.presetId) },
                            onSelect = {
                                viewModel.applyShowcase(item)
                                onNavigateToStudio()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 6 Authentic Use Cases Section
        Text(
            text = "Engineered For Real-World Impact",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "From high-density outdoor print to digital contactless menus",
            color = TextMuted,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        val useCases = listOf(
            Triple(Icons.Default.RestaurantMenu, "Restaurant & Bar Menus", "Replace grimy plastic menus with scannable table art that matches your interior aesthetic. Zero lag, zero downloads."),
            Triple(Icons.Default.VolunteerActivism, "Weddings & Luxury Events", "Aesthetic QR codes that look gorgeous on linen cardstock, foil invitations, and wedding table settings."),
            Triple(Icons.Default.Wifi, "Instant WiFi Sign-In", "Let guests and customers join encrypted WPA2/WPA3 networks in 1 second without typing complex passwords."),
            Triple(Icons.Default.ContactPage, "Creative Portfolios & vCards", "Teleport people straight to your Behance, GitHub, LinkedIn, or digital contact card directly from your business card."),
            Triple(Icons.Default.Inventory2, "Product Packaging & Merch", "Print-ready high-density marks with Error Correction Level H that scan flawlessly across curved bottles and textured fabric."),
            Triple(Icons.Default.Share, "Social Links & Bio Pages", "Drive viral traffic to your Instagram, TikTok, YouTube, or Spotify releases with high-contrast custom designs.")
        )

        useCases.forEach { (icon, title, desc) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceDark)
                            .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(desc, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Anti-Subscription & Error Correction Level H Guarantee
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(18.dp)),
            color = SurfaceDark
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Error Correction Level H (ISO/IEC 18004)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "QRWho generates standard static QR codes where your content is encoded directly into the pixel matrix with up to 30% error restoration. Zero middleman redirect servers, zero expiry, zero watermarks.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Authentic FAQ Accordion (All 6 from QRWho)
        Text(
            text = "Frequently Asked Questions",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        val faqs = listOf(
            Pair("Is QRWho really 100% free with no watermarks?", "Yes. QRWho is completely free forever. We generate full-resolution 2048px PNG and print-ready files with zero watermarks, zero accounts, and zero paywalls."),
            Pair("Do QR codes created on QRWho ever expire?", "Never. QRWho creates standard static QR codes where your URL, text, or WiFi credentials are encoded directly into the pixel matrix. There is no middleman redirect server, so your codes will work for decades even if offline."),
            Pair("Are my uploaded photos and links private?", "100% private. All rendering, image processing, encoding, and camera verification happens entirely inside your local memory using client-side algorithms. Your files never touch any external server."),
            Pair("How does the active scannability checker and Auto-Fix work?", "As you tweak colors, shapes, and images, our background engine decodes the exact raster bitmap using camera-grade computer vision algorithms. If contrast or geometry drops below reliable scan thresholds, the Wand 'Fix Scan' button instantly calculates the nearest high-contrast scannable parameter set."),
            Pair("Can I print these QR codes on large posters and billboards?", "Absolutely. When you hit 'Download PNG', QRWho renders a high-density 2048x2048 pixel canvas at maximum crispness, suitable for 300 DPI offset printing on business cards, menus, apparel, and massive outdoor banners."),
            Pair("What is Error Correction Level H and why does QRWho use it?", "Level H (High) is the highest standard in ISO/IEC 18004 QR specifications. It restores up to 30% of damaged or obscured code data, allowing us to embed photos, custom shapes, and logos while guaranteeing instant camera read rates.")
        )

        faqs.forEach { (q, a) ->
            FaqAccordionItem(question = q, answer = a)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Ko-fi Support Section at the End of Home Page
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, com.example.ui.components.KofiRed.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            color = CardDark
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(com.example.ui.components.KofiRed.copy(alpha = 0.15f))
                        .border(1.dp, com.example.ui.components.KofiRed.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalCafe,
                        contentDescription = "Support QRWho",
                        tint = com.example.ui.components.KofiRed,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Support QRWho to Keep it Free & Colorful! ☕🎨",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "QRWho is built with love—100% free, private, and watermark-free forever. If QRWho helped you build amazing artistic QR codes, consider supporting us on Ko-fi!",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

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
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(44.dp)
                        .testTag("landing_bottom_kofi_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalCafe,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Support Us on Ko-fi ☕", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Footer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                .background(SurfaceDark)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.qrwho_logo),
                    contentDescription = null,
                    modifier = Modifier.size(29.dp).clip(RoundedCornerShape(7.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("QRWho", color = TextPrimary, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "The premier artistic QR code generator. 100% private, on-device, and print-ready.",
                color = TextMuted,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "© 2026 QRWho Studio · ISO/IEC 18004 Standard · High Error Correction",
                color = TextMuted,
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ShowcaseCard(
    item: ShowcaseItem,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val preset = QrPresets.findById(item.presetId) ?: QrPresets.fallbackList.first()

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            val qrThumb: Bitmap = remember(preset.id) {
                QrGenerator.getOrGenerateThumbnail(preset.style, 240)
            }

            // Live Actual QR Code Art (NOT just color dots!)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(preset.style.bgColor))
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = qrThumb.asImageBitmap(),
                    contentDescription = item.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ElectricCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(item.badge, color = ElectricCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = if (isFavorite) "Favorited" else "Favorite",
                        tint = if (isFavorite) Color(0xFFFFB800) else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.title,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = item.tagline,
                color = TextMuted,
                fontSize = 11.sp,
                maxLines = 2,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Open Studio", color = ElectricCyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
fun FaqAccordionItem(question: String, answer: String) {
    var expanded by remember { androidx.compose.runtime.mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .clickable { expanded = !expanded },
        color = SurfaceDark
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.HelpOutline, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = question,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = CardBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = answer,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
