# Implementation Plan — S24 Battery Optimization Suite

This project contains PowerShell, Shell, and Batch scripts alongside a companion Kotlin-based Android application (`android-app`) to automate and manage battery optimization on Samsung Galaxy S24 series devices without root privileges.

## 1. Objectives & Technical Strategy
- **Bloatware Management**: Automate disabling of high-drain packages (Bixby, Knox Matrix anomalies, Samsung context services, Facebook background tasks).
- **Background Restrictions**: Restrict resource-heavy apps (Instagram, WhatsApp, Tandem) via AppOps.
- **System Setting Adjustments**: Adjust CPU responsiveness, radio scans, Doze parameters, animations, RAM Plus, and battery protection thresholds.
- **Companion App**: A lightweight Android utility to inspect running packages, battery stats, or toggle settings where possible.

## 2. Architecture & Script Design
- **s24-battery-optimize.ps1**: Main PowerShell script executing adb commands targeting One UI 7 / Android 16 configurations.
- **s24-battery-revert.ps1**: Revert script to restore default settings and re-enable packages.
- **s24-optimize.bat** / **s24-restore.bat**: Windows Batch scripts for basic automation and recovery.
- **s24-optimize.sh** / **s24-restore.sh**: Unix Shell scripts (macOS/Linux) equivalent to the batch versions for cross-platform automation.
- **Companion App**: Kotlin-based Gradle project utilizing local system services to view or manage status.

## 3. Implementation Phases
### Phase 1: Diagnostics and Script Testing
- Check device connection status via ADB.
- Capture screen views for visual verification of settings (e.g., Settings menu, battery status).
- Maintain script alignment with official Android 16 / One UI 7 APIs.

### Phase 2: App Development (Companion)
- Configure Gradle structure and dependencies.
- Build visual screens for companion diagnostics.
- Ensure proper logging and error handling.
