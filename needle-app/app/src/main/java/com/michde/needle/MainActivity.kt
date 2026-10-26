package com.michde.needle

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.michde.needle.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var router: NeedleRouter
    private lateinit var ttsManager: TtsManager

    private val speechLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                binding.etPrompt.setText(spokenText)
                executeRoute(spokenText)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        router = NeedleRouter(this)
        ttsManager = TtsManager(this)

        binding.btnRoute.setOnClickListener {
            hideKeyboard()
            val query = binding.etPrompt.text.toString()
            if (query.isNotBlank()) executeRoute(query)
        }

        binding.etPrompt.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
                val query = binding.etPrompt.text.toString()
                if (query.isNotBlank()) executeRoute(query)
                true
            } else false
        }

        binding.btnMic.setOnClickListener {
            startSpeechInput()
        }

        binding.btnQuickHostAi.setOnClickListener {
            val intent = packageManager.getLaunchIntentForPackage("com.wannaphong.hostai")
            if (intent != null) startActivity(intent)
            else Toast.makeText(this, "HostAI non trovato", Toast.LENGTH_SHORT).show()
        }

        binding.btnQuickWebChat.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://127.0.0.1:8080/chat")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(browserIntent)
        }
    }

    private fun startSpeechInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Needle Router: parla adesso...")
        }
        try {
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Riconoscimento vocale non disponibile", Toast.LENGTH_SHORT).show()
        }
    }

    private fun executeRoute(query: String) {
        val sb = StringBuilder()
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("PROMPT: \"$query\"\n")

        val startTime = System.currentTimeMillis()
        val result = router.route(query)
        val routeLatency = System.currentTimeMillis() - startTime

        when (result) {
            is RouteResult.DeviceAction -> {
                sb.append("DECISION: ${result.actionName} (${routeLatency} ms)\n")
                sb.append("DETAIL: ${result.details}\n")
                sb.append("STATUS: ${result.feedback}\n")
                binding.tvConsole.text = sb.toString()
                ttsManager.speak(result.feedback)

                if (result.directAction != null) {
                    val success = result.directAction.invoke()
                    if (!success) {
                        sb.append("ERR: Esecuzione azione hardware fallita\n")
                        binding.tvConsole.text = sb.toString()
                    }
                }

                if (result.intent != null) {
                    try {
                        startActivity(result.intent)
                    } catch (e: Exception) {
                        sb.append("ERR: ${e.message}\n")
                        binding.tvConsole.text = sb.toString()
                    }
                }
            }
            is RouteResult.HostAiPrompt -> {
                sb.append("DECISION: ROUTE_TO_HOSTAI_LLM (${routeLatency} ms)\n")
                sb.append("DETAIL: ${result.explanation}\n")
                sb.append("STATUS: Querying HostAI engine :8080...\n")
                binding.tvConsole.text = sb.toString()

                lifecycleScope.launch(Dispatchers.IO) {
                    val reply = router.queryHostAi(result.prompt)
                    withContext(Dispatchers.Main) {
                        sb.append("─── HOSTAI INFERENCE REPLY ───\n")
                        sb.append("$reply\n")
                        binding.tvConsole.text = sb.toString()
                        ttsManager.speak(reply)
                    }
                }
            }
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        currentFocus?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
    }
}
