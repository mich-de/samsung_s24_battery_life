package com.s24optimizer.data

import android.content.Context

/**
 * A rolling series of battery samples, used to turn the raw charge counter into an
 * actual drain rate.
 *
 * The app applies 113 optimizations; without this there is no way to tell whether any
 * of them helped. Samples are cheap (five numbers) and the counter is cumulative, so
 * even two readings hours apart give an exact average for the window between them.
 */
object BatteryHistory {

    private const val PREFS = "battery_history"
    private const val KEY_SAMPLES = "samples"
    private const val MAX_SAMPLES = 600
    private const val MAX_AGE_MS = 48L * 60 * 60 * 1000

    data class Sample(
        val timestamp: Long,
        val level: Int,
        val chargeCounterUah: Int,
        val plugged: Boolean,
        val screenOn: Boolean,
    )

    data class Stats(
        val windowMs: Long,
        val sampleCount: Int,
        /** Overall discharge rate across the window. */
        val mahPerHour: Float,
        val percentPerHour: Float,
        /** Rate measured only across intervals where the screen stayed off. */
        val standbyMahPerHour: Float,
        val standbyMs: Long,
        /** Rate measured only across intervals where the screen stayed on. */
        val activeMahPerHour: Float,
        val activeMs: Long,
    ) {
        val hasEnoughData: Boolean get() = sampleCount >= 2 && windowMs >= 5 * 60 * 1000L
    }

    fun record(context: Context, basic: BatteryTelemetry.Basic, now: Long = System.currentTimeMillis()) {
        if (basic.chargeCounterUah <= 0 || basic.level < 0) return
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val existing = decode(prefs.getString(KEY_SAMPLES, "").orEmpty())

        val sample = Sample(now, basic.level, basic.chargeCounterUah, basic.plugged, basic.screenOn)

        // Charging invalidates everything before it: the counter goes back up and any
        // rate spanning the transition would be meaningless.
        val kept = if (basic.plugged) emptyList() else existing.filter { !it.plugged }

        val trimmed = (kept + sample)
            .filter { now - it.timestamp <= MAX_AGE_MS }
            .takeLast(MAX_SAMPLES)

        prefs.edit().putString(KEY_SAMPLES, encode(trimmed)).apply()
    }

    /**
     * Records a screen on/off transition as two samples one millisecond apart, so the
     * interval that just ended and the one just starting each have a clean pair of
     * endpoints with a single screen state.
     */
    fun recordTransition(context: Context, basic: BatteryTelemetry.Basic, screenOnBefore: Boolean) {
        val now = System.currentTimeMillis()
        record(context, basic.copy(screenOn = screenOnBefore), now)
        record(context, basic.copy(screenOn = !screenOnBefore), now + 1)
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY_SAMPLES).apply()
    }

    fun samples(context: Context): List<Sample> =
        decode(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_SAMPLES, "").orEmpty())

    fun stats(context: Context): Stats {
        val s = samples(context).filter { !it.plugged }
        if (s.size < 2) return Stats(0, s.size, 0f, 0f, 0f, 0, 0f, 0)

        val first = s.first()
        val last = s.last()
        val windowMs = last.timestamp - first.timestamp
        if (windowMs <= 0) return Stats(0, s.size, 0f, 0f, 0f, 0, 0f, 0)

        val hours = windowMs / 3_600_000f
        val totalMah = (first.chargeCounterUah - last.chargeCounterUah) / 1000f

        var standbyMah = 0f
        var standbyMs = 0L
        var activeMah = 0f
        var activeMs = 0L

        // Attribute each interval to standby or active only when the screen state was
        // the same at both ends; mixed intervals are left out rather than guessed at.
        for (i in 0 until s.size - 1) {
            val a = s[i]
            val b = s[i + 1]
            val dt = b.timestamp - a.timestamp
            if (dt <= 0) continue
            val dMah = (a.chargeCounterUah - b.chargeCounterUah) / 1000f
            if (dMah < 0) continue
            when {
                !a.screenOn && !b.screenOn -> { standbyMah += dMah; standbyMs += dt }
                a.screenOn && b.screenOn -> { activeMah += dMah; activeMs += dt }
            }
        }

        return Stats(
            windowMs = windowMs,
            sampleCount = s.size,
            mahPerHour = if (hours > 0) totalMah / hours else 0f,
            percentPerHour = if (hours > 0) (first.level - last.level) / hours else 0f,
            standbyMahPerHour = rate(standbyMah, standbyMs),
            standbyMs = standbyMs,
            activeMahPerHour = rate(activeMah, activeMs),
            activeMs = activeMs,
        )
    }

    private fun rate(mah: Float, ms: Long): Float =
        if (ms > 60_000) mah / (ms / 3_600_000f) else 0f

    private fun encode(samples: List<Sample>): String =
        samples.joinToString(";") {
            "${it.timestamp},${it.level},${it.chargeCounterUah},${if (it.plugged) 1 else 0},${if (it.screenOn) 1 else 0}"
        }

    private fun decode(raw: String): List<Sample> {
        if (raw.isBlank()) return emptyList()
        return raw.split(';').mapNotNull { row ->
            val p = row.split(',')
            if (p.size != 5) return@mapNotNull null
            Sample(
                timestamp = p[0].toLongOrNull() ?: return@mapNotNull null,
                level = p[1].toIntOrNull() ?: return@mapNotNull null,
                chargeCounterUah = p[2].toIntOrNull() ?: return@mapNotNull null,
                plugged = p[3] == "1",
                screenOn = p[4] == "1",
            )
        }
    }
}
