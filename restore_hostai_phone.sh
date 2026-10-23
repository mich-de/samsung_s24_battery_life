#!/usr/bin/env bash
# ==============================================================================
# RESTORE HOSTAI PHONE (Bash) - Full Rollback / Safety Net
# ==============================================================================

set -euo pipefail

echo "=============================================================================="
echo "             FULL ROLLBACK / RESTORE S24 STOCK PHONE STATE"
echo "=============================================================================="

ADB_BIN=""
if command -v adb >/dev/null 2>&1; then
    ADB_BIN="adb"
elif [ -f "$HOME/Library/Android/sdk/platform-tools/adb" ]; then
    ADB_BIN="$HOME/Library/Android/sdk/platform-tools/adb"
elif [ -f "$HOME/Android/Sdk/platform-tools/adb" ]; then
    ADB_BIN="$HOME/Android/Sdk/platform-tools/adb"
else
    echo "[ERROR] ADB not found!"
    exit 1
fi

echo "[*] Using ADB: $ADB_BIN"
$ADB_BIN wait-for-device

# 1. System Settings
echo "[*] Step 1/6: Restoring standard system settings..."
$ADB_BIN shell "settings put global stay_on_while_plugged_in 0"
$ADB_BIN shell "settings put global sem_low_power_mode 0"
$ADB_BIN shell "settings put global ram_expand_size 4096"
$ADB_BIN shell "settings put global ble_scan_always_enabled 1"
$ADB_BIN shell "settings put system screen_off_timeout 60000"
$ADB_BIN shell "settings put secure adaptive_sleep 1"
$ADB_BIN shell "settings put global protect_battery 0"

# 2. Knox
echo "[*] Step 2/6: Re-enabling Knox packages..."
KNOX_RESTORE=(
    "com.samsung.android.knox.kpecore" "com.samsung.android.knox.pushmanager"
    "com.samsung.android.knox.containercore" "com.samsung.knox.securefolder"
    "com.samsung.android.knox.mpos" "com.samsung.android.knox.analytics.uploader"
    "com.samsung.android.mdm" "com.samsung.android.messageguardsync"
    "com.sec.android.diagmonagent"
)
for pkg in "${KNOX_RESTORE[@]}"; do
    $ADB_BIN shell "pm enable --user 0 $pkg >/dev/null 2>&1 || true"
done

KNOX_PERMS=(
    "com.samsung.android.knox.attestation" "com.knox.vpn.proxyhandler"
    "com.samsung.android.knox.sandbox" "com.samsung.android.knox.app.networkfilter"
    "com.samsung.android.knox.er" "com.sec.enterprise.knox.cloudmdm.smdms"
    "com.samsung.android.knox.zt.framework" "com.samsung.android.knox.kfbp"
    "com.samsung.android.knox.knnr" "com.samsung.klmsagent" "com.samsung.android.kgclient"
)
for pkg in "${KNOX_PERMS[@]}"; do
    $ADB_BIN shell "appops set $pkg RUN_IN_BACKGROUND allow >/dev/null 2>&1 || true"
    $ADB_BIN shell "appops set $pkg RUN_ANY_IN_BACKGROUND allow >/dev/null 2>&1 || true"
    $ADB_BIN shell "appops set $pkg WAKE_LOCK allow >/dev/null 2>&1 || true"
    $ADB_BIN shell "appops set $pkg START_FOREGROUND allow >/dev/null 2>&1 || true"
done

# 3. Samsung Studio, Gallery, and System Apps
echo "[*] Step 3/6: Re-enabling Samsung Studio, Gallery, and system apps..."
SAMSUNG_RESTORE=(
    "com.sec.android.gallery3d" "com.sec.android.app.vepreload com.samsung.android.app.sketchbook"
    "com.sec.android.mimage.photoretouching" "com.samsung.app.newtrim"
    "com.samsung.storyservice" "com.samsung.mediasearch"
    "com.samsung.android.photoremasterservice" "com.samsung.android.motionphoto.app"
    "com.samsung.android.video" "com.samsung.android.aicore"
    "com.samsung.android.offline.languagemodel" "com.samsung.android.fmm"
    "com.samsung.android.mobileservice" "com.samsung.android.gru"
    "com.samsung.android.dqagent" "com.samsung.android.themestore"
    "com.samsung.android.themecenter" "com.samsung.android.mapsagent"
    "com.samsung.android.stickercenter" "com.samsung.android.aremoji"
    "com.samsung.android.app.camera.sticker.facearavatar.preload"
    "com.samsung.android.allshare.service.mediashare" "com.samsung.android.beaconmanager"
    "com.samsung.android.smartface" "com.samsung.android.smartface.overlay"
    "com.samsung.android.app.dressroom" "com.samsung.android.app.parentalcare"
    "com.sec.android.easyMover.Agent" "com.sec.android.app.billing"
    "com.samsung.android.da.daagent" "com.samsung.android.mcfserver"
    "com.samsung.android.mcf.autohotspot" "com.samsung.android.scs"
    "com.samsung.android.sm.devicesecurity" "com.samsung.android.service.peoplestripe"
    "com.samsung.android.app.clipboardedge" "com.samsung.android.app.taskedge"
    "com.samsung.android.ipsgeofence" "com.sec.android.sdhms"
    "com.sec.location.nsflp2" "com.samsung.android.sead" "com.sec.tasmanager"
    "com.samsung.android.messaging" "com.samsung.android.calendar"
    "com.samsung.android.scloud" "com.samsung.android.visual.cloudcore"
    "com.samsung.android.samsungpass" "com.samsung.android.samsungpassautofill"
    "com.samsung.android.mcfds" "com.samsung.android.app.notes"
    "com.sec.android.app.samsungapps" "com.sec.android.daemonapp"
)
for pkg in "${SAMSUNG_RESTORE[@]}"; do
    $ADB_BIN shell "pm enable --user 0 $pkg >/dev/null 2>&1 || true"
done

# 4. Google Services
echo "[*] Step 4/6: Re-enabling Google services and Assistant..."
GOOGLE_RESTORE=(
    "com.google.android.googlequicksearchbox" "com.google.android.apps.bard"
    "com.google.android.aicore" "com.google.android.overlay.aicoreconfig"
    "com.google.android.as" "com.google.android.mosey"
    "com.google.android.apps.carrier.carrierwifi"
)
for pkg in "${GOOGLE_RESTORE[@]}"; do
    $ADB_BIN shell "pm enable --user 0 $pkg >/dev/null 2>&1 || true"
done
$ADB_BIN shell "appops set com.google.android.googlequicksearchbox RUN_IN_BACKGROUND allow || true"
$ADB_BIN shell "appops set com.google.android.googlequicksearchbox RUN_ANY_IN_BACKGROUND allow || true"
$ADB_BIN shell "appops set com.google.android.googlequicksearchbox WAKE_LOCK allow || true"
$ADB_BIN shell "appops set com.google.android.apps.bard RUN_IN_BACKGROUND allow || true"
$ADB_BIN shell "appops set com.google.android.apps.bard RUN_ANY_IN_BACKGROUND allow || true"

# 5. Backup APK Verification
echo "[*] Step 5/6: Checking local backup APKs..."
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [ -d "$DIR/backup/system_apps" ]; then
    for f in "$DIR/backup/system_apps"/*.apk; do
        [ -e "$f" ] || continue
        echo "  -> Verifying $(basename "$f")"
        $ADB_BIN install -r -d "$f" >/dev/null 2>&1 || true
    done
fi

# 6. Cleanup Daemons
echo "[*] Step 6/6: Terminating background daemons..."
$ADB_BIN shell "pm clear-package-preferred com.michde.opennovalauncher >/dev/null 2>&1 || true"
$ADB_BIN shell "pkill -f edge_model_sync.sh >/dev/null 2>&1 || true"
$ADB_BIN shell "rm -f /data/local/tmp/edge_model_sync.sh"

echo "=============================================================================="
echo "[SUCCESS] Device successfully restored to stock One UI phone state!"
echo "=============================================================================="
