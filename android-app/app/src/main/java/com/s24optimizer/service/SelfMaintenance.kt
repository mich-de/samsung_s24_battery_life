package com.s24optimizer.service

import android.content.Context
import android.util.Log
import com.s24optimizer.exec.AdbExecutor

/**
 * Housekeeping that must run whenever the app process comes up and Shizuku is usable.
 *
 * The app used to whitelist WhatsApp from Doze while leaving itself subject to it, so
 * One UI would put the battery optimizer to sleep and its screen-off service would stop
 * firing — silently, and with the screen-off values still applied.
 */
object SelfMaintenance {

    private const val TAG = "SelfMaintenance"

    @Volatile
    var selfExempt: Boolean = false
        private set

    fun schedule(context: Context) {
        val appCtx = context.applicationContext
        AdbExecutor.instance.onReady {
            ensureSelfExempt(appCtx)
            WhatsAppCallOptimizer.ensure()
            ScreenOffService.reconcile(appCtx)
            // Catches up a boundary missed while Shizuku was down, and re-arms the alarm.
            ChargeScheduleReceiver.sync(appCtx)
        }
    }

    private fun ensureSelfExempt(context: Context) {
        val pkg = context.packageName
        val exec = AdbExecutor.instance
        if (isExempt(exec, pkg)) {
            selfExempt = true
            return
        }
        exec.execute("cmd deviceidle whitelist +$pkg")
        // Doze exemption alone is not enough: App Standby can still bucket the app into
        // RARE and throttle its wakeups.
        exec.execute("am set-standby-bucket $pkg active")
        selfExempt = isExempt(exec, pkg)
        Log.i(TAG, "Self Doze exemption: $selfExempt")
    }

    private fun isExempt(exec: AdbExecutor, pkg: String): Boolean =
        exec.execute("cmd deviceidle whitelist").stdout
            .lineSequence()
            .any { it.contains(",$pkg,") || it.trim().endsWith(",$pkg") }
}
