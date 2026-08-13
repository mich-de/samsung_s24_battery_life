package com.s24optimizer.service

import android.util.Log
import com.s24optimizer.exec.AdbExecutor

object WhatsAppCallOptimizer {
    private const val WHATSAPP_PKG = "com.whatsapp"
    private const val TAG = "WhatsAppOpt"

    @Volatile
    private var whitelisted = false

    /** True once WhatsApp has been *verified* present in the Doze whitelist. */
    val isWhitelisted: Boolean get() = whitelisted

    /**
     * Keeps WhatsApp out of Doze so incoming calls are not delayed or dropped.
     *
     * Idempotent and safe to call repeatedly — it re-verifies against the live whitelist
     * instead of trusting a cached exit code, because One UI can drop entries after an
     * OS update.
     */
    fun ensure() {
        if (whitelisted) return
        val exec = AdbExecutor.instance
        if (!exec.permissionsGranted) {
            Log.w(TAG, "Shizuku not granted, will retry when it becomes available")
            return
        }
        if (!isInstalled(exec)) {
            Log.i(TAG, "WhatsApp not installed, nothing to do")
            whitelisted = true
            return
        }
        if (verify(exec)) {
            whitelisted = true
            Log.i(TAG, "Already whitelisted")
            return
        }
        val r = exec.execute("cmd deviceidle whitelist +$WHATSAPP_PKG")
        whitelisted = verify(exec)
        Log.i(TAG, "Whitelist: verified=$whitelisted out=${r.stdout.trim()} err=${r.stderr.trim()}")
    }

    /** Forces a re-check on the next [ensure]. */
    fun invalidate() {
        whitelisted = false
    }

    private fun isInstalled(exec: AdbExecutor): Boolean =
        exec.execute("pm list packages $WHATSAPP_PKG").stdout.contains("package:$WHATSAPP_PKG")

    private fun verify(exec: AdbExecutor): Boolean =
        exec.execute("cmd deviceidle whitelist").stdout
            .lineSequence()
            .any { it.contains(",$WHATSAPP_PKG,") || it.trim().endsWith(",$WHATSAPP_PKG") }
}
