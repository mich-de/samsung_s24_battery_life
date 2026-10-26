package com.michde.needle

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
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
        val feedback: String,
        val directAction: (() -> Boolean)? = null
    ) : RouteResult()

    data class HostAiPrompt(
        val prompt: String,
        val explanation: String
    ) : RouteResult()
}

/**
 * Needle Router: Ultra-fast local tool router & intent parser for Galaxy S24.
 * Integrates comprehensive hardware, app launcher, and system controls.
 */
class NeedleRouter(private val context: Context) {

    companion object {
        private var isFlashlightOn = false
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    fun route(query: String): RouteResult {
        val q = query.trim().lowercase()

        // ── 1. Flashlight / Torcia Control ──
        if (q.contains("torcia") || q.contains("flash") || q.contains("luce") || q.contains("flashlight")) {
            val enable = when {
                q.contains("spegni") || q.contains("disattiva") || q.contains("off") || q.contains("stop") -> false
                q.contains("accendi") || q.contains("attiva") || q.contains("apri") || q.contains("on") -> true
                else -> !isFlashlightOn // toggle
            }

            return RouteResult.DeviceAction(
                actionName = "TOOL_CALL // TOGGLE_TORCH",
                details = "Target State: ${if (enable) "ON" else "OFF"}",
                intent = null,
                feedback = "Torcia Galaxy S24 ${if (enable) "attivata" else "disattivata"}.",
                directAction = { toggleFlashlight(enable) }
            )
        }

        // ── 2. Volume & Audio Control ──
        if (q.contains("volume") || q.contains("audio") || q.contains("silenzioso") || q.contains("vibrazione")) {
            val audioIntent = Intent(Settings.ACTION_SOUND_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return RouteResult.DeviceAction(
                actionName = "TOOL_CALL // SOUND_SETTINGS",
                details = "Sound and vibration controls",
                intent = audioIntent,
                feedback = "Apertura pannello Suoni e Vibrazione One UI..."
            )
        }

        // ── 3. Wi-Fi / Bluetooth / Connettività ──
        if (q.contains("wifi") || q.contains("wi-fi") || q.contains("connessione")) {
            return RouteResult.DeviceAction(
                actionName = "TOOL_CALL // WIFI_SETTINGS",
                details = "Action: Settings.ACTION_WIFI_SETTINGS",
                intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) },
                feedback = "Apertura impostazioni Wi-Fi..."
            )
        }
        if (q.contains("bluetooth") || q.contains("bt")) {
            return RouteResult.DeviceAction(
                actionName = "TOOL_CALL // BLUETOOTH_SETTINGS",
                details = "Action: Settings.ACTION_BLUETOOTH_SETTINGS",
                intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) },
                feedback = "Apertura impostazioni Bluetooth..."
            )
        }

        // ── 4. Batteria & Risparmio Energetico ──
        if (q.contains("batteria") || q.contains("battery") || q.contains("risparmio energetico")) {
            val batteryIntent = Intent(Intent.ACTION_POWER_USAGE_SUMMARY).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return RouteResult.DeviceAction(
                actionName = "TOOL_CALL // BATTERY_SETTINGS",
                details = "Power & Battery Usage",
                intent = batteryIntent,
                feedback = "Apertura pannello stato Batteria Galaxy S24..."
            )
        }

        // ── 5. Display / Schermo / Luminosità ──
        if (q.contains("display") || q.contains("schermo") || q.contains("luminosit") || q.contains("brightness")) {
            val displayIntent = Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return RouteResult.DeviceAction(
                actionName = "TOOL_CALL // DISPLAY_SETTINGS",
                details = "Display & Screen Settings",
                intent = displayIntent,
                feedback = "Apertura impostazioni Display e Schermo..."
            )
        }

        // ── 6. Chiamate / Telefono / Rubrica ──
        if (q.startsWith("chiama ") || q.startsWith("call ") || q.contains("telefono") || q.contains("dialer")) {
            val target = q.removePrefix("chiama ").removePrefix("call ").trim()
            val callIntent = if (target.matches(Regex("""\+?[0-9\s\-]+""")) && target.isNotBlank()) {
                Intent(Intent.ACTION_DIAL, Uri.parse("tel:$target"))
            } else {
                Intent(Intent.ACTION_DIAL)
            }.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }

            return RouteResult.DeviceAction(
                actionName = "TOOL_CALL // TELEPHONE_DIALER",
                details = "Dialer target: $target",
                intent = callIntent,
                feedback = "Apertura dialer telefonico Galaxy S24..."
            )
        }

        if (q.contains("contatti") || q.contains("rubrica") || q.contains("contacts")) {
            val contactsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("content://contacts/people")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return RouteResult.DeviceAction(
                actionName = "TOOL_CALL // CONTACTS",
                details = "Action: Contacts List",
                intent = contactsIntent,
                feedback = "Apertura Rubrica contatti..."
            )
        }

        // ── 7. Messaggi SMS ──
        if (q.startsWith("invia messaggio") || q.startsWith("manda sms") || q.contains("messaggi") || q.contains("sms")) {
            val smsIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_MESSAGING)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return RouteResult.DeviceAction(
                actionName = "TOOL_CALL // MESSAGES_SMS",
                details = "App: One UI Messages",
                intent = smsIntent,
                feedback = "Apertura applicazione Messaggi..."
            )
        }

        // ── 8. Sveglie, Timer e Orologio ──
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
                    feedback = "Impostazione sveglia alle %02d:%02d su Samsung Clock...".format(hour, min)
                )
            }
            val clockIntent = context.packageManager.getLaunchIntentForPackage("com.sec.android.app.clockpackage")
                ?: Intent(AlarmClock.ACTION_SHOW_ALARMS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            return RouteResult.DeviceAction(
                actionName = "TOOL_CALL // OPEN_CLOCK",
                details = "Samsung Clock & Alarms",
                intent = clockIntent,
                feedback = "Apertura Orologio e Sveglie..."
            )
        }

        // ── 9. Gestione File (My Files) ──
        if (q.contains("file") || q.contains("archivio") || q.contains("download")) {
            val filesIntent = context.packageManager.getLaunchIntentForPackage("com.sec.android.app.myfiles")
                ?: Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(Uri.parse("content://com.android.externalstorage.documents/root/primary"), "*/*")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            return RouteResult.DeviceAction(
                actionName = "TOOL_CALL // FILE_MANAGER",
                details = "One UI My Files",
                intent = filesIntent,
                feedback = "Apertura Archivio File..."
            )
        }

        // ── 10. Registratore Vocale (Voice Recorder) ──
        if (q.contains("registra") || q.contains("registratore") || q.contains("memo vocale")) {
            val voiceIntent = context.packageManager.getLaunchIntentForPackage("com.sec.android.app.voicenote")
                ?: Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            return RouteResult.DeviceAction(
                actionName = "TOOL_CALL // VOICE_RECORDER",
                details = "Samsung Voice Note",
                intent = voiceIntent,
                feedback = "Avvio Registratore Vocale..."
            )
        }

        // ── 11. Apertura / Lancio Applicazioni Generali ──
        if (q.startsWith("apri ") || q.startsWith("open ") || q.startsWith("avvia ") || q.startsWith("lancia ") || q.startsWith("vai a ")) {
            val appTarget = q.removePrefix("apri ")
                .removePrefix("open ")
                .removePrefix("avvia ")
                .removePrefix("lancia ")
                .removePrefix("vai a ")
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

        // ── 12. Impostazioni Generiche ──
        if (q.contains("impostazioni") || q.contains("settings")) {
            return RouteResult.DeviceAction(
                actionName = "TOOL_CALL // ANDROID_SETTINGS",
                details = "Opening system settings pane",
                intent = Intent(Settings.ACTION_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) },
                feedback = "Apertura impostazioni di sistema..."
            )
        }

        // ── 13. Ricerca Web / Google ──
        if (q.startsWith("cerca ") || q.startsWith("search ") || q.startsWith("google ")) {
            val searchTerm = q.removePrefix("cerca ").removePrefix("search ").removePrefix("google ").trim()
            val webIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(SearchManager.QUERY, searchTerm)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return RouteResult.DeviceAction(
                actionName = "TOOL_CALL // WEB_SEARCH",
                details = "Query: $searchTerm",
                intent = webIntent,
                feedback = "Ricerca per '$searchTerm'..."
            )
        }

        // ── 14. Fallback -> HostAI Local Neural Engine (port 8080) ──
        return RouteResult.HostAiPrompt(
            prompt = query,
            explanation = "Query complessa/ragionamento. Routing verso HostAI (Xclipse 940 OpenCL :8080)"
        )
    }

    private fun toggleFlashlight(enable: Boolean): Boolean {
        return try {
            val camManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            for (id in camManager.cameraIdList) {
                val chars = camManager.getCameraCharacteristics(id)
                val hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val facing = chars.get(CameraCharacteristics.LENS_FACING)
                if (hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    camManager.setTorchMode(id, enable)
                    isFlashlightOn = enable
                    return true
                }
            }
            false
        } catch (e: Exception) {
            Log.e("NeedleRouter", "Errore torcia: ${e.message}", e)
            false
        }
    }

    private fun findAppIntent(appName: String): Intent? {
        val pm = context.packageManager

        // Aliases & Hardware shortcuts
        when (appName) {
            "camera", "fotocamera", "cam" -> {
                val camIntent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (camIntent.resolveActivity(pm) != null) return camIntent
                return pm.getLaunchIntentForPackage("com.sec.android.app.camera")
            }
            "galleria", "gallery", "foto", "photos" -> {
                return pm.getLaunchIntentForPackage("com.sec.android.gallery3d")
                    ?: pm.getLaunchIntentForPackage("com.google.ai.edge.gallery")
                    ?: Intent(Intent.ACTION_VIEW).apply {
                        type = "image/*"
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
            }
            "orologio", "clock", "sveglia" -> {
                return pm.getLaunchIntentForPackage("com.sec.android.app.clockpackage")
            }
            "browser", "internet", "web", "brave" -> {
                return pm.getLaunchIntentForPackage("com.brave.browser")
                    ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com")).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            }
            "hostai", "host ai" -> {
                return pm.getLaunchIntentForPackage("com.wannaphong.hostai")
            }
            "s24", "ottimizzatore", "optimizer" -> {
                return pm.getLaunchIntentForPackage("com.s24optimizer")
            }
            "launcher", "nova" -> {
                return pm.getLaunchIntentForPackage("com.michde.opennovalauncher")
                    ?: pm.getLaunchIntentForPackage("com.teslacoilsw.launcher")
            }
            "store", "play store", "aurora" -> {
                return pm.getLaunchIntentForPackage("com.aurora.store")
                    ?: pm.getLaunchIntentForPackage("com.android.vending")
            }
            "fdroid", "f-droid" -> {
                return pm.getLaunchIntentForPackage("org.fdroid.fdroid")
            }
            "impostazioni", "settings" -> {
                return Intent(Settings.ACTION_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            }
        }

        // Query Launcher Intents directly across all installed packages
        val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(launcherIntent, 0)
        for (info in resolveInfos) {
            val label = info.loadLabel(pm).toString().lowercase()
            val pkg = info.activityInfo.packageName.lowercase()
            if (label == appName || label.contains(appName) || pkg.contains(appName)) {
                return pm.getLaunchIntentForPackage(info.activityInfo.packageName)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
        }

        // Fallback installed apps list
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
