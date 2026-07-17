package com.s24optimizer.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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
    var selectedTabIndex by remember { mutableStateOf(0) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var log by remember { mutableStateOf("") }
    var isRunning by remember { mutableStateOf(value = false) }
    var italian by remember { mutableStateOf(false) }
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
            if (opt.checkCommands.isEmpty()) {
                appliedStates = appliedStates + (opt.id to false)
                continue
            }
            var applied = true
            for (cmd in opt.checkCommands) {
                val result = executor.execute(cmd)
                val checkPassed = result.isSuccess && result.stdout.trim() == "1"
                if (!checkPassed) {
                    android.util.Log.w("MainScreen", "Check fail [${opt.id}]: cmd=$cmd exit=${result.exitCode} stderr=${result.stderr.trim()}")
                    applied = false
                    break
                }
            }
            appliedStates = appliedStates + (opt.id to applied)
            delay(50)
        }
    }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val profileManager = remember { ProfileManager(context) }
    var profiles by remember { mutableStateOf(profileManager.loadAll()) }
    var showProfileDialog by remember { mutableStateOf(false) }

    val t = { en: String, it: String -> if (italian) it else en }

    val gradientBg = Brush.verticalGradient(listOf(SurfaceDark, SurfaceDark, Color(0xFF0F111A)))

    Box(modifier = Modifier.fillMaxSize().background(gradientBg)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                t("S24 Battery Optimizer", "S24 Ottimizzatore Batteria"),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = FontFamily.Monospace,
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (shizukuStatus) NeonCyan else CoralAccent)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    if (shizukuStatus) "Shizuku Active" else "Shizuku Disconnected",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (shizukuStatus) NeonCyan else CoralAccent,
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { showProfileDialog = true }) {
                            Icon(Icons.Default.Save, contentDescription = "Profiles", tint = ElectricBlue)
                        }
                        IconButton(onClick = { italian = !italian }) {
                            Icon(Icons.Default.Translate, contentDescription = "Language", tint = ElectricBlue)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                    ),
                )
            },
            containerColor = Color.Transparent,
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Build tab list: Quick Settings, categories, Logs
            val tabs = buildList {
                add(t("Quick Settings", "Impostazioni Rapide"))
                optimizations.keys.forEach { add(if (italian) it.labelIt else it.labelEn) }
                add(t("Logs", "Log"))
            }

            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 0.dp,
            ) {
                tabs.forEachIndexed { index, label ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                label,
                                maxLines = 1,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                    )
                }
            }

            val categoriesCount = optimizations.keys.size

            if (selectedTabIndex == 0) {
                QuickSettingsBar(
                    italian = italian,
                    executor = executor,
                    isRunning = isRunning,
                    onLog = { msg -> log += "$msg\n" },
                )
            }

            if (selectedTabIndex > 0 && selectedTabIndex <= categoriesCount) {
                val catIndex = selectedTabIndex - 1
                val categoriesList = optimizations.keys.toList()
                if (catIndex < categoriesList.size) {
                    val cat = categoriesList[catIndex]
                    val opts = optimizations[cat] ?: emptyList()
                    val appliedCount = appliedStates.count { it.value }
                    var searchQuery by remember { mutableStateOf("") }
                    val filteredOpts = if (searchQuery.isBlank()) opts else opts.filter {
                        val title = if (italian) it.titleIt else it.titleEn
                        title.contains(searchQuery, ignoreCase = true)
                    }

                    // Category header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, Brush.horizontalGradient(listOf(ElectricBlue.copy(alpha = 0.4f), NeonCyan.copy(alpha = 0.2f))), RoundedCornerShape(16.dp))
                            .background(SurfaceElevated)
                    ) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    cat.icon(),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = ElectricBlue,
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (italian) cat.labelIt else cat.labelEn,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                )
                            }

                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider(color = OutlineDim.copy(alpha = 0.4f))
                            Spacer(Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        (if (italian) "Selezionate: " else "Selected: ") +
                                            "${selectedIds.size}/${opts.size}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = FontFamily.Monospace,
                                    )
                                    if (appliedCount > 0) {
                                        Text(
                                            (if (italian) "Applicate: " else "Applied: ") + "$appliedCount",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontFamily = FontFamily.Monospace,
                                            color = NeonCyan,
                                        )
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { selectedIds = opts.map { it.id }.toSet() },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue.copy(alpha = 0.2f), contentColor = ElectricBlue),
                                        shape = RoundedCornerShape(10.dp),
                                    ) { Text(t("Select All", "Seleziona Tutte"), style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace) }
                                    OutlinedButton(
                                        onClick = { selectedIds = emptySet() },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                                        border = BorderStroke(1.dp, OutlineDim.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(10.dp),
                                    ) { Text(t("Clear", "Deseleziona"), style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace) }
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        isRunning = true
                                        log = ""
                                        for (opt in opts.filter { it.id in selectedIds }) {
                                            var allOk = true
                                            for (cmd in opt.applyCommands) {
                                                withContext(Dispatchers.Main) { log += "${opt.titleEn}: $cmd\n" }
                                                val result = executor.execute(cmd)
                                                withContext(Dispatchers.Main) { log += "  $result\n" }
                                                if (!result.isSuccess) allOk = false
                                            }
                                            appliedStates = appliedStates + (opt.id to allOk)
                                        }
                                        withContext(Dispatchers.Main) { log += t("Done!", "Fatto!"); isRunning = false }
                                    }
                                },
                                enabled = selectedIds.isNotEmpty() && !isRunning && shizukuStatus,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricBlue.copy(alpha = 0.2f),
                                    contentColor = ElectricBlue,
                                    disabledContainerColor = ElectricBlue.copy(alpha = 0.05f),
                                    disabledContentColor = TextSecondary.copy(alpha = 0.3f),
                                ),
                                shape = RoundedCornerShape(10.dp),
                            ) {
                                if (isRunning) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = ElectricBlue,
                                    )
                                } else {
                                    Text(t("Apply Selected", "Applica Selezionate"), fontFamily = FontFamily.Monospace)
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        isRunning = true
                                        log = ""
                                        for (opt in opts.filter { it.id in selectedIds }) {
                                            for (cmd in opt.revertCommands) {
                                                withContext(Dispatchers.Main) { log += "${opt.titleEn}: $cmd\n" }
                                                val result = executor.execute(cmd)
                                                withContext(Dispatchers.Main) { log += "  $result\n" }
                                            }
                                            appliedStates = appliedStates + (opt.id to false)
                                        }
                                        withContext(Dispatchers.Main) { log += t("Done!", "Fatto!"); isRunning = false }
                                    }
                                },
                                enabled = selectedIds.isNotEmpty() && !isRunning && shizukuStatus,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralAccent, disabledContentColor = CoralAccent.copy(alpha = 0.3f)),
                                border = BorderStroke(1.dp, CoralAccent.copy(alpha = if (selectedIds.isNotEmpty()) 0.5f else 0.15f)),
                                shape = RoundedCornerShape(10.dp),
                            ) { Text(t("Revert Selected", "Ripristina Selezionate"), fontFamily = FontFamily.Monospace, maxLines = 1) }
                            }
                        }
                    }

                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).heightIn(min = 40.dp),
                    placeholder = { Text(t("Search...", "Cerca..."), style = MaterialTheme.typography.bodySmall, color = TextSecondary.copy(alpha = 0.5f)) },
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = TextPrimary),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue.copy(alpha = 0.4f),
                        unfocusedBorderColor = OutlineDim.copy(alpha = 0.3f),
                        cursorColor = ElectricBlue,
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard,
                    ),
                )

                // Optimization list
                LazyColumn(modifier = Modifier.weight(1f).padding(top = 4.dp)) {
                    items(filteredOpts) { opt ->
                        OptimizationRow(
                            opt = opt,
                            selected = opt.id in selectedIds,
                            applied = appliedStates[opt.id] ?: false,
                            italian = italian,
                            onToggle = {
                                selectedIds = if (opt.id in selectedIds) selectedIds - opt.id else selectedIds + opt.id
                            },
                        )
                    }
                }
            }
            }

            if (selectedTabIndex > categoriesCount) {
                val scrollState = rememberScrollState()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Brush.horizontalGradient(listOf(ElectricBlue.copy(alpha = 0.4f), NeonCyan.copy(alpha = 0.2f))), RoundedCornerShape(16.dp))
                        .background(SurfaceElevated)
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(t("Execution Log", "Registro Esecuzione"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            OutlinedButton(
                                onClick = { log = "" },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                                border = BorderStroke(1.dp, OutlineDim.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(10.dp),
                            ) { Text(t("Clear", "Pulisci"), style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace) }
                            Spacer(Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        try {
                                            val dir = java.io.File(context.getExternalFilesDir(null), "logs")
                                            dir.mkdirs()
                                            val file = java.io.File(dir, "exec_log_${java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())}.txt")
                                            file.writeText(log.ifEmpty { t("No logs yet", "Nessun log ancora") })
                                            withContext(Dispatchers.Main) { log += "\n[LOG salvato: ${file.absolutePath}]\n" }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) { log += "\n[ERRORE salvataggio: ${e.message}]\n" }
                                        }
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricBlue),
                                border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(10.dp),
                            ) { Text(t("Save", "Salva"), style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace) }
                        }
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(color = OutlineDim.copy(alpha = 0.4f))
                        Spacer(Modifier.height(8.dp))
                        SelectionContainer {
                            val styledLog = if (log.isEmpty()) {
                                buildAnnotatedString { withStyle(SpanStyle(color = TextSecondary)) { append(t("No logs yet", "Nessun log ancora")) } }
                            } else {
                                buildAnnotatedString {
                                    log.lines().forEachIndexed { i, line ->
                                        if (i > 0) append("\n")
                                        val color = when {
                                            line.startsWith("ERR:") || line.contains("ERR:") -> CoralAccent
                                            line.contains("[LOG salvato:") -> ElectricBlue
                                            line.contains("[ERRORE salvataggio:") -> CoralAccent
                                            line.contains("Done!") || line.contains("Fatto!") || line.trim() == "OK" -> NeonCyan
                                            else -> TextPrimary
                                        }
                                        withStyle(SpanStyle(color = color)) { append(line) }
                                    }
                                }
                            }
                            Text(
                                text = styledLog,
                                modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(4.dp),
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                            )
                        }
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
    val rowBg = when {
        applied && selected -> ElectricBlue.copy(alpha = 0.1f)
        applied -> SurfaceElevated.copy(alpha = 0.8f)
        selected -> ElectricBlue.copy(alpha = 0.05f)
        else -> SurfaceCard
    }
    val rowBorder = if (selected) ElectricBlue.copy(alpha = 0.5f) else OutlineDim.copy(alpha = 0.15f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (selected) Modifier.border(
                    1.dp,
                    Brush.horizontalGradient(listOf(ElectricBlue.copy(alpha = 0.5f), NeonCyan.copy(alpha = 0.2f))),
                    RoundedCornerShape(12.dp)
                ) else Modifier
            )
            .background(rowBg)
            .clickable(onClick = onToggle)
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (italian) opt.titleIt else opt.titleEn,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (italian) opt.descIt else opt.descEn,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2
                )
            }
            Spacer(Modifier.width(4.dp))
            if (applied && opt.checkCommands.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(NeonCyan)
                )
                Spacer(Modifier.width(4.dp))
            }
            Checkbox(
                checked = selected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = ElectricBlue,
                    uncheckedColor = OutlineDim.copy(alpha = 0.5f),
                    checkmarkColor = SurfaceDark,
                ),
            )
        }
    }
}
