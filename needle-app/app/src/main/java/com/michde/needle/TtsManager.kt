package com.michde.needle

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TtsManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        // Tenta prima il motore Kokoro VoxSherpa se installato, altrimenti default di sistema
        val voxSherpaEngine = "com.CodeBySonu.VoxSherpa"
        tts = try {
            TextToSpeech(context, this, voxSherpaEngine)
        } catch (e: Exception) {
            TextToSpeech(context, this)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.ITALIAN)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.language = Locale.ENGLISH
            }
            tts?.setSpeechRate(1.05f)
            tts?.setPitch(1.0f)
            isInitialized = true
            Log.i("TtsManager", "TTS initialized successfully with engine: ${tts?.defaultEngine}")
        } else {
            // Fallback al motore generico di sistema
            Log.w("TtsManager", "Failed to init preferred TTS engine, falling back to default...")
            tts = TextToSpeech(context) { s ->
                if (s == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.ITALIAN
                    isInitialized = true
                }
            }
        }
    }

    fun speak(text: String) {
        if (!isInitialized || tts == null) {
            Log.w("TtsManager", "TTS not initialized yet")
            return
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "NeedleTtsConfirmation")
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
