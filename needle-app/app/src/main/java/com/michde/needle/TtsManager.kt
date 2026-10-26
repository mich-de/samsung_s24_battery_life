package com.michde.needle

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class TtsManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        // Priorità motore Sherpa-ONNX TTS (offline Piper/Kokoro), poi VoxSherpa, fallback Samsung SMT
        val sherpaEngine = "com.k2fsa.sherpa.onnx.tts.engine"
        val voxSherpaEngine = "com.CodeBySonu.VoxSherpa"
        tts = try {
            TextToSpeech(context, this, sherpaEngine)
        } catch (e: Exception) {
            try {
                TextToSpeech(context, this, voxSherpaEngine)
            } catch (e2: Exception) {
                TextToSpeech(context, this, "com.samsung.SMT")
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            tts?.setAudioAttributes(audioAttributes)

            tts?.language = Locale.ITALIAN
            tts?.setSpeechRate(1.0f)
            tts?.setPitch(1.0f)
            isInitialized = true

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    Log.i("TtsManager", "Kokoro TTS Playback STARTED: $utteranceId")
                }
                override fun onDone(utteranceId: String?) {
                    Log.i("TtsManager", "Kokoro TTS Playback FINISHED: $utteranceId")
                }
                override fun onError(utteranceId: String?) {
                    Log.e("TtsManager", "Kokoro TTS Playback ERROR: $utteranceId")
                }
            })
            Log.i("TtsManager", "TTS Engine Active: ${tts?.defaultEngine}")
        } else {
            Log.e("TtsManager", "TTS Init failed, retrying system fallback...")
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

        val params = Bundle().apply {
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC)
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
        }
        val res = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "NeedleKokoro_${System.currentTimeMillis()}")
        Log.i("TtsManager", "speak called: code=$res text='$text'")
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
