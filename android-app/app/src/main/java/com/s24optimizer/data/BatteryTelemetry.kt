package com.s24optimizer.data

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import com.s24optimizer.exec.AdbExecutor

/**
 * Live battery readings.
 *
 * Level, charge counter, current and temperature come from [BatteryManager], which needs
 * no permission at all — so the drain meter keeps working even when Shizuku is down. The
 * Samsung-specific health fields (ASOC, BSOH, protection state, pack age) only exist in
 * `dumpsys battery` and do need shell access.
 */
object BatteryTelemetry {

    data class Basic(
        val level: Int,
        val chargeCounterUah: Int,
        val currentNowUa: Int,
        val temperatureC: Float,
        val voltageMv: Int,
        val plugged: Boolean,
        val screenOn: Boolean,
    ) {
        /** Full-charge capacity implied by the current counter, in mAh. */
        val impliedCapacityMah: Int
            get() = if (level > 0 && chargeCounterUah > 0)
                (chargeCounterUah / 1000f * 100f / level).toInt() else 0
    }

    data class Health(
        /** Samsung "Absolute State of Charge": rated capacity remaining, in percent. */
        val asoc: Int,
        /** Samsung "Battery State of Health". */
        val bsoh: Int,
        val maxTempC: Float,
        val maxCurrentMa: Int,
        /** Weeks since the cell was manufactured. */
        val ageWeeks: Int,
        val firstUseDate: String,
        /**
         * One UI's protection mode, as stored in `protect_battery` and reported as
         * `mProtectBatteryMode`: 0 = off, 1 and 2 = Maximum (charging stops at
         * [protectThresholdPct]), 3 = Basic (charges to 100%, then waits for 95% before
         * topping up). Only Maximum holds the pack below full.
         */
        val protectMode: Int,
        /**
         * The threshold stored for One UI's "Maximum" protection. It is remembered even
         * while another mode is selected, so it says what the cap *would* be, not that a
         * cap is in force right now.
         */
        val protectThresholdPct: Int,
        /**
         * Ceiling One UI applies once the phone has been left plugged in for
         * [ltcHighSocMinutes] (its "long term charging" logic).
         */
        val ltcHighPct: Int,
        val ltcHighSocMinutes: Int,
    ) {
        val protectOn: Boolean get() = protectMode > 0

        /** True only in Maximum: the one mode where [protectThresholdPct] is a real cap. */
        val protectCapped: Boolean get() = protectMode == 1 || protectMode == 2
    }

    fun readBasic(context: Context): Basic {
        val bm = context.getSystemService(BatteryManager::class.java)
        val sticky: Intent? = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val level = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
        val counter = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) ?: -1
        val current = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) ?: 0

        val tempTenths = sticky?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
        val voltage = sticky?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
        val pluggedExtra = sticky?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0

        val pm = context.getSystemService(PowerManager::class.java)

        return Basic(
            level = level,
            chargeCounterUah = counter,
            currentNowUa = current,
            temperatureC = if (tempTenths > 0) tempTenths / 10f else 0f,
            voltageMv = voltage,
            plugged = pluggedExtra != 0,
            screenOn = pm?.isInteractive ?: true,
        )
    }

    fun readHealth(exec: AdbExecutor): Health? {
        if (!exec.permissionsGranted) return null
        val out = exec.execute("dumpsys battery").stdout
        if (out.isBlank()) return null

        fun int(pattern: String, default: Int = -1): Int =
            Regex(pattern).find(out)?.groupValues?.getOrNull(1)?.trim()?.toIntOrNull() ?: default

        fun str(pattern: String): String =
            Regex(pattern).find(out)?.groupValues?.getOrNull(1)?.trim().orEmpty()

        // Deliberately not derived from mFullCapacityEnable. That flag mirrors the sysfs
        // batt_full_capacity node, which One UI only arms once the level has fallen to
        // the threshold — so it reads false on a phone sitting at 100% with Maximum
        // selected and enforced, which is exactly when it was mistaken for "the limit is
        // not being applied". Whether charging is actually being held is visible in
        // `dumpsys battery` as status 4 (NOT_CHARGING) with a negative current.
        val protectMode = int("""mProtectBatteryMode:\s*(\d+)""", 0)

        return Health(
            asoc = int("""mSavedBatteryAsoc:\s*\[(\d+)]"""),
            bsoh = int("""mSavedBatteryBsoh:\s*(\d+)"""),
            maxTempC = int("""mSavedBatteryMaxTemp:\s*(\d+)""", 0) / 10f,
            maxCurrentMa = int("""mSavedBatteryMaxCurrent:\s*(\d+)""", 0),
            ageWeeks = int("""LLB DIFF:\s*(\d+)""", 0),
            firstUseDate = str("""battery FirstUseDate:\s*\[(\d+)]"""),
            protectMode = protectMode,
            protectThresholdPct = int("""mProtectionThreshold:\s*(\d+)""", 0),
            ltcHighPct = int("""mLtcHighThreshold:\s*(\d+)""", 0),
            ltcHighSocMinutes = int("""mLtcHighSocDuration:\s*(\d+)""", 0),
        )
    }
}
