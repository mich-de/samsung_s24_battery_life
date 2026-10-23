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

// ── Color Palette (Technical Minimal / Teenage Engineering Style) ──
val ElectricBlue = Color(0xFFFF5A1F)
val NeonCyan = Color(0xFFFF7A45)
val DeepPurple = Color(0xFFB3360F)
val WarmAmber = Color(0xFFFF5A1F)
val CoralAccent = Color(0xFFE5481A)
val SurfaceDark = Color(0xFF0E0E0E)
val SurfaceCard = Color(0xFF151515)
val SurfaceElevated = Color(0xFF1C1C1C)
val OutlineDim = Color(0xFF262626)
val TextPrimary = Color(0xFFECEBE8)
val TextSecondary = Color(0xFF9A968F)

// ── Gradients ──
val GlowBlue = Color(0xFFFF5A1F).copy(alpha = 0.15f)
val GlowCyan = Color(0xFFFF7A45).copy(alpha = 0.10f)

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

// ── Shapes (Technical Minimal Sharp 6dp / 10dp) ──
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(16.dp),
)

@Composable
fun S24Theme(content: @Composable () -> Unit) {
    // Pure Technical Minimal dark scheme (#0E0E0E, #151515, #FF5A1F)
    MaterialTheme(
        colorScheme = DarkColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
