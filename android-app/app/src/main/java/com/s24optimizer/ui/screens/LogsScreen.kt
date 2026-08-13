package com.s24optimizer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.s24optimizer.ui.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LogsScreen(
    italian: Boolean,
    log: String,
    onClearLog: () -> Unit,
) {
    val t = { en: String, it: String -> if (italian) it else en }
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    // Auto-scroll to bottom on new log
    LaunchedEffect(log) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(12.dp))

        // ── Header ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    t("Execution Log", "Registro Esecuzione"),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    if (log.isEmpty()) t("No activity yet", "Nessuna attività")
                    else "${log.lines().size} " + t("lines", "righe"),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Copy
                IconButton(onClick = {
                    clipboardManager.setText(AnnotatedString(log.ifEmpty { "No logs" }))
                }) {
                    Icon(Icons.Default.ContentCopy, null, tint = MaterialTheme.colorScheme.primary)
                }
                // Save
                IconButton(onClick = {
                    scope.launch(Dispatchers.IO) {
                        try {
                            val dir = java.io.File(context.getExternalFilesDir(null), "logs")
                            dir.mkdirs()
                            val file = java.io.File(
                                dir,
                                "exec_log_${java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())}.txt"
                            )
                            file.writeText(log.ifEmpty { "No logs" })
                        } catch (_: Exception) { }
                    }
                }) {
                    Icon(Icons.Default.SaveAlt, null, tint = MaterialTheme.colorScheme.primary)
                }
                // Clear
                IconButton(onClick = onClearLog) {
                    Icon(Icons.Default.DeleteOutline, null, tint = CoralAccent)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Log content ──
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceElevated)
                .border(1.dp, OutlineDim.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                .padding(16.dp),
        ) {
            SelectionContainer {
                val styledLog = if (log.isEmpty()) {
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = TextSecondary)) {
                            append(t("Waiting for commands...\nApply optimizations to see output here.", "In attesa di comandi...\nApplica ottimizzazioni per vedere l'output qui."))
                        }
                    }
                } else {
                    buildAnnotatedString {
                        log.lines().forEachIndexed { i, line ->
                            if (i > 0) append("\n")
                            val color = when {
                                line.startsWith("ERR:") || line.contains("ERR:") -> CoralAccent
                                line.contains("[LOG salvato:") -> MaterialTheme.colorScheme.primary
                                line.contains("[ERRORE salvataggio:") -> CoralAccent
                                line.contains("Done!") || line.contains("Fatto!") || line.trim() == "OK" -> NeonCyan
                                line.contains("[Screen-Off") -> WarmAmber
                                line.startsWith("  ") -> TextSecondary
                                else -> TextPrimary
                            }
                            withStyle(SpanStyle(color = color)) { append(line) }
                        }
                    }
                }

                Text(
                    text = styledLog,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}
