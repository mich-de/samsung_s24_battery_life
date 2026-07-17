package com.s24optimizer.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adb
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Security
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
) {
    enum class Category(val labelEn: String, val labelIt: String) {
        BLOAT_SAMSUNG("Samsung Bloat", "Bloat Samsung"),
        BLOAT_GOOGLE("Google Bloat", "Bloat Google"),
        BLOAT_FACEBOOK("Facebook/Meta", "Facebook/Meta"),
        BLOAT_MICROSOFT("Microsoft Bloat", "Bloat Microsoft"),
        KNOX_RESET("Knox Matrix Reset", "Reset Knox Matrix"),
        GOOGLE_PLAY_RESET("Google Play Reset", "Reset Google Play"),
        BACKGROUND("Background Restrict", "Limitazioni Background"),
        SYSTEM("System Settings", "Impostazioni Sistema");

        fun icon(): ImageVector = when (this) {
            BLOAT_SAMSUNG -> Icons.Default.PhoneAndroid
            BLOAT_GOOGLE -> Icons.Default.Apps
            BLOAT_FACEBOOK -> Icons.Default.Face
            BLOAT_MICROSOFT -> Icons.Default.Backup
            KNOX_RESET -> Icons.Default.Security
            GOOGLE_PLAY_RESET -> Icons.Default.Build
            BACKGROUND -> Icons.Default.Adb
            SYSTEM -> Icons.Default.Settings
        }
    }
}
