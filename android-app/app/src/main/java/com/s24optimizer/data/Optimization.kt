package com.s24optimizer.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

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
    val group: String = "",
) {
    enum class Category(val labelEn: String, val labelIt: String) {
        BLOAT("Bloat", "Bloat"),
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
