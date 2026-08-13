package com.s24optimizer.data

import com.s24optimizer.exec.AdbExecutor

/**
 * Carries out one optimization: apply it, revert it, or read back whether it is active.
 *
 * Exists because not every entry is a list of shell lines. Master sync has no settings key
 * and has to go through ContentResolver in this process, and every caller — the apply and
 * revert buttons, the state load on startup — has to agree about that. When the three
 * operations were open-coded at each call site they could disagree, and a box would tick
 * from a command that had not actually changed anything.
 */
object OptimizationRunner {

    /** Runs the apply half. Returns true when every step reported success. */
    fun apply(exec: AdbExecutor, opt: Optimization, onLog: (String) -> Unit = {}): Boolean =
        when (opt.local) {
            Optimization.LocalAction.MASTER_SYNC_OFF -> setSync(false, onLog)
            null -> runShell(exec, opt.applyCommands, opt.titleEn, onLog)
        }

    /** Runs the revert half. Returns true when every step reported success. */
    fun revert(exec: AdbExecutor, opt: Optimization, onLog: (String) -> Unit = {}): Boolean =
        when (opt.local) {
            Optimization.LocalAction.MASTER_SYNC_OFF -> setSync(true, onLog)
            null -> runShell(exec, opt.revertCommands, opt.titleEn, onLog)
        }

    /**
     * Whether the change is active on the device right now.
     *
     * Null means the entry cannot be observed — a one-shot action such as a cache clear,
     * which has no state to read back. Callers show those as not applied rather than
     * inventing an answer.
     */
    fun isApplied(exec: AdbExecutor, opt: Optimization): Boolean? = when (opt.local) {
        Optimization.LocalAction.MASTER_SYNC_OFF -> !MasterSync.isEnabled()
        null -> {
            val expr = opt.checkExpression()
            if (expr == null) null
            else exec.execute(expr).let { it.error == null && it.stdout.trim() == "1" }
        }
    }

    private fun setSync(enabled: Boolean, onLog: (String) -> Unit): Boolean {
        MasterSync.setEnabled(enabled)
        val now = MasterSync.isEnabled()
        onLog("master sync -> ${if (now) "on" else "off"}")
        return now == enabled
    }

    private fun runShell(
        exec: AdbExecutor,
        commands: List<String>,
        title: String,
        onLog: (String) -> Unit,
    ): Boolean {
        var ok = true
        for (cmd in commands) {
            onLog("$title: $cmd")
            val result = exec.execute(cmd)
            onLog("  $result")
            if (!result.isSuccess) ok = false
        }
        return ok
    }
}
