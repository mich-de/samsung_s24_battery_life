package com.s24optimizer.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

data class OptimizationState(
    val id: String,
    var applied: Boolean = false,
)

data class Profile(
    val name: String,
    val timestamp: Long = System.currentTimeMillis(),
    val states: List<OptimizationState>,
)

class ProfileManager(private val context: Context) {

    private val gson = Gson()
    private val file: File = File(context.filesDir, "profiles.json")

    fun save(name: String, appliedIds: Set<String>) {
        val profile = Profile(name = name, states = appliedIds.map { OptimizationState(it, true) })
        val profiles = loadAll().toMutableList()
        profiles.removeAll { it.name == name }
        profiles.add(profile)
        file.writeText(gson.toJson(profiles))
    }

    fun loadAll(): List<Profile> {
        if (!file.exists()) return emptyList()
        val type = object : TypeToken<List<Profile>>() {}.type
        return gson.fromJson(file.readText(), type) ?: emptyList()
    }

    fun delete(name: String) {
        val profiles = loadAll().toMutableList()
        profiles.removeAll { it.name == name }
        file.writeText(gson.toJson(profiles))
    }
}
