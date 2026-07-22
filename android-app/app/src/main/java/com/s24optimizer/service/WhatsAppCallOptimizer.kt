package com.s24optimizer.service

import android.util.Log
import com.s24optimizer.exec.AdbExecutor

object WhatsAppCallOptimizer {
    private const val WHATSAPP_PKG = "com.whatsapp"
    private const val TAG = "WhatsAppOpt"

    private var whitelisted = false

    fun ensure() {
        if (whitelisted) return
        whitelist()
    }

    private fun whitelist() {
        if (!AdbExecutor.instance.permissionsGranted) {
            Log.w(TAG, "Shizuku not yet granted, will retry")
            return
        }
        val r = AdbExecutor.instance.execute("cmd deviceidle whitelist +$WHATSAPP_PKG")
        whitelisted = r.isSuccess
        Log.i(TAG, "Whitelist: success=$whitelisted out=${r.stdout.trim()} err=${r.stderr.trim()}")
    }
}
