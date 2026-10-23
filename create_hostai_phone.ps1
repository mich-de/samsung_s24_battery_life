# ==============================================================================
# CREATE HOSTAI PHONE (PowerShell) - Samsung Galaxy S24 (Exynos 2400)
# ==============================================================================

$adb = "C:\Users\mdeangelis\AppData\Local\Android\Sdk\platform-tools\adb.exe"
if (-not (Test-Path $adb)) {
    if (Get-Command adb -ErrorAction SilentlyContinue) {
        $adb = "adb"
    } else {
        Write-Error "ADB not found in Android SDK or PATH!"
        exit 1
    }
}

Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host "              HOSTAI PHONE SETUP - SAMSUNG GALAXY S24" -ForegroundColor Cyan
Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "[*] Waiting for ADB device..." -ForegroundColor Yellow
& $adb wait-for-device
$serial = (& $adb get-serialno).Trim()
Write-Host "[*] Connected device: $serial" -ForegroundColor Green
Write-Host ""

# 1. Directories
Write-Host "[*] Step 1/7: Preparing directories..." -ForegroundColor Yellow
& $adb shell "mkdir -p /storage/emulated/0/Download/models"
& $adb shell "mkdir -p /data/local/tmp"

# 2. System Settings
Write-Host "[*] Step 2/7: Configuring system & thermal parameters..." -ForegroundColor Yellow
$settings = @(
    "global stay_on_while_plugged_in 0",
    "global sem_low_power_mode 1",
    "global ram_expand_size 0",
    "global ble_scan_always_enabled 0",
    "system screen_off_timeout 30000",
    "secure adaptive_sleep 1",
    "secure voice_interaction_service ''",
    "secure assistant ''",
    "global protect_battery 3",
    "global battery_protection_threshold 80"
)
foreach ($s in $settings) { & $adb shell "settings put $s" }

# 3. Knox Purge
Write-Host "[*] Step 3/7: Purging Knox & Enterprise telemetry..." -ForegroundColor Yellow
$knoxDisable = @(
    "com.samsung.android.knox.kpecore", "com.samsung.android.knox.pushmanager",
    "com.samsung.android.knox.containercore", "com.samsung.knox.securefolder",
    "com.samsung.android.knox.mpos", "com.samsung.android.knox.analytics.uploader",
    "com.samsung.android.mdm", "com.samsung.android.messageguardsync",
    "com.sec.android.diagmonagent"
)
foreach ($pkg in $knoxDisable) {
    & $adb shell "pm disable-user --user 0 $pkg >/dev/null 2>&1"
    & $adb shell "am force-stop $pkg >/dev/null 2>&1"
}

$knoxRestrict = @(
    "com.samsung.android.knox.attestation", "com.knox.vpn.proxyhandler",
    "com.samsung.android.knox.sandbox", "com.samsung.android.knox.app.networkfilter",
    "com.samsung.android.knox.er", "com.sec.enterprise.knox.cloudmdm.smdms",
    "com.samsung.android.knox.zt.framework", "com.samsung.android.knox.kfbp",
    "com.samsung.android.knox.knnr", "com.samsung.klmsagent", "com.samsung.android.kgclient"
)
foreach ($pkg in $knoxRestrict) {
    & $adb shell "appops set $pkg RUN_IN_BACKGROUND deny >/dev/null 2>&1"
    & $adb shell "appops set $pkg RUN_ANY_IN_BACKGROUND deny >/dev/null 2>&1"
    & $adb shell "appops set $pkg WAKE_LOCK deny >/dev/null 2>&1"
    & $adb shell "appops set $pkg START_FOREGROUND deny >/dev/null 2>&1"
    & $adb shell "am force-stop $pkg >/dev/null 2>&1"
}

# 4. Samsung Studio, Gallery, and AI Bloat
Write-Host "[*] Step 4/7: Purging Samsung Studio, Gallery, and AI bloat..." -ForegroundColor Yellow
$samsungBloat = @(
    "com.sec.android.gallery3d", "com.sec.android.app.vepreload com.samsung.android.app.sketchbook",
    "com.sec.android.mimage.photoretouching", "com.samsung.app.newtrim",
    "com.samsung.storyservice", "com.samsung.mediasearch",
    "com.samsung.android.photoremasterservice", "com.samsung.android.motionphoto.app",
    "com.samsung.android.video", "com.samsung.android.aicore",
    "com.samsung.android.offline.languagemodel", "com.samsung.android.fmm",
    "com.samsung.android.mobileservice", "com.samsung.android.gru",
    "com.samsung.android.dqagent", "com.samsung.android.themestore",
    "com.samsung.android.themecenter", "com.samsung.android.mapsagent",
    "com.samsung.android.stickercenter", "com.samsung.android.aremoji",
    "com.samsung.android.app.camera.sticker.facearavatar.preload",
    "com.samsung.android.allshare.service.mediashare", "com.samsung.android.beaconmanager",
    "com.samsung.android.smartface", "com.samsung.android.smartface.overlay",
    "com.samsung.android.app.dressroom", "com.samsung.android.app.parentalcare",
    "com.sec.android.easyMover.Agent", "com.sec.android.app.billing",
    "com.samsung.android.da.daagent", "com.samsung.android.mcfserver",
    "com.samsung.android.mcf.autohotspot", "com.samsung.android.scs",
    "com.samsung.android.sm.devicesecurity", "com.samsung.android.service.peoplestripe",
    "com.samsung.android.app.clipboardedge", "com.samsung.android.app.taskedge",
    "com.samsung.android.ipsgeofence", "com.sec.android.sdhms",
    "com.sec.location.nsflp2", "com.samsung.android.sead", "com.sec.tasmanager",
    "com.samsung.android.messaging", "com.samsung.android.calendar",
    "com.samsung.android.scloud", "com.samsung.android.visual.cloudcore",
    "com.samsung.android.samsungpass", "com.samsung.android.samsungpassautofill",
    "com.samsung.android.mcfds", "com.samsung.android.app.notes",
    "com.sec.android.app.samsungapps", "com.sec.android.daemonapp"
)
foreach ($pkg in $samsungBloat) {
    & $adb shell "pm disable-user --user 0 $pkg >/dev/null 2>&1"
    & $adb shell "am force-stop $pkg >/dev/null 2>&1"
}

# 5. Google Bloat & Gemini Configuration
Write-Host "[*] Step 5/7: Configuring Google & Gemini jail..." -ForegroundColor Yellow
$googleBloat = @(
    "com.google.android.aicore", "com.google.android.overlay.aicoreconfig",
    "com.google.android.as", "com.google.android.mosey",
    "com.google.android.apps.carrier.carrierwifi"
)
foreach ($pkg in $googleBloat) {
    & $adb shell "pm disable-user --user 0 $pkg >/dev/null 2>&1"
    & $adb shell "am force-stop $pkg >/dev/null 2>&1"
}

& $adb shell "pm enable --user 0 com.google.android.googlequicksearchbox >/dev/null 2>&1"
& $adb shell "pm enable --user 0 com.google.android.apps.bard >/dev/null 2>&1"
& $adb shell "appops set com.google.android.googlequicksearchbox RUN_IN_BACKGROUND deny"
& $adb shell "appops set com.google.android.googlequicksearchbox RUN_ANY_IN_BACKGROUND deny"
& $adb shell "appops set com.google.android.googlequicksearchbox WAKE_LOCK deny"
& $adb shell "appops set com.google.android.apps.bard RUN_IN_BACKGROUND deny"
& $adb shell "appops set com.google.android.apps.bard RUN_ANY_IN_BACKGROUND deny"
& $adb shell "am force-stop com.google.android.googlequicksearchbox"

# 6. HostAI APK & Daemon
Write-Host "[*] Step 6/7: Deploying HostAI..." -ForegroundColor Yellow
$apkPath = "$PSScriptRoot\releases\hostai-s24-exynos.apk"
if (Test-Path $apkPath) {
    & $adb install -r $apkPath
}

$launcherPath = "$PSScriptRoot\releases\opennovalauncher-s24.apk"
if (Test-Path $launcherPath) {
    & $adb install -r $launcherPath
    & $adb shell "cmd package set-home-activity com.michde.opennovalauncher/.MainActivity >/dev/null 2>&1"
}

$daemonScript = @'
#!/system/bin/sh
while true; do
  find /storage/emulated/0/Android/data/com.google.ai.edge.gallery/files/ -type f -name "*.litertlm" -exec cp -u {} /storage/emulated/0/Download/models/ \; 2>/dev/null
  sleep 15
done
'@
& $adb shell "cat << 'EOF' > /data/local/tmp/edge_model_sync.sh`n$daemonScript`nEOF"
& $adb shell "chmod 755 /data/local/tmp/edge_model_sync.sh"
& $adb shell "nohup /data/local/tmp/edge_model_sync.sh >/dev/null 2>&1 &"

& $adb shell "appops set com.wannaphong.hostai MANAGE_EXTERNAL_STORAGE allow"
& $adb shell "cmd deviceidle whitelist +com.wannaphong.hostai"
& $adb shell "am start -n com.wannaphong.hostai/.MainActivity"

# 7. Compaction & Trim
Write-Host "[*] Step 7/7: Executing memory compaction and UFS trim..." -ForegroundColor Yellow
& $adb shell "am kill-all"
& $adb shell "cmd activity compact system"
& $adb shell "pm trim-caches 999999999999"
& $adb shell "sm fstrim"
& $adb shell "sm idle-maint run"

Write-Host ""
Write-Host "==============================================================================" -ForegroundColor Green
Write-Host "[SUCCESS] Galaxy S24 successfully configured as HostAI Appliance!" -ForegroundColor Green
Write-Host "==============================================================================" -ForegroundColor Green
Write-Host ""
Write-Host "[*] Live Memory Status:" -ForegroundColor Cyan
& $adb shell "cat /proc/meminfo | head -n 4"
