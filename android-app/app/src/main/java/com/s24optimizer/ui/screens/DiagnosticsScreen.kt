package com.s24optimizer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.s24optimizer.data.*
import com.s24optimizer.exec.AdbExecutor
import com.s24optimizer.service.SelfMaintenance
import com.s24optimizer.service.WhatsAppCallOptimizer
import com.s24optimizer.ui.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

private data class DiagState(
    val basic: BatteryTelemetry.Basic? = null,
    val health: BatteryTelemetry.Health? = null,
    val stats: BatteryHistory.Stats? = null,
    val radio: Diagnostics.Radio? = null,
    val power: Diagnostics.PowerUse? = null,
    val conflicts: List<Diagnostics.Conflict> = emptyList(),
    val regressions: List<Optimization> = emptyList(),
    val loading: Boolean = true,
)

@Composable
fun DiagnosticsScreen(
    italian: Boolean,
    executor: AdbExecutor,
    shizukuStatus: Boolean,
    appliedStates: Map<String, Boolean>,
    onLog: (String) -> Unit,
) {
    fun t(en: String, it: String) = if (italian) it else en

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var state by remember { mutableStateOf(DiagState()) }
    var refreshKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(refreshKey, shizukuStatus) {
        state = state.copy(loading = true)
        val loaded = withContext(Dispatchers.IO) {
            val basic = BatteryTelemetry.readBasic(context)
            BatteryHistory.record(context, basic)

            val health = BatteryTelemetry.readHealth(executor)
            val radio = Diagnostics.readRadio(executor)
            val labels = Diagnostics.readUidLabels(executor)
            DiagState(
                basic = basic,
                health = health,
                stats = BatteryHistory.stats(context),
                radio = radio,
                power = Diagnostics.readPowerUse(executor, labels),
                conflicts = Diagnostics.detectConflicts(context, executor, health, radio, basic),
                regressions = AppliedHistory.regressions(context, appliedStates),
                loading = false,
            )
        }
        state = loaded
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    t("Diagnostics", "Diagnostica"),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    t("Measured, not assumed", "Misurato, non supposto"),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
            }
            IconButton(onClick = { refreshKey++ }, enabled = !state.loading) {
                if (state.loading) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Refresh, "Refresh", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (!shizukuStatus) {
            InfoBanner(
                t(
                    "Shizuku is offline — only readings that need no permission are shown.",
                    "Shizuku non attivo — vengono mostrate solo le letture che non richiedono permessi.",
                ),
                CoralAccent,
            )
            Spacer(Modifier.height(16.dp))
        }

        // ── Conflicts first: these are the actionable items ──
        if (state.conflicts.isNotEmpty()) {
            DiagCard(t("Conflicts", "Conflitti"), Icons.Default.Warning, WarmAmber) {
                state.conflicts.forEachIndexed { i, c ->
                    if (i > 0) Spacer(Modifier.height(12.dp))
                    ConflictRow(c, italian)
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // ── Regressions ──
        if (state.regressions.isNotEmpty()) {
            DiagCard(t("Regressions", "Regressioni"), Icons.Default.Warning, CoralAccent) {
                Text(
                    t(
                        "${state.regressions.size} optimization(s) you applied are no longer active. " +
                            "A system update usually undoes these.",
                        "${state.regressions.size} ottimizzazioni che avevi applicato non sono più attive. " +
                            "Di solito è un aggiornamento di sistema a disfarle.",
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(10.dp))
                state.regressions.take(12).forEach {
                    Text(
                        "• ${if (italian) it.titleIt else it.titleEn}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                    )
                }
                Spacer(Modifier.height(12.dp))
                FilledTonalButton(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            for (opt in state.regressions) {
                                for (cmd in opt.applyCommands) {
                                    val r = executor.execute(cmd)
                                    withContext(Dispatchers.Main) { onLog("${opt.titleEn}: $cmd\n  $r") }
                                }
                            }
                            withContext(Dispatchers.Main) { refreshKey++ }
                        }
                    },
                    enabled = shizukuStatus,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(t("Re-apply all", "Riapplica tutte"), fontFamily = FontFamily.Monospace)
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // ── Drain ──
        DiagCard(t("Drain", "Consumo"), Icons.AutoMirrored.Filled.ShowChart, ElectricBlue) {
            val s = state.stats
            if (s == null || !s.hasEnoughData) {
                Text(
                    t(
                        "Not enough samples yet. Leave the phone unplugged for a while and come back — " +
                            "readings are taken whenever the app runs or the screen turns on or off.",
                        "Campioni insufficienti. Lascia il telefono scollegato per un po' e torna qui — " +
                            "le letture vengono prese quando l'app è aperta o allo spegnimento/accensione dello schermo.",
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            } else {
                MetricRow(t("Overall", "Totale"), "%.0f mAh/h".format(s.mahPerHour), ElectricBlue)
                MetricRow(t("Per hour", "All'ora"), "%.1f %%/h".format(s.percentPerHour), TextPrimary)
                if (s.standbyMs > 0) {
                    MetricRow(
                        t("Standby", "Standby"),
                        "%.0f mAh/h  (%s)".format(s.standbyMahPerHour, duration(s.standbyMs)),
                        NeonCyan,
                    )
                }
                if (s.activeMs > 0) {
                    MetricRow(
                        t("Screen on", "Schermo acceso"),
                        "%.0f mAh/h  (%s)".format(s.activeMahPerHour, duration(s.activeMs)),
                        WarmAmber,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    t(
                        "Window ${duration(s.windowMs)} • ${s.sampleCount} samples",
                        "Finestra ${duration(s.windowMs)} • ${s.sampleCount} campioni",
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
            state.basic?.let { b ->
                Spacer(Modifier.height(10.dp))
                Text(
                    t(
                        "Now: ${b.level}% • ${"%.1f".format(b.temperatureC)}°C • ${b.voltageMv} mV" +
                            if (b.plugged) " • charging" else "",
                        "Ora: ${b.level}% • ${"%.1f".format(b.temperatureC)}°C • ${b.voltageMv} mV" +
                            if (b.plugged) " • in carica" else "",
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Battery health ──
        DiagCard(t("Battery health", "Salute batteria"), Icons.Default.BatteryFull, NeonCyan) {
            val h = state.health
            if (h == null) {
                Text(
                    t("Requires Shizuku.", "Richiede Shizuku."),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            } else {
                MetricRow("ASOC", "${h.asoc}%", healthColor(h.asoc))
                MetricRow("BSOH", "${h.bsoh}%", healthColor(h.bsoh))
                state.basic?.takeIf { it.impliedCapacityMah > 0 }?.let {
                    MetricRow(
                        t("Capacity", "Capacità"),
                        "${it.impliedCapacityMah} mAh" +
                            (state.power?.ratedMah?.takeIf { r -> r > 0 }?.let { r -> " / $r" } ?: ""),
                        TextPrimary,
                    )
                }
                MetricRow(t("Age", "Età"), t("${h.ageWeeks} weeks", "${h.ageWeeks} settimane"), TextPrimary)
                if (h.firstUseDate.length == 8) {
                    MetricRow(
                        t("First use", "Prima accensione"),
                        "${h.firstUseDate.substring(6)}/${h.firstUseDate.substring(4, 6)}/${h.firstUseDate.substring(0, 4)}",
                        TextSecondary,
                    )
                }
                // Lifetime high-water mark, not a reading from now — say so, or a red
                // number here reads as a problem the user could act on.
                MetricRow(
                    t("Peak temp (lifetime)", "Temp. massima (storica)"),
                    "%.1f°C".format(h.maxTempC),
                    if (h.maxTempC >= 45f) CoralAccent else TextPrimary,
                )
                MetricRow(
                    t("Charge limit", "Limite ricarica"),
                    // Basic remembers a threshold it never applies; printing it there
                    // reads as a cap that is in force when charging still runs to 100%.
                    when {
                        h.protectCapped -> t("${h.protectThresholdPct}% (Maximum)", "${h.protectThresholdPct}% (Massima)")
                        h.protectOn -> t("Basic — 100%, tops up below 95%", "Base — 100%, ricarica sotto il 95%")
                        else -> t("off", "disattivo")
                    },
                    TextPrimary,
                )
                if (h.ltcHighPct > 0) {
                    MetricRow(
                        t("Long-term charging", "Ricarica prolungata"),
                        t(
                            "cap ${h.ltcHighPct}% after ${h.ltcHighSocMinutes / 1440}d plugged",
                            "tetto ${h.ltcHighPct}% dopo ${h.ltcHighSocMinutes / 1440}g attaccato",
                        ),
                        TextSecondary,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Radio ──
        DiagCard(t("Radio", "Radio"), Icons.Default.NetworkCheck, DeepPurple) {
            val r = state.radio
            if (r == null || !r.hasSignal) {
                Text(
                    t("No mobile signal reading available.", "Nessuna lettura del segnale mobile disponibile."),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            } else {
                MetricRow("RSRP", "${r.rsrp} dBm — ${r.quality}", if (r.weak) WarmAmber else NeonCyan)
                MetricRow("RSRQ", "${r.rsrq} dB", TextPrimary)
                MetricRow("SINR", "${r.sinr} dB", TextPrimary)
                if (r.band.isNotEmpty() || r.operator.isNotEmpty()) {
                    MetricRow(t("Cell", "Cella"), "${r.operator} ${r.band}".trim(), TextSecondary)
                }
                MetricRow(
                    t("Network mode", "Modalità rete"),
                    if (r.nrEnabled) t("5G + LTE", "5G + LTE") else t("LTE (5G off)", "LTE (5G disattivo)"),
                    if (r.nrEnabled && r.weak) WarmAmber else TextPrimary,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Top consumers ──
        DiagCard(t("Top consumers", "Top consumatori"), Icons.Default.Bolt, WarmAmber) {
            val p = state.power
            if (p == null || p.consumers.isEmpty()) {
                Text(
                    t("Requires Shizuku.", "Richiede Shizuku."),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            } else {
                MetricRow("CPU", "%.1f mAh".format(p.cpuMah), TextPrimary)
                MetricRow(t("Mobile radio", "Radio mobile"), "%.1f mAh".format(p.mobileRadioMah), TextPrimary)
                Spacer(Modifier.height(10.dp))
                p.consumers.forEach { c ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                c.label,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary,
                                maxLines = 1,
                            )
                            if (c.fgsDuration.isNotEmpty()) {
                                Text(
                                    t("foreground service ${c.fgsDuration}", "servizio in primo piano ${c.fgsDuration}"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WarmAmber,
                                )
                            }
                        }
                        Text(
                            "%.2f mAh".format(c.mah),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary,
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    t(
                        "Since the last batterystats reset.",
                        "Dall'ultimo azzeramento di batterystats.",
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── App's own state ──
        DiagCard(t("Optimizer status", "Stato dell'app"), Icons.Default.BatteryFull, ElectricBlue) {
            StatusRow(
                t("Exempt from Doze", "Esente da Doze"),
                SelfMaintenance.selfExempt,
                t("the optimizer itself must not be put to sleep", "l'app non deve essere messa a dormire"),
            )
            StatusRow(
                t("WhatsApp whitelisted", "WhatsApp in whitelist"),
                WhatsAppCallOptimizer.isWhitelisted,
                t("keeps calls from being delayed", "evita ritardi sulle chiamate"),
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun DiagCard(
    title: String,
    icon: ImageVector,
    accent: Color,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceCard)
            .border(1.dp, accent.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
            .padding(16.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = accent,
                )
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            color = valueColor,
        )
    }
}

@Composable
private fun StatusRow(label: String, ok: Boolean, hint: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (ok) NeonCyan else CoralAccent),
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
            Text(hint, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

@Composable
private fun ConflictRow(c: Diagnostics.Conflict, italian: Boolean) {
    val color = when (c.severity) {
        Diagnostics.Severity.CRITICAL -> CoralAccent
        Diagnostics.Severity.WARNING -> WarmAmber
        Diagnostics.Severity.INFO -> TextSecondary
    }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                if (italian) c.titleIt else c.titleEn,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            if (italian) c.detailIt else c.detailEn,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.padding(start = 14.dp),
        )
    }
}

@Composable
private fun InfoBanner(text: String, accent: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(accent.copy(alpha = 0.08f))
            .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(12.dp),
    ) {
        Text(text, style = MaterialTheme.typography.bodySmall, color = accent)
    }
}

private fun healthColor(pct: Int): Color = when {
    pct < 0 -> TextSecondary
    pct >= 90 -> NeonCyan
    pct >= 80 -> WarmAmber
    else -> CoralAccent
}

private fun duration(ms: Long): String {
    val h = TimeUnit.MILLISECONDS.toHours(ms)
    val m = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}
