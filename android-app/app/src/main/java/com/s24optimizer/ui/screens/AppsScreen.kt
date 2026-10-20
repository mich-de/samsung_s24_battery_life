package com.s24optimizer.ui.screens

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.s24optimizer.data.PerAppRRState
import com.s24optimizer.exec.AdbExecutor
import com.s24optimizer.service.PerAppRefreshRateService
import com.s24optimizer.ui.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val MODES = listOf(
    "adaptive_120" to "120Hz Adaptive",
    "high_96" to "96Hz",
    "standard_60" to "60Hz",
)

private data class AppInfo(
    val pkg: String,
    val label: String,
)

@Composable
fun AppsScreen(
    italian: Boolean,
    executor: AdbExecutor,
    appliedStates: Map<String, Boolean>,
    onAppliedStatesChanged: (Map<String, Boolean>) -> Unit,
    onLog: (String) -> Unit,
) {
    val t = { en: String, it: String -> if (italian) it else en }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var apps by remember { mutableStateOf(listOf<AppInfo>()) }
    var searchQ by remember { mutableStateOf("") }
    var filterMode by remember { mutableStateOf(0) }
    var defaultMode by remember { mutableStateOf(PerAppRRState.defaultMode) }
    var appModes by remember { mutableStateOf(PerAppRRState.mappings) }
    var enabled by remember { mutableStateOf(PerAppRRState.enabled) }
    var isAccEnabled by remember { mutableStateOf(false) }
    var bulkExpanded by remember { mutableStateOf(false) }
    var dirty by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) { PerAppRRState.reload() }
        defaultMode = PerAppRRState.defaultMode
        appModes = PerAppRRState.mappings
        enabled = PerAppRRState.enabled
        apps = loadApps(context)
        isAccEnabled = isAccServiceEnabled(context)
        dirty = false
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            isAccEnabled = isAccServiceEnabled(context)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(12.dp))

        // ── Header ──
        Text(
            t("Per-App Refresh Rate", "Frequenza per App"),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            t(
                "Assign individual refresh rates to apps",
                "Assegna frequenze individuali alle app",
            ),
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )

        Spacer(Modifier.height(12.dp))

        // ── Enable toggle + Acc status ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceElevated)
                .border(
                    1.dp,
                    if (enabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else OutlineDim.copy(alpha = 0.15f),
                    RoundedCornerShape(16.dp),
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        t("Per-App RR", "RR per App"),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    if (dirty) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(WarmAmber),
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isAccEnabled) NeonCyan else CoralAccent),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (isAccEnabled) t("Accessibility: ON", "Accessibilità: ON")
                        else t("Accessibility: OFF — tap to enable", "Accessibilità: OFF — tocca"),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isAccEnabled) NeonCyan else CoralAccent,
                    )
                }
            }
            if (!isAccEnabled) {
                IconButton(onClick = {
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }) {
                    Icon(Icons.Default.Settings, null, tint = CoralAccent)
                }
            }
            Switch(
                checked = enabled,
                onCheckedChange = { v ->
                    enabled = v
                    onAppliedStatesChanged(mapOf("per_app_refresh_rate" to v))
                    scope.launch(Dispatchers.IO) {
                        if (v) {
                            executor.execute("settings put secure s24opt_per_app_rr_enabled 1")
                            context.sendBroadcast(Intent(PerAppRefreshRateService.ACTION_RELOAD))
                            onLog(t("Per-App RR enabled", "RR per App attivato"))
                        } else {
                            executor.execute("settings delete secure s24opt_per_app_rr_enabled")
                            val defCmds = PerAppRRState.getApplyCommands(PerAppRRState.defaultMode)
                            defCmds.forEach { executor.execute(it) }
                            onLog(t("Per-App RR disabled", "RR per App disattivato"))
                        }
                        PerAppRRState.reload()
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }

        Spacer(Modifier.height(12.dp))

        // ── Default mode ──
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceCard)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Icon(Icons.Default.Tune, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(t("Default:", "Default:"), style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Spacer(Modifier.width(6.dp))
            MODES.forEach { (id, label) ->
                FilterChip(
                    selected = defaultMode == id,
                    onClick = { defaultMode = id; dirty = true },
                    label = {
                        Text(label, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
                    },
                    modifier = Modifier.padding(end = 4.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                    ),
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Search + bulk ──
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = searchQ,
                onValueChange = { searchQ = it },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        t("Search apps...", "Cerca app..."),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary.copy(alpha = 0.4f),
                    )
                },
                textStyle = MaterialTheme.typography.bodySmall.copy(color = TextPrimary),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    unfocusedBorderColor = OutlineDim.copy(alpha = 0.2f),
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard,
                ),
            )
            Spacer(Modifier.width(8.dp))
            Box {
                OutlinedButton(
                    onClick = { bulkExpanded = true },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                ) {
                    Icon(Icons.Default.DynamicFeed, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        t("Bulk", "Massa"),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                DropdownMenu(expanded = bulkExpanded, onDismissRequest = { bulkExpanded = false }) {
                    MODES.forEach { (id, label) ->
                        DropdownMenuItem(
                            text = { Text(t("Set all to $label", "Tutte a $label")) },
                            onClick = {
                                val filtered = if (searchQ.isBlank()) apps
                                else apps.filter { it.label.contains(searchQ, ignoreCase = true) || it.pkg.contains(searchQ, ignoreCase = true) }
                                val updated = mutableMapOf<String, String>().apply { putAll(appModes) }
                                filtered.forEach { updated[it.pkg] = id }
                                appModes = updated; dirty = true
                                bulkExpanded = false
                            },
                        )
                    }
                    DropdownMenuItem(
                        text = { Text(t("Clear all", "Rimuovi tutte")) },
                        onClick = {
                            val filtered = if (searchQ.isBlank()) apps
                            else apps.filter { it.label.contains(searchQ, ignoreCase = true) || it.pkg.contains(searchQ, ignoreCase = true) }
                            appModes = appModes - filtered.map { it.pkg }.toSet()
                            dirty = true; bulkExpanded = false
                        },
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Filter chips ──
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            listOf(
                t("All", "Tutte") to 0,
                t("Mapped", "Impostate") to 1,
                t("Unmapped", "Libere") to 2,
            ).forEach { (label, idx) ->
                FilterChip(
                    selected = filterMode == idx,
                    onClick = { filterMode = idx },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonCyan.copy(alpha = 0.15f),
                        selectedLabelColor = NeonCyan,
                    ),
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                "${appModes.size}/${apps.size}",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = TextSecondary,
            )
        }

        Spacer(Modifier.height(6.dp))

        // ── App list ──
        val filtered = when (filterMode) {
            1 -> apps.filter { it.pkg in appModes }
            2 -> apps.filter { it.pkg !in appModes }
            else -> apps
        }.let { list ->
            if (searchQ.isBlank()) list
            else list.filter {
                it.label.contains(searchQ, ignoreCase = true) || it.pkg.contains(searchQ, ignoreCase = true)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            items(filtered, key = { it.pkg }) { app ->
                val mode = appModes[app.pkg]
                var expanded by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (mode != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else Color.Transparent)
                        .clickable { expanded = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            app.label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            app.pkg,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary.copy(alpha = 0.5f),
                            maxLines = 1,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (mode != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else OutlineDim.copy(alpha = 0.1f)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            mode?.let { m -> MODES.firstOrNull { (id, _) -> id == m }?.second }
                                ?: t("Default", "Default"),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = if (mode != null) MaterialTheme.colorScheme.primary else TextSecondary.copy(alpha = 0.6f),
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowDropDown, null, Modifier.size(18.dp), tint = TextSecondary.copy(alpha = 0.5f))

                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text(t("Default", "Default")) },
                            onClick = { appModes = appModes - app.pkg; dirty = true; expanded = false },
                        )
                        MODES.forEach { (id, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = { appModes = appModes + (app.pkg to id); dirty = true; expanded = false },
                            )
                        }
                    }
                }
            }
        }

        // ── Save button ──
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                scope.launch(Dispatchers.IO) {
                    val ok = PerAppRRState.saveMappings(defaultMode, appModes)
                    if (ok) {
                        PerAppRRState.defaultMode = defaultMode
                        PerAppRRState.mappings = appModes
                        context.sendBroadcast(Intent(PerAppRefreshRateService.ACTION_RELOAD))
                        dirty = false
                    }
                    onLog(t("Per-App mappings saved!", "Impostazioni salvate!"))
                }
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                contentColor = MaterialTheme.colorScheme.primary,
            ),
            shape = RoundedCornerShape(14.dp),
            enabled = dirty,
        ) {
            Icon(Icons.Default.Save, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                if (!dirty) t("Saved ✓", "Salvato ✓")
                else t("Save Mappings", "Salva Impostazioni"),
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

private fun loadApps(ctx: Context): List<AppInfo> {
    val pm = ctx.packageManager
    val entries = mutableListOf<AppInfo>()

    val ex = AdbExecutor.instance
    if (ex.permissionsGranted) {
        val r = ex.execute("pm list packages -3")
        if (r.isSuccess) {
            r.stdout.lines().forEach { line ->
                val pkg = line.removePrefix("package:").trim()
                if (pkg.isNotBlank() && pkg != ctx.packageName) {
                    val label = try {
                        val ai = pm.getApplicationInfo(pkg, 0)
                        ai.loadLabel(pm).toString()
                    } catch (_: Exception) { pkg }
                    entries.add(AppInfo(pkg = pkg, label = label))
                }
            }
        }
    }

    if (entries.isEmpty()) {
        val apps = if (Build.VERSION.SDK_INT >= 33) {
            pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION") pm.getInstalledApplications(0)
        }
        apps.filter { it.packageName != ctx.packageName }.forEach {
            val label = try { it.loadLabel(pm).toString() } catch (_: Exception) { it.packageName }
            entries.add(AppInfo(pkg = it.packageName, label = label))
        }
    }

    return entries.distinctBy { it.pkg }.sortedBy { it.label }
}

private fun isAccServiceEnabled(ctx: Context): Boolean {
    return try {
        val am = ctx.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val services = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        services.any { it.resolveInfo.serviceInfo.name.contains("PerAppRefreshRateService") }
    } catch (_: Exception) { false }
}
