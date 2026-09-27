package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.RotatingTagline
import com.example.ui.theme.BgDark
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Auto proceed after 2.2 seconds
    LaunchedEffect(Unit) {
        delay(2200)
        onFinished()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "SplashGlowAnimation")

    // Pulsing scale for back-glow light
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    // Pulsing alpha for the radiant back light
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.90f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Slow rotation for multi-spectral halo
    val haloRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "haloRotation"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onFinished() }
            .testTag("app_launch_splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Deep background ambient radial glow
        Box(
            modifier = Modifier
                .size(340.dp)
                .scale(glowScale)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ElectricCyan.copy(alpha = 0.22f * glowAlpha),
                            NeonViolet.copy(alpha = 0.18f * glowAlpha),
                            Color(0xFFFF2A6D).copy(alpha = 0.08f * glowAlpha),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Container holding HD Logo and Glowing Light behind it
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(220.dp)
            ) {
                // Layer 1: Outer Rotating Radiant Halo Flare
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(glowScale)
                        .rotate(haloRotation)
                        .background(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    ElectricCyan.copy(alpha = 0.65f * glowAlpha),
                                    NeonViolet.copy(alpha = 0.65f * glowAlpha),
                                    Color(0xFFFF2A6D).copy(alpha = 0.55f * glowAlpha),
                                    Color(0xFF00FF87).copy(alpha = 0.45f * glowAlpha),
                                    ElectricCyan.copy(alpha = 0.65f * glowAlpha)
                                )
                            ),
                            shape = CircleShape
                        )
                        .blur(28.dp)
                )

                // Layer 2: Intense Center Radiant Glow Behind Logo
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(glowScale * 1.05f)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    ElectricCyan.copy(alpha = 0.85f * glowAlpha),
                                    NeonViolet.copy(alpha = 0.70f * glowAlpha),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                        .blur(16.dp)
                )

                // Layer 3: Concentric Neon Light Ring
                Box(
                    modifier = Modifier
                        .size(146.dp)
                        .scale(glowScale * 0.98f)
                        .border(
                            width = 2.dp,
                            brush = Brush.sweepGradient(
                                listOf(ElectricCyan, NeonViolet, Color(0xFFFF2A6D), ElectricCyan)
                            ),
                            shape = RoundedCornerShape(32.dp)
                        )
                )

                // Layer 4: HD Logo in Crisp Glassmorphic Frame
                Surface(
                    modifier = Modifier
                        .size(136.dp)
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(28.dp),
                            ambientColor = ElectricCyan,
                            spotColor = NeonViolet
                        )
                        .clip(RoundedCornerShape(28.dp))
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                listOf(
                                    ElectricCyan,
                                    NeonViolet.copy(alpha = 0.8f),
                                    Color(0xFFFF2A6D).copy(alpha = 0.5f)
                                )
                            ),
                            shape = RoundedCornerShape(28.dp)
                        ),
                    color = Color(0xFF101426)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.qrwho_logo),
                            contentDescription = "QRWho HD Logo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(18.dp))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Brand Name with Glowing Styling
            Text(
                text = "QRWho",
                color = TextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Tagline
            RotatingTagline(
                prefix = "QR codes that ",
                words = listOf("actually scan", "pop with style", "stand out", "inspire", "dazzle"),
                fontSize = 14.sp,
                highlightColor = ElectricCyan
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Feature Badges
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(ElectricCyan.copy(alpha = 0.12f))
                        .border(1.dp, ElectricCyan.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(11.dp))
                        Text("335+ Presets", color = ElectricCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(EmeraldGreen.copy(alpha = 0.12f))
                        .border(1.dp, EmeraldGreen.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("100% Free & Offline", color = EmeraldGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Bottom Tap Hint
        Text(
            text = "Tap anywhere to begin",
            color = TextMuted,
            fontSize = 11.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}
