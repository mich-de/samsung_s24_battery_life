package com.s24optimizer.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.s24optimizer.data.Optimizations
import com.s24optimizer.exec.AdbExecutor
import com.s24optimizer.ui.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ── Quick Settings Enums ──
private enum class PerfMode(val labelEn: String, val labelIt: String, val value: Int) {
    LIGHT("Light", "Leggero", 0),
    STANDARD("Standard", "Standard", 1),
    PERFORMANCE("Performance", "Prestazioni", 2);
}

private enum class QRefreshRate(val labelEn: String, val labelIt: String, val hz: Int) {
    HZ_60("60Hz", "60Hz", 60),
    HZ_120("120Hz", "120Hz", 120);
}

@Composable
fun HomeScreen(
    italian: Boolean,
    onToggleLanguage: () -> Unit,
    executor: AdbExecutor,
    shizukuStatus: Boolean,
    appliedStates: Map<String, Boolean>,
    isRunning: Boolean,
    onLog: (String) -> Unit,
) {
    val t = { en: String, it: String -> if (italian) it else en }
    val scrollState = rememberScrollState()
    val allOpts = remember { Optimizations.getAll() }
    val appliedCount = appliedStates.count { it.value }
    val totalCount = allOpts.size

    // Animated progress
    val animatedProgress by animateFloatAsState(
        targetValue = if (totalCount > 0) appliedCount.toFloat() / totalCount else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "progress",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(12.dp))

        // ── Header ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    "S24 Optimizer",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "v2.3 • Galaxy S24",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
            }
            IconButton(onClick = onToggleLanguage) {
                Icon(Icons.Default.Translate, contentDescription = "Language", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Status + Progress Card ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            SurfaceElevated,
                            SurfaceCard,
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                    )
                )
                .border(
                    1.dp,
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            NeonCyan.copy(alpha = 0.15f),
                            Color.Transparent,
                        )
                    ),
                    RoundedCornerShape(24.dp),
                )
                .padding(24.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Battery ring
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(100.dp),
                ) {
                    val primary = MaterialTheme.colorScheme.primary
                    val ringBg = OutlineDim.copy(alpha = 0.3f)

                    // Pulsing glow
                    val infiniteTransition = rememberInfiniteTransition(label = "glow")
                    val glowAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.15f,
                        targetValue = 0.35f,
                        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
                        label = "glowAlpha",
                    )

                    Canvas(modifier = Modifier.size(100.dp)) {
                        val strokeW = 10.dp.toPx()
                        // Glow
                        drawCircle(
                            color = if (shizukuStatus) primary.copy(alpha = glowAlpha) else CoralAccent.copy(alpha = glowAlpha * 0.5f),
                            radius = size.minDimension / 2f,
                        )
                        // Background ring
                        drawArc(
                            color = ringBg,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeW, cap = StrokeCap.Round),
                        )
                        // Progress ring
                        drawArc(
                            color = primary,
                            startAngle = -90f,
                            sweepAngle = animatedProgress * 360f,
                            useCenter = false,
                            style = Stroke(width = strokeW, cap = StrokeCap.Round),
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$appliedCount",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            "/${totalCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                        )
                    }
                }

                Spacer(Modifier.width(20.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Shizuku status
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (shizukuStatus) NeonCyan else CoralAccent),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (shizukuStatus) "Shizuku Active" else "Shizuku Offline",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (shizukuStatus) NeonCyan else CoralAccent,
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        t(
                            "$appliedCount optimizations active",
                            "$appliedCount ottimizzazioni attive",
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        t(
                            "${totalCount - appliedCount} available to apply",
                            "${totalCount - appliedCount} disponibili",
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Quick Settings ──
        QuickSettingsSection(
            italian = italian,
            executor = executor,
            isRunning = isRunning,
            onLog = onLog,
        )

        Spacer(Modifier.height(20.dp))

        // ── Charge Schedule ──
        ChargeScheduleCard(
            italian = italian,
            shizukuStatus = shizukuStatus,
            onLog = onLog,
        )

        Spacer(Modifier.height(20.dp))

        // ── Reddit Tips Card ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(WarmAmber.copy(alpha = 0.08f))
                .border(1.dp, WarmAmber.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                .padding(16.dp),
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lightbulb, null, tint = WarmAmber, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        t("Community Tip", "Suggerimento Community"),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = WarmAmber,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    t(
                        "Samsung Customization Service wakes Play Services every 15min via Physical Activity tracking. Revoke the permission in the Optimize tab for a significant standby improvement. (Reddit r/GalaxyS24, July 2026)",
                        "Customization Service sveglia Play Services ogni 15min tramite Physical Activity. Revoca il permesso nella tab Ottimizza per un miglioramento significativo in standby. (Reddit r/GalaxyS24, luglio 2026)",
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 18.sp,
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Footer ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceElevated.copy(alpha = 0.5f))
                .border(1.dp, OutlineDim.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                .padding(16.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "S24 Optimizer v2.3",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    t("by mich-de", "da mich-de"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "github.com/mich-de/samsung_s24_battery_life",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary.copy(alpha = 0.6f),
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ── Quick Settings Section ──
@Composable
private fun QuickSettingsSection(
    italian: Boolean,
    executor: AdbExecutor,
    isRunning: Boolean,
    onLog: (String) -> Unit,
) {
    val prefs = LocalContext.current.getSharedPreferences("quick_settings", 0)
    val scope = rememberCoroutineScope()
    val t = { en: String, it: String -> if (italian) it else en }

    var perfMode by remember { mutableStateOf(PerfMode.entries.getOrElse(prefs.getInt("perf_mode", 1)) { PerfMode.STANDARD }) }
    var refreshRate by remember { mutableStateOf(if (prefs.getBoolean("hz120", false)) QRefreshRate.HZ_120 else QRefreshRate.HZ_60) }

    fun applyPerfMode(mode: PerfMode) {
        scope.launch(Dispatchers.IO) {
            val cmds = when (mode) {
                PerfMode.LIGHT -> listOf(
                    "settings put global sem_enhanced_cpu_responsiveness 0",
                    "settings put global enhanced_processing 0",
                    "settings put global restricted_device_performance 1,0",
                )
                PerfMode.STANDARD -> listOf(
                    "settings put global sem_enhanced_cpu_responsiveness 0",
                    "settings put global enhanced_processing 0",
                    "settings put global restricted_device_performance 0,1",
                )
                PerfMode.PERFORMANCE -> listOf(
                    "settings put global sem_enhanced_cpu_responsiveness 1",
                    "settings put global enhanced_processing 1",
                    "settings put global restricted_device_performance 0,1",
                )
            }
            for (cmd in cmds) {
                withContext(Dispatchers.Main) { onLog("${mode.labelEn}: $cmd") }
                val result = executor.execute(cmd)
                withContext(Dispatchers.Main) { onLog("  $result") }
            }
        }
    }

    fun applyRefreshRate(rate: QRefreshRate) {
        scope.launch(Dispatchers.IO) {
            val cmds = when (rate) {
                QRefreshRate.HZ_60 -> listOf(
                    "settings put secure refresh_rate_mode 0",
                    "settings put global pms_settings_refresh_rate_enabled 0",
                )
                QRefreshRate.HZ_120 -> listOf(
                    "settings put secure refresh_rate_mode 1",
                    "settings put global pms_settings_refresh_rate_enabled 1",
                )
            }
            for (cmd in cmds) {
                withContext(Dispatchers.Main) { onLog("${rate.labelEn}: $cmd") }
                val result = executor.execute(cmd)
                withContext(Dispatchers.Main) { onLog("  $result") }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceElevated)
            .border(1.dp, OutlineDim.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Settings, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                t("Quick Settings", "Impostazioni Rapide"),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.height(16.dp))

        // Performance Mode
        SectionHeader(Icons.Default.FlashOn, t("Performance Mode", "Modalità Prestazioni"))
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PerfMode.entries.forEach { mode ->
                QuickChip(
                    label = if (italian) mode.labelIt else mode.labelEn,
                    selected = perfMode == mode,
                    enabled = !isRunning,
                    onClick = {
                        perfMode = mode
                        prefs.edit().putInt("perf_mode", mode.ordinal).apply()
                        applyPerfMode(mode)
                    },
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = OutlineDim.copy(alpha = 0.3f))
        Spacer(Modifier.height(16.dp))

        // Refresh Rate
        SectionHeader(Icons.Default.Refresh, t("Refresh Rate", "Frequenza Schermo"))
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QRefreshRate.entries.forEach { rate ->
                QuickChip(
                    label = rate.labelEn,
                    selected = refreshRate == rate,
                    enabled = !isRunning,
                    onClick = {
                        refreshRate = rate
                        prefs.edit().putBoolean("hz120", rate == QRefreshRate.HZ_120).apply()
                        applyRefreshRate(rate)
                    },
                )
            }
        }
    }
}

@Composable
internal fun SectionHeader(icon: ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
        )
    }
}

@Composable
internal fun QuickChip(label: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
    val border = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else OutlineDim.copy(alpha = 0.25f)

    Surface(
        modifier = Modifier.then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(20.dp),
        color = bg,
        border = androidx.compose.foundation.BorderStroke(if (selected) 1.5.dp else 1.dp, border),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.primary else TextSecondary,
        )
    }
}

@Composable
private fun Canvas(modifier: Modifier, onDraw: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit) {
    Spacer(modifier = modifier.drawBehind(onDraw))
}
