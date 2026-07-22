package com.s24optimizer

import android.app.Application
import android.content.Intent
import com.s24optimizer.data.PerAppRRState
import com.s24optimizer.exec.AdbExecutor
import com.s24optimizer.service.ScreenOffService
import com.s24optimizer.service.WhatsAppCallOptimizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class S24OptimizerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AdbExecutor.instance
        checkAndStartService()
        CoroutineScope(Dispatchers.IO).launch {
            delay(2000)
            PerAppRRState.reload()
            WhatsAppCallOptimizer.ensure()
        }
    }

    private fun checkAndStartService() {
        val features = ScreenOffService.getActiveFeatures(this)
        if (features.isNotEmpty()) {
            val intent = Intent(this, ScreenOffService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
    }
}
