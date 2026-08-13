package com.s24optimizer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.s24optimizer.data.ChargeSchedule
import com.s24optimizer.service.ChargeScheduleReceiver
import com.s24optimizer.ui.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Configures the overnight charge cap: the settings One UI only offers through its
 * sleep-profiling service, stated as two plain times instead.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChargeScheduleCard(
    italian: Boolean,
    shizukuStatus: Boolean,
    onLog: (String) -> Unit,
) {
    val t = { en: String, it: String -> if (italian) it else en }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Settings.Secure is readable without any permission, so the card shows the real
    // stored schedule even while Shizuku is down — it just cannot change it.
    var config by remember { mutableStateOf(ChargeSchedule.load(context)) }
    var pickingNight by remember { mutableStateOf<Boolean?>(null) }

    fun commit(updated: ChargeSchedule.Config) {
        config = updated
        scope.launch(Dispatchers.IO) {
            val saved = ChargeSchedule.save(updated)
            withContext(Dispatchers.Main) {
                onLog(
                    if (saved) "Charge schedule: ${if (updated.enabled) "on" else "off"} " +
                        "${ChargeSchedule.formatMinute(updated.nightMinute)}→" +
                        "${ChargeSchedule.formatMinute(updated.dayMinute)} @ ${updated.capPct}%"
                    else "Charge schedule: save failed (Shizuku unavailable)"
                )
            }
            if (updated.enabled) ChargeScheduleReceiver.sync(context)
            else ChargeScheduleReceiver.disable(context)
        }
    }

    val nightNow = config.usable && ChargeSchedule.isNightNow(config)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceElevated)
            .border(1.dp, OutlineDim.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(20.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                Icons.Default.BatteryChargingFull, null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    t("Charge Schedule", "Programma Ricarica"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    t(
                        "Cap overnight, full before the alarm",
                        "Tetto di notte, pieno prima della sveglia",
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
            Switch(
                checked = config.enabled,
                enabled = shizukuStatus,
                onCheckedChange = { commit(config.copy(enabled = it)) },
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            TimeTile(
                icon = Icons.Default.Bedtime,
                label = t("Cap at", "Tetto dalle"),
                value = ChargeSchedule.formatMinute(config.nightMinute),
                highlighted = config.enabled && nightNow,
                enabled = shizukuStatus,
                modifier = Modifier.weight(1f),
                onClick = { pickingNight = true },
            )
            TimeTile(
                icon = Icons.Default.WbSunny,
                label = t("Full by", "Pieno alle"),
                value = ChargeSchedule.formatMinute(config.dayMinute),
                highlighted = config.enabled && !nightNow,
                enabled = shizukuStatus,
                modifier = Modifier.weight(1f),
                onClick = { pickingNight = false },
            )
        }

        Spacer(Modifier.height(16.dp))

        SectionHeader(Icons.Default.BatteryChargingFull, t("Night cap", "Tetto notturno"))
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(80, 85, 90).forEach { pct ->
                QuickChip(
                    label = "$pct%",
                    selected = config.capPct == pct,
                    enabled = shizukuStatus,
                    onClick = { commit(config.copy(capPct = pct)) },
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Text(
            when {
                !shizukuStatus -> t(
                    "Shizuku is offline — the schedule cannot be changed or applied.",
                    "Shizuku è offline: il programma non può essere modificato né applicato.",
                )
                !config.usable -> t(
                    "The two times must differ.",
                    "I due orari devono essere diversi.",
                )
                !config.enabled -> t(
                    "Off. One UI battery protection is left as you set it.",
                    "Disattivo. La protezione batteria di One UI resta come l'hai impostata.",
                )
                nightNow -> t(
                    "Now: Maximum, charging stops at ${config.capPct}%. Released at ${ChargeSchedule.formatMinute(config.dayMinute)}.",
                    "Ora: Massima, la ricarica si ferma al ${config.capPct}%. Rilasciata alle ${ChargeSchedule.formatMinute(config.dayMinute)}.",
                )
                else -> t(
                    "Now: Basic, charges to 100%. Cap returns at ${ChargeSchedule.formatMinute(config.nightMinute)}.",
                    "Ora: Base, carica al 100%. Il tetto torna alle ${ChargeSchedule.formatMinute(config.nightMinute)}.",
                )
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (config.enabled && config.usable && shizukuStatus) TextPrimary else TextSecondary,
            lineHeight = 18.sp,
        )

        Spacer(Modifier.height(10.dp))

        // The single most misread thing about this feature, so it is stated on the card
        // rather than left to be discovered on a morning when nothing happened.
        Text(
            t(
                "The cap stops charging — it never discharges. A phone already above ${config.capPct}% at ${ChargeSchedule.formatMinute(config.nightMinute)} simply stays there until you use it. Set the cap time before you plug in for the night.",
                "Il tetto ferma la ricarica, non scarica. Se alle ${ChargeSchedule.formatMinute(config.nightMinute)} il telefono è già sopra il ${config.capPct}% resta lì finché non lo usi. Imposta l'ora del tetto prima di attaccarlo per la notte.",
            ),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary.copy(alpha = 0.75f),
            lineHeight = 16.sp,
        )
    }

    if (pickingNight != null) {
        val night = pickingNight == true
        val initial = if (night) config.nightMinute else config.dayMinute
        val pickerState = rememberTimePickerState(
            initialHour = initial / 60,
            initialMinute = initial % 60,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { pickingNight = null },
            containerColor = SurfaceCard,
            title = {
                Text(
                    if (night) t("Cap charging from", "Tetto ricarica dalle")
                    else t("Release to 100% at", "Rilascia al 100% alle"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = { TimePicker(state = pickerState) },
            confirmButton = {
                TextButton(onClick = {
                    val minute = pickerState.hour * 60 + pickerState.minute
                    commit(
                        if (night) config.copy(nightMinute = minute)
                        else config.copy(dayMinute = minute)
                    )
                    pickingNight = null
                }) { Text(t("Set", "Imposta")) }
            },
            dismissButton = {
                TextButton(onClick = { pickingNight = null }) { Text(t("Cancel", "Annulla")) }
            },
        )
    }
}

@Composable
private fun TimeTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    highlighted: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val accent = MaterialTheme.colorScheme.primary
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (highlighted) accent.copy(alpha = 0.12f) else SurfaceCard)
            .border(
                if (highlighted) 1.5.dp else 1.dp,
                if (highlighted) accent.copy(alpha = 0.5f) else OutlineDim.copy(alpha = 0.25f),
                RoundedCornerShape(16.dp),
            )
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon, null,
                modifier = Modifier.size(14.dp),
                tint = if (highlighted) accent else TextSecondary,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = if (highlighted) accent else TextPrimary,
        )
    }
}
