package com.s24optimizer.service

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.SystemClock
import android.util.Log
import com.s24optimizer.data.BatteryTelemetry
import com.s24optimizer.data.RestartSchedule
import com.s24optimizer.exec.AdbExecutor
import java.util.Locale
import java.util.concurrent.Executors

/**
 * Drives [RestartSchedule]: one self-rearming alarm, a guard check, a cancellable warning, and
 * only then the reboot.
 *
 * Shaped like [ChargeScheduleReceiver] — one alarm at a time, recomputed from the clock on
 * every wake — because the same reasoning applies: a missed alarm, a reboot or a Shizuku
 * outage should cost one skipped round, not leave the schedule dead.
 *
 * The guards are the whole point. Rebooting a phone that is in the user's hand, or on a call,
 * or off the charger at 20%, would make the feature worse than not having it. When a guard
 * blocks, the round is not thrown away: it retries every [RestartSchedule.RETRY_MINUTES] until
 * [RestartSchedule.GRACE_MINUTES] past the scheduled time, then defers to the next occurrence.
 */
class RestartScheduleReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "RestartSched"

        const val ACTION_TICK = "com.s24optimizer.action.RESTART_SCHEDULE_TICK"
        const val ACTION_CONFIRM = "com.s24optimizer.action.RESTART_SCHEDULE_CONFIRM"
        const val ACTION_CANCEL = "com.s24optimizer.action.RESTART_SCHEDULE_CANCEL"

        /** Carries the end of the retry window across re-arms, so no state has to be stored. */
        private const val EXTRA_DEADLINE = "deadline"

        private const val REQUEST_TICK = 4301
        private const val REQUEST_CONFIRM = 4302
        private const val REQUEST_CANCEL = 4303

        private const val CHANNEL_ID = "restart_schedule"
        private const val NOTIF_ID = 4301

        // When the warning went up, on the same uptime clock PowerManagerService uses for
        // mLastUserActivityTime. In SharedPreferences because the process can be killed
        // between the warning and the confirm.
        private const val PREFS = "restart_schedule_state"
        private const val KEY_WARNED_AT = "warned_at"

        private val io = Executors.newSingleThreadExecutor { r ->
            Thread(r, "restart-sched-io").apply { isDaemon = true }
        }

        private val italian: Boolean get() = Locale.getDefault().language == "it"

        private fun t(en: String, it: String) = if (italian) it else en

        // ---- alarms -------------------------------------------------------------------

        private fun tickIntent(context: Context, deadline: Long): PendingIntent {
            val intent = Intent(context, RestartScheduleReceiver::class.java)
                .setAction(ACTION_TICK)
                .putExtra(EXTRA_DEADLINE, deadline)
            return PendingIntent.getBroadcast(
                context, REQUEST_TICK, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        private fun actionIntent(context: Context, action: String, request: Int): PendingIntent {
            val intent = Intent(context, RestartScheduleReceiver::class.java).setAction(action)
            return PendingIntent.getBroadcast(
                context, request, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        /**
         * Arms the schedule, or clears it if it is off. Cheap and safe to call from anywhere —
         * boot, the card, Shizuku coming back.
         */
        fun sync(context: Context) {
            val appCtx = context.applicationContext
            io.execute {
                val config = RestartSchedule.load(appCtx)
                if (!config.enabled || !config.usable) {
                    cancelAll(appCtx)
                    return@execute
                }
                armNextOccurrence(appCtx, config)
            }
        }

        /** Stops the schedule and clears anything already in flight. */
        fun disable(context: Context) {
            val appCtx = context.applicationContext
            io.execute { cancelAll(appCtx) }
        }

        private fun cancelAll(context: Context) {
            val am = context.getSystemService(AlarmManager::class.java)
            am?.cancel(tickIntent(context, 0L))
            am?.cancel(actionIntent(context, ACTION_CONFIRM, REQUEST_CONFIRM))
            dismiss(context)
        }

        private fun armNextOccurrence(context: Context, config: RestartSchedule.Config) {
            val next = RestartSchedule.nextOccurrence(config)
            if (next == Long.MAX_VALUE) return
            armTick(context, next, 0L)
            Log.i(TAG, "Next restart armed for $next")
        }

        private fun armTick(context: Context, at: Long, deadline: Long) {
            val am = context.getSystemService(AlarmManager::class.java) ?: return
            val pi = tickIntent(context, deadline)
            if (canScheduleExact(context, am)) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
            } else {
                // A few minutes of drift on a 03:30 restart is nothing; losing the round is
                // not. Take the inexact alarm rather than none.
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
            }
        }

        /**
         * From Android 12 an exact alarm needs SCHEDULE_EXACT_ALARM, which is not granted on
         * install. The app holds shell access, so it grants itself the app op rather than
         * sending the user to a settings page.
         */
        private fun canScheduleExact(context: Context, am: AlarmManager): Boolean {
            if (Build.VERSION.SDK_INT < 31) return true
            if (am.canScheduleExactAlarms()) return true
            val exec = AdbExecutor.instance
            if (!exec.permissionsGranted) return false
            exec.execute("cmd appops set ${context.packageName} SCHEDULE_EXACT_ALARM allow")
            return am.canScheduleExactAlarms()
        }

        // ---- guards -------------------------------------------------------------------

        /**
         * Names of the guards that are currently blocking, localised for the log and the
         * notification. Empty means the phone is free to restart.
         */
        private fun blockingGuards(context: Context, config: RestartSchedule.Config): List<String> {
            val blocked = ArrayList<String>(4)
            val basic = BatteryTelemetry.readBasic(context)

            if (config.guard(RestartSchedule.GUARD_SCREEN_OFF) && basic.screenOn) {
                blocked.add(t("screen on", "schermo acceso"))
            }
            if (config.guard(RestartSchedule.GUARD_CHARGING) && !basic.plugged) {
                blocked.add(t("not charging", "non in carica"))
            }
            if (config.guard(RestartSchedule.GUARD_BATTERY_30) &&
                basic.level in 0 until RestartSchedule.BATTERY_FLOOR_PCT
            ) {
                blocked.add(t("battery ${basic.level}%", "batteria ${basic.level}%"))
            }
            if (config.guard(RestartSchedule.GUARD_NO_CALL) && inCall(context)) {
                blocked.add(t("call in progress", "chiamata in corso"))
            }
            return blocked
        }

        /**
         * Audio mode rather than telephony state: it needs no permission and, unlike
         * READ_PHONE_STATE, it also catches VoIP — a WhatsApp call puts the device in
         * MODE_IN_COMMUNICATION, and that is the call this phone actually makes.
         */
        private fun inCall(context: Context): Boolean {
            val am = context.getSystemService(AudioManager::class.java) ?: return false
            return when (am.mode) {
                AudioManager.MODE_IN_CALL,
                AudioManager.MODE_IN_COMMUNICATION,
                AudioManager.MODE_RINGTONE -> true
                else -> false
            }
        }

        /**
         * True if the user touched the phone after the warning went up.
         *
         * Needed because the guards alone cannot see this: with a 30-second screen timeout,
         * someone who picks the phone up, reads the notification and puts it down is back to
         * "screen off" before the countdown ends. Silence during the warning has to mean
         * nobody was there, not that they looked away in time.
         */
        private fun touchedDuringWarning(context: Context, exec: AdbExecutor): Boolean {
            val warnedAt = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getLong(KEY_WARNED_AT, 0L)
            if (warnedAt <= 0L) return false
            val last = exec.execute(RestartSchedule.USER_ACTIVITY_PROBE).stdout.trim().toLongOrNull()
            if (last == null) {
                // Unreadable dump: the guards already passed, so carry on rather than making
                // the feature depend on a string format that may change.
                Log.w(TAG, "Could not read mLastUserActivityTime, proceeding on the guards alone")
                return false
            }
            return last > warnedAt
        }

        // ---- notification -------------------------------------------------------------

        private fun warn(context: Context, firesAt: Long) {
            val nm = context.getSystemService(NotificationManager::class.java) ?: return
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    t("Scheduled Restart", "Riavvio Programmato"),
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply {
                    description = t(
                        "Warning before a scheduled restart",
                        "Preavviso prima di un riavvio programmato",
                    )
                }
            )
            val notification = Notification.Builder(context, CHANNEL_ID)
                .setContentTitle(t("Restarting shortly", "Riavvio tra poco"))
                .setContentText(
                    t(
                        "Scheduled restart in ${RestartSchedule.COUNTDOWN_SECONDS}s. Tap Cancel to skip it.",
                        "Riavvio programmato tra ${RestartSchedule.COUNTDOWN_SECONDS}s. Tocca Annulla per saltarlo.",
                    )
                )
                .setSmallIcon(android.R.drawable.ic_popup_sync)
                .setCategory(Notification.CATEGORY_ALARM)
                .setWhen(firesAt)
                .setUsesChronometer(true)
                .setChronometerCountDown(true)
                .setOngoing(false)
                .addAction(
                    Notification.Action.Builder(
                        null, t("Cancel", "Annulla"),
                        actionIntent(context, ACTION_CANCEL, REQUEST_CANCEL),
                    ).build()
                )
                .addAction(
                    Notification.Action.Builder(
                        null, t("Restart now", "Riavvia ora"),
                        actionIntent(context, ACTION_CONFIRM, REQUEST_CONFIRM),
                    ).build()
                )
                .build()

            ensureNotificationsAllowed(context)
            nm.notify(NOTIF_ID, notification)
        }

        /**
         * On Android 13 POST_NOTIFICATIONS is a runtime permission, and without it this whole
         * feature would reboot the phone with no warning ever shown — the opposite of what the
         * countdown is for. Shell access is already available, so grant it directly.
         */
        private fun ensureNotificationsAllowed(context: Context) {
            if (Build.VERSION.SDK_INT < 33) return
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) return
            val exec = AdbExecutor.instance
            if (!exec.permissionsGranted) return
            exec.execute("pm grant ${context.packageName} android.permission.POST_NOTIFICATIONS")
        }

        private fun dismiss(context: Context) {
            context.getSystemService(NotificationManager::class.java)?.cancel(NOTIF_ID)
        }

        // ---- handlers -----------------------------------------------------------------

        private fun onTick(context: Context, deadlineExtra: Long) {
            val config = RestartSchedule.load(context)
            if (!config.enabled || !config.usable) {
                cancelAll(context)
                return
            }
            val now = System.currentTimeMillis()
            // A deadline of 0 means this is the scheduled fire, not a retry: the retry window
            // opens here.
            val deadline = if (deadlineExtra > 0) deadlineExtra
            else now + RestartSchedule.GRACE_MINUTES * 60_000L

            val blocked = blockingGuards(context, config)
            if (blocked.isEmpty()) {
                // Re-arm the normal schedule before the countdown, not after: if the process is
                // killed during those 60 seconds, the next round must still be armed.
                armNextOccurrence(context, config)
                val firesAt = now + RestartSchedule.COUNTDOWN_SECONDS * 1000L
                val am = context.getSystemService(AlarmManager::class.java)
                val pi = actionIntent(context, ACTION_CONFIRM, REQUEST_CONFIRM)
                am?.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, firesAt, pi)
                context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .edit().putLong(KEY_WARNED_AT, SystemClock.uptimeMillis()).commit()
                warn(context, firesAt)
                Log.i(TAG, "Guards clear, restarting in ${RestartSchedule.COUNTDOWN_SECONDS}s")
                return
            }

            if (now < deadline) {
                armTick(context, now + RestartSchedule.RETRY_MINUTES * 60_000L, deadline)
                Log.i(TAG, "Blocked by ${blocked.joinToString(", ")}, retrying in ${RestartSchedule.RETRY_MINUTES} min")
            } else {
                armNextOccurrence(context, config)
                Log.i(TAG, "Blocked by ${blocked.joinToString(", ")} past the grace window, deferred")
            }
        }

        private fun onConfirm(context: Context) {
            dismiss(context)
            val config = RestartSchedule.load(context)
            if (!config.enabled) return
            // Re-checked, not trusted from 60 seconds ago: picking the phone up during the
            // countdown is precisely the case the warning exists for.
            val blocked = blockingGuards(context, config)
            if (blocked.isNotEmpty()) {
                Log.i(TAG, "Restart cancelled at the last moment: ${blocked.joinToString(", ")}")
                return
            }
            val exec = AdbExecutor.instance
            if (!exec.awaitReady()) {
                Log.w(TAG, "Shizuku offline, restart skipped")
                return
            }
            if (touchedDuringWarning(context, exec)) {
                Log.i(TAG, "Restart cancelled: the phone was used during the warning")
                return
            }
            Log.i(TAG, "Restarting now")
            val result = exec.execute(RestartSchedule.REBOOT_COMMAND)
            // Reached only if the reboot did not happen; the process normally dies mid-call.
            if (!result.isSuccess) Log.e(TAG, "Restart command failed: $result")
        }

        private fun onCancel(context: Context) {
            dismiss(context)
            context.getSystemService(AlarmManager::class.java)
                ?.cancel(actionIntent(context, ACTION_CONFIRM, REQUEST_CONFIRM))
            Log.i(TAG, "Restart cancelled by the user")
            // The next occurrence was armed before the countdown started, so nothing to redo.
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != ACTION_TICK && action != ACTION_CONFIRM && action != ACTION_CANCEL) return
        // The alarm may be what started the process, and nothing else is holding it alive:
        // returning from onReceive would race a kill. goAsync() keeps the broadcast open.
        val pending = goAsync()
        val appCtx = context.applicationContext
        val deadline = intent.getLongExtra(EXTRA_DEADLINE, 0L)
        io.execute {
            try {
                when (action) {
                    ACTION_TICK -> onTick(appCtx, deadline)
                    ACTION_CONFIRM -> onConfirm(appCtx)
                    ACTION_CANCEL -> onCancel(appCtx)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Handling $action failed", e)
            } finally {
                pending.finish()
            }
        }
    }
}
