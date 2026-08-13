package com.s24optimizer.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.s24optimizer.data.AppliedHistory
import com.s24optimizer.data.Optimization
import com.s24optimizer.data.OptimizationRunner
import com.s24optimizer.data.Optimizations
import com.s24optimizer.exec.AdbExecutor
import com.s24optimizer.service.ScreenOffService
import com.s24optimizer.ui.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Entries whose state lives in [ScreenOffService] rather than in a settings row. */
private val SCREEN_OFF_IDS = setOf("screen_off_low_hz", "screen_off_psm", "screen_off_sync")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptimizeScreen(
    italian: Boolean,
    executor: AdbExecutor,
    shizukuStatus: Boolean,
    appliedStates: Map<String, Boolean>,
    onAppliedStatesChanged: (Map<String, Boolean>) -> Unit,
    isRunning: Boolean,
    onRunningChanged: (Boolean) -> Unit,
    onLog: (String) -> Unit,
) {
    val t = { en: String, it: String -> if (italian) it else en }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val allByCategory = remember { Optimizations.byCategory() }
    var searchQuery by remember { mutableStateOf("") }
    var expandedCategories by remember {
        mutableStateOf(allByCategory.keys.toSet()) // All expanded by default
    }

    // Ids currently being written to the device. A row shows a spinner instead of its box
    // while it is in here, so the box never sits on a value the device has not confirmed.
    var busyIds by remember { mutableStateOf(setOf<String>()) }

    // Filter out PER_APP_RR — that has its own screen
    val categories = remember {
        allByCategory.filter { it.key != Optimization.Category.PER_APP_RR }
    }

    /**
     * Applies or reverts one entry and then asks the device what actually happened.
     *
     * The box is set from [OptimizationRunner.isApplied], not from whether the commands
     * exited cleanly: `settings put` reports success for a key nothing reads, and that is
     * exactly the case the tick has to be able to expose. Only one-shot entries, which have
     * nothing to read back, fall back to the exit status.
     */
    fun toggle(opt: Optimization, turnOn: Boolean) {
        if (opt.id in busyIds) return
        busyIds = busyIds + opt.id
        scope.launch(Dispatchers.IO) {
            onRunningChanged(true)
            try {
                runOne(context, executor, opt, turnOn, onAppliedStatesChanged, onLog)
            } finally {
                withContext(Dispatchers.Main) {
                    busyIds = busyIds - opt.id
                    onRunningChanged(false)
                }
            }
        }
    }

    /** Same thing for a whole category, one entry after another. */
    fun toggleAll(opts: List<Optimization>, turnOn: Boolean) {
        val targets = opts.filter { runnableNow(it, shizukuStatus) && (appliedStates[it.id] == true) != turnOn }
        if (targets.isEmpty()) return
        busyIds = busyIds + targets.map { it.id }
        scope.launch(Dispatchers.IO) {
            onRunningChanged(true)
            try {
                for (opt in targets) {
                    runOne(context, executor, opt, turnOn, onAppliedStatesChanged, onLog)
                    withContext(Dispatchers.Main) { busyIds = busyIds - opt.id }
                }
            } finally {
                withContext(Dispatchers.Main) {
                    busyIds = busyIds - targets.map { it.id }.toSet()
                    onRunningChanged(false)
                    onLog(t("Done!", "Fatto!"))
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // ── Search Bar ──
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = {
                Text(
                    t("Search optimizations...", "Cerca ottimizzazioni..."),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary.copy(alpha = 0.5f),
                )
            },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = TextSecondary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, null, tint = TextSecondary)
                    }
                }
            },
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                unfocusedBorderColor = OutlineDim.copy(alpha = 0.2f),
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedContainerColor = SurfaceElevated,
                unfocusedContainerColor = SurfaceCard,
            ),
        )

        if (!shizukuStatus) {
            Text(
                t(
                    "Shizuku is down — only the entries that do not need it can be changed.",
                    "Shizuku non attivo — si possono cambiare solo le voci che non lo richiedono.",
                ),
                style = MaterialTheme.typography.labelSmall,
                color = CoralAccent,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp),
            )
        }

        // ── Category list ──
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            categories.forEach { (category, opts) ->
                val isExpanded = category in expandedCategories
                val filteredOpts = if (searchQuery.isBlank()) opts else opts.filter {
                    val title = if (italian) it.titleIt else it.titleEn
                    val desc = if (italian) it.descIt else it.descEn
                    title.contains(searchQuery, ignoreCase = true) || desc.contains(searchQuery, ignoreCase = true)
                }

                // Skip empty categories after filtering
                if (searchQuery.isNotBlank() && filteredOpts.isEmpty()) return@forEach

                // Category header
                item(key = "cat_${category.name}") {
                    val catApplied = opts.count { appliedStates[it.id] == true }
                    val rotationAngle by animateFloatAsState(
                        targetValue = if (isExpanded) 0f else -90f,
                        animationSpec = tween(200),
                        label = "rotate",
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceElevated)
                            .clickable {
                                expandedCategories = if (isExpanded) expandedCategories - category
                                else expandedCategories + category
                            }
                            .padding(start = 16.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            category.icon(),
                            null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (italian) category.labelIt else category.labelEn,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "$catApplied/${opts.size} " + t("active", "attive"),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (catApplied > 0) NeonCyan else TextSecondary,
                            )
                            // The same count as a bar. With a category collapsed this is the
                            // only thing left saying how much of it is on, and a ratio is
                            // read faster from a length than from two numbers.
                            Spacer(Modifier.height(5.dp))
                            LinearProgressIndicator(
                                progress = { if (opts.isEmpty()) 0f else catApplied.toFloat() / opts.size },
                                modifier = Modifier
                                    .fillMaxWidth(0.55f)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = if (catApplied > 0) NeonCyan else TextSecondary,
                                trackColor = OutlineDim.copy(alpha = 0.45f),
                                gapSize = 0.dp,
                                drawStopIndicator = {},
                            )
                        }

                        // Whole-category actions. They act at once like every other control
                        // on this screen — there is no staged selection to apply later.
                        IconButton(
                            onClick = { toggleAll(filteredOpts, turnOn = true) },
                            enabled = !isRunning,
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.PlaylistAddCheck,
                                t("Apply all", "Applica tutte"),
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        IconButton(
                            onClick = { toggleAll(filteredOpts, turnOn = false) },
                            enabled = !isRunning,
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                Icons.Default.PlaylistRemove,
                                t("Revert all", "Ripristina tutte"),
                                modifier = Modifier.size(20.dp),
                                tint = CoralAccent,
                            )
                        }

                        Icon(
                            Icons.Default.ExpandMore,
                            null,
                            modifier = Modifier.size(20.dp).rotate(rotationAngle),
                            tint = TextSecondary,
                        )
                    }
                }

                // Items
                if (isExpanded) {
                    // Sub-group headers, taken from each entry's own group rather than from
                    // its position in the list. Entries sharing a group are collected under
                    // one header even when they are not neighbours, and the headers come out
                    // in the order the groups first appear. Ungrouped entries lead, so they
                    // never end up filed under whichever header happened to precede them.
                    val displayItems = buildList<Any> {
                        val grouped = filteredOpts.groupBy { if (italian) it.groupIt else it.groupEn }
                        grouped[""]?.let { addAll(it) }
                        for ((group, entries) in grouped) {
                            if (group.isEmpty()) continue
                            add(group)
                            addAll(entries)
                        }
                    }

                    items(displayItems, key = { item ->
                        when (item) {
                            is String -> "group_${category.name}_$item"
                            is Optimization -> item.id
                            else -> item.hashCode()
                        }
                    }) { entry ->
                        when (entry) {
                            // A sub-heading was bare text at the same weight as a row title,
                            // so on a long category it read as another entry. A dot and a
                            // rule give it a shape no row has.
                            is String -> Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    Modifier
                                        .size(5.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    entry.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                                )
                                Spacer(Modifier.width(10.dp))
                                HorizontalDivider(color = OutlineDim.copy(alpha = 0.35f))
                            }

                            is Optimization -> {
                                OptimizationItem(
                                    opt = entry,
                                    applied = appliedStates[entry.id] == true,
                                    busy = entry.id in busyIds,
                                    enabled = runnableNow(entry, shizukuStatus),
                                    italian = italian,
                                    onToggle = { on -> toggle(entry, on) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** A one-shot action — a cache clear or a reset — has nothing to read back afterwards. */
private fun Optimization.isOneShot() = local == null && checkCommands.isEmpty()

/** Whether this entry can be changed right now: only shell entries need Shizuku up. */
private fun runnableNow(opt: Optimization, shizukuStatus: Boolean) =
    opt.local != null || shizukuStatus

/**
 * Runs one entry and pushes the device's own answer back into the applied map.
 *
 * Not a method on [OptimizationRunner] because it also carries the two things that only
 * matter to this screen: the applied-history breadcrumb and the screen-off service, which
 * has to be started once the first of its entries is on and stopped when the last goes off.
 */
private suspend fun runOne(
    context: android.content.Context,
    executor: AdbExecutor,
    opt: Optimization,
    turnOn: Boolean,
    onAppliedStatesChanged: (Map<String, Boolean>) -> Unit,
    onLog: (String) -> Unit,
) {
    val lines = mutableListOf<String>()
    val ok =
        if (turnOn) OptimizationRunner.apply(executor, opt) { lines += it }
        else OptimizationRunner.revert(executor, opt) { lines += it }

    // The device is asked again rather than trusted to have done what was asked.
    val observed = OptimizationRunner.isApplied(executor, opt) ?: (turnOn && ok)
    if (turnOn && ok && !observed && !opt.isOneShot()) {
        lines += "  ${opt.titleEn}: commands succeeded but the device still reads as off"
    }

    if (opt.id in SCREEN_OFF_IDS) {
        val remaining = ScreenOffService.getActiveFeatures(context)
        withContext(Dispatchers.Main) {
            if (remaining.isEmpty()) {
                context.stopService(Intent(context, ScreenOffService::class.java))
                lines += "[Screen-Off service stopped]"
            } else if (turnOn) {
                val started = ScreenOffService.start(context)
                lines += if (started) "[Screen-Off service started]"
                else "[Screen-Off service refused by system, retry on next app open]"
            }
        }
    }

    withContext(Dispatchers.Main) {
        lines.forEach(onLog)
        onAppliedStatesChanged(mapOf(opt.id to observed))
    }
    // Remember the intent, so a later OS update that undoes this shows up as a regression
    // rather than as an option the user simply never enabled.
    if (turnOn && observed) AppliedHistory.markApplied(context, opt.id)
    if (!turnOn) AppliedHistory.markReverted(context, opt.id)
}

// ── Single Optimization Item ──
@Composable
private fun OptimizationItem(
    opt: Optimization,
    applied: Boolean,
    busy: Boolean,
    enabled: Boolean,
    italian: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val accent = MaterialTheme.colorScheme.primary
    val rowBg by animateColorAsState(
        if (applied) accent.copy(alpha = 0.08f) else Color.Transparent,
        tween(180), label = "rowBg",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(rowBg)
            // Tapping the row opens the description; only the control on the right writes
            // to the device, so a stray tap on a long list cannot disable an app.
            .clickable { expanded = !expanded }
            .padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Active rows carry a bar down their left edge. The background tint alone was too
        // faint to pick out when scrolling, and this is the thing you scan a long list for.
        Box(
            Modifier
                .padding(vertical = 6.dp)
                .width(3.dp)
                .height(if (expanded) 40.dp else 26.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (applied) accent else Color.Transparent)
        )
        Spacer(Modifier.width(11.dp))
        Icon(
            opt.icon ?: opt.category.icon(),
            null,
            modifier = Modifier.size(18.dp),
            tint = if (applied) accent else TextSecondary,
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f).padding(vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (italian) opt.titleIt else opt.titleEn,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (applied) TextPrimary else TextPrimary.copy(alpha = 0.85f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (!opt.verified) {
                    Spacer(Modifier.width(6.dp))
                    // Was a bare 14dp glyph, which reads as decoration. A labelled pill says
                    // what it means without waiting for the description to be opened.
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CoralAccent.copy(alpha = 0.14f))
                            .padding(horizontal = 5.dp, vertical = 1.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.HelpOutline,
                            // Spelled out rather than left as a bare glyph: an unexplained
                            // warning next to a ticked box is worse than none.
                            if (italian) "Non verificata: la spunta non prova che sia cambiato qualcosa"
                            else "Unverified: the tick does not prove anything changed",
                            modifier = Modifier.size(11.dp),
                            tint = CoralAccent,
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            if (italian) "non verificata" else "unverified",
                            style = MaterialTheme.typography.labelSmall,
                            color = CoralAccent,
                            fontSize = 9.sp,
                        )
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = if (italian) opt.descIt else opt.descEn,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = if (expanded) Int.MAX_VALUE else 1,
                // Without this the single collapsed line was cut mid-word with nothing to
                // say it continued, so a row looked like it had a truncated sentence in it.
                overflow = TextOverflow.Ellipsis,
            )
            if (!enabled) {
                Text(
                    if (italian) "Richiede Shizuku" else "Needs Shizuku",
                    style = MaterialTheme.typography.labelSmall,
                    color = CoralAccent.copy(alpha = 0.7f),
                )
            }
        }
        Spacer(Modifier.width(8.dp))

        when {
            busy -> CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = accent,
            )

            // Nothing to read back, so nothing to tick: these get a button that runs once.
            opt.isOneShot() -> TextButton(
                onClick = { onToggle(true) },
                enabled = enabled,
            ) {
                Text(
                    if (italian) "Esegui" else "Run",
                    style = MaterialTheme.typography.labelMedium,
                )
            }

            // A switch, not a checkbox. A checkbox in a list reads as "picked for a batch
            // action later" — which is what this screen used to do and no longer does. A
            // switch reads as the state of the thing right now, which is what it now is.
            else -> Switch(
                checked = applied,
                onCheckedChange = { onToggle(it) },
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = SurfaceDark,
                    checkedTrackColor = accent,
                    checkedBorderColor = accent,
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = SurfaceElevated,
                    uncheckedBorderColor = OutlineDim,
                ),
            )
        }
    }
}
