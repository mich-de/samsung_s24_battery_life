package com.s24optimizer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.s24optimizer.data.Optimization
import com.s24optimizer.data.Optimizations
import com.s24optimizer.data.Profile
import com.s24optimizer.data.ProfileManager
import com.s24optimizer.exec.AdbExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val optimizations = remember { Optimizations.byCategory() }
    var expandedCategory by remember { mutableStateOf<Optimization.Category?>(null) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var log by remember { mutableStateOf("") }
    var isRunning by remember { mutableStateOf(value = false) }
    var italian by remember { mutableStateOf(Locale.getDefault().language == "it") }
    val executor = AdbExecutor.instance
    var shizukuStatus by remember { mutableStateOf(executor.isConnected && executor.permissionsGranted) }

    val allOptimizations = remember { Optimizations.getAll() }
    var appliedStates by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }

    // Update status periodically + load applied states when Shizuku connects
    LaunchedEffect(Unit) {
        while(true) {
            val connected = executor.isConnected
            val granted = executor.permissionsGranted
            shizukuStatus = connected && granted
            android.util.Log.i("MainScreen", "Status poll: pingBinder=$connected, granted=$granted")
            kotlinx.coroutines.delay(2000)
        }
    }

    LaunchedEffect(shizukuStatus) {
        if (!shizukuStatus) return@LaunchedEffect
        val all = allOptimizations
        for (opt in all) {
            if (opt.checkCommands.isEmpty()) continue
            var applied = true
            for (cmd in opt.checkCommands) {
                if (!executor.check(cmd)) { applied = false; break }
            }
            appliedStates = appliedStates + (opt.id to applied)
            delay(100)
        }
        android.util.Log.i("MainScreen", "Applied states loaded: ${appliedStates.size}/${all.size}")
    }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val profileManager = remember { ProfileManager(context) }
    var profiles by remember { mutableStateOf(profileManager.loadAll()) }
    var showProfileDialog by remember { mutableStateOf(false) }

    val t = { en: String, it: String -> if (italian) it else en }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(t("S24 Battery Optimizer", "S24 Ottimizzatore Batteria"), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = if (shizukuStatus) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(8.dp),
                            ) {}
                            Spacer(Modifier.width(4.dp))
                            Text(
                                if (shizukuStatus) "Shizuku Active" else "Shizuku Disconnected",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (shizukuStatus) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showProfileDialog = true }) {
                        Icon(Icons.Default.Save, contentDescription = "Profiles")
                    }
                    IconButton(onClick = { italian = !italian }) {
                        Icon(Icons.Default.Translate, contentDescription = "Language")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Tabs per category
            PrimaryScrollableTabRow(
                selectedTabIndex = optimizations.keys.indexOf(expandedCategory).coerceAtLeast(0),
                modifier = Modifier.fillMaxWidth()
            ) {
                optimizations.keys.forEach { cat ->
                    Tab(
                        selected = cat == expandedCategory,
                        onClick = { expandedCategory = if (expandedCategory == cat) null else cat },
                        text = { Text(if (italian) cat.labelIt else cat.labelEn, maxLines = 1) }
                    )
                }
            }

            // Selected count bar
            val appliedCount = appliedStates.count { it.value }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        (if (italian) "Selezionate: " else "Selected: ") +
                            "${selectedIds.size}/${optimizations.values.flatten().size}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (appliedCount > 0) {
                        Text(
                            (if (italian) "Applicate: " else "Applied: ") + "$appliedCount",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            selectedIds = optimizations.values.asSequence().flatten().map { it.id }.toSet()
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) { Text(t("Select All", "Seleziona Tutte"), style = MaterialTheme.typography.bodySmall) }

                    OutlinedButton(
                        onClick = { selectedIds = emptySet() },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) { Text(t("Clear", "Deseleziona"), style = MaterialTheme.typography.bodySmall) }
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            isRunning = true
                            log = ""
                            val toApply = optimizations.values.asSequence().flatten().filter { it.id in selectedIds }.toList()
                            for (opt in toApply) {
                                for (cmd in opt.applyCommands) {
                                    withContext(Dispatchers.Main) {
                                        log += "${opt.titleEn}: $cmd\n"
                                    }
                                    val result = executor.execute(cmd)
                                    withContext(Dispatchers.Main) {
                                        log += "  $result\n"
                                    }
                                }
                                if (opt.checkCommands.isNotEmpty()) {
                                    var applied = true
                                    for (cmd in opt.checkCommands) {
                                        if (!executor.check(cmd)) { applied = false; break }
                                    }
                                    appliedStates = appliedStates + (opt.id to applied)
                                }
                            }
                            withContext(Dispatchers.Main) {
                                log += t("Done!", "Fatto!")
                                isRunning = false
                            }
                        }
                    },
                    enabled = selectedIds.isNotEmpty() && !isRunning && shizukuStatus,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isRunning) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(t("Apply Selected", "Applica Selezionate"))
                    }
                }

                OutlinedButton(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            isRunning = true
                            log = ""
                            val toRevert = optimizations.values.asSequence().flatten().filter { it.id in selectedIds }.toList()
                            for (opt in toRevert) {
                                for (cmd in opt.revertCommands) {
                                    withContext(Dispatchers.Main) {
                                        log += "${opt.titleEn}: $cmd\n"
                                    }
                                    val result = executor.execute(cmd)
                                    withContext(Dispatchers.Main) {
                                        log += "  $result\n"
                                    }
                                }
                                if (opt.checkCommands.isNotEmpty()) {
                                    var applied = true
                                    for (cmd in opt.checkCommands) {
                                        if (!executor.check(cmd)) { applied = false; break }
                                    }
                                    appliedStates = appliedStates + (opt.id to applied)
                                }
                            }
                            withContext(Dispatchers.Main) {
                                log += t("Done!", "Fatto!")
                                isRunning = false
                            }
                        }
                    },
                    enabled = selectedIds.isNotEmpty() && !isRunning && shizukuStatus,
                    modifier = Modifier.weight(1f)
                ) { Text(t("Revert Selected", "Ripristina Selezionate")) }
            }

            // Optimization list
            LazyColumn(modifier = Modifier.weight(1f)) {
                optimizations.forEach { (cat, opts) ->
                    if (expandedCategory == null || (expandedCategory == cat)) {
                        items(opts) { opt ->
                            OptimizationRow(
                                opt = opt,
                                selected = opt.id in selectedIds,
                                applied = appliedStates[opt.id] ?: false,
                                italian = italian,
                                onToggle = {
                                    selectedIds = if (opt.id in selectedIds) {
                                        selectedIds - opt.id
                                    } else {
                                        selectedIds + opt.id
                                    }
                                },
                            )
                        }
                    }
                }
            }

            // Log output
            AnimatedVisibility(visible = log.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    SelectionContainer {
                        Text(
                            text = log,
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        if (showProfileDialog) {
            ProfileDialog(
                profiles = profiles,
                onSave = { name ->
                    profileManager.save(name, selectedIds)
                    profiles = profileManager.loadAll()
                },
                onLoad = { profile ->
                    selectedIds = profile.states.filter { it.applied }.map { it.id }.toSet()
                    showProfileDialog = false
                },
                onDelete = { name ->
                    profileManager.delete(name)
                    profiles = profileManager.loadAll()
                },
                onDismiss = { showProfileDialog = false },
                italian = italian
            )
        }
    }
}

@Composable
fun ProfileDialog(
    profiles: List<Profile>,
    onSave: (String) -> Unit,
    onLoad: (Profile) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit,
    italian: Boolean
) {
    var newProfileName by remember { mutableStateOf("") }
    val t = { en: String, it: String -> if (italian) it else en }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(t("Optimization Profiles", "Profili Ottimizzazione")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = newProfileName,
                    onValueChange = { newProfileName = it },
                    label = { Text(t("Save Current Selection as...", "Salva selezione come...")) },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (newProfileName.isNotBlank()) {
                                    onSave(newProfileName)
                                    newProfileName = ""
                                }
                            },
                            enabled = newProfileName.isNotBlank()
                        ) { Icon(Icons.Default.Add, null) }
                    }
                )

                if (profiles.isNotEmpty()) {
                    Text(t("Saved Profiles:", "Profili Salvati:"), style = MaterialTheme.typography.labelMedium)
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(profiles) { profile ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    profile.name,
                                    modifier = Modifier.weight(1f).clickable { onLoad(profile) },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                IconButton(onClick = { onDelete(profile.name) }) {
                                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(t("Close", "Chiudi")) }
        }
    )
}

@Composable
fun OptimizationRow(
    opt: Optimization,
    selected: Boolean,
    applied: Boolean,
    italian: Boolean,
    onToggle: () -> Unit,
) {
    val surfaceColor = when {
        applied && selected -> MaterialTheme.colorScheme.primaryContainer
        applied -> MaterialTheme.colorScheme.surfaceContainerHigh
        else -> MaterialTheme.colorScheme.surface
    }
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp),
        onClick = onToggle,
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (italian) opt.titleIt else opt.titleEn,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (italian) opt.descIt else opt.descEn,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
            Spacer(Modifier.width(4.dp))
            if (applied && opt.checkCommands.isNotEmpty()) {
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(8.dp),
                ) {}
                Spacer(Modifier.width(4.dp))
            }
            Checkbox(checked = selected, onCheckedChange = { onToggle() })
        }
    }
}
