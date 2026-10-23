@echo off
REM ==============================================================================
REM CREATE HOSTAI PHONE - Samsung Galaxy S24 (Exynos 2400 / One UI 9)
REM Transforms Galaxy S24 into a dedicated Edge LLM appliance with ~4.5 GB free RAM.
REM ==============================================================================

setlocal EnableDelayedExpansion
title HostAI Phone Creator - Galaxy S24

echo ==============================================================================
echo               HOSTAI PHONE SETUP - SAMSUNG GALAXY S24
echo ==============================================================================
echo.

REM 1. Detect ADB
set ADB=
if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" set ADB="%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
if "%ADB%"=="" where adb >nul 2>&1 && set ADB=adb

if "%ADB%"=="" (
    echo [ERROR] ADB not found! Install Android SDK or add adb to PATH.
    pause
    exit /b 1
)

echo [*] Using ADB: %ADB%
echo [*] Waiting for device...
%ADB% wait-for-device

for /f "tokens=*" %%i in ('%ADB% get-serialno') do set SERIAL=%%i
echo [*] Connected device serial: %SERIAL%
echo.

REM 2. Create Target Directories on Device
echo [*] Step 1/7: Preparing filesystem directories...
%ADB% shell "mkdir -p /storage/emulated/0/Download/models"
%ADB% shell "mkdir -p /data/local/tmp"

REM 3. Deploy System & Performance Settings
echo [*] Step 2/7: Applying low-latency system settings...
%ADB% shell "settings put global stay_on_while_plugged_in 0"
%ADB% shell "settings put global sem_low_power_mode 1"
%ADB% shell "settings put global ram_expand_size 0"
%ADB% shell "settings put global ble_scan_always_enabled 0"
%ADB% shell "settings put system screen_off_timeout 30000"
%ADB% shell "settings put secure adaptive_sleep 1"
%ADB% shell "settings put secure voice_interaction_service ''"
%ADB% shell "settings put secure assistant ''"
%ADB% shell "settings put global protect_battery 3"
%ADB% shell "settings put global battery_protection_threshold 80"

REM 4. Purge Knox Packages
echo [*] Step 3/7: Purging and neutralizing Knox & Enterprise bloat...
set KNOX_DISABLE=com.samsung.android.knox.kpecore com.samsung.android.knox.pushmanager com.samsung.android.knox.containercore com.samsung.knox.securefolder com.samsung.android.knox.mpos com.samsung.android.knox.analytics.uploader com.samsung.android.mdm com.samsung.android.messageguardsync com.sec.android.diagmonagent
for %%p in (%KNOX_DISABLE%) do (
    %ADB% shell "pm disable-user --user 0 %%p >nul 2>&1"
    %ADB% shell "am force-stop %%p >nul 2>&1"
)

set KNOX_RESTRICT=com.samsung.android.knox.attestation com.knox.vpn.proxyhandler com.samsung.android.knox.sandbox com.samsung.android.knox.app.networkfilter com.samsung.android.knox.er com.sec.enterprise.knox.cloudmdm.smdms com.samsung.android.knox.zt.framework com.samsung.android.knox.kfbp com.samsung.android.knox.knnr com.samsung.klmsagent com.samsung.android.kgclient
for %%p in (%KNOX_RESTRICT%) do (
    %ADB% shell "appops set %%p RUN_IN_BACKGROUND deny >nul 2>&1"
    %ADB% shell "appops set %%p RUN_ANY_IN_BACKGROUND deny >nul 2>&1"
    %ADB% shell "appops set %%p WAKE_LOCK deny >nul 2>&1"
    %ADB% shell "appops set %%p START_FOREGROUND deny >nul 2>&1"
    %ADB% shell "am force-stop %%p >nul 2>&1"
)

REM 5. Purge Samsung Gallery, Studio, AICore & Duplicate Bloat
echo [*] Step 4/7: Purging Samsung Studio, Gallery, and AI telemetry...
set SAMSUNG_BLOAT=com.sec.android.gallery3d com.sec.android.app.vepreload com.samsung.android.app.sketchbook com.sec.android.mimage.photoretouching com.samsung.app.newtrim com.samsung.storyservice com.samsung.mediasearch com.samsung.android.photoremasterservice com.samsung.android.motionphoto.app com.samsung.android.video com.samsung.android.aicore com.samsung.android.offline.languagemodel com.samsung.android.fmm com.samsung.android.mobileservice com.samsung.android.gru com.samsung.android.dqagent com.samsung.android.themestore com.samsung.android.themecenter com.samsung.android.mapsagent com.samsung.android.stickercenter com.samsung.android.aremoji com.samsung.android.app.camera.sticker.facearavatar.preload com.samsung.android.allshare.service.mediashare com.samsung.android.beaconmanager com.samsung.android.smartface com.samsung.android.smartface.overlay com.samsung.android.app.dressroom com.samsung.android.app.parentalcare com.sec.android.easyMover.Agent com.sec.android.app.billing com.samsung.android.da.daagent com.samsung.android.mcfserver com.samsung.android.mcf.autohotspot com.samsung.android.scs com.samsung.android.sm.devicesecurity com.samsung.android.service.peoplestripe com.samsung.android.app.clipboardedge com.samsung.android.app.taskedge com.samsung.android.ipsgeofence com.sec.android.sdhms com.sec.location.nsflp2 com.samsung.android.sead com.sec.tasmanager com.samsung.android.messaging com.samsung.android.calendar com.samsung.android.scloud com.samsung.android.visual.cloudcore com.samsung.android.samsungpass com.samsung.android.samsungpassautofill com.samsung.android.mcfds com.samsung.android.app.notes com.sec.android.app.samsungapps com.sec.android.daemonapp

for %%p in (%SAMSUNG_BLOAT%) do (
    %ADB% shell "pm disable-user --user 0 %%p >nul 2>&1"
    %ADB% shell "am force-stop %%p >nul 2>&1"
)

REM 6. Purge Google Bloat & Jail Google Search for Gemini
echo [*] Step 5/7: Purging Google background bloat and configuring Gemini...
set GOOGLE_BLOAT=com.google.android.aicore com.google.android.overlay.aicoreconfig com.google.android.as com.google.android.mosey com.google.android.apps.carrier.carrierwifi
for %%p in (%GOOGLE_BLOAT%) do (
    %ADB% shell "pm disable-user --user 0 %%p >nul 2>&1"
    %ADB% shell "am force-stop %%p >nul 2>&1"
)

%ADB% shell "pm enable --user 0 com.google.android.googlequicksearchbox >nul 2>&1"
%ADB% shell "pm enable --user 0 com.google.android.apps.bard >nul 2>&1"
%ADB% shell "appops set com.google.android.googlequicksearchbox RUN_IN_BACKGROUND deny"
%ADB% shell "appops set com.google.android.googlequicksearchbox RUN_ANY_IN_BACKGROUND deny"
%ADB% shell "appops set com.google.android.googlequicksearchbox WAKE_LOCK deny"
%ADB% shell "appops set com.google.android.apps.bard RUN_IN_BACKGROUND deny"
%ADB% shell "appops set com.google.android.apps.bard RUN_ANY_IN_BACKGROUND deny"
%ADB% shell "am force-stop com.google.android.googlequicksearchbox"

REM 7. Install / Launch HostAI & Start Background Daemons
echo [*] Step 6/7: Deploying HostAI and model sync daemon...
if exist "%~dp0releases\hostai-s24-exynos.apk" (
    echo [*] Installing HostAI APK...
    %ADB% install -r "%~dp0releases\hostai-s24-exynos.apk"
)

if exist "%~dp0releases\opennovalauncher-s24.apk" (
    echo [*] Installing OpenNova S24 Launcher...
    %ADB% install -r "%~dp0releases\opennovalauncher-s24.apk"
    %ADB% shell "cmd package set-home-activity com.michde.opennovalauncher/.MainActivity >nul 2>&1"
)

REM Push edge model sync daemon
%ADB% shell "echo '#!/system/bin/sh' > /data/local/tmp/edge_model_sync.sh"
%ADB% shell "echo 'while true; do' >> /data/local/tmp/edge_model_sync.sh"
%ADB% shell "echo '  find /storage/emulated/0/Android/data/com.google.ai.edge.gallery/files/ -type f -name \"*.litertlm\" -exec cp -u {} /storage/emulated/0/Download/models/ \; 2>/dev/null' >> /data/local/tmp/edge_model_sync.sh"
%ADB% shell "echo '  sleep 15' >> /data/local/tmp/edge_model_sync.sh"
%ADB% shell "echo 'done' >> /data/local/tmp/edge_model_sync.sh"
%ADB% shell "chmod 755 /data/local/tmp/edge_model_sync.sh"
%ADB% shell "nohup /data/local/tmp/edge_model_sync.sh >/dev/null 2>&1 &"

REM Grant permissions and launch HostAI
%ADB% shell "appops set com.wannaphong.hostai MANAGE_EXTERNAL_STORAGE allow"
%ADB% shell "cmd deviceidle whitelist +com.wannaphong.hostai"
%ADB% shell "am start -n com.wannaphong.hostai/.MainActivity"

REM 8. Memory Compaction & Trim
echo [*] Step 7/7: Executing memory compaction and UFS storage trim...
%ADB% shell "am kill-all"
%ADB% shell "cmd activity compact system"
%ADB% shell "pm trim-caches 999999999999"
%ADB% shell "sm fstrim"
%ADB% shell "sm idle-maint run"

echo.
echo ==============================================================================
echo [SUCCESS] Galaxy S24 successfully configured as HostAI Appliance!
echo ==============================================================================
echo.
echo [*] Current Memory Status:
%ADB% shell "cat /proc/meminfo | head -n 4"
echo.
pause
