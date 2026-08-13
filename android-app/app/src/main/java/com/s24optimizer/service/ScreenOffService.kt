package com.s24optimizer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import com.s24optimizer.data.BatteryHistory
import com.s24optimizer.data.BatteryTelemetry
import com.s24optimizer.data.MasterSync
import com.s24optimizer.exec.AdbExecutor
import java.util.concurrent.Executors

class ScreenOffService : Service() {

    companion object {
        private const val TAG = "ScreenOffSvc"
        private const val CHANNEL_ID = "screen_off_mods"
        private const val NOTIF_ID = 1

        const val MARKER_LOW_HZ = "s24opt_screen_off_low_hz"
        const val MARKER_PSM = "s24opt_screen_off_psm"
        const val MARKER_SYNC = "s24opt_screen_off_sync"

        private val ALL_MARKERS = listOf(MARKER_LOW_HZ, MARKER_PSM, MARKER_SYNC)

        /**
         * One feature's two halves plus a way to read back which one the device holds.
         *
         * Both halves and the probe sit in the same object so they cannot drift apart the
         * way they did when each was hand-written into its own map. The probe is what lets
         * an orphan be recognised from the device itself, without trusting our own record.
         */
        private sealed interface Mod {
            /** True when Shizuku has to be up for this mod to do anything. */
            val needsShell: Boolean
            fun apply(exec: AdbExecutor, screenOff: Boolean)
            fun atScreenOffValue(exec: AdbExecutor): Boolean
        }

        private class ShellMod(
            private val off: String,
            private val on: String,
            private val probe: String,
            private val offValue: String,
        ) : Mod {
            override val needsShell = true
            override fun apply(exec: AdbExecutor, screenOff: Boolean) {
                exec.execute(if (screenOff) off else on)
            }
            override fun atScreenOffValue(exec: AdbExecutor) =
                exec.execute(probe).stdout.trim() == offValue
        }

        /**
         * Master sync, which has no settings key at all. The old commands wrote
         * `global auto_sync`, a row this device does not have — every screen transition
         * reported success and sync stayed on the whole time.
         */
        private object SyncMod : Mod {
            override val needsShell = false
            override fun apply(exec: AdbExecutor, screenOff: Boolean) =
                MasterSync.setEnabled(!screenOff)
            override fun atScreenOffValue(exec: AdbExecutor) = !MasterSync.isEnabled()
        }

        private val MODS: Map<String, Mod> = mapOf(
            MARKER_LOW_HZ to ShellMod(
                off = "settings put system peak_refresh_rate 60.0; settings put system min_refresh_rate 60.0",
                on = "settings put system peak_refresh_rate 120.0; settings put system min_refresh_rate 24.0",
                probe = "settings get system peak_refresh_rate",
                offValue = "60.0",
            ),
            MARKER_PSM to ShellMod(
                off = "settings put global low_power 1",
                on = "settings put global low_power 0",
                probe = "settings get global low_power",
                offValue = "1",
            ),
            MARKER_SYNC to SyncMod,
        )

        // Which features currently hold their screen-off values. Kept in SharedPreferences
        // (not Settings.Secure) so it survives a process kill without needing shell access
        // to read it back.
        private const val PREFS = "screen_off_state"
        private const val KEY_DIRTY = "dirty_features"

        private val io = Executors.newSingleThreadExecutor { r ->
            Thread(r, "screen-off-io").apply { isDaemon = true }
        }

        fun getActiveFeatures(context: Context): Set<String> {
            val cr = context.contentResolver
            val features = mutableSetOf<String>()
            for (m in ALL_MARKERS) {
                val v = try {
                    Settings.Secure.getInt(cr, m)
                } catch (_: Settings.SettingNotFoundException) {
                    0
                }
                if (v == 1) features.add(m)
            }
            return features
        }

        private fun dirty(context: Context): Set<String> =
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getStringSet(KEY_DIRTY, emptySet()) ?: emptySet()

        private fun setDirty(context: Context, features: Set<String>) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putStringSet(KEY_DIRTY, features).commit()
        }

        private fun screenOn(context: Context): Boolean =
            context.getSystemService(PowerManager::class.java)?.isInteractive ?: true

        /** Applies the screen-off half and records it, so it can be undone later. */
        /**
         * The subset of [features] that can run right now.
         *
         * Master sync goes through ContentResolver, so it neither needs nor waits for the
         * shell bridge. Treating the set as all-or-nothing would have let a Shizuku outage
         * strand sync in whichever half it happened to be in.
         */
        private fun runnable(features: Set<String>, exec: AdbExecutor): Set<String> {
            val shellUp = exec.permissionsGranted
            return features.filterTo(mutableSetOf()) { f ->
                val mod = MODS[f] ?: return@filterTo false
                shellUp || !mod.needsShell
            }
        }

        private fun applyScreenOff(context: Context, features: Set<String>) {
            val exec = AdbExecutor.instance
            val doable = runnable(features, exec)
            if (doable.size < features.size) {
                Log.w(TAG, "Shizuku unavailable, skipping ${features.size - doable.size} screen-off feature(s)")
            }
            if (doable.isEmpty()) return
            // Record before executing: a kill halfway through still leaves a trail.
            setDirty(context, dirty(context) + doable)
            for (f in doable) MODS[f]?.apply(exec, screenOff = true)
        }

        /** Applies the screen-on half and clears the record for whatever it managed to restore. */
        private fun applyScreenOn(context: Context, features: Set<String>) {
            if (features.isEmpty()) return
            val exec = AdbExecutor.instance
            val doable = runnable(features, exec)
            if (doable.size < features.size) {
                // Leave the rest in the dirty record: reconcile() will retry once
                // Shizuku is back rather than silently abandoning the values.
                Log.w(TAG, "Shizuku unavailable, keeping ${features.size - doable.size} feature(s) pending restore")
            }
            if (doable.isEmpty()) return
            for (f in doable) MODS[f]?.apply(exec, screenOff = false)
            setDirty(context, dirty(context) - doable)
        }

        /**
         * Restores anything left in the screen-off state by a service that died before it
         * could undo itself (process kill, reboot, or a Shizuku outage). Safe to call at
         * any time: it only touches features it knows it changed, and only while the
         * screen is on.
         *
         * Without this a killed service leaves low_power=1 and master sync off forever.
         */
        fun reconcile(context: Context) {
            val appCtx = context.applicationContext
            io.execute {
                if (!screenOn(appCtx)) return@execute
                val exec = AdbExecutor.instance
                val pending = dirty(appCtx).toMutableSet()
                // Our own record is not the only source of truth: a reinstall or a cleared
                // data dir wipes it, while the values it describes outlive both. Anything
                // still sitting at its screen-off value with the screen on is an orphan.
                for (f in runnable(getActiveFeatures(appCtx), exec)) {
                    if (f in pending) continue
                    if (MODS[f]?.atScreenOffValue(exec) == true) pending += f
                }
                if (pending.isEmpty()) return@execute
                Log.i(TAG, "Reconciling orphaned screen-off state: $pending")
                applyScreenOn(appCtx, pending)
            }
        }

        /**
         * Starts the service if it is allowed right now.
         *
         * From Android 12 a background process may not start a foreground service, and the
         * app process is often spawned in the background (boot, provider access, a screen
         * transition). Throwing there would kill the process before it could reconcile, so
         * a refusal is reported rather than raised: [ensureRunning] retries from the UI,
         * which is always an allowed context.
         */
        fun start(context: Context): Boolean {
            val intent = Intent(context, ScreenOffService::class.java)
            return try {
                if (Build.VERSION.SDK_INT >= 26) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                true
            } catch (e: Exception) {
                // ForegroundServiceStartNotAllowedException on 31+, IllegalStateException below.
                Log.w(TAG, "Foreground start refused: ${e.javaClass.simpleName}")
                false
            }
        }

        /** Called from the foreground (activity resume): starts the service if it is needed. */
        fun ensureRunning(context: Context) {
            if (getActiveFeatures(context).isNotEmpty()) start(context) else reconcile(context)
        }
    }

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return
            val features = getActiveFeatures(context)
            if (features.isEmpty()) return
            // Shell calls are blocking; keep them off the main thread and hold the
            // broadcast open until they finish.
            val pending = goAsync()
            val appCtx = context.applicationContext
            io.execute {
                try {
                    // Screen transitions are the only boundaries at which standby and
                    // active drain can be told apart, so sample the battery here.
                    BatteryHistory.recordTransition(
                        appCtx,
                        BatteryTelemetry.readBasic(appCtx),
                        screenOnBefore = action == Intent.ACTION_SCREEN_OFF,
                    )
                    when (action) {
                        Intent.ACTION_SCREEN_OFF -> applyScreenOff(appCtx, features)
                        Intent.ACTION_SCREEN_ON -> applyScreenOn(appCtx, features)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Screen transition failed", e)
                } finally {
                    pending.finish()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Service created")
        createNotificationChannel()

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(screenReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(screenReceiver, filter)
        }

        val notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Screen-Off Mods Active")
            .setContentText("Managing display/sync behavior")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIF_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIF_ID, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val features = getActiveFeatures(this)
        Log.i(TAG, "Active features: $features")
        // Undo anything a previous instance left behind before taking over.
        reconcile(this)
        if (features.isEmpty()) {
            Log.i(TAG, "No features active, stopping")
            stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        Log.i(TAG, "Service destroyed")
        try { unregisterReceiver(screenReceiver) } catch (_: Exception) {}
        // Never leave the device in the screen-off configuration once we stop managing it.
        val appCtx = applicationContext
        val pending = dirty(appCtx)
        if (pending.isNotEmpty()) {
            io.execute { applyScreenOn(appCtx, pending) }
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Screen-Off Mods", NotificationManager.IMPORTANCE_LOW).apply {
            description = "Notification for screen-off background service"
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
