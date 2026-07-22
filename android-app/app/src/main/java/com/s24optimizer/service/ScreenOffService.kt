package com.s24optimizer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import com.s24optimizer.exec.AdbExecutor

class ScreenOffService : Service() {

    companion object {
        private const val TAG = "ScreenOffSvc"
        private const val CHANNEL_ID = "screen_off_mods"

        const val MARKER_LOW_HZ = "s24opt_screen_off_low_hz"
        const val MARKER_PSM = "s24opt_screen_off_psm"
        const val MARKER_SYNC = "s24opt_screen_off_sync"

        fun getActiveFeatures(context: Context): Set<String> {
            val cr = context.contentResolver
            val features = mutableSetOf<String>()
            try {
                if (Settings.Secure.getInt(cr, MARKER_LOW_HZ) == 1) features.add(MARKER_LOW_HZ)
                if (Settings.Secure.getInt(cr, MARKER_PSM) == 1) features.add(MARKER_PSM)
                if (Settings.Secure.getInt(cr, MARKER_SYNC) == 1) features.add(MARKER_SYNC)
            } catch (_: Settings.SettingNotFoundException) {}
            return features
        }
    }

    private val executor: AdbExecutor get() = AdbExecutor.instance

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val features = getActiveFeatures(context)
            if (features.isEmpty()) return
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> onScreenOff(features)
                Intent.ACTION_SCREEN_ON -> onScreenOn(features)
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
        registerReceiver(screenReceiver, filter)

        val notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Screen-Off Mods Active")
            .setContentText("Managing display/sync behavior")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setOngoing(true)
            .build()
        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val features = getActiveFeatures(this)
        Log.i(TAG, "Active features: $features")
        if (features.isEmpty()) {
            Log.i(TAG, "No features active, stopping")
            stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        Log.i(TAG, "Service destroyed")
        try { unregisterReceiver(screenReceiver) } catch (_: Exception) {}
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun onScreenOff(features: Set<String>) {
        if (!executor.permissionsGranted) { Log.w(TAG, "Shizuku unavailable"); return }
        for (f in features) {
            when (f) {
                MARKER_LOW_HZ -> executor.execute("settings put system peak_refresh_rate 60.0; settings put system min_refresh_rate 60.0")
                MARKER_PSM -> executor.execute("settings put global low_power 1")
                MARKER_SYNC -> executor.execute("settings put global auto_sync 0")
            }
        }
    }

    private fun onScreenOn(features: Set<String>) {
        if (!executor.permissionsGranted) { Log.w(TAG, "Shizuku unavailable"); return }
        for (f in features) {
            when (f) {
                MARKER_LOW_HZ -> executor.execute("settings put system peak_refresh_rate 120.0; settings put system min_refresh_rate 24.0")
                MARKER_PSM -> executor.execute("settings put global low_power 0")
                MARKER_SYNC -> executor.execute("settings put global auto_sync 1")
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Screen-Off Mods", NotificationManager.IMPORTANCE_LOW).apply {
            description = "Notification for screen-off background service"
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
