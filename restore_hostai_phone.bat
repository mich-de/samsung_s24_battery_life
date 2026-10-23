@echo off
REM ==============================================================================
REM RESTORE HOSTAI PHONE - Samsung Galaxy S24 (Full Rollback / Safety Net)
REM Restores all Samsung services, Knox packages, Google App, and factory settings.
REM ==============================================================================

setlocal EnableDelayedExpansion
title Restore Phone - Full Rollback Safety Net

echo ==============================================================================
echo             FULL ROLLBACK / RESTORE S24 STOCK PHONE STATE
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
echo.

REM 2. Restore System Settings
echo [*] Step 1/6: Restoring standard system settings...
%ADB% shell "settings put global stay_on_while_plugged_in 0"
%ADB% shell "settings put global sem_low_power_mode 0"
%ADB% shell "settings put global ram_expand_size 4096"
%ADB% shell "settings put global ble_scan_always_enabled 1"
%ADB% shell "settings put system screen_off_timeout 60000"
%ADB% shell "settings put secure adaptive_sleep 1"
%ADB% shell "settings put global protect_battery 0"

REM 3. Restore Knox & Enterprise Packages
echo [*] Step 2/6: Re-enabling Knox packages and enterprise services...
set KNOX_RESTORE=com.samsung.android.knox.kpecore com.samsung.android.knox.pushmanager com.samsung.android.knox.containercore com.samsung.knox.securefolder com.samsung.android.knox.mpos com.samsung.android.knox.analytics.uploader com.samsung.android.mdm com.samsung.android.messageguardsync com.sec.android.diagmonagent
for %%p in (%KNOX_RESTORE%) do (
    %ADB% shell "pm enable --user 0 %%p >nul 2>&1"
)

set KNOX_PERMS=com.samsung.android.knox.attestation com.knox.vpn.proxyhandler com.samsung.android.knox.sandbox com.samsung.android.knox.app.networkfilter com.samsung.android.knox.er com.sec.enterprise.knox.cloudmdm.smdms com.samsung.android.knox.zt.framework com.samsung.android.knox.kfbp com.samsung.android.knox.knnr com.samsung.klmsagent com.samsung.android.kgclient
for %%p in (%KNOX_PERMS%) do (
    %ADB% shell "appops set %%p RUN_IN_BACKGROUND allow >nul 2>&1"
    %ADB% shell "appops set %%p RUN_ANY_IN_BACKGROUND allow >nul 2>&1"
    %ADB% shell "appops set %%p WAKE_LOCK allow >nul 2>&1"
    %ADB% shell "appops set %%p START_FOREGROUND allow >nul 2>&1"
)

REM 4. Restore Samsung Gallery, Studio, AICore & Apps
echo [*] Step 3/6: Re-enabling Samsung Studio, Gallery, and system apps...
set SAMSUNG_RESTORE=com.sec.android.gallery3d com.sec.android.app.vepreload com.samsung.android.app.sketchbook com.sec.android.mimage.photoretouching com.samsung.app.newtrim com.samsung.storyservice com.samsung.mediasearch com.samsung.android.photoremasterservice com.samsung.android.motionphoto.app com.samsung.android.video com.samsung.android.aicore com.samsung.android.offline.languagemodel com.samsung.android.fmm com.samsung.android.mobileservice com.samsung.android.gru com.samsung.android.dqagent com.samsung.android.themestore com.samsung.android.themecenter com.samsung.android.mapsagent com.samsung.android.stickercenter com.samsung.android.aremoji com.samsung.android.app.camera.sticker.facearavatar.preload com.samsung.android.allshare.service.mediashare com.samsung.android.beaconmanager com.samsung.android.smartface com.samsung.android.smartface.overlay com.samsung.android.app.dressroom com.samsung.android.app.parentalcare com.sec.android.easyMover.Agent com.sec.android.app.billing com.samsung.android.da.daagent com.samsung.android.mcfserver com.samsung.android.mcf.autohotspot com.samsung.android.scs com.samsung.android.sm.devicesecurity com.samsung.android.service.peoplestripe com.samsung.android.app.clipboardedge com.samsung.android.app.taskedge com.samsung.android.ipsgeofence com.sec.android.sdhms com.sec.location.nsflp2 com.samsung.android.sead com.sec.tasmanager com.samsung.android.messaging com.samsung.android.calendar com.samsung.android.scloud com.samsung.android.visual.cloudcore com.samsung.android.samsungpass com.samsung.android.samsungpassautofill com.samsung.android.mcfds com.samsung.android.app.notes com.sec.android.app.samsungapps com.sec.android.daemonapp

for %%p in (%SAMSUNG_RESTORE%) do (
    %ADB% shell "pm enable --user 0 %%p >nul 2>&1"
)

REM 5. Restore Google App, AICore, and Assistant
echo [*] Step 4/6: Re-enabling Google services and Assistant...
set GOOGLE_RESTORE=com.google.android.googlequicksearchbox com.google.android.apps.bard com.google.android.aicore com.google.android.overlay.aicoreconfig com.google.android.as com.google.android.mosey com.google.android.apps.carrier.carrierwifi
for %%p in (%GOOGLE_RESTORE%) do (
    %ADB% shell "pm enable --user 0 %%p >nul 2>&1"
)

%ADB% shell "appops set com.google.android.googlequicksearchbox RUN_IN_BACKGROUND allow"
%ADB% shell "appops set com.google.android.googlequicksearchbox RUN_ANY_IN_BACKGROUND allow"
%ADB% shell "appops set com.google.android.googlequicksearchbox WAKE_LOCK allow"
%ADB% shell "appops set com.google.android.apps.bard RUN_IN_BACKGROUND allow"
%ADB% shell "appops set com.google.android.apps.bard RUN_ANY_IN_BACKGROUND allow"

REM 6. Reinstall any physical APKs from backup if missing
echo [*] Step 5/6: Verifying backup APKs...
if exist "%~dp0backup\system_apps" (
    for %%f in ("%~dp0backup\system_apps\*.apk") do (
        echo [*] Checking %%~nxf...
        %ADB% install -r -d "%%f" >nul 2>&1
    )
)

REM 7. Clean up background sync daemon and restore default launcher
echo [*] Step 6/6: Restoring default launcher and terminating background daemons...
%ADB% shell "pm clear-package-preferred com.michde.opennovalauncher >nul 2>&1"
%ADB% shell "pkill -f edge_model_sync.sh >nul 2>&1"
%ADB% shell "rm -f /data/local/tmp/edge_model_sync.sh"

echo.
echo ==============================================================================
echo [SUCCESS] Device successfully restored to stock One UI phone state!
echo ==============================================================================
echo.
pause
