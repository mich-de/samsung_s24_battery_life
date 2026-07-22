package com.s24optimizer.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.s24optimizer.data.PerAppRRState
import com.s24optimizer.exec.AdbExecutor
import com.s24optimizer.service.PerAppRefreshRateService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class AppEntry(
    val packageName: String,
    val label: String,
)

private val MODE_OPTIONS = listOf(
    "adaptive_120" to "120Hz Adaptive",
    "high_96" to "96Hz",
    "standard_60" to "60Hz",
)

@Composable
fun PerAppRRDialog(
    italian: Boolean,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val t = { en: String, it: String -> if (italian) it else en }

    var defaultMode by remember { mutableStateOf(PerAppRRState.defaultMode) }
    var appMappings by remember { mutableStateOf(PerAppRRState.mappings) }
    var apps by remember { mutableStateOf(listOf<AppEntry>()) }
    var searchQuery by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }
    var isAccessibilityEnabled by remember {
        mutableStateOf(isAccessibilityServiceEnabled(context))
    }

    LaunchedEffect(Unit) {
        apps = loadInstalledApps(context)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(t("Close", "Chiudi")) }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    t("Per-App Refresh Rate", "Frequenza Personalizzata per App"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, null, tint = TextPrimary)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 520.dp)) {
                if (!isAccessibilityEnabled) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CoralAccent.copy(alpha = 0.15f))
                            .clickable {
                                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                            }
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = CoralAccent, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                t(
                                    "Enable accessibility service in Settings → Accessibility → Installed Apps → S24 Optimizer",
                                    "Abilita il servizio in Impostazioni → Accessibilità → App installate → S24 Optimizer",
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = CoralAccent,
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                Text(
                    t("Default mode (for unmapped apps):", "Modalità predefinita (app non impostate):"),
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Monospace,
                )
                Spacer(Modifier.height(4.dp))
                MODE_OPTIONS.forEach { (id, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { defaultMode = id }
                            .padding(vertical = 2.dp),
                    ) {
                        RadioButton(
                            selected = defaultMode == id,
                            onClick = { defaultMode = id },
                            colors = RadioButtonDefaults.colors(selectedColor = ElectricBlue),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(label, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = OutlineDim.copy(alpha = 0.3f))
                Spacer(Modifier.height(8.dp))

                val filteredApps = if (searchQuery.isBlank()) apps
                    else apps.filter { it.label.contains(searchQuery, ignoreCase = true) || it.packageName.contains(searchQuery, ignoreCase = true) }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 36.dp),
                    placeholder = { Text(t("Search apps...", "Cerca app..."), style = MaterialTheme.typography.bodySmall, color = TextSecondary.copy(alpha = 0.5f)) },
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = TextPrimary),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue.copy(alpha = 0.4f),
                        unfocusedBorderColor = OutlineDim.copy(alpha = 0.3f),
                        cursorColor = ElectricBlue,
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard,
                    ),
                )

                Spacer(Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        val currentMode = appMappings[app.packageName]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    app.label,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.Monospace,
                                )
                                Text(
                                    app.packageName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary.copy(alpha = 0.6f),
                                    fontFamily = FontFamily.Monospace,
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            var expanded by remember { mutableStateOf(false) }
                            Box {
                                OutlinedButton(
                                    onClick = { expanded = true },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (currentMode != null) ElectricBlue.copy(alpha = 0.5f) else OutlineDim.copy(alpha = 0.3f),
                                    ),
                                ) {
                                    Text(
                                        currentMode?.let { MODE_OPTIONS.firstOrNull { m -> m.first == it }?.second } ?: t("Default", "Default"),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                    )
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(t("Default", "Default"), style = MaterialTheme.typography.bodySmall) },
                                        onClick = {
                                            appMappings = appMappings - app.packageName
                                            expanded = false
                                        },
                                    )
                                    MODE_OPTIONS.forEach { (id, label) ->
                                        DropdownMenuItem(
                                            text = { Text(label, style = MaterialTheme.typography.bodySmall) },
                                            onClick = {
                                                appMappings = appMappings + (app.packageName to id)
                                                expanded = false
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            val ok = PerAppRRState.saveMappings(defaultMode, appMappings)
                            if (ok) {
                                PerAppRRState.defaultMode = defaultMode
                                PerAppRRState.mappings = appMappings
                                context.sendBroadcast(Intent(PerAppRefreshRateService.ACTION_RELOAD))
                            }
                            saved = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue.copy(alpha = 0.2f), contentColor = ElectricBlue),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !saved,
                ) {
                    if (saved) {
                        Text(
                            t("Saved!", "Salvato!"),
                            fontFamily = FontFamily.Monospace,
                            color = NeonCyan,
                        )
                    } else {
                        Text(
                            t("Save Mappings", "Salva Impostazioni"),
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                }
            }
        },
    )
}

private fun loadInstalledApps(context: android.content.Context): List<AppEntry> {
    val pm = context.packageManager
    val intent = android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_LAUNCHER)
    val activities = pm.queryIntentActivities(intent, 0)
    return activities.map { resolveInfo ->
        val ai = resolveInfo.activityInfo
        AppEntry(
            packageName = ai.packageName,
            label = ai.loadLabel(pm).toString(),
        )
    }.distinctBy { it.packageName }.sortedBy { it.label }
}

private fun isAccessibilityServiceEnabled(context: android.content.Context): Boolean {
    try {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false
        return enabledServices.split(':').any { it.contains(context.packageName + "/.service.PerAppRefreshRateService") }
    } catch (_: Exception) {
        return false
    }
}
