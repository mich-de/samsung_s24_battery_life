package com.s24optimizer.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.s24optimizer.data.ChargeSchedule
import com.s24optimizer.exec.AdbExecutor
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Drives [ChargeSchedule]: one alarm at a time, set for whichever boundary comes next.
 *
 * A single self-rearming alarm rather than one per boundary, because the thing that matters
 * is the phase the phone should be in *now* — recomputing that from the clock on every wake
 * means a missed alarm, a reboot or a Shizuku outage costs at most one late transition
 * instead of leaving the cap on all day.
 */
class ChargeScheduleReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ChargeSched"
        const val ACTION_TICK = "com.s24optimizer.action.CHARGE_SCHEDULE_TICK"
        private const val REQUEST_CODE = 4201

        // Which half we last applied. In SharedPreferences rather than Settings.Secure so
        // it can be read without shell access, mirroring ScreenOffService's dirty record.
        private const val PREFS = "charge_schedule_state"
        private const val KEY_NIGHT_APPLIED = "night_applied"

        private val io = Executors.newSingleThreadExecutor { r ->
            Thread(r, "charge-sched-io").apply { isDaemon = true }
        }

        private fun nightApplied(context: Context): Boolean =
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_NIGHT_APPLIED, false)

        private fun setNightApplied(context: Context, value: Boolean) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_NIGHT_APPLIED, value).commit()
        }

        private fun pendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, ChargeScheduleReceiver::class.java).setAction(ACTION_TICK)
            return PendingIntent.getBroadcast(
                context, REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        /**
         * Brings the device in line with the schedule and arms the next transition.
         *
         * Safe and cheap to call from anywhere — boot, Shizuku becoming available, the user
         * editing the schedule. While the schedule is enabled it owns `protect_battery`:
         * a value set by hand from One UI is replaced at the next call.
         */
        fun sync(context: Context) {
            val appCtx = context.applicationContext
            io.execute { syncNow(appCtx) }
        }

        /** The body of [sync], on whatever thread the caller has already moved to. */
        private fun syncNow(appCtx: Context) {
            val config = ChargeSchedule.load(appCtx)
            if (!config.enabled || !config.usable) {
                cancelAlarm(appCtx)
                return
            }
            awaitShizuku()
            armAlarm(appCtx, config)
            applyPhase(appCtx, config)
        }

        /**
         * Blocks briefly for Shizuku's binder.
         *
         * The alarm is often the thing that starts the process, and the binder arrives a
         * moment later — so at the instant the boundary fires `permissionsGranted` is false
         * even though Shizuku is running perfectly. Reading that as "no shell available" and
         * returning skipped the transition the alarm existed for, which is exactly what a
         * live 11:52 test caught: the alarm fired and re-armed, and the cap never went on.
         */
        private fun awaitShizuku(timeoutMs: Long = 12_000L) {
            val exec = AdbExecutor.instance
            if (exec.permissionsGranted) return
            val latch = CountDownLatch(1)
            exec.onReady { latch.countDown() }
            // Waited in slices, re-checking the flag directly: a listener registered while
            // the executor is mid-flush can be dropped, and the wait would then run its full
            // length on a phone where Shizuku was ready all along.
            val deadline = timeoutMs / 500
            for (i in 0 until deadline) {
                if (latch.await(500, TimeUnit.MILLISECONDS) || exec.permissionsGranted) return
            }
            // Shizuku really is down. SelfMaintenance's own onReady is still registered, so
            // the phase is applied the moment it comes back.
            Log.w(TAG, "Shizuku did not become ready within ${timeoutMs}ms")
        }

        /**
         * Stops the schedule. If the cap is currently held it is released first: a user who
         * switches this off at night should not be left with a phone that quietly refuses to
         * charge past 80% the next morning.
         */
        fun disable(context: Context) {
            val appCtx = context.applicationContext
            io.execute {
                cancelAlarm(appCtx)
                if (!nightApplied(appCtx)) return@execute
                val exec = AdbExecutor.instance
                if (!exec.permissionsGranted) {
                    // Keep the record: the next sync() undoes it once Shizuku is back.
                    Log.w(TAG, "Shizuku unavailable, cap left in place pending release")
                    return@execute
                }
                exec.execute(ChargeSchedule.DAY_COMMAND)
                setNightApplied(appCtx, false)
            }
        }

        private fun applyPhase(context: Context, config: ChargeSchedule.Config) {
            val exec = AdbExecutor.instance
            if (!exec.permissionsGranted) {
                Log.w(TAG, "Shizuku unavailable, phase not applied")
                return
            }
            // The device is the source of truth, not our own record: a reinstall, or a
            // change made by hand from One UI, leaves the two disagreeing.
            val night = ChargeSchedule.isNightNow(config)
            if (night) {
                val state = exec.executeBatch(listOf(ChargeSchedule.MODE_PROBE, ChargeSchedule.CAP_PROBE))
                val inPlace = state[0].stdout.trim() == ChargeSchedule.MODE_NIGHT &&
                    state[1].stdout.trim() == config.capPct.toString()
                if (inPlace && nightApplied(context)) return
                // Record before executing, so a kill halfway through still leaves a trail.
                setNightApplied(context, true)
                exec.execute(ChargeSchedule.nightCommand(config.capPct))
            } else {
                val released = exec.execute(ChargeSchedule.MODE_PROBE).stdout.trim() !=
                    ChargeSchedule.MODE_NIGHT
                if (released && !nightApplied(context)) return
                exec.execute(ChargeSchedule.DAY_COMMAND)
                setNightApplied(context, false)
            }
            Log.i(TAG, "Phase applied: ${if (night) "night cap ${config.capPct}%" else "day, full charge"}")
        }

        private fun armAlarm(context: Context, config: ChargeSchedule.Config) {
            val am = context.getSystemService(AlarmManager::class.java) ?: return
            val next = ChargeSchedule.nextBoundary(config)
            val pi = pendingIntent(context)
            if (canScheduleExact(context, am)) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, pi)
            } else {
                // Minutes of drift are irrelevant to a charge cap; missing the transition
                // entirely is not. Take the inexact alarm rather than nothing.
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, pi)
            }
            Log.i(TAG, "Next boundary armed for $next")
        }

        private fun cancelAlarm(context: Context) {
            context.getSystemService(AlarmManager::class.java)?.cancel(pendingIntent(context))
        }

        /**
         * From Android 12 an exact alarm needs SCHEDULE_EXACT_ALARM, which is not granted on
         * install. Sending the user to a settings page for it would be the usual answer, but
         * this app already holds shell access, so it grants itself the app op instead.
         */
        private fun canScheduleExact(context: Context, am: AlarmManager): Boolean {
            if (Build.VERSION.SDK_INT < 31) return true
            if (am.canScheduleExactAlarms()) return true
            val exec = AdbExecutor.instance
            if (!exec.permissionsGranted) return false
            exec.execute("cmd appops set ${context.packageName} SCHEDULE_EXACT_ALARM allow")
            return am.canScheduleExactAlarms()
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TICK) return
        Log.i(TAG, "Boundary reached")
        // Returning from onReceive tells the system the process is idle again, and a process
        // the alarm itself started has nothing else keeping it alive — the work would be
        // racing a kill. goAsync() keeps the broadcast open until the transition is done.
        val pending = goAsync()
        val appCtx = context.applicationContext
        io.execute {
            try {
                syncNow(appCtx)
            } catch (e: Exception) {
                Log.e(TAG, "Boundary handling failed", e)
            } finally {
                pending.finish()
            }
        }
    }
}
