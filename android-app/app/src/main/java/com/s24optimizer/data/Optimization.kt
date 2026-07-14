package com.s24optimizer.data

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
        SYSTEM("System Settings", "Impostazioni Sistema"),
    }
}
