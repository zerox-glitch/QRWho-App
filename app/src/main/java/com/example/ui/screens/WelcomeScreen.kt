package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NoAccounts
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.qr.engine.QrGenerator
import com.example.qr.engine.QrPreset
import com.example.qr.engine.QrPresets
import com.example.qr.engine.QrStyle
import com.example.ui.components.RotatingTagline
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BeaconRose
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WelcomeScreen(
    onStartCreating: () -> Unit,
    onOpenScanner: () -> Unit,
    onDismiss: () -> Unit,
    onSelectPreset: (QrPreset) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var dontShowAgain by remember { mutableStateOf(false) }

    fun markSeenAndProceed(action: () -> Unit) {
        val prefs = context.getSharedPreferences("qrwho_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("has_seen_welcome_v1", true).apply()
        action()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Navigation & Close Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.qrwho_logo),
                        contentDescription = "QRWho Logo",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Column {
                        Text(
                            text = "QRWho",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic
                        )
                        Text(
                            text = "Artistic Studio & Scanner",
                            color = ElectricCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(
                    onClick = { markSeenAndProceed(onDismiss) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceDark)
                        .testTag("welcome_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Welcome Page",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // 1. HERO CALLOUT BANNER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1E1B4B),
                                Color(0xFF0F172A),
                                Color(0xFF134E4A)
                            )
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.horizontalGradient(listOf(ElectricCyan, NeonViolet, BeaconRose)),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Badge: 100% Free & No Ads
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFF10B981), RoundedCornerShape(30.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "100% FREE • ZERO ADS • ZERO PAYWALLS",
                                color = EmeraldGreen,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "The World's Only Truly Free\nArtistic QR Code Studio",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    RotatingTagline(
                        prefix = "Generate codes that ",
                        words = listOf("actually scan", "look stunning", "pop on print", "stand out", "inspire"),
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Transform bland, boring black-and-white barcodes into breathtaking artistic masterpieces with frames, custom shapes, vivid gradients, and center emblems — always scannable with Level H error protection.",
                        color = TextSecondary,
                        fontSize = 12.5.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. OUR MISSION CARD
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NeonViolet.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = BeaconRose,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Our Mission",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Why QRWho was built",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "QR codes are an essential part of modern life — menus, business cards, Wi-Fi, payments, event tickets, and creative art. Yet virtually every QR code generator in app stores charges predatory subscriptions ($5 to $10 per week!), hides basic vector exports behind paywalls, or bombards you with annoying full-screen video ads.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "We built QRWho to prove there is a better way: a studio-grade, fully customizable artistic QR generator and lightning-fast camera scanner that is 100% free, forever ad-free, completely private, and community-supported.",
                        color = ElectricCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Ko-fi Support Link
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceDark)
                            .border(1.dp, Color(0xFFFF5E5B).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ko-fi.com/qrwho"))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalCafe,
                                contentDescription = null,
                                tint = Color(0xFFFF5E5B),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Keep QRWho free: Buy creator a coffee",
                                color = TextPrimary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFFFF5E5B),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. SHOWCASE OF OUR MOST AMAZING DESIGNS
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Amazing Preset Designs",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "37+ artistic templates ready in 1-tap",
                            color = TextMuted,
                            fontSize = 11.5.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElectricCyan.copy(alpha = 0.15f))
                            .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Live Previews",
                            color = ElectricCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Horizontal Carousel of Signature Designs
                val featuredPresetIds = remember {
                    listOf(
                        "tpl-bracket",
                        "tpl-badge",
                        "tpl-arch",
                        "tpl-cup",
                        "tpl-globe",
                        "tpl-floral",
                        "tpl-note",
                        "tpl-card",
                        "tpl-diamond",
                        "tpl-halloween",
                        "tpl-christmas",
                        "tpl-music",
                        "tpl-ramadan"
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    featuredPresetIds.forEach { pid ->
                        val preset = QrPresets.list.find { it.id == pid }
                        if (preset != null) {
                            ShowcaseDesignCard(
                                preset = preset,
                                onClick = {
                                    onSelectPreset(preset)
                                    markSeenAndProceed(onStartCreating)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. WHY WE ARE THE #1 CHOICE: CORE PILLARS
            Text(
                text = "Unmatched Freedom & Features",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Everything professionals and creators need in one place",
                color = TextMuted,
                fontSize = 11.5.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Feature Grid / Pillars
            val pillars = listOf(
                FeatureItem(
                    icon = Icons.Default.VisibilityOff,
                    color = Color(0xFF10B981),
                    title = "Zero Ads & Zero Popups",
                    desc = "Never interrupted by ads, video walls, or promotional timers. Seamless and distraction-free."
                ),
                FeatureItem(
                    icon = Icons.Default.Palette,
                    color = ElectricCyan,
                    title = "20+ Custom Frame Styles",
                    desc = "Choose from Brackets, Rosette Badges, Cathedral Arches, Coffee Cups, Wax Seals, Notes, and Luggage Tags."
                ),
                FeatureItem(
                    icon = Icons.Default.Tune,
                    color = NeonViolet,
                    title = "100% Fully Customizable",
                    desc = "Tweak 12 module shapes (dots, squircle, stars, bubbles, hearts), eye styles, linear/radial gradients, and custom logos."
                ),
                FeatureItem(
                    icon = Icons.Default.Shield,
                    color = AmberWarning,
                    title = "Level H Error Correction",
                    desc = "30% Reed-Solomon redundancy ensures your QR code scans flawlessly even with embedded art and custom logos."
                ),
                FeatureItem(
                    icon = Icons.Default.QrCodeScanner,
                    color = Color(0xFF38BDF8),
                    title = "Built-in Camera & Gallery Scanner",
                    desc = "Lightning-fast camera reader with flashlight toggle, instant URL opening, contact dialing, and History Vault."
                ),
                FeatureItem(
                    icon = Icons.Default.FileDownload,
                    color = BeaconRose,
                    title = "Vector SVG & 2048px PNG",
                    desc = "Export crystal-clear vector SVG for print shops, billboards, and merch, plus high-resolution PNG for digital sharing."
                ),
                FeatureItem(
                    icon = Icons.Default.Lock,
                    color = Color(0xFFA855F7),
                    title = "100% Offline & Private",
                    desc = "All QR generation runs entirely locally on your device. No signups, no servers, and zero data tracking."
                )
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                pillars.forEach { feature ->
                    FeatureCardRow(feature = feature)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. CALL TO ACTION BUTTONS
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Primary: Enter Studio
                Button(
                    onClick = { markSeenAndProceed(onStartCreating) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan,
                        contentColor = Color(0xFF0F172A)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(8.dp, RoundedCornerShape(14.dp), ambientColor = ElectricCyan, spotColor = ElectricCyan)
                        .testTag("welcome_start_creating_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Brush,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Start Creating QR Codes 🚀",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Secondary: Open Scanner
                OutlinedButton(
                    onClick = { markSeenAndProceed(onOpenScanner) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("welcome_open_scanner_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Open Camera Scanner 📷",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Checkbox: Don't show on startup
                Row(
                    modifier = Modifier
                        .clickable {
                            val prefs = context.getSharedPreferences("qrwho_prefs", Context.MODE_PRIVATE)
                            val current = prefs.getBoolean("has_seen_welcome_v1", false)
                            prefs.edit().putBoolean("has_seen_welcome_v1", !current).apply()
                            dontShowAgain = !current
                        }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "You can reopen this guide anytime from the top menu",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ShowcaseDesignCard(
    preset: QrPreset,
    onClick: () -> Unit
) {
    var thumbnailBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(preset.id) {
        withContext(Dispatchers.Default) {
            val bmp = QrGenerator.getOrGenerateThumbnail(preset.style, 180)
            thumbnailBitmap = bmp
        }
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() }
            .testTag("welcome_preset_${preset.id}")
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Thumbnail Box
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center
            ) {
                if (thumbnailBitmap != null) {
                    Image(
                        bitmap = thumbnailBitmap!!.asImageBitmap(),
                        contentDescription = preset.name,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = preset.name,
                color = TextPrimary,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Text(
                text = preset.style.frameStyle.label,
                color = ElectricCyan,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

private data class FeatureItem(
    val icon: ImageVector,
    val color: Color,
    val title: String,
    val desc: String
)

@Composable
private fun FeatureCardRow(feature: FeatureItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardDark)
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(feature.color.copy(alpha = 0.18f))
                .border(1.dp, feature.color.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = null,
                tint = feature.color,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = feature.title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = feature.desc,
                color = TextSecondary,
                fontSize = 11.5.sp,
                lineHeight = 16.sp
            )
        }
    }
}
