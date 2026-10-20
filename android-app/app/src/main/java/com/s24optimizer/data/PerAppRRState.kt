package com.s24optimizer.data

import com.s24optimizer.exec.AdbExecutor
import org.json.JSONObject

object PerAppRRState {
    private const val KEY_ENABLED = "s24opt_per_app_rr_enabled"
    private const val KEY_MAPPINGS = "s24opt_per_app_rr_mappings"

    @Volatile
    var enabled: Boolean = false
    @Volatile
    var defaultMode: String = "adaptive_120"
    @Volatile
    var mappings: Map<String, String> = emptyMap()

    fun reload() {
        val ex = AdbExecutor.instance
        if (!ex.permissionsGranted) return
        val r1 = ex.execute("settings get secure $KEY_ENABLED")
        enabled = r1.stdout.trim() == "1"
        val r2 = ex.execute("settings get secure $KEY_MAPPINGS")
        parseMappings(r2.stdout.trim())
    }

    private fun parseMappings(raw: String) {
        if (raw.isBlank() || raw == "null") return
        try {
            val json = JSONObject(raw)
            defaultMode = json.optString("default", "adaptive_120")
            val apps = json.optJSONObject("apps") ?: return
            val m = mutableMapOf<String, String>()
            for (key in apps.keys()) m[key] = apps.getString(key)
            mappings = m
        } catch (_: Exception) {}
    }

    fun saveMappings(newDefault: String, newMappings: Map<String, String>): Boolean {
        val ex = AdbExecutor.instance
        if (!ex.permissionsGranted) return false
        val json = JSONObject().apply {
            put("default", newDefault)
            val apps = JSONObject()
            newMappings.forEach { (pkg, mode) -> apps.put(pkg, mode) }
            put("apps", apps)
        }
        val escaped = json.toString().replace("'", "'\\''")
        val r = ex.execute("settings put secure $KEY_MAPPINGS '$escaped'")
        return r.isSuccess
    }

    fun getApplyCommands(modeId: String): List<String> = when (modeId) {
        "adaptive_120" -> listOf(
            "settings put secure refresh_rate_mode 2",
            "settings put system peak_refresh_rate 120.0",
            "settings put system min_refresh_rate 24.0",
        )
        "high_96" -> listOf(
            "settings put secure refresh_rate_mode 1",
            "settings put system peak_refresh_rate 96.0",
            "settings put system min_refresh_rate 96.0",
        )
        "standard_60" -> listOf(
            "settings put secure refresh_rate_mode 0",
            "settings put system peak_refresh_rate 60.0",
            "settings put system min_refresh_rate 60.0",
        )
        else -> emptyList()
    }
}
