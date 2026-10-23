# ========================================================
# S24 Emergency App Restore Script (PowerShell)
# ========================================================

$adb = "C:\Users\mdeangelis\AppData\Local\Android\Sdk\platform-tools\adb.exe"
if (-not (Test-Path $adb)) { $adb = "adb.exe" }

Write-Host "[*] Checking ADB connection..." -ForegroundColor Cyan
& $adb devices

$appsToRestore = @(
    "com.sec.android.app.vepreload com.samsung.android.app.sketchbook",
    "com.sec.android.mimage.photoretouching",
    "com.samsung.app.newtrim",
    "com.samsung.storyservice",
    "com.samsung.mediasearch",
    "com.sec.android.gallery3d",
    "com.google.android.aicore",
    "com.samsung.android.aicore",
    "com.google.android.overlay.aicoreconfig",
    "com.google.android.googlequicksearchbox",
    "com.google.android.apps.bard"
)

foreach ($pkg in $appsToRestore) {
    Write-Host "[*] Re-enabling $pkg..." -ForegroundColor Yellow
    & $adb shell "pm enable --user 0 $pkg"
}

Write-Host "[OK] System apps restored successfully!" -ForegroundColor Green
