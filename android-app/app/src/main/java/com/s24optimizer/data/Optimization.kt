package com.s24optimizer.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * One toggleable change to the device.
 *
 * The invariant the whole UI rests on: **ticked means the optimization is active on the
 * phone right now**, unticked means it is not. So a title always names the change
 * ("Disable Smart Stay"), never the underlying feature ("Smart Stay") — otherwise ticking
 * a box called "Smart Stay OFF" reads as a double negative and nobody can say what state
 * the phone is in.
 *
 * That invariant is only worth anything if [checkCommands] actually observes the key the
 * system reads. Several entries used to write a settings key that exists nowhere on the
 * device: the write succeeded, the check read back what it had just written, and the app
 * reported "applied" while nothing had changed.
 *
 * How a key gets proven, against a live SM-S921B on One UI 8.5: `dumpsys settings` prints
 * every row with a `default:` value and an edit history naming the writer. A key the OS
 * ships has a default and a first `insert` from `android` or a Samsung package. A key that
 * only ever appears with a first `insert` from `com.android.shell` was created by this app
 * writing it — nothing reads it. Entries still resting on such a key carry [verified] =
 * false rather than a confident tick.
 */
data class Optimization(
    val id: String,
    val titleEn: String,
    val titleIt: String,
    val descEn: String,
    val descIt: String,
    val category: Category,
    val applyCommands: List<String>,
    val revertCommands: List<String>,
    val checkCommands: List<String> = emptyList(),
    val icon: ImageVector? = null,
    /**
     * Sub-heading this entry sits under inside its category. Empty means no sub-heading.
     *
     * The heading is a property of the entry, not of its position: the screen collects
     * every entry sharing a group under one header, in the order the groups first appear.
     * The list this replaced keyed headings off list indices, so inserting or removing an
     * entry silently filed its neighbours under the wrong heading.
     */
    val groupEn: String = "",
    val groupIt: String = "",
    /**
     * False when the key this entry writes could not be shown to be one the system reads.
     * The change may still work — it just cannot be told apart from a no-op, so the tick
     * must not be presented as proof that anything happened.
     */
    val verified: Boolean = true,
    /**
     * Set when the change has no shell equivalent at all, so [applyCommands],
     * [revertCommands] and [checkCommands] are empty and [OptimizationRunner] does the work
     * in this process instead.
     */
    val local: LocalAction? = null,
) {
    /** Changes the shell cannot make, carried out by the app itself. */
    enum class LocalAction { MASTER_SYNC_OFF }

    /**
     * A single shell line printing `1` only when every check passes.
     *
     * Lives here rather than in the UI because both the initial state load and the
     * re-read after a toggle must ask the device the same question — if they diverged,
     * a freshly ticked box could disagree with the next refresh.
     */
    fun checkExpression(): String? {
        if (checkCommands.isEmpty()) return null
        val conditions = checkCommands.joinToString(" && ") { "[ \"\$($it)\" = \"1\" ]" }
        return "if $conditions; then echo 1; else echo 0; fi"
    }

    enum class Category(val labelEn: String, val labelIt: String) {
        // Every entry here is named after the package it turns off, which on its own would
        // break the ticked-means-active rule above. Rather than prefix fifty rows with the
        // same verb — and lose the app name as the thing you scan for — the verb sits in the
        // category, so a ticked "Bixby Vision" under "Disable Bloat" reads only one way.
        BLOAT("Disable Bloat", "Disattiva Bloat"),
        SYSTEM("System", "Sistema"),
        REFRESH_RATE("Refresh Rate", "Frequenza"),
        PER_APP_RR("Per-App RR", "RR per App"),
        ADVANCED("Advanced", "Avanzate"),
        MAINTENANCE("Maintenance", "Manutenzione");

        fun icon(): ImageVector = when (this) {
            BLOAT -> Icons.Default.PhoneAndroid
            SYSTEM -> Icons.Default.Settings
            REFRESH_RATE -> Icons.Default.Refresh
            PER_APP_RR -> Icons.Default.Apps
            ADVANCED -> Icons.Default.Tune
            MAINTENANCE -> Icons.Default.Build
        }
    }
}
