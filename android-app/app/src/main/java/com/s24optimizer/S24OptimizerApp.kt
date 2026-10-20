package com.s24optimizer

import android.app.Application
import com.s24optimizer.data.PerAppRRState
import com.s24optimizer.exec.AdbExecutor
import com.s24optimizer.service.ScreenOffService
import com.s24optimizer.service.SelfMaintenance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class S24OptimizerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AdbExecutor.instance
        checkAndStartService()
        // Doze exemption, WhatsApp whitelist and screen-off reconciliation all run as
        // soon as Shizuku is usable, instead of racing a fixed 2s delay.
        SelfMaintenance.schedule(this)
        CoroutineScope(Dispatchers.IO).launch {
            PerAppRRState.reload()
        }
    }

    private fun checkAndStartService() {
        // Always reconcile: the markers may be gone while the values are still applied from
        // a previous run, and a refused service start must not stop the cleanup either.
        ScreenOffService.reconcile(this)
        if (ScreenOffService.getActiveFeatures(this).isNotEmpty()) {
            // Best effort — if the process was spawned in the background the platform
            // refuses, and MainActivity retries on resume.
            ScreenOffService.start(this)
        }
    }
}
