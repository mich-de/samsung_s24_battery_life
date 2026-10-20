package com.s24optimizer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.s24optimizer.data.RestartSchedule
import com.s24optimizer.service.RestartScheduleReceiver
import com.s24optimizer.ui.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Configures the scheduled restart: a time, the days it applies to, and the conditions under
 * which it is allowed to go through.
 *
 * Samsung ships the same idea under Device care, but on this device its activity is disabled
 * and `scpm_restart` is off, so there is nothing to turn on. This drives it from a plain alarm
 * and `svc power reboot` instead.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestartScheduleCard(
    italian: Boolean,
    shizukuStatus: Boolean,
    onLog: (String) -> Unit,
) {
    val t = { en: String, it: String -> if (italian) it else en }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Settings.Secure is readable without permission, so the stored schedule shows even while
    // Shizuku is down — it just cannot be changed.
    var config by remember { mutableStateOf(RestartSchedule.load(context)) }
    var picking by remember { mutableStateOf(false) }

    fun commit(updated: RestartSchedule.Config) {
        config = updated
        scope.launch(Dispatchers.IO) {
            val saved = RestartSchedule.save(updated)
            withContext(Dispatchers.Main) {
                onLog(
                    if (saved) "Restart schedule: ${if (updated.enabled) "on" else "off"} " +
                        "${RestartSchedule.formatMinute(updated.minuteOfDay)} " +
                        "days=${updated.dayMask} guards=${updated.guardMask}"
                    else "Restart schedule: save failed (Shizuku unavailable)"
                )
            }
            if (updated.enabled) RestartScheduleReceiver.sync(context)
            else RestartScheduleReceiver.disable(context)
        }
    }

    val dayLabels = if (italian) listOf("L", "M", "M", "G", "V", "S", "D")
    else listOf("M", "T", "W", "T", "F", "S", "S")

    val guards = listOf(
        RestartSchedule.GUARD_SCREEN_OFF to t("screen off", "schermo spento"),
        RestartSchedule.GUARD_CHARGING to t("charging", "in carica"),
        RestartSchedule.GUARD_BATTERY_30 to t("battery >30%", "batteria >30%"),
        RestartSchedule.GUARD_NO_CALL to t("no call", "no chiamata"),
    )

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
                Icons.Default.RestartAlt, null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    t("Scheduled Restart", "Riavvio Programmato"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    t(
                        "Periodic reboot, only while the phone is idle",
                        "Riavvio periodico, solo a telefono fermo",
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

        TimeTile(
            icon = Icons.Default.Schedule,
            label = t("Restart at", "Riavvio alle"),
            value = RestartSchedule.formatMinute(config.minuteOfDay),
            highlighted = config.enabled && config.usable,
            enabled = shizukuStatus,
            modifier = Modifier.fillMaxWidth(),
            onClick = { picking = true },
        )

        Spacer(Modifier.height(16.dp))

        SectionHeader(Icons.Default.Schedule, t("Days", "Giorni"))
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            dayLabels.forEachIndexed { index, label ->
                val bit = 1 shl index
                QuickChip(
                    label = label,
                    selected = config.dayMask and bit != 0,
                    enabled = shizukuStatus,
                    modifier = Modifier.weight(1f),
                    compact = true,
                    onClick = { commit(config.copy(dayMask = config.dayMask xor bit)) },
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        SectionHeader(Icons.Default.Shield, t("Conditions", "Condizioni"))
        Spacer(Modifier.height(8.dp))
        // Two rows of two rather than a flow: four labels of very different widths wrap
        // unevenly, and a fixed grid reads as a checklist, which is what these are.
        guards.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                pair.forEach { (bit, label) ->
                    QuickChip(
                        label = label,
                        selected = config.guard(bit),
                        enabled = shizukuStatus,
                        modifier = Modifier.weight(1f),
                        onClick = { commit(config.copy(guardMask = config.guardMask xor bit)) },
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
        }

        Spacer(Modifier.height(8.dp))

        Text(
            when {
                !shizukuStatus -> t(
                    "Shizuku is offline — the schedule cannot be changed or armed.",
                    "Shizuku è offline: il programma non può essere modificato né armato.",
                )
                !config.usable -> t(
                    "Pick at least one day.",
                    "Scegli almeno un giorno.",
                )
                !config.enabled -> t(
                    "Off. The phone restarts only when you ask it to.",
                    "Disattivo. Il telefono si riavvia solo quando glielo chiedi tu.",
                )
                else -> {
                    val next = RestartSchedule.nextOccurrence(config)
                    val fmt = SimpleDateFormat("EEEE d/MM", if (italian) Locale.ITALIAN else Locale.ENGLISH)
                    val when_ = "${fmt.format(Date(next))} " +
                        t("at", "alle") + " ${RestartSchedule.formatMinute(config.minuteOfDay)}"
                    t(
                        "Next: $when_. If a condition is not met it retries every ${RestartSchedule.RETRY_MINUTES} minutes for up to ${RestartSchedule.GRACE_MINUTES / 60} hours, then waits for the next day.",
                        "Prossimo: $when_. Se una condizione non è soddisfatta riprova ogni ${RestartSchedule.RETRY_MINUTES} minuti fino a ${RestartSchedule.GRACE_MINUTES / 60} ore, poi rimanda al giorno dopo.",
                    )
                }
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (config.enabled && config.usable && shizukuStatus) TextPrimary else TextSecondary,
            lineHeight = 18.sp,
        )

        Spacer(Modifier.height(10.dp))

        // Learned the hard way on 14 August: the phone rebooted at 02:14 and the charge cap
        // stayed on until 10:00, because nothing had restarted Shizuku. Stated on the card so
        // nobody has to work it out from a morning where the schedule silently did nothing.
        Text(
            t(
                "A restart kills Shizuku: everything that needs it — the charge schedule included — stays frozen until you start it again. Until Shizuku starts on boot by itself, weekly is safer than nightly. You get ${RestartSchedule.COUNTDOWN_SECONDS} seconds of warning with a Cancel button.",
                "Ogni riavvio spegne Shizuku: tutto ciò che lo usa — programma ricarica compreso — resta fermo finché non lo riavvii. Finché Shizuku non parte da solo al boot, settimanale è più sicuro di giornaliero. Hai ${RestartSchedule.COUNTDOWN_SECONDS} secondi di preavviso con un pulsante Annulla.",
            ),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary.copy(alpha = 0.75f),
            lineHeight = 16.sp,
        )
    }

    if (picking) {
        val pickerState = rememberTimePickerState(
            initialHour = config.minuteOfDay / 60,
            initialMinute = config.minuteOfDay % 60,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { picking = false },
            containerColor = SurfaceCard,
            title = {
                Text(
                    t("Restart at", "Riavvio alle"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = { TimePicker(state = pickerState) },
            confirmButton = {
                TextButton(onClick = {
                    commit(config.copy(minuteOfDay = pickerState.hour * 60 + pickerState.minute))
                    picking = false
                }) { Text(t("Set", "Imposta")) }
            },
            dismissButton = {
                TextButton(onClick = { picking = false }) { Text(t("Cancel", "Annulla")) }
            },
        )
    }
}
