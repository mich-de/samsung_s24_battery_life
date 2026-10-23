@echo off
REM ========================================================
REM S24 Emergency App Restore Script
REM Re-enables or reinstalls any purged system apps
REM ========================================================

set ADB=C:\Users\mdeangelis\AppData\Local\Android\Sdk\platform-tools\adb.exe
if not exist "%ADB%" set ADB=adb.exe

echo [*] Checking ADB connection...
"%ADB%" devices

echo.
echo [*] Restoring Creative Studio, Photo Editor, and Media Services...
"%ADB%" shell "pm enable --user 0 com.sec.android.app.vepreload com.samsung.android.app.sketchbook"
"%ADB%" shell "pm enable --user 0 com.sec.android.mimage.photoretouching"
"%ADB%" shell "pm enable --user 0 com.samsung.app.newtrim"
"%ADB%" shell "pm enable --user 0 com.samsung.storyservice"
"%ADB%" shell "pm enable --user 0 com.samsung.mediasearch"
"%ADB%" shell "pm enable --user 0 com.sec.android.gallery3d"

echo.
echo [*] Restoring AICore...
"%ADB%" shell "pm enable --user 0 com.google.android.aicore"
"%ADB%" shell "pm enable --user 0 com.samsung.android.aicore"
"%ADB%" shell "pm enable --user 0 com.google.android.overlay.aicoreconfig"

echo.
echo [*] Restoring Google Search and Assistant...
"%ADB%" shell "pm enable --user 0 com.google.android.googlequicksearchbox"
"%ADB%" shell "pm enable --user 0 com.google.android.apps.bard"
"%ADB%" shell "appops set com.google.android.googlequicksearchbox RUN_IN_BACKGROUND allow"
"%ADB%" shell "appops set com.google.android.googlequicksearchbox RUN_ANY_IN_BACKGROUND allow"

echo.
echo [*] Restoring Knox Packages (if needed)...
"%ADB%" shell "pm enable --user 0 com.samsung.android.knox.kpecore"
"%ADB%" shell "pm enable --user 0 com.samsung.android.knox.containercore"
"%ADB%" shell "pm enable --user 0 com.samsung.knox.securefolder"
"%ADB%" shell "pm enable --user 0 com.samsung.android.knox.pushmanager"
"%ADB%" shell "pm enable --user 0 com.samsung.android.knox.mpos"
"%ADB%" shell "pm enable --user 0 com.samsung.android.knox.analytics.uploader"

echo.
echo [OK] All requested apps re-enabled for user 0!
pause
