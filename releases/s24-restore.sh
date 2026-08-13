#!/bin/bash
# ═══════════════════════════════════════════════════════════════════
# S24 OPTIMIZER — Ripristino Default v2.0
# Device: SM-S921B (Galaxy S24) | One UI 7 / Android 16
# ═══════════════════════════════════════════════════════════════════

# Cerca l'eseguibile ADB
if command -v adb &> /dev/null; then
    ADB="adb"
elif [ -f "$HOME/Library/Android/sdk/platform-tools/adb" ]; then
    ADB="$HOME/Library/Android/sdk/platform-tools/adb"
elif [ -f "$HOME/Android/Sdk/platform-tools/adb" ]; then
    ADB="$HOME/Android/Sdk/platform-tools/adb"
else
    echo "Errore: ADB non trovato. Assicurati che sia installato e nel PATH."
    exit 1
fi

echo "[S24] Ripristino bloat Samsung..."
$ADB shell pm enable com.samsung.android.bixby.agent
$ADB shell pm enable com.samsung.android.bixby.wakeup
$ADB shell pm enable com.samsung.android.bixbyvision.framework
$ADB shell pm enable com.samsung.android.visionintelligence
$ADB shell pm enable com.samsung.android.game.gametools
$ADB shell pm enable com.samsung.android.game.gos
$ADB shell pm enable com.samsung.android.smartsuggestions
$ADB shell pm enable com.samsung.android.aware.service
$ADB shell pm enable com.samsung.android.bbc.bbcagent
$ADB shell pm enable com.samsung.android.app.reminder
$ADB shell pm enable com.samsung.android.app.routines
$ADB shell pm enable com.samsung.android.app.routineplus
$ADB shell pm enable com.samsung.android.liveeffectservice
$ADB shell pm enable com.samsung.android.oneconnect
$ADB shell pm enable com.samsung.android.service.stplatform
$ADB shell pm enable com.samsung.android.forest
$ADB shell pm enable com.samsung.accessory.budsunitemgr
$ADB shell pm enable com.samsung.android.rubin.app

echo "[S24] Ripristino Facebook + Microsoft..."
$ADB shell pm enable com.facebook.katana
$ADB shell pm enable com.facebook.orca
$ADB shell pm enable com.microsoft.emmx
$ADB shell pm enable com.microsoft.office.excel
$ADB shell pm enable com.microsoft.office.word
$ADB shell pm enable com.microsoft.rdc.androidx

echo "[S24] Ripristino impostazioni sistema..."
$ADB shell settings put global adaptive_battery_management_enabled 0
$ADB shell settings put global app_auto_restriction_enabled 0
$ADB shell settings put global battery_saver_mode 0
$ADB shell settings put global sem_enhanced_cpu_responsiveness 1
$ADB shell settings put global enhanced_processing 1
$ADB shell settings put global ble_scan_always_enabled 1
$ADB shell settings put system nearby_scanning_enabled 1
$ADB shell settings put system nearby_scanning_permission_allowed 1
$ADB shell settings put global wifi_power_save 0
$ADB shell settings put global wifi_wakeup_enabled 1
$ADB shell settings put system aod_mode 1
$ADB shell settings put global always_on_display_enabled 1
$ADB shell settings put system screen_off_timeout 60000
$ADB shell settings put secure doze_enabled 0
$ADB shell settings put system lift_to_wake 1
$ADB shell settings put system intelligent_sleep_mode 1
$ADB shell settings put system notification_light_pulse 1
$ADB shell settings put global window_animation_scale 1
$ADB shell settings put global transition_animation_scale 1
$ADB shell settings put global animator_duration_scale 1
$ADB shell settings put global ram_expand_size 8192
$ADB shell settings put global battery_protection_default_value 1
$ADB shell settings put global mcf_quick_share_visibility 1

echo "[S24] Ripristino background..."
$ADB shell appops set com.instagram.android RUN_ANY_IN_BACKGROUND allow
$ADB shell appops set net.tandem RUN_ANY_IN_BACKGROUND allow
$ADB shell appops set com.whatsapp RUN_ANY_IN_BACKGROUND allow

echo ""
echo "=== Ripristino completato. Riavvia il telefono. ==="
