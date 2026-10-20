package com.s24optimizer.ui

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Color Palette ──
val ElectricBlue = Color(0xFF00D4FF)
val NeonCyan = Color(0xFF00E5A0)
val DeepPurple = Color(0xFF7C4DFF)
val WarmAmber = Color(0xFFFFB74D)
val CoralAccent = Color(0xFFFF6B6B)
val SurfaceDark = Color(0xFF0A0E14)
val SurfaceCard = Color(0xFF131920)
val SurfaceElevated = Color(0xFF1A2230)
val OutlineDim = Color(0xFF2A3545)
val TextPrimary = Color(0xFFE6EDF3)
val TextSecondary = Color(0xFF8B949E)

// ── Gradients ──
val GlowBlue = Color(0xFF00D4FF).copy(alpha = 0.15f)
val GlowCyan = Color(0xFF00E5A0).copy(alpha = 0.10f)

private val DarkColors = darkColorScheme(
    primary = ElectricBlue,
    secondary = NeonCyan,
    tertiary = WarmAmber,
    background = SurfaceDark,
    surface = SurfaceCard,
    surfaceVariant = SurfaceElevated,
    surfaceContainerHigh = SurfaceElevated,
    surfaceContainerLow = SurfaceCard,
    surfaceContainer = SurfaceCard,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = OutlineDim,
    outlineVariant = OutlineDim.copy(alpha = 0.5f),
    error = CoralAccent,
    primaryContainer = ElectricBlue.copy(alpha = 0.12f),
    onPrimaryContainer = ElectricBlue,
    secondaryContainer = NeonCyan.copy(alpha = 0.12f),
    onSecondaryContainer = NeonCyan,
    tertiaryContainer = WarmAmber.copy(alpha = 0.12f),
    onTertiaryContainer = WarmAmber,
    errorContainer = CoralAccent.copy(alpha = 0.12f),
    onErrorContainer = CoralAccent,
    inverseSurface = TextPrimary,
    inverseOnSurface = SurfaceDark,
)

// ── Typography ──
val AppTypography = Typography(
    displayLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 34.sp, letterSpacing = (-0.5).sp),
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 28.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 20.sp, letterSpacing = 0.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, letterSpacing = 0.1.sp),
    titleSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, letterSpacing = 0.1.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp, letterSpacing = 0.15.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp, letterSpacing = 0.15.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 12.sp, letterSpacing = 0.2.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 14.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 12.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Normal, fontSize = 11.sp, letterSpacing = 0.5.sp),
)

// ── Shapes ──
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun S24Theme(content: @Composable () -> Unit) {
    val colorScheme = if (Build.VERSION.SDK_INT >= 31) {
        // Dynamic Color: use wallpaper-derived dark palette for a native feel
        val dynamicColors = dynamicDarkColorScheme(LocalContext.current)
        dynamicColors.copy(
            background = SurfaceDark,
            surface = SurfaceCard,
            surfaceVariant = SurfaceElevated,
            surfaceContainerHigh = SurfaceElevated,
            surfaceContainerLow = SurfaceCard,
            surfaceContainer = SurfaceCard,
            outline = OutlineDim,
            outlineVariant = OutlineDim.copy(alpha = 0.5f),
        )
    } else {
        DarkColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
