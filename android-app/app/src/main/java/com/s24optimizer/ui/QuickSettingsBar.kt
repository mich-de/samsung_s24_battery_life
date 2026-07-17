package com.s24optimizer.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.s24optimizer.exec.AdbExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class PerfMode(val labelEn: String, val labelIt: String, val value: Int) {
    LIGHT("Light", "Leggero", 0),
    STANDARD("Standard", "Standard", 1),
    PERFORMANCE("Performance", "Prestazioni", 2);
}

enum class RefreshRate(val labelEn: String, val labelIt: String, val hz: Int) {
    HZ_60("60Hz", "60Hz", 60),
    HZ_120("120Hz", "120Hz", 120);
}

enum class PhotoQuality(val label: String, val mp: Int) {
    MP_12("12MP", 12),
    MP_24("24MP", 24),
    MP_50("50MP", 50);
}

@Composable
fun QuickSettingsBar(
    italian: Boolean,
    executor: AdbExecutor,
    isRunning: Boolean,
    onLog: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val prefs = LocalContext.current.getSharedPreferences("quick_settings", 0)
    val scope = rememberCoroutineScope()

    var perfMode by remember { mutableStateOf(PerfMode.entries.getOrElse(prefs.getInt("perf_mode", 1)) { PerfMode.STANDARD }) }
    var refreshRate by remember { mutableStateOf(if (prefs.getBoolean("hz120", false)) RefreshRate.HZ_120 else RefreshRate.HZ_60) }
    var photoQuality by remember { mutableStateOf(PhotoQuality.entries.getOrElse(prefs.getInt("photo_quality", 0)) { PhotoQuality.MP_12 }) }

    val t = { en: String, it: String -> if (italian) it else en }

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

    fun applyRefreshRate(rate: RefreshRate) {
        scope.launch(Dispatchers.IO) {
            val cmds = when (rate) {
                RefreshRate.HZ_60 -> listOf(
                    "settings put secure refresh_rate_mode 0",
                    "settings put global pms_settings_refresh_rate_enabled 0",
                )
                RefreshRate.HZ_120 -> listOf(
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

    fun applyPhotoQuality(quality: PhotoQuality) {
        scope.launch(Dispatchers.IO) {
            val cmds = listOf(
                "settings put system camera_photo_resolution ${quality.mp}",
                "settings put system camera_rear_resolution ${quality.mp}",
            )
            for (cmd in cmds) {
                withContext(Dispatchers.Main) { onLog("${quality.label}: $cmd") }
                val result = executor.execute(cmd)
                withContext(Dispatchers.Main) { onLog("  $result") }
            }
        }
    }

    val borderGradient = Brush.horizontalGradient(listOf(ElectricBlue.copy(alpha = 0.6f), NeonCyan.copy(alpha = 0.3f)))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, borderGradient, RoundedCornerShape(16.dp))
            .background(SurfaceElevated)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = ElectricBlue,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    t("Quick Settings", "Impostazioni Rapide"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                )
            }

            Spacer(Modifier.height(16.dp))

            FdHeader(Icons.Default.FlashOn, t("Performance Mode", "Modalità Prestazioni"))
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PerfMode.entries.forEach { mode ->
                    FdChip(
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

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = OutlineDim.copy(alpha = 0.4f))

            Spacer(Modifier.height(12.dp))
            FdHeader(Icons.Default.Refresh, t("Refresh Rate", "Frequenza Schermo"))
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RefreshRate.entries.forEach { rate ->
                    FdChip(
                        label = rate.labelEn,
                        selected = refreshRate == rate,
                        enabled = !isRunning,
                        onClick = {
                            refreshRate = rate
                            prefs.edit().putBoolean("hz120", rate == RefreshRate.HZ_120).apply()
                            applyRefreshRate(rate)
                        },
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = OutlineDim.copy(alpha = 0.4f))

            Spacer(Modifier.height(12.dp))
            FdHeader(Icons.Default.PhotoCamera, t("Photo Quality (rear)", "Qualità Foto (posteriore)"))
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PhotoQuality.entries.forEach { q ->
                    FdChip(
                        label = q.label,
                        selected = photoQuality == q,
                        enabled = !isRunning,
                        onClick = {
                            photoQuality = q
                            prefs.edit().putInt("photo_quality", q.ordinal).apply()
                            applyPhotoQuality(q)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun FdHeader(icon: ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = ElectricBlue)
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace, color = TextSecondary)
    }
}

@Composable
private fun FdChip(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (selected) ElectricBlue.copy(alpha = 0.15f) else Color.Transparent
    val borderC = if (selected) ElectricBlue.copy(alpha = 0.6f) else OutlineDim.copy(alpha = 0.3f)

    Surface(
        modifier = Modifier.then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(20.dp),
        color = bg,
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, borderC),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) ElectricBlue else TextSecondary,
        )
    }
}
