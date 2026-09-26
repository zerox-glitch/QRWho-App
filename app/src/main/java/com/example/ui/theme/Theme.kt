package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = Color(0xFF02121A),
    primaryContainer = Color(0xFF0F3042),
    onPrimaryContainer = ElectricCyan,
    secondary = NeonViolet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF281E56),
    onSecondaryContainer = Color(0xFFD4C8FF),
    tertiary = BeaconRose,
    background = BgDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = CardDark,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder
)

private val LightColorScheme = darkColorScheme( // QRWho uses a dedicated creative dark studio palette
    primary = ElectricCyan,
    onPrimary = Color(0xFF02121A),
    primaryContainer = Color(0xFF0F3042),
    onPrimaryContainer = ElectricCyan,
    secondary = NeonViolet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF281E56),
    onSecondaryContainer = Color(0xFFD4C8FF),
    tertiary = BeaconRose,
    background = BgDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = CardDark,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve bespoke aesthetic design
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
