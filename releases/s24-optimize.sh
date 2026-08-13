#!/bin/bash
# ═══════════════════════════════════════════════════════════════════
# S24 BATTERY OPTIMIZER — Script Definitivo v2.0
# Device: SM-S921B (Galaxy S24) | One UI 7 / Android 16
# ═══════════════════════════════════════════════════════════════════
# Cosa preserva: 120Hz · 5G · Google Maps · Gmail · Hey Google + Gemini
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

echo "[$(date '+%Y-%m-%d %H:%M:%S')] === S24 OPTIMIZER v2 ==="
echo ""

# ── 1. BLOAT SAMSUNG — 18 pacchetti ────────────────────────────────
echo "[1/6] Disabilitazione bloat Samsung..."

# Bixby Voice
$ADB shell pm disable-user --user 0 com.samsung.android.bixby.agent

# Bixby Wakeup
$ADB shell pm disable-user --user 0 com.samsung.android.bixby.wakeup

# Bixby Vision
$ADB shell pm disable-user --user 0 com.samsung.android.bixbyvision.framework

# Vision Intelligence
$ADB shell pm disable-user --user 0 com.samsung.android.visionintelligence

# Game Tools
$ADB shell pm disable-user --user 0 com.samsung.android.game.gametools

# Game Optimizing Service
$ADB shell pm disable-user --user 0 com.samsung.android.game.gos

# Smart Suggestions
$ADB shell pm disable-user --user 0 com.samsung.android.smartsuggestions

# Aware Service
$ADB shell pm disable-user --user 0 com.samsung.android.aware.service

# BBC Agent
$ADB shell pm disable-user --user 0 com.samsung.android.bbc.bbcagent

# Reminder
$ADB shell pm disable-user --user 0 com.samsung.android.app.reminder

# Routines
$ADB shell pm disable-user --user 0 com.samsung.android.app.routines

# Routine Plus
$ADB shell pm disable-user --user 0 com.samsung.android.app.routineplus

# Live Effect Service
$ADB shell pm disable-user --user 0 com.samsung.android.liveeffectservice

# OneConnect
$ADB shell pm disable-user --user 0 com.samsung.android.oneconnect

# STPlatform
$ADB shell pm disable-user --user 0 com.samsung.android.service.stplatform

# Forest
$ADB shell pm disable-user --user 0 com.samsung.android.forest

# Buds Manager
$ADB shell pm disable-user --user 0 com.samsung.accessory.budsunitemgr

# Rubin App
$ADB shell pm disable-user --user 0 com.samsung.android.rubin.app


# ── 2. BLOAT FACEBOOK + MICROSOFT ───────────────────────────────────
echo "[2/6] Disabilitazione bloat Facebook + Microsoft..."

# Facebook
$ADB shell pm disable-user --user 0 com.facebook.katana

# Messenger
$ADB shell pm disable-user --user 0 com.facebook.orca

# Microsoft Edge
$ADB shell pm disable-user --user 0 com.microsoft.emmx

# Excel
$ADB shell pm disable-user --user 0 com.microsoft.office.excel

# Word
$ADB shell pm disable-user --user 0 com.microsoft.office.word

# Remote Desktop
$ADB shell pm disable-user --user 0 com.microsoft.rdc.androidx


# ── 3. KNOX RESET — FIX CICLO DI DRAIN ─────────────────────────────
echo "[3/6] Reset Knox Matrix (fix noto drain Aprile 2026)..."

# KPECore
$ADB shell pm clear com.samsung.android.knox.kpecore

# Attestation
$ADB shell pm clear com.samsung.android.knox.attestation

# Push Manager
$ADB shell pm clear com.samsung.android.knox.pushmanager

# Container Core
$ADB shell pm clear com.samsung.android.knox.containercore

# Analytics Uploader
$ADB shell pm clear com.samsung.android.knox.analytics.uploader


# ── 4. IMPOSTAZIONI SISTEMA ─────────────────────────────────────────
echo "[4/6] Impostazioni sistema (batteria, radio, schermo)..."

# Adaptive Battery
$ADB shell settings put global adaptive_battery_management_enabled 1

# App Auto Restriction
$ADB shell settings put global app_auto_restriction_enabled 1

# Battery Saver
$ADB shell settings put global battery_saver_mode 1

# Enhanced CPU Responsiveness
$ADB shell settings put global sem_enhanced_cpu_responsiveness 0

# Enhanced Processing
$ADB shell settings put global enhanced_processing 0

# BLE Scan Always
$ADB shell settings put global ble_scan_always_enabled 0

# Nearby Scanning
$ADB shell settings put system nearby_scanning_enabled 0

# Nearby Permission
$ADB shell settings put system nearby_scanning_permission_allowed 0

# WiFi Power Save
$ADB shell settings put global wifi_power_save 1

# WiFi Wakeup
$ADB shell settings put global wifi_wakeup_enabled 0

# AOD Mode
$ADB shell settings put system aod_mode 0

# Always On Display
$ADB shell settings put global always_on_display_enabled 0

# Screen Off Timeout
$ADB shell settings put system screen_off_timeout 30000

# Doze Enabled
$ADB shell settings put secure doze_enabled 1

# Lift to Wake
$ADB shell settings put system lift_to_wake 0

# Intelligent Sleep Mode
$ADB shell settings put system intelligent_sleep_mode 0

# Notification Light Pulse
$ADB shell settings put system notification_light_pulse 0

# Animazioni Scale
$ADB shell settings put global window_animation_scale 0.5
$ADB shell settings put global transition_animation_scale 0.5
$ADB shell settings put global animator_duration_scale 0.5

# RAM Plus
$ADB shell settings put global ram_expand_size 2048

# Protezione Batteria
$ADB shell settings put global battery_protection_default_value 3
$ADB shell settings put global battery_protection_threshold 80

# Quick Share MCF
$ADB shell settings put global mcf_quick_share_visibility 0


# ── 5. RESTRIZIONE BACKGROUND ───────────────────────────────────────
echo "[5/6] Restrizione background app pesanti..."

# Instagram
$ADB shell appops set com.instagram.android RUN_ANY_IN_BACKGROUND deny

# Tandem
$ADB shell appops set net.tandem RUN_ANY_IN_BACKGROUND deny

# WhatsApp
$ADB shell appops set com.whatsapp RUN_ANY_IN_BACKGROUND deny


# ── 6. RUBIN.APP — REVOCA PERMESSO ATTIVITÀ ────────────────────────
echo "[6/6] Revoca permesso attività fisica rubin.app..."
$ADB shell pm revoke com.samsung.android.rubin.app android.permission.ACTIVITY_RECOGNITION

echo ""
echo "============================================================"
echo "  COMPLETATO! Riavvia il telefono per applicare tutto."
echo "============================================================"
echo ""
echo "  Per annullare tutte le modifiche:"
echo "    ./s24-restore.sh"
echo ""
echo "  Attenzione: dopo il riavvio, il telefono potrebbe impiegare"
echo "  1-2 cicli di carica per ricalibrare la stima della batteria."
echo "============================================================"
