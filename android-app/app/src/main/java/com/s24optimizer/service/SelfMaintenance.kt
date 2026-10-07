package com.s24optimizer.service

import android.content.Context
import android.util.Log
import com.s24optimizer.data.AppliedHistory
import com.s24optimizer.data.OptimizationRunner
import com.s24optimizer.data.Optimizations
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
            reapplyDrifted(appCtx)
        }
    }

    /**
     * Re-asserts the few settings One UI walks back on its own.
     *
     * Scoped twice over, because a maintenance pass that rewrites settings freely is worse
     * than the drift it fixes: only entries flagged [com.s24optimizer.data.Optimization.selfHeals],
     * and among those only the ones [AppliedHistory] records the user as having asked for.
     * Everything else stays a manual repair from the Diagnostics screen.
     *
     * The check runs before the write, so a setting still in place costs one read and no
     * write. An entry whose state cannot be observed counts as still applied rather than
     * being rewritten blindly on every process start.
     */
    private fun reapplyDrifted(context: Context) {
        val exec = AdbExecutor.instance
        val watched = Optimizations.getAll()
            .filter { it.selfHeals && it.id in AppliedHistory.all(context) }
        if (watched.isEmpty()) return
        val states = watched.associate { it.id to (OptimizationRunner.isApplied(exec, it) ?: true) }
        // Goes through the same predicate the Diagnostics screen reads, so "drifted" cannot
        // come to mean one thing here and another there.
        for (opt in AppliedHistory.regressions(context, states)) {
            Log.i(TAG, "re-applying ${opt.id}: ${OptimizationRunner.apply(exec, opt)}")
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
