package com.s24optimizer.ui

import androidx.compose.runtime.*
import com.s24optimizer.data.OptimizationRunner
import com.s24optimizer.data.Optimizations
import com.s24optimizer.exec.AdbExecutor
import com.s24optimizer.ui.navigation.AppNavigation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** Optimizations checked per shell invocation. */
private const val CHECK_CHUNK = 25

@Composable
fun MainScreen() {
    var italian by remember { mutableStateOf(false) }
    val executor = AdbExecutor.instance
    var shizukuStatus by remember { mutableStateOf(executor.isConnected && executor.permissionsGranted) }
    var appliedStates by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    var log by remember { mutableStateOf("") }
    var isRunning by remember { mutableStateOf(false) }

    val allOptimizations = remember { Optimizations.getAll() }

    // Periodic Shizuku status poll
    LaunchedEffect(Unit) {
        while (true) {
            shizukuStatus = executor.isConnected && executor.permissionsGranted
            delay(2000)
        }
    }

    // Load applied states when Shizuku connects.
    // Every check is a blocking shell call, so this runs on IO and batches many checks
    // into a single shell rather than spawning one process per optimization.
    LaunchedEffect(shizukuStatus) {
        // Entries the app answers itself need no shell, so they are read before the
        // Shizuku guard: with the bridge down their box is still truthful.
        val local = allOptimizations.filter { it.local != null }
        if (local.isNotEmpty()) {
            appliedStates = appliedStates + local.associate {
                it.id to (OptimizationRunner.isApplied(executor, it) ?: false)
            }
        }
        if (!shizukuStatus) return@LaunchedEffect

        val shell = allOptimizations.filter { it.local == null }
        val checkable = shell.filter { it.checkCommands.isNotEmpty() }
        val uncheckable = shell.filter { it.checkCommands.isEmpty() }
        if (uncheckable.isNotEmpty()) {
            appliedStates = appliedStates + uncheckable.associate { it.id to false }
        }

        for (chunk in checkable.chunked(CHECK_CHUNK)) {
            val results = withContext(Dispatchers.IO) {
                executor.executeBatch(chunk.mapNotNull { it.checkExpression() })
            }
            appliedStates = appliedStates + chunk.mapIndexed { i, opt ->
                val r = results.getOrNull(i)
                opt.id to (r != null && r.error == null && r.stdout.trim() == "1")
            }.toMap()
        }
    }

    AppNavigation(
        italian = italian,
        onToggleLanguage = { italian = !italian },
        executor = executor,
        shizukuStatus = shizukuStatus,
        appliedStates = appliedStates,
        onAppliedStatesChanged = { update -> appliedStates = appliedStates + update },
        log = log,
        onLog = { msg -> log += "$msg\n" },
        onClearLog = { log = "" },
        isRunning = isRunning,
        onRunningChanged = { isRunning = it },
    )
}
