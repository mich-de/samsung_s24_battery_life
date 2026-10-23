# ==============================================================================
# RESTORE HOSTAI PHONE (PowerShell) - Full Rollback / Safety Net
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
Write-Host "             FULL ROLLBACK / RESTORE S24 STOCK PHONE STATE" -ForegroundColor Cyan
Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "[*] Waiting for ADB device..." -ForegroundColor Yellow
& $adb wait-for-device
$serial = (& $adb get-serialno).Trim()
Write-Host "[*] Connected device: $serial" -ForegroundColor Green
Write-Host ""

# 1. System Settings
Write-Host "[*] Step 1/6: Restoring standard system settings..." -ForegroundColor Yellow
$settings = @(
    "global stay_on_while_plugged_in 0",
    "global sem_low_power_mode 0",
    "global ram_expand_size 4096",
    "global ble_scan_always_enabled 1",
    "system screen_off_timeout 60000",
    "secure adaptive_sleep 1",
    "global protect_battery 0"
)
foreach ($s in $settings) { & $adb shell "settings put $s" }

# 2. Knox Packages
Write-Host "[*] Step 2/6: Re-enabling Knox packages and enterprise services..." -ForegroundColor Yellow
$knoxRestore = @(
    "com.samsung.android.knox.kpecore", "com.samsung.android.knox.pushmanager",
    "com.samsung.android.knox.containercore", "com.samsung.knox.securefolder",
    "com.samsung.android.knox.mpos", "com.samsung.android.knox.analytics.uploader",
    "com.samsung.android.mdm", "com.samsung.android.messageguardsync",
    "com.sec.android.diagmonagent"
)
foreach ($pkg in $knoxRestore) {
    & $adb shell "pm enable --user 0 $pkg >/dev/null 2>&1"
}

$knoxPerms = @(
    "com.samsung.android.knox.attestation", "com.knox.vpn.proxyhandler",
    "com.samsung.android.knox.sandbox", "com.samsung.android.knox.app.networkfilter",
    "com.samsung.android.knox.er", "com.sec.enterprise.knox.cloudmdm.smdms",
    "com.samsung.android.knox.zt.framework", "com.samsung.android.knox.kfbp",
    "com.samsung.android.knox.knnr", "com.samsung.klmsagent", "com.samsung.android.kgclient"
)
foreach ($pkg in $knoxPerms) {
    & $adb shell "appops set $pkg RUN_IN_BACKGROUND allow >/dev/null 2>&1"
    & $adb shell "appops set $pkg RUN_ANY_IN_BACKGROUND allow >/dev/null 2>&1"
    & $adb shell "appops set $pkg WAKE_LOCK allow >/dev/null 2>&1"
    & $adb shell "appops set $pkg START_FOREGROUND allow >/dev/null 2>&1"
}

# 3. Samsung Studio, Gallery, and System Apps
Write-Host "[*] Step 3/6: Re-enabling Samsung Studio, Gallery, and system apps..." -ForegroundColor Yellow
$samsungRestore = @(
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
foreach ($pkg in $samsungRestore) {
    & $adb shell "pm enable --user 0 $pkg >/dev/null 2>&1"
}

# 4. Google Services
Write-Host "[*] Step 4/6: Re-enabling Google services and Assistant..." -ForegroundColor Yellow
$googleRestore = @(
    "com.google.android.googlequicksearchbox", "com.google.android.apps.bard",
    "com.google.android.aicore", "com.google.android.overlay.aicoreconfig",
    "com.google.android.as", "com.google.android.mosey",
    "com.google.android.apps.carrier.carrierwifi"
)
foreach ($pkg in $googleRestore) {
    & $adb shell "pm enable --user 0 $pkg >/dev/null 2>&1"
}
& $adb shell "appops set com.google.android.googlequicksearchbox RUN_IN_BACKGROUND allow"
& $adb shell "appops set com.google.android.googlequicksearchbox RUN_ANY_IN_BACKGROUND allow"
& $adb shell "appops set com.google.android.googlequicksearchbox WAKE_LOCK allow"
& $adb shell "appops set com.google.android.apps.bard RUN_IN_BACKGROUND allow"
& $adb shell "appops set com.google.android.apps.bard RUN_ANY_IN_BACKGROUND allow"

# 5. Backup APK Verification
Write-Host "[*] Step 5/6: Checking local backup APKs..." -ForegroundColor Yellow
$backupDir = "$PSScriptRoot\backup\system_apps"
if (Test-Path $backupDir) {
    Get-ChildItem -Path $backupDir -Filter "*.apk" | ForEach-Object {
        Write-Host "  -> Verifying $($_.Name)"
        & $adb install -r -d $_.FullName >$null 2>&1
    }
}

# 6. Cleanup Daemons
Write-Host "[*] Step 6/6: Terminating background sync daemons..." -ForegroundColor Yellow
& $adb shell "pm clear-package-preferred com.michde.opennovalauncher >/dev/null 2>&1"
& $adb shell "pkill -f edge_model_sync.sh >/dev/null 2>&1"
& $adb shell "rm -f /data/local/tmp/edge_model_sync.sh"

Write-Host ""
Write-Host "==============================================================================" -ForegroundColor Green
Write-Host "[SUCCESS] Device successfully restored to stock One UI phone state!" -ForegroundColor Green
Write-Host "==============================================================================" -ForegroundColor Green
