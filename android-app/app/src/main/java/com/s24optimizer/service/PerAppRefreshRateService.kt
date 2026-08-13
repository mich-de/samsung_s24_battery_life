package com.s24optimizer.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
import com.s24optimizer.data.PerAppRRState
import com.s24optimizer.exec.AdbExecutor
import com.s24optimizer.service.WhatsAppCallOptimizer

class PerAppRefreshRateService : AccessibilityService() {

    companion object {
        const val ACTION_RELOAD = "com.s24optimizer.PER_APP_RR_RELOAD"
        private const val TAG = "PerAppRR"
    }

    private var lastPackage: String? = null
    private var lastMode: String? = null
    private val executor: AdbExecutor get() = AdbExecutor.instance

    private val reloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.i(TAG, "Reloading state")
            PerAppRRState.reload()
            lastPackage = null
            lastMode = null
        }
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(reloadReceiver, IntentFilter(ACTION_RELOAD), RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(reloadReceiver, IntentFilter(ACTION_RELOAD))
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "AccessibilityService connected")
        PerAppRRState.reload()
        lastPackage = null
        lastMode = null
        WhatsAppCallOptimizer.ensure()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != TYPE_WINDOW_STATE_CHANGED) return
        if (!PerAppRRState.enabled) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == lastPackage) return
        lastPackage = pkg

        val modeId = PerAppRRState.mappings[pkg] ?: PerAppRRState.defaultMode
        if (modeId == lastMode) return
        lastMode = modeId

        val commands = PerAppRRState.getApplyCommands(modeId)
        if (commands.isEmpty() || !executor.permissionsGranted) return
        Log.i(TAG, "Applying $modeId for $pkg")
        for (cmd in commands) {
            executor.execute(cmd)
        }
    }

    override fun onInterrupt() {
        Log.i(TAG, "AccessibilityService interrupted")
    }

    override fun onDestroy() {
        try { unregisterReceiver(reloadReceiver) } catch (_: Exception) {}
        super.onDestroy()
    }
}
