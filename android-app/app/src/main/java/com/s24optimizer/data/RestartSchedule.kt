package com.s24optimizer.data

import android.content.Context
import android.provider.Settings
import com.s24optimizer.exec.AdbExecutor
import java.util.Calendar

/**
 * A weekly restart schedule: reboot the phone at a chosen time, on chosen days, but only while
 * nobody is using it.
 *
 * One UI has the same idea — Samsung's "Auto restart" under Device care — but on this device
 * `scpm_restart` is off and its activity is disabled from the factory, so there is nothing to
 * drive. A periodic reboot is the standard answer to One UI drifting after a few days up:
 * fragmented memory, Samsung services accumulating state, the post-update drain documented in
 * the project notes.
 *
 * Config lives in `Settings.Secure`, exactly as [ChargeSchedule] does, so it survives clearing
 * the app's data. Reading needs no permission; writing goes through Shizuku.
 */
object RestartSchedule {

    const val KEY_ENABLED = "s24opt_restart_enabled"
    const val KEY_MINUTE = "s24opt_restart_minute"
    const val KEY_DAYS = "s24opt_restart_days"
    const val KEY_GUARDS = "s24opt_restart_guards"

    /** 03:30 — past the deepest part of the night, well before any plausible alarm. */
    const val DEFAULT_MINUTE = 3 * 60 + 30

    /** Sunday only. Weekly rather than nightly because every reboot kills Shizuku. */
    const val DEFAULT_DAYS = 1 shl 6

    // Guard bits. Stored rather than hard-wired so the card can show them as chips and the
    // user can loosen one knowing exactly which one they loosened.
    const val GUARD_SCREEN_OFF = 1
    const val GUARD_CHARGING = 1 shl 1
    const val GUARD_BATTERY_30 = 1 shl 2
    const val GUARD_NO_CALL = 1 shl 3
    const val GUARD_ALL = GUARD_SCREEN_OFF or GUARD_CHARGING or GUARD_BATTERY_30 or GUARD_NO_CALL

    /** Battery floor for [GUARD_BATTERY_30]: a reboot costs a few percent by itself. */
    const val BATTERY_FLOOR_PCT = 30

    /**
     * How long past the scheduled minute a blocked round keeps trying, and how often. Two
     * hours because a restart at 05:30 instead of 03:30 is still fine; at 09:00 it is not.
     */
    const val GRACE_MINUTES = 120
    const val RETRY_MINUTES = 15

    /** Seconds of cancellable warning before the reboot goes through. */
    const val COUNTDOWN_SECONDS = 60

    /**
     * `svc` is the shell's own front end to PowerManager and `com.android.shell` holds
     * android.permission.REBOOT, so no root is needed. The reason string is not decoration:
     * it lands in `ro.boot.bootreason` as `reboot,s24opt_scheduled`, which is what tells a
     * restart of ours apart from a `reboot,rescueparty` at a glance.
     */
    const val REBOOT_COMMAND = "svc power reboot s24opt_scheduled"

    /**
     * Last time the user touched the device, in `SystemClock.uptimeMillis()`, straight from
     * PowerManagerService.
     *
     * The screen-off guard alone is not enough during the warning. Screen timeout on this
     * phone is 30 s, so a user who picks it up, reads the notification and puts it down
     * without tapping Cancel is asleep again well before the countdown ends — and the guard
     * happily re-passes. That is exactly what happened on the first live test on 14 August:
     * the screen was woken at 11:43:03, and the reboot still went through at 11:44:00.
     */
    const val USER_ACTIVITY_PROBE =
        "dumpsys power | grep -o 'mLastUserActivityTime=[0-9]*' | head -1 | cut -d= -f2"

    /** Bit 0 = Monday … bit 6 = Sunday, matching [dayBit]'s ISO ordering. */
    data class Config(
        val enabled: Boolean = false,
        val minuteOfDay: Int = DEFAULT_MINUTE,
        val dayMask: Int = DEFAULT_DAYS,
        val guardMask: Int = GUARD_ALL,
    ) {
        /** A schedule with no day selected can never fire. */
        val usable: Boolean get() = dayMask != 0

        fun hasDay(isoDay: Int): Boolean = dayMask and (1 shl (isoDay - 1)) != 0

        fun guard(bit: Int): Boolean = guardMask and bit != 0
    }

    fun load(context: Context): Config {
        val cr = context.contentResolver
        fun int(key: String, default: Int): Int =
            try { Settings.Secure.getInt(cr, key) } catch (_: Settings.SettingNotFoundException) { default }

        return Config(
            enabled = int(KEY_ENABLED, 0) == 1,
            minuteOfDay = int(KEY_MINUTE, DEFAULT_MINUTE).coerceIn(0, 1439),
            dayMask = int(KEY_DAYS, DEFAULT_DAYS) and 0x7F,
            guardMask = int(KEY_GUARDS, GUARD_ALL) and GUARD_ALL,
        )
    }

    /** Persists [config]. Needs Shizuku; returns false if any write was refused. */
    fun save(config: Config): Boolean {
        val exec = AdbExecutor.instance
        if (!exec.permissionsGranted) return false
        val results = exec.executeBatch(
            listOf(
                "settings put secure $KEY_ENABLED ${if (config.enabled) 1 else 0}",
                "settings put secure $KEY_MINUTE ${config.minuteOfDay}",
                "settings put secure $KEY_DAYS ${config.dayMask}",
                "settings put secure $KEY_GUARDS ${config.guardMask}",
            )
        )
        return results.all { it.isSuccess }
    }

    /** ISO day of week (1 = Monday … 7 = Sunday) for a [Calendar] day constant. */
    private fun isoDay(calendarDay: Int): Int =
        if (calendarDay == Calendar.SUNDAY) 7 else calendarDay - 1

    /**
     * Wall-clock time of the next selected day whose [Config.minuteOfDay] falls strictly after
     * [from]. Scans a full week, so "today, later" and "next Sunday" are the same code path.
     */
    fun nextOccurrence(config: Config, from: Long = System.currentTimeMillis()): Long {
        if (!config.usable) return Long.MAX_VALUE
        val c = Calendar.getInstance().apply {
            timeInMillis = from
            set(Calendar.HOUR_OF_DAY, config.minuteOfDay / 60)
            set(Calendar.MINUTE, config.minuteOfDay % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // Eight steps, not seven: today's slot may already have passed, in which case the
        // same weekday a week out is the answer.
        for (i in 0..7) {
            if (c.timeInMillis > from && config.hasDay(isoDay(c.get(Calendar.DAY_OF_WEEK)))) {
                return c.timeInMillis
            }
            c.add(Calendar.DAY_OF_YEAR, 1)
        }
        return Long.MAX_VALUE
    }

    /** Time of day only, shared with the charge schedule rather than rewritten. */
    fun formatMinute(minuteOfDay: Int): String = ChargeSchedule.formatMinute(minuteOfDay)
}
