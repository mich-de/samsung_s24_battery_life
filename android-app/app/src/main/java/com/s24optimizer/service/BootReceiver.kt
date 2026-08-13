package com.s24optimizer.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Brings the screen-off service back after a reboot.
 *
 * Without this the service only ever starts when something launches the app process,
 * which after a reboot means "when the user opens the app by hand". START_STICKY does
 * not survive a reboot.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.QUICKBOOT_POWERON" -> {}
            else -> return
        }
        Log.i("BootReceiver", "Boot: ${intent.action}")

        // A reboot clears low_power/auto_sync overrides only sometimes; reconcile
        // explicitly rather than assuming.
        ScreenOffService.reconcile(context)

        if (ScreenOffService.getActiveFeatures(context).isNotEmpty()) {
            ScreenOffService.start(context)
        }
        // Alarms do not survive a reboot. Re-arm before Shizuku is in the picture: arming
        // needs no shell, and sync() applies the phase again once Shizuku is up.
        ChargeScheduleReceiver.sync(context)
        SelfMaintenance.schedule(context)
    }
}
