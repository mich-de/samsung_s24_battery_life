package com.s24optimizer.data

import android.content.Context
import com.s24optimizer.exec.AdbExecutor
import com.s24optimizer.service.ScreenOffService

/**
 * Read-only inspection of the things that actually drive battery drain on this device:
 * radio quality, the heaviest consumers, and settings that contradict each other.
 *
 * Nothing here changes state — it only reports, so the user can decide.
 */
object Diagnostics {

    data class Radio(
        val rssi: Int,
        val rsrp: Int,
        val rsrq: Int,
        val sinr: Int,
        val band: String,
        val operator: String,
        /** Allowed radio technologies, e.g. "GPRS|EDGE|UMTS|LTE|GSM|LTE_CA". */
        val networkMode: String,
    ) {
        val hasSignal: Boolean get() = rsrp != 0

        /** The modem is allowed to use 5G, so it will spend power looking for it. */
        val nrEnabled: Boolean get() = networkMode.split("|").any { it.trim() == "NR" }

        /** Below roughly -105 dBm the modem starts spending real power to hold the link. */
        val weak: Boolean get() = hasSignal && rsrp <= -105

        val quality: String
            get() = when {
                !hasSignal -> "?"
                rsrp > -90 -> "Ottimo"
                rsrp > -100 -> "Buono"
                rsrp > -110 -> "Debole"
                else -> "Critico"
            }
    }

    data class Consumer(
        val uid: String,
        val label: String,
        val mah: Float,
        /** Foreground-service time, if the dump reported any. */
        val fgsDuration: String,
    )

    data class PowerUse(
        val capacityMah: Int,
        val ratedMah: Int,
        val cpuMah: Float,
        val mobileRadioMah: Float,
        val screenOnDuration: String,
        val consumers: List<Consumer>,
    )

    enum class Severity { INFO, WARNING, CRITICAL }

    /** Above this, a lithium cell held on the charger ages measurably faster. */
    private const val HELD_FULL_PCT = 95

    data class Conflict(
        val id: String,
        val titleIt: String,
        val titleEn: String,
        val detailIt: String,
        val detailEn: String,
        val severity: Severity,
    )

    // ── Radio ──

    fun readRadio(exec: AdbExecutor): Radio? {
        if (!exec.permissionsGranted) return null
        // mCellInfo is enormous, so pull only the registered cell's identity out of it.
        val out = exec.execute(
            "dumpsys telephony.registry | grep -m3 mSignalStrength; " +
                "dumpsys telephony.registry | grep -m1 -o 'mRegistered=YES.\\{0,220\\}'; " +
                // What the modem is actually allowed to use. `preferred_network_mode` is
                // the legacy global and is not rewritten when the allowed-types bitmask
                // changes, so it keeps claiming 5G long after NR was switched off.
                "for s in 0 1; do cmd phone get-allowed-network-types-for-users -s \$s; done"
        ).stdout
        if (out.isBlank()) return null

        // Only the entry with a live LTE measurement is useful; the others read "Invalid".
        val lte = Regex("""mLte=CellSignalStrengthLte: rssi=(-?\d+) rsrp=(-?\d+) rsrq=(-?\d+) rssnr=(-?\d+)""")
            .find(out)?.groupValues

        val registered = Regex("""mRegistered=YES.*?mBands=\[(\d+)].*?mAlphaLong=([^\s]*)""")
            .find(out)?.groupValues

        // e.g. "GPRS|EDGE|UMTS|HSDPA|HSUPA|HSPA|LTE|HSPA+|GSM|LTE_CA". Slots with no
        // subscription print an error line instead, which simply never matches.
        val mode = Regex("""^[A-Z0-9+_]+(?:\|[A-Z0-9+_]+)+$""", RegexOption.MULTILINE)
            .findAll(out).lastOrNull()?.value.orEmpty()

        return Radio(
            rssi = lte?.getOrNull(1)?.toIntOrNull() ?: 0,
            rsrp = lte?.getOrNull(2)?.toIntOrNull() ?: 0,
            rsrq = lte?.getOrNull(3)?.toIntOrNull() ?: 0,
            // The modem reports Integer.MAX_VALUE when SINR is unavailable.
            sinr = lte?.getOrNull(4)?.toIntOrNull()?.takeIf { it != Int.MAX_VALUE } ?: 0,
            band = registered?.getOrNull(1)?.let { "B$it" }.orEmpty(),
            operator = registered?.getOrNull(2).orEmpty(),
            networkMode = mode,
        )
    }

    // ── Power use ──

    fun readPowerUse(exec: AdbExecutor, uidLabels: Map<String, String>): PowerUse? {
        if (!exec.permissionsGranted) return null
        // One pass over the dump: the four-space indent picks the global totals rather
        // than the per-screen-state breakdowns nested under them.
        val out = exec.execute(
            "dumpsys batterystats | grep -E " +
                "'Capacity: |^    cpu: |^    mobile_radio: |^  UID |Screen on: ' | head -20",
            timeoutMs = 30_000L,
        ).stdout
        if (out.isBlank()) return null

        fun f(pattern: String): Float =
            Regex(pattern).find(out)?.groupValues?.getOrNull(1)?.toFloatOrNull() ?: 0f

        val consumers = Regex("""^  UID (\S+): ([\d.]+)(.*)$""", RegexOption.MULTILINE)
            .findAll(out)
            .map { m ->
                val uid = m.groupValues[1]
                Consumer(
                    uid = uid,
                    label = uidLabels[uid] ?: systemUidLabel(uid) ?: uid,
                    mah = m.groupValues[2].toFloatOrNull() ?: 0f,
                    fgsDuration = Regex("""fgs: [\d.]+ \(([^)]+)\)""")
                        .find(m.groupValues[3])?.groupValues?.getOrNull(1).orEmpty(),
                )
            }
            .sortedByDescending { it.mah }
            .take(10)
            .toList()

        return PowerUse(
            capacityMah = f("""Capacity: ([\d.]+)""").toInt(),
            ratedMah = f("""Rated: ([\d.]+)""").toInt(),
            cpuMah = f("""(?m)^    cpu: ([\d.]+)"""),
            mobileRadioMah = f("""(?m)^    mobile_radio: ([\d.]+)"""),
            screenOnDuration = Regex("""Screen on: (\S+)""").find(out)?.groupValues?.getOrNull(1).orEmpty(),
            consumers = consumers,
        )
    }

    /** Maps `u0aNNN` / numeric UIDs to package names via `pm list packages -U`. */
    fun readUidLabels(exec: AdbExecutor): Map<String, String> {
        if (!exec.permissionsGranted) return emptyMap()
        val out = exec.execute("pm list packages -U").stdout
        val map = HashMap<String, String>()
        for (m in Regex("""package:(\S+) uid:(\d+)""").findAll(out)) {
            val pkg = m.groupValues[1]
            val uid = m.groupValues[2].toIntOrNull() ?: continue
            val key = if (uid >= 10000) "u0a${uid - 10000}" else uid.toString()
            // Shared UIDs list several packages; first one wins, which is good enough
            // for a label.
            map.putIfAbsent(key, pkg)
        }
        return map
    }

    private fun systemUidLabel(uid: String): String? = when (uid) {
        "0" -> "kernel / root"
        "1000" -> "android (system)"
        "1001" -> "radio / telephony"
        "1010" -> "wifi"
        "1041" -> "audioserver"
        "1046" -> "mediaserver"
        "5004" -> "sistema (Samsung)"
        else -> null
    }

    // ── Conflicts ──

    fun detectConflicts(
        context: Context,
        exec: AdbExecutor,
        health: BatteryTelemetry.Health?,
        radio: Radio?,
        basic: BatteryTelemetry.Basic,
    ): List<Conflict> {
        val found = mutableListOf<Conflict>()
        if (!exec.permissionsGranted) return found

        val probe = exec.executeBatch(
            listOf(
                "settings get global adaptive_battery_management_enabled",
                "pm list packages -d | grep -c com.samsung.android.rubin.app",
                "settings get global ble_scan_always_enabled",
                "settings get global low_power",
                "cmd deviceidle whitelist",
            )
        )
        fun v(i: Int) = probe.getOrNull(i)?.stdout?.trim().orEmpty()

        val adaptiveBattery = v(0) == "1"
        val rubinDisabled = (v(1).toIntOrNull() ?: 0) > 0
        val bleScanOff = v(2) == "0"
        val lowPower = v(3) == "1"
        val whitelist = v(4)
        // Not `settings get global auto_sync`: that row does not exist here, so the probe
        // returned "null" and the orphan check below could never fire. SyncManager is the
        // only thing that knows, and it answers through ContentResolver.
        val autoSyncOff = !MasterSync.isEnabled()

        if (adaptiveBattery && rubinDisabled) {
            found += Conflict(
                "rubin_vs_adaptive",
                "Adaptive Battery senza motore di apprendimento",
                "Adaptive Battery without its learning engine",
                "Adaptive Battery è attivo ma Samsung Rubin (Customization Service) è disabilitato: il sistema continua a raccogliere dati d'uso senza poterli usare. Scegli: o riattivi Rubin, o disattivi Adaptive Battery.",
                "Adaptive Battery is on but Samsung Rubin is disabled, so usage data is collected and never used. Either re-enable Rubin or turn Adaptive Battery off.",
                Severity.WARNING,
            )
        }

        // Reported from what the cell is doing, not from the protection flags. In Basic
        // mode reaching 100% is the designed behaviour, so this is information, not a
        // fault: sitting above 95% is what ages the cell, and that much is observable.
        if (health?.protectOn == true && !health.protectCapped &&
            basic.plugged && basic.level >= HELD_FULL_PCT
        ) {
            found += Conflict(
                "batt_held_at_full",
                "Batteria al ${basic.level}% sotto carica",
                "Battery at ${basic.level}% on the charger",
                "La protezione è su Base: carica fino al 100% e riprende solo sotto il 95%, non c'è nessun tetto. È lo stare a lungo sopra il 95% che invecchia la cella, non il numero di cicli. Stacca quando puoi, oppure passa a Massima per un tetto fisso all'${health.protectThresholdPct.takeIf { it > 0 } ?: 80}%.",
                "Protection is set to Basic: it charges to 100% and only resumes below 95% — there is no cap. Time spent above 95% is what ages the cell, not cycle count. Unplug when you can, or switch to Maximum for a fixed ${health.protectThresholdPct.takeIf { it > 0 } ?: 80}% ceiling.",
                Severity.INFO,
            )
        }

        // Maximum is a hard ceiling at every hour of the day. If the pack is above it and
        // still taking current, the setting is not reaching the charger and the app would
        // otherwise keep reporting a limit that is not there. Being parked above the cap
        // without drawing current is normal — the cap stops charging, it never discharges.
        if (health?.protectCapped == true && basic.plugged && basic.currentNowUa > 0 &&
            health.protectThresholdPct in 1..99 && basic.level > health.protectThresholdPct + 2
        ) {
            found += Conflict(
                "batt_cap_not_enforced",
                "Tetto ${health.protectThresholdPct}% impostato ma superato",
                "${health.protectThresholdPct}% cap set but exceeded",
                "La protezione è su Massima con tetto ${health.protectThresholdPct}%, ma la batteria è al ${basic.level}% sotto carica: il limite non sta arrivando al caricabatterie. Riapri Impostazioni > Batteria > Protezione batteria e riseleziona Massima.",
                "Protection is set to Maximum with a ${health.protectThresholdPct}% cap, yet the battery is at ${basic.level}% on the charger: the limit is not reaching the charger. Re-open Settings > Battery > Battery protection and pick Maximum again.",
                Severity.WARNING,
            )
        }

        // Both the charge schedule and the Battery Protection optimizations write
        // protect_battery. Whoever wrote last wins until the next boundary, at which point
        // the schedule takes it back — so a cap applied by hand looks like it stopped working.
        val schedule = ChargeSchedule.load(context)
        if (schedule.enabled && schedule.usable && health != null) {
            val night = ChargeSchedule.isNightNow(schedule)
            if (night != health.protectCapped) {
                val boundary = ChargeSchedule.formatMinute(
                    if (night) schedule.dayMinute else schedule.nightMinute
                )
                found += Conflict(
                    "charge_schedule_overridden",
                    "Protezione batteria fuori sincrono col programma",
                    "Battery protection out of step with the schedule",
                    if (night)
                        "Il programma è in fascia notturna (tetto ${schedule.capPct}%) ma la protezione non è su Massima: qualcosa l'ha cambiata dopo l'ultimo passaggio. Riaprire l'app la rimette a posto; altrimenti il programma la recupera alle $boundary."
                    else
                        "Il programma è in fascia diurna (ricarica al 100%) ma la protezione è su Massima: un tetto impostato a mano qui viene rimosso dal programma alle $boundary. Se vuoi un tetto fisso tutto il giorno, disattiva il programma.",
                    if (night)
                        "The schedule is in its night window (${schedule.capPct}% cap) but protection is not set to Maximum: something changed it since the last transition. Re-opening the app restores it, otherwise the schedule reclaims it at $boundary."
                    else
                        "The schedule is in its day window (charge to 100%) but protection is set to Maximum: a cap set by hand here is removed by the schedule at $boundary. Turn the schedule off if you want a cap that holds all day.",
                    Severity.INFO,
                )
            }
        }

        if (bleScanOff && whitelist.contains("health")) {
            found += Conflict(
                "ble_scan_vs_wearable",
                "Scansione BLE disattivata con app indossabili esenti da Doze",
                "BLE scanning off while a wearable app is Doze-exempt",
                "La scansione Bluetooth in background è disattivata, ma un'app per indossabili è esente da Doze: si sveglia in continuazione senza riuscire a trovare il dispositivo.",
                "Background Bluetooth scanning is off while a wearable companion app is Doze-exempt: it wakes repeatedly and never finds the device.",
                Severity.WARNING,
            )
        }

        // The screen-off service sets these and is supposed to undo them. If they are
        // still applied while the screen is on, something left them behind.
        val psmFeature = ScreenOffService.MARKER_PSM in ScreenOffService.getActiveFeatures(context)
        val syncFeature = ScreenOffService.MARKER_SYNC in ScreenOffService.getActiveFeatures(context)
        if (basic.screenOn && lowPower && psmFeature) {
            found += Conflict(
                "orphan_low_power",
                "Risparmio energetico rimasto attivo a schermo acceso",
                "Power saving stuck on with the screen on",
                "PSM Automatico ha lasciato il risparmio energetico attivo. Verrà ripristinato al prossimo cambio di stato dello schermo; se persiste, disattiva la mod.",
                "Auto PSM left power saving enabled. It will be restored on the next screen transition; if it persists, turn the mod off.",
                Severity.CRITICAL,
            )
        }
        if (basic.screenOn && autoSyncOff && syncFeature) {
            found += Conflict(
                "orphan_auto_sync",
                "Sincronizzazione account rimasta disattivata",
                "Account sync left disabled",
                "La mod di sincronizzazione ha lasciato auto-sync spento: mail e calendario non si aggiornano.",
                "The sync mod left auto-sync off: mail and calendar are not updating.",
                Severity.CRITICAL,
            )
        }

        if (radio != null && radio.weak && radio.nrEnabled) {
            found += Conflict(
                "nr_at_cell_edge",
                "5G attivo con segnale debole",
                "5G enabled on a weak signal",
                "RSRP ${radio.rsrp} dBm: il modem cerca il 5G a bordo cella e consuma molto. Passa a LTE/3G/2G da Impostazioni > Connessioni > Reti mobili > Modalità di rete.",
                "RSRP ${radio.rsrp} dBm: the modem keeps scanning for 5G at the cell edge, which is expensive. Switch to LTE/3G/2G in Settings > Connections > Mobile networks > Network mode.",
                Severity.WARNING,
            )
        }

        // No entry for the recorded peak temperature: it is a high-water mark the firmware
        // keeps for the life of the pack, so it can never be cleared and would sit in this
        // list forever. The Health card already shows it, in red once it passes 45°C.

        return found
    }
}
