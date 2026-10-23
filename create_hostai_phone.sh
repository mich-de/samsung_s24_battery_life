#!/usr/bin/env bash
# ==============================================================================
# CREATE HOSTAI PHONE (Bash) - Samsung Galaxy S24 (Exynos 2400)
# ==============================================================================

set -euo pipefail

echo "=============================================================================="
echo "              HOSTAI PHONE SETUP - SAMSUNG GALAXY S24"
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
SERIAL=$($ADB_BIN get-serialno)
echo "[*] Connected device: $SERIAL"

# 1. Dirs
echo "[*] Step 1/7: Preparing directories..."
$ADB_BIN shell "mkdir -p /storage/emulated/0/Download/models"
$ADB_BIN shell "mkdir -p /data/local/tmp"

# 2. Settings
echo "[*] Step 2/7: Configuring system parameters..."
$ADB_BIN shell "settings put global stay_on_while_plugged_in 0"
$ADB_BIN shell "settings put global sem_low_power_mode 1"
$ADB_BIN shell "settings put global ram_expand_size 0"
$ADB_BIN shell "settings put global ble_scan_always_enabled 0"
$ADB_BIN shell "settings put system screen_off_timeout 30000"
$ADB_BIN shell "settings put secure adaptive_sleep 1"
$ADB_BIN shell "settings put secure voice_interaction_service ''"
$ADB_BIN shell "settings put secure assistant ''"
$ADB_BIN shell "settings put global protect_battery 3"
$ADB_BIN shell "settings put global battery_protection_threshold 80"

# 3. Knox
echo "[*] Step 3/7: Purging Knox telemetry..."
KNOX_DISABLE=(
    "com.samsung.android.knox.kpecore" "com.samsung.android.knox.pushmanager"
    "com.samsung.android.knox.containercore" "com.samsung.knox.securefolder"
    "com.samsung.android.knox.mpos" "com.samsung.android.knox.analytics.uploader"
    "com.samsung.android.mdm" "com.samsung.android.messageguardsync"
    "com.sec.android.diagmonagent"
)
for pkg in "${KNOX_DISABLE[@]}"; do
    $ADB_BIN shell "pm disable-user --user 0 $pkg >/dev/null 2>&1 || true"
    $ADB_BIN shell "am force-stop $pkg >/dev/null 2>&1 || true"
done

KNOX_RESTRICT=(
    "com.samsung.android.knox.attestation" "com.knox.vpn.proxyhandler"
    "com.samsung.android.knox.sandbox" "com.samsung.android.knox.app.networkfilter"
    "com.samsung.android.knox.er" "com.sec.enterprise.knox.cloudmdm.smdms"
    "com.samsung.android.knox.zt.framework" "com.samsung.android.knox.kfbp"
    "com.samsung.android.knox.knnr" "com.samsung.klmsagent" "com.samsung.android.kgclient"
)
for pkg in "${KNOX_RESTRICT[@]}"; do
    $ADB_BIN shell "appops set $pkg RUN_IN_BACKGROUND deny >/dev/null 2>&1 || true"
    $ADB_BIN shell "appops set $pkg RUN_ANY_IN_BACKGROUND deny >/dev/null 2>&1 || true"
    $ADB_BIN shell "appops set $pkg WAKE_LOCK deny >/dev/null 2>&1 || true"
    $ADB_BIN shell "appops set $pkg START_FOREGROUND deny >/dev/null 2>&1 || true"
    $ADB_BIN shell "am force-stop $pkg >/dev/null 2>&1 || true"
done

# 4. Samsung Bloat
echo "[*] Step 4/7: Purging Samsung Studio, Gallery, and AI bloat..."
SAMSUNG_BLOAT=(
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
for pkg in "${SAMSUNG_BLOAT[@]}"; do
    $ADB_BIN shell "pm disable-user --user 0 $pkg >/dev/null 2>&1 || true"
    $ADB_BIN shell "am force-stop $pkg >/dev/null 2>&1 || true"
done

# 5. Google Bloat
echo "[*] Step 5/7: Configuring Google & Gemini jail..."
GOOGLE_BLOAT=(
    "com.google.android.aicore" "com.google.android.overlay.aicoreconfig"
    "com.google.android.as" "com.google.android.mosey"
    "com.google.android.apps.carrier.carrierwifi"
)
for pkg in "${GOOGLE_BLOAT[@]}"; do
    $ADB_BIN shell "pm disable-user --user 0 $pkg >/dev/null 2>&1 || true"
    $ADB_BIN shell "am force-stop $pkg >/dev/null 2>&1 || true"
done

$ADB_BIN shell "pm enable --user 0 com.google.android.googlequicksearchbox >/dev/null 2>&1 || true"
$ADB_BIN shell "pm enable --user 0 com.google.android.apps.bard >/dev/null 2>&1 || true"
$ADB_BIN shell "appops set com.google.android.googlequicksearchbox RUN_IN_BACKGROUND deny || true"
$ADB_BIN shell "appops set com.google.android.googlequicksearchbox RUN_ANY_IN_BACKGROUND deny || true"
$ADB_BIN shell "appops set com.google.android.googlequicksearchbox WAKE_LOCK deny || true"
$ADB_BIN shell "appops set com.google.android.apps.bard RUN_IN_BACKGROUND deny || true"
$ADB_BIN shell "appops set com.google.android.apps.bard RUN_ANY_IN_BACKGROUND deny || true"
$ADB_BIN shell "am force-stop com.google.android.googlequicksearchbox || true"

# 6. HostAI APK
echo "[*] Step 6/7: Deploying HostAI..."
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [ -f "$DIR/releases/hostai-s24-exynos.apk" ]; then
    $ADB_BIN install -r "$DIR/releases/hostai-s24-exynos.apk"
fi

if [ -f "$DIR/releases/opennovalauncher-s24.apk" ]; then
    $ADB_BIN install -r "$DIR/releases/opennovalauncher-s24.apk"
    $ADB_BIN shell "cmd package set-home-activity com.michde.opennovalauncher/.MainActivity >/dev/null 2>&1 || true"
fi

$ADB_BIN shell "cat << 'EOF' > /data/local/tmp/edge_model_sync.sh
#!/system/bin/sh
while true; do
  find /storage/emulated/0/Android/data/com.google.ai.edge.gallery/files/ -type f -name '*.litertlm' -exec cp -u {} /storage/emulated/0/Download/models/ \; 2>/dev/null
  sleep 15
done
EOF"
$ADB_BIN shell "chmod 755 /data/local/tmp/edge_model_sync.sh"
$ADB_BIN shell "nohup /data/local/tmp/edge_model_sync.sh >/dev/null 2>&1 &"

$ADB_BIN shell "appops set com.wannaphong.hostai MANAGE_EXTERNAL_STORAGE allow"
$ADB_BIN shell "cmd deviceidle whitelist +com.wannaphong.hostai"
$ADB_BIN shell "am start -n com.wannaphong.hostai/.MainActivity"

# 7. Compaction & Trim
echo "[*] Step 7/7: Executing memory compaction..."
$ADB_BIN shell "am kill-all"
$ADB_BIN shell "cmd activity compact system"
$ADB_BIN shell "pm trim-caches 999999999999"
$ADB_BIN shell "sm fstrim"
$ADB_BIN shell "sm idle-maint run"

echo "=============================================================================="
echo "[SUCCESS] Galaxy S24 successfully configured as HostAI Appliance!"
echo "=============================================================================="
$ADB_BIN shell "cat /proc/meminfo | head -n 4"
