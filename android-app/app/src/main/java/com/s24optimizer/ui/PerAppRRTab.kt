package com.s24optimizer.ui

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.s24optimizer.data.PerAppRRState
import com.s24optimizer.exec.AdbExecutor
import com.s24optimizer.service.PerAppRefreshRateService
import kotlinx.coroutines.Dispatchers
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
    val hasLauncher: Boolean,
)

@Composable
fun PerAppRRTab(
    italian: Boolean,
    executor: AdbExecutor,
    appliedStates: Map<String, Boolean>,
    onAppliedStatesChanged: (Map<String, Boolean>) -> Unit,
    onLog: (String) -> Unit,
    context: Context,
) {
    val t = { en: String, it: String -> if (italian) it else en }
    val scope = rememberCoroutineScope()
    var apps by remember { mutableStateOf(listOf<AppInfo>()) }
    var searchQ by remember { mutableStateOf("") }
    var filterMode by remember { mutableStateOf(0) } // 0=all, 1=mapped, 2=unmapped
    var defaultMode by remember { mutableStateOf(PerAppRRState.defaultMode) }
    var appModes by remember { mutableStateOf(PerAppRRState.mappings) }
    var enabled by remember { mutableStateOf(PerAppRRState.enabled) }
    var isAccEnabled by remember { mutableStateOf(false) }
    var bulkExpanded by remember { mutableStateOf(false) }
    var dirty by remember { mutableStateOf(false) }

    // Track if changes were made since last save
    val markDirty = { dirty = true }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            PerAppRRState.reload()
        }
        defaultMode = PerAppRRState.defaultMode
        appModes = PerAppRRState.mappings
        enabled = PerAppRRState.enabled
        apps = loadApps(context)
        isAccEnabled = isAccServiceEnabled(context)
        dirty = false
    }

    // Periodic Acc status refresh (every 3s)
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(3000)
            isAccEnabled = isAccServiceEnabled(context)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        Spacer(Modifier.height(8.dp))

        // ── Enable toggle + Acc status ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, Brush.horizontalGradient(listOf(ElectricBlue.copy(alpha = if (enabled) 0.5f else 0.15f), NeonCyan.copy(alpha = if (enabled) 0.3f else 0.08f))), RoundedCornerShape(14.dp))
                .background(SurfaceElevated)
                .padding(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            t("Per-App RR", "RR per App"),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                        )
                        if (dirty) {
                            Spacer(Modifier.width(6.dp))
                            Box(
                                Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(WarmAmber)
                            )
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(5.dp).clip(RoundedCornerShape(3.dp)).background(if (isAccEnabled) NeonCyan else CoralAccent))
                        Spacer(Modifier.width(5.dp))
                        Text(
                            if (isAccEnabled) t("Acc: ON", "Acc: ON")
                            else t("Acc: OFF — tap", "Acc: OFF — tocca"),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = if (isAccEnabled) NeonCyan else CoralAccent,
                        )
                    }
                }
                if (!isAccEnabled) {
                    IconButton(onClick = {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    }) {
                        Icon(Icons.Default.Settings, "Acc settings", tint = CoralAccent)
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
                                // Restore to default RR
                                val defCmds = PerAppRRState.getApplyCommands(PerAppRRState.defaultMode)
                                defCmds.forEach { executor.execute(it) }
                                onLog(t("Per-App RR disabled, restored default", "RR per App disattivato, default ripristinato"))
                            }
                            PerAppRRState.reload()
                        }
                    },
                    colors = SwitchDefaults.colors(checkedTrackColor = ElectricBlue.copy(alpha = 0.4f), checkedThumbColor = ElectricBlue),
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Default mode selector ──
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(SurfaceCard).padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Icon(Icons.Default.Tune, null, Modifier.size(16.dp), tint = ElectricBlue)
            Spacer(Modifier.width(8.dp))
            Text(t("Default:", "Default:"), style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, color = TextSecondary)
            Spacer(Modifier.width(6.dp))
            MODES.forEach { (id, label) ->
                FilterChip(
                    selected = defaultMode == id,
                    onClick = { defaultMode = id; markDirty() },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace) },
                    modifier = Modifier.padding(end = 4.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricBlue.copy(alpha = 0.2f),
                        selectedLabelColor = ElectricBlue,
                    ),
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        // ── Search + filter chips row ──
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = searchQ,
                onValueChange = { searchQ = it },
                modifier = Modifier.weight(1f).heightIn(min = 36.dp),
                placeholder = { Text(t("Search apps...", "Cerca app..."), style = MaterialTheme.typography.bodySmall, color = TextSecondary.copy(alpha = 0.4f)) },
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = TextPrimary),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue.copy(alpha = 0.4f),
                    unfocusedBorderColor = OutlineDim.copy(alpha = 0.2f),
                    cursorColor = ElectricBlue,
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard,
                ),
            )
            Spacer(Modifier.width(6.dp))
            // Bulk action
            Box {
                OutlinedButton(
                    onClick = { bulkExpanded = true },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.3f)),
                ) {
                    Icon(Icons.Default.DynamicFeed, null, Modifier.size(16.dp), tint = ElectricBlue)
                    Spacer(Modifier.width(4.dp))
                    Text(t("Bulk", "Massa"), style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, color = ElectricBlue)
                }
                DropdownMenu(expanded = bulkExpanded, onDismissRequest = { bulkExpanded = false }) {
                    MODES.forEach { (id, label) ->
                        DropdownMenuItem(
                            text = { Text(t("Set all to $label", "Tutte a $label"), style = MaterialTheme.typography.bodySmall) },
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
                        text = { Text(t("Clear all mappings", "Rimuovi tutte"), style = MaterialTheme.typography.bodySmall) },
                        onClick = {
                            val filtered = if (searchQ.isBlank()) apps
                                else apps.filter { it.label.contains(searchQ, ignoreCase = true) || it.pkg.contains(searchQ, ignoreCase = true) }
                            val updated = appModes - filtered.map { it.pkg }.toSet()
                            appModes = updated; dirty = true
                            bulkExpanded = false
                        },
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // ── Filter chips ──
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(
                t("All", "Tutte") to 0,
                t("Mapped", "Impostate") to 1,
                t("Unmapped", "Non impostate") to 2,
            ).forEach { (label, idx) ->
                FilterChip(
                    selected = filterMode == idx,
                    onClick = { filterMode = idx },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace) },
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
            else list.filter { it.label.contains(searchQ, ignoreCase = true) || it.pkg.contains(searchQ, ignoreCase = true) }
        }

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            items(filtered, key = { it.pkg }) { app ->
                val mode = appModes[app.pkg]
                var expanded by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (mode != null) ElectricBlue.copy(alpha = 0.06f) else SurfaceCard)
                        .clickable { expanded = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                app.label,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                fontFamily = FontFamily.Monospace,
                            )
                            Text(
                                app.pkg,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary.copy(alpha = 0.5f),
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (mode != null) ElectricBlue.copy(alpha = 0.15f) else OutlineDim.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Text(
                                mode?.let { m -> MODES.firstOrNull { (id, _) -> id == m }?.second } ?: t("Default", "Default"),
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = if (mode != null) ElectricBlue else TextSecondary.copy(alpha = 0.6f),
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowDropDown, null, Modifier.size(18.dp), tint = TextSecondary.copy(alpha = 0.5f))

                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(
                                text = { Text(t("Default", "Default"), style = MaterialTheme.typography.bodySmall) },
                                onClick = {
                                    appModes = appModes - app.pkg; dirty = true
                                    expanded = false
                                },
                            )
                            MODES.forEach { (id, label) ->
                                DropdownMenuItem(
                                    text = { Text(label, style = MaterialTheme.typography.bodySmall) },
                                    onClick = {
                                        appModes = appModes + (app.pkg to id); dirty = true
                                        expanded = false
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Save button ──
        Spacer(Modifier.height(6.dp))
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
                containerColor = ElectricBlue.copy(alpha = 0.2f),
                contentColor = ElectricBlue,
            ),
            shape = RoundedCornerShape(10.dp),
        ) {
            Icon(Icons.Default.Save, null, Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(t("Save Mappings", "Salva Impostazioni"), fontFamily = FontFamily.Monospace)
        }
    }
}

private fun loadApps(ctx: Context): List<AppInfo> {
    val pm = ctx.packageManager
    val entries = mutableListOf<AppInfo>()

    // Try Shizuku first for full list (no package visibility restrictions)
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
                    entries.add(AppInfo(pkg = pkg, label = label, hasLauncher = true))
                }
            }
        }
    }

    // Fallback: PackageManager API (may be limited by visibility rules)
    if (entries.isEmpty()) {
        val apps = if (Build.VERSION.SDK_INT >= 33) {
            pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION") pm.getInstalledApplications(0)
        }
        apps.filter { it.packageName != ctx.packageName }.forEach {
            val label = try { it.loadLabel(pm).toString() } catch (_: Exception) { it.packageName }
            entries.add(AppInfo(pkg = it.packageName, label = label, hasLauncher = true))
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
