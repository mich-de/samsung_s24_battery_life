package com.s24optimizer.data

import android.content.Context

/**
 * Remembers which optimizations the user has deliberately applied.
 *
 * Needed to tell "never enabled" apart from "enabled, then silently undone". One UI 8.5
 * re-enabled several packages that had been disabled here; without this record such a
 * regression is indistinguishable from an option the user never touched.
 */
object AppliedHistory {

    private const val PREFS = "applied_history"
    private const val KEY = "ids"

    fun markApplied(context: Context, id: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY, all(context) + id).apply()
    }

    fun markReverted(context: Context, id: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY, all(context) - id).apply()
    }

    fun all(context: Context): Set<String> =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getStringSet(KEY, emptySet())?.toSet() ?: emptySet()

    /**
     * Optimizations that were applied but whose check no longer passes — i.e. something
     * outside the app (usually an OS update) undid them.
     */
    fun regressions(context: Context, appliedStates: Map<String, Boolean>): List<Optimization> {
        val expected = all(context)
        if (expected.isEmpty()) return emptyList()
        return Optimizations.getAll().filter { opt ->
            opt.id in expected &&
                opt.checkCommands.isNotEmpty() &&
                appliedStates[opt.id] == false
        }
    }
}
