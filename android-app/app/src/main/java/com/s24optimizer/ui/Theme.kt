package com.s24optimizer.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val ElectricBlue = Color(0xFF00D4FF)
val NeonCyan = Color(0xFF00E5A0)
val DeepPurple = Color(0xFF7C4DFF)
val WarmAmber = Color(0xFFFFB74D)
val CoralAccent = Color(0xFFFF6B6B)
val SurfaceDark = Color(0xFF0D1117)
val SurfaceCard = Color(0xFF161B22)
val SurfaceElevated = Color(0xFF1C2333)
val OutlineDim = Color(0xFF30363D)
val TextPrimary = Color(0xFFE6EDF3)
val TextSecondary = Color(0xFF8B949E)

private val DarkColors = darkColorScheme(
    primary = ElectricBlue,
    secondary = NeonCyan,
    tertiary = WarmAmber,
    background = SurfaceDark,
    surface = SurfaceCard,
    surfaceVariant = SurfaceElevated,
    surfaceContainerHigh = SurfaceElevated,
    surfaceContainerLow = SurfaceCard,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = OutlineDim,
    outlineVariant = OutlineDim.copy(alpha = 0.5f),
    error = CoralAccent,
    primaryContainer = ElectricBlue.copy(alpha = 0.15f),
    onPrimaryContainer = ElectricBlue,
    secondaryContainer = NeonCyan.copy(alpha = 0.15f),
    onSecondaryContainer = NeonCyan,
    tertiaryContainer = WarmAmber.copy(alpha = 0.15f),
    onTertiaryContainer = WarmAmber,
    errorContainer = CoralAccent.copy(alpha = 0.15f),
    onErrorContainer = CoralAccent,
)

@Composable
fun S24Theme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        content = content
    )
}
