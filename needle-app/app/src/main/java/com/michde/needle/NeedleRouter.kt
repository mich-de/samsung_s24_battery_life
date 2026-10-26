package com.michde.needle

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class RouteResult {
    data class DeviceAction(
        val actionName: String,
        val details: String,
        val intent: Intent? = null,
        val feedback: String
    ) : RouteResult()

    data class HostAiPrompt(
        val prompt: String,
        val explanation: String
    ) : RouteResult()
}

/**
 * Needle Router: Fast sub-millisecond edge router.
 * Evaluates intents locally (15 MB RAM overhead) and dispatches either:
 * 1. Device Tool Call (Launch App, Set Alarm, Search Web, Toggle Settings)
 * 2. Reasoning / Knowledge Query -> HostAI Local API (port 8080)
 */
class NeedleRouter(private val context: Context) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    fun route(query: String): RouteResult {
        val q = query.trim().lowercase()

        // ── 1. App Launching Tool Call ──
        if (q.startsWith("apri ") || q.startsWith("open ") || q.startsWith("avvia ") || q.startsWith("lancia ")) {
            val appTarget = q.removePrefix("apri ")
                .removePrefix("open ")
                .removePrefix("avvia ")
                .removePrefix("lancia ")
                .trim()
            val intent = findAppIntent(appTarget)
            return if (intent != null) {
                RouteResult.DeviceAction(
                    actionName = "TOOL_CALL // LAUNCH_PACKAGE",
                    details = "Target: $appTarget",
                    intent = intent,
                    feedback = "Avvio dell'applicazione '$appTarget' in corso..."
                )
            } else {
                RouteResult.DeviceAction(
                    actionName = "TOOL_CALL // LAUNCH_PACKAGE",
                    details = "Package non trovato per: $appTarget",
                    intent = null,
                    feedback = "Applicazione '$appTarget' non trovata tra i pacchetti installati."
                )
            }
        }

        // ── 2. Clock / Alarm / Timer Tool Call ──
        if (q.contains("sveglia") || q.contains("alarm") || q.contains("timer")) {
            val hourRegex = Regex("""(\d{1,2})[:.](\d{2})""")
            val match = hourRegex.find(q)
            if (match != null) {
                val hour = match.groupValues[1].toInt()
                val min = match.groupValues[2].toInt()
                val alarmIntent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                    putExtra(AlarmClock.EXTRA_HOUR, hour)
                    putExtra(AlarmClock.EXTRA_MINUTES, min)
                    putExtra(AlarmClock.EXTRA_MESSAGE, "Needle Alarm")
                    putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                return RouteResult.DeviceAction(
                    actionName = "TOOL_CALL // SET_DEVICE_ALARM",
                    details = "Time: %02d:%02d".format(hour, min),
                    intent = alarmIntent,
                    feedback = "Impostazione sveglia alle %02d:%02d su One UI Clock...".format(hour, min)
                )
            }
        }

        // ── 3. Device Settings Tool Call ──
        if (q.contains("impostazioni") || q.contains("settings") || q.contains("wifi") || q.contains("batteria") || q.contains("bluetooth")) {
            val settingsIntent = when {
                q.contains("wifi") -> Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)
                q.contains("bluetooth") -> Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS)
                else -> Intent(android.provider.Settings.ACTION_SETTINGS)
            }.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }

            return RouteResult.DeviceAction(
                actionName = "TOOL_CALL // ANDROID_SETTINGS",
                details = "Opening system settings pane",
                intent = settingsIntent,
                feedback = "Apertura impostazioni di sistema..."
            )
        }

        // ── 4. Web Search / Browser Tool Call ──
        if (q.startsWith("cerca ") || q.startsWith("search ") || q.startsWith("google ")) {
            val searchTerm = q.removePrefix("cerca ").removePrefix("search ").removePrefix("google ").trim()
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(searchTerm)}")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return RouteResult.DeviceAction(
                actionName = "TOOL_CALL // WEB_SEARCH",
                details = "Query: $searchTerm",
                intent = webIntent,
                feedback = "Ricerca web per '$searchTerm'..."
            )
        }

        // ── 5. Fallback -> HostAI Local Neural Engine (port 8080) ──
        return RouteResult.HostAiPrompt(
            prompt = query,
            explanation = "Frase non di automazione locale. Routing verso HostAI (Xclipse 940 OpenCL :8080)"
        )
    }

    private fun findAppIntent(appName: String): Intent? {
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(0)
        for (app in packages) {
            val label = pm.getApplicationLabel(app).toString().lowercase()
            if (label == appName || label.contains(appName) || app.packageName.contains(appName)) {
                return pm.getLaunchIntentForPackage(app.packageName)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
        }
        return null
    }

    suspend fun queryHostAi(prompt: String): String {
        return try {
            val json = JSONObject().apply {
                put("model", "gemma-4-E2B-it.litertlm")
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", "Sei un assistente AI conciso ed efficiente in esecuzione sul Galaxy S24.")
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                })
                put("temperature", 0.7)
                put("max_tokens", 512)
            }

            val body = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("http://127.0.0.1:8080/v1/chat/completions")
                .post(body)
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return "[HOSTAI ERR]: Engine status ${response.code} (Assicurati che HostAI sia avviato su :8080)"
            }

            val resBody = response.body?.string() ?: return "[HOSTAI ERR]: Empty response"
            val resJson = JSONObject(resBody)
            val choices = resJson.optJSONArray("choices")
            if (choices != null && choices.length() > 0) {
                val message = choices.getJSONObject(0).optJSONObject("message")
                message?.optString("content") ?: "[HOSTAI ERR]: No content"
            } else {
                "[HOSTAI ERR]: Malformed reply"
            }
        } catch (e: Exception) {
            "[HOSTAI OFFLINE]: ${e.localizedMessage ?: e.message}\n(Avvia il motore con [ START INFERENCE ENGINE ] in HostAI)"
        }
    }
}
