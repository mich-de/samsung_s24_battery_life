package com.s24optimizer.data

import android.content.Context
import android.provider.Settings
import com.s24optimizer.exec.AdbExecutor
import java.util.Calendar

/**
 * A two-phase charge schedule: cap the charge overnight, let it reach 100% before the alarm.
 *
 * One UI can do this by itself, but only through Samsung's Customization Service, which has
 * to be given permission to profile the user's sleep before it will estimate a window. This
 * asks for none of that: the two boundaries are stated outright and a plain alarm flips
 * `protect_battery` between Maximum and Basic at each one.
 *
 * The configuration lives in `Settings.Secure` rather than SharedPreferences so it survives
 * clearing the app's data, the same way the screen-off and per-app refresh rate markers do.
 * Reading it needs no permission; writing goes through Shizuku.
 */
object ChargeSchedule {

    const val KEY_ENABLED = "s24opt_charge_sched_enabled"
    const val KEY_NIGHT_MINUTE = "s24opt_charge_sched_night"
    const val KEY_DAY_MINUTE = "s24opt_charge_sched_day"
    const val KEY_CAP = "s24opt_charge_sched_cap"

    /** 00:00 — the user's stated bedtime boundary. */
    const val DEFAULT_NIGHT_MINUTE = 0

    /** 07:00, an hour before an 08:00 alarm: long enough to climb from the cap to full. */
    const val DEFAULT_DAY_MINUTE = 7 * 60

    const val DEFAULT_CAP = 80

    data class Config(
        val enabled: Boolean = false,
        /** Minute of the day at which the cap goes on. */
        val nightMinute: Int = DEFAULT_NIGHT_MINUTE,
        /** Minute of the day at which charging is released back to 100%. */
        val dayMinute: Int = DEFAULT_DAY_MINUTE,
        val capPct: Int = DEFAULT_CAP,
    ) {
        /** A schedule whose two boundaries coincide has no night at all. */
        val usable: Boolean get() = nightMinute != dayMinute && capPct in 50..95
    }

    /**
     * Maximum: charging stops at [capPct] and does not resume until the level falls below it.
     * It caps, it never discharges — a phone already above [capPct] simply stops taking
     * current and stays where it is.
     */
    fun nightCommand(capPct: Int) =
        "settings put global battery_protection_threshold $capPct; settings put global protect_battery 1"

    /** Basic: charges to 100%, then waits for 95% before topping up again. */
    const val DAY_COMMAND = "settings put global protect_battery 3"

    /** Reads back the state the charger is actually in, whatever we believe we set. */
    const val MODE_PROBE = "settings get global protect_battery"
    const val CAP_PROBE = "settings get global battery_protection_threshold"
    const val MODE_NIGHT = "1"

    fun load(context: Context): Config {
        val cr = context.contentResolver
        fun int(key: String, default: Int): Int =
            try { Settings.Secure.getInt(cr, key) } catch (_: Settings.SettingNotFoundException) { default }

        return Config(
            enabled = int(KEY_ENABLED, 0) == 1,
            nightMinute = int(KEY_NIGHT_MINUTE, DEFAULT_NIGHT_MINUTE).coerceIn(0, 1439),
            dayMinute = int(KEY_DAY_MINUTE, DEFAULT_DAY_MINUTE).coerceIn(0, 1439),
            capPct = int(KEY_CAP, DEFAULT_CAP).coerceIn(50, 95),
        )
    }

    /** Persists [config]. Needs Shizuku; returns false if any write was refused. */
    fun save(config: Config): Boolean {
        val exec = AdbExecutor.instance
        if (!exec.permissionsGranted) return false
        val results = exec.executeBatch(
            listOf(
                "settings put secure $KEY_ENABLED ${if (config.enabled) 1 else 0}",
                "settings put secure $KEY_NIGHT_MINUTE ${config.nightMinute}",
                "settings put secure $KEY_DAY_MINUTE ${config.dayMinute}",
                "settings put secure $KEY_CAP ${config.capPct}",
            )
        )
        return results.all { it.isSuccess }
    }

    /**
     * Whether [minuteOfDay] falls in the capped half. The window is allowed to wrap past
     * midnight, which is the normal case for a 21:00 → 07:00 schedule.
     */
    fun isNightAt(config: Config, minuteOfDay: Int): Boolean =
        if (config.nightMinute <= config.dayMinute) {
            minuteOfDay >= config.nightMinute && minuteOfDay < config.dayMinute
        } else {
            minuteOfDay >= config.nightMinute || minuteOfDay < config.dayMinute
        }

    fun minuteOfDay(millis: Long): Int {
        val c = Calendar.getInstance().apply { timeInMillis = millis }
        return c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)
    }

    fun isNightNow(config: Config, now: Long = System.currentTimeMillis()): Boolean =
        isNightAt(config, minuteOfDay(now))

    /** Wall-clock time of the next occurrence of [minuteOfDay], strictly after [from]. */
    fun nextOccurrence(minuteOfDay: Int, from: Long): Long {
        val c = Calendar.getInstance().apply {
            timeInMillis = from
            set(Calendar.HOUR_OF_DAY, minuteOfDay / 60)
            set(Calendar.MINUTE, minuteOfDay % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // Also covers the DST-shifted case, where the requested minute may not exist today.
        if (c.timeInMillis <= from) c.add(Calendar.DAY_OF_YEAR, 1)
        return c.timeInMillis
    }

    /** Whichever of the two boundaries comes first from [from]. */
    fun nextBoundary(config: Config, from: Long = System.currentTimeMillis()): Long =
        minOf(nextOccurrence(config.nightMinute, from), nextOccurrence(config.dayMinute, from))

    fun formatMinute(minuteOfDay: Int): String =
        "%02d:%02d".format(minuteOfDay / 60, minuteOfDay % 60)
}
