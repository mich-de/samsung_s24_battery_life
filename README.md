# S24 Battery Optimizer

🌐 **English | [Español](README-ES.md) | [Italiano](README-IT.md) | [Português](README-PT.md)**

> **Restore your Galaxy S24 battery life** — disable bloatware, stop known drain bugs, and optimize system settings, **no root required**.

<p align="center">
  <img src="https://github.com/mich-de/samsung_s24_battery_life/blob/main/current_android_screenshot.png" alt="S24 Battery Optimizer v2.3" width="300" />
</p>

---

## 📋 Compatibility

| Model | One UI / Android |
|-------|------------------|
| SM-S921B (S24 base) | One UI 8.5 / Android 16 (tested on Exynos) |
| SM-S926B (S24+) | One UI 8.5 / Android 16 (untested) |
| SM-S928B (S24 Ultra) | One UI 8.5 / Android 16 (untested) |
| S23, S22, S21 series | One UI 5+ / Android 13+ (untested) |

---

## 🔧 What it does

### Disabled bloatware (30+ packages)

| Category | Packages | Reason |
|----------|----------|--------|
| **Bixby** | agent, wakeup, vision, visionintelligence, 13 language packs | Always-listening assistant + AI |
| **Samsung Apps** | Game Tools, GOS, Smart Suggestions, Aware Service, BBC Agent, Reminder, Routines, Routine Plus, Live Effect, OneConnect, STPlatform, Forest, Buds Manager, Rubin App, My Galaxy, Samsung Health | Bloat + background drain |
| **Knox** | kpecore, attestation, pushmanager, container, analytics, zt framework | April 2026 attestation loop bug |
| **App Update Center** | updatecenter, scpm, statsd, lool | Pushes "Experience" app recommendations + telemetry |
| **Samsung Pay** | spayfw | Payment service (Google Wallet unaffected) |
| **Google** | Messages, Docs, Chromecast, Feedback, Supervision | Unused services |
| **Facebook/Meta** | Facebook, Messenger | Social battery drain |
| **Microsoft** | Edge, Excel, Word, Remote Desktop, Outlook | Office sync drain |

### ⚙️ System settings (25+)

| Category | Setting | Value |
|----------|---------|-------|
| **Battery** | Adaptive Battery | ON |
| | App Auto Restriction | ON |
| | Battery Saver | ON |
| | Enhanced CPU Resp. | OFF |
| | Enhanced Processing | OFF |
| **Radio** | BLE Scan Always | OFF |
| | Nearby Scanning | OFF |
| | WiFi Power Save | ON |
| | WiFi Wakeup | OFF |
| **Display** | AOD / Always On | OFF |
| | Screen Timeout | 30s |
| | Lift to Wake | OFF |
| | Smart Stay | OFF |
| | Doze | ON |
| **Performance** | Animations | 0.5x |
| | RAM Plus | 2 GB (selectable: 0/2/4/6/8 GB) |
| **Protection** | Max charge | 80% |

### 🚫 Background restricted (3+ apps)

`appops set RUN_ANY_IN_BACKGROUND deny` on:
- **Instagram** — continuous wakelocks
- **WhatsApp** — background sync drain
- **Tandem** — frequent polling

---

## 📊 Battery Results — Before vs After

Tested on **SM-S921B (Exynos 2400) · One UI 8 · Android 16**

| Metric | Before (Jul 16) | After (Jul 17) | Improvement |
|--------|----------------|---------------|-------------|
| **Standby drain** | ~78 mA/h | **53 mA/h** | **-32%** |
| **Estimated standby** | ~1.5 days | **2 days 4 hours** | **+43%** |
| **Deep Doze** | ❌ Not active | ✅ **Active** | ✅ |
| **Instagram (mAh/h)** | 57.1 | 29.8 | **-48%** |
| **System (mAh/h)** | 28.9 | 11.4 | **-61%** |
| **Kernel (mAh/h)** | 12.0 | 9.6 | **-20%** |

### 12.5-hour discharge test (post-optimization)

```
Duration:    12h 40m on battery
Screen-on:   56m (7.4%)
Drain:       33% (1,286 mAh / 3,900 mAh capacity)
Standby:     ~53 mA/h drain rate — excellent

Top consumers (mAh):
  Instagram              377 (29%)
  System (UID 1000)      144 (11%)
  WhatsApp               140 (11%)
  Kernel                 122 (9%)
  Google Play Services    45 (4%)
```

Deep Doze consumed only **581 mAh** over ~11 hours of standby — the phone enters deep sleep correctly after disabling Samsung bloat that was holding wakelocks.

---

## 📱 Android Companion App (Shizuku)

The project includes a native Kotlin Android app at `android-app/`. It runs ADB shell commands directly on the phone via **Shizuku** — no PC needed after initial setup.

Latest release: **[v2.3](https://github.com/mich-de/samsung_s24_battery_life/releases/tag/v2.3)** — APK available on GitHub Releases.

**Author:** [mich-de](https://github.com/mich-de) | `github.com/mich-de/samsung_s24_battery_life`

### v2.3 — July 27 Bloatware Discovery

New bloatware found after the S921BXXSGDZG1 OTA (July 27, 2026) that were pushing "Experience" app install recommendations:

- **App Update Center** — pushes "Experience" app installs after OTA
- **Device Protection / lool** — background scoring process
- **Smart Contextual Platform (scpm)** — usage pattern analytics
- **Samsung Analytics (statsd)** — telemetry collection
- **Theme Designer, Share Live, Easy Setup, SmartThings Find, Galaxy App Booster**

New system toggles: **Samsung Error Reports OFF**, **Samsung HQM Telemetry OFF**

### v1.1 — UI Redesign

- **Full UI redesign** — dark cyberpunk theme with gradient cards, electric blue accents, monospace typography
- **Tab-based navigation** — Quick Settings presets, category browsing, execution logs
- **Real-time status** — check which optimizations are applied, with green dots for active
- **Search bar** — filter optimizations by name within each category
- **Colored execution logs** — green for success, red for errors, blue for info
- **Save logs to file** — export execution logs for analysis
- **Profile manager** — save and load optimization profiles
- **Language toggle** — English (default) / Italian
- **New app icon** — custom processed icon (white on transparent)

### Optimizations (98 total)

| Category | Count |
|----------|-------|
| Samsung Bloat | 27 |
| Google Bloat | 6 |
| Facebook/Meta | 2 |
| Microsoft Bloat | 5 |
| Knox Matrix Reset | 6 |
| Google Play Reset | 2 |
| Background Restrict | 3 |
| System Settings | 47 |

Every optimization includes clear descriptions of what it does and what you lose.

### Setup (Android 16 / One UI 8.5)

1. **Install Shizuku v13.6+** from [GitHub Releases](https://github.com/RikkaApps/Shizuku/releases)
2. **Start Shizuku** via Wireless Debugging or ADB
3. **Open S24 Battery Optimizer** — it auto-requests Shizuku permission
4. **Browse categories**, toggle optimizations, tap **Apply Selected**

---

## ⚡ Manual ADB commands (what the app can't do)

The app handles everything via Shizuku. These manual steps are only needed once for setup:

### Start Shizuku (needed by the app)

```powershell
# Via ADB (phone connected to PC with USB Debugging):
.\adb.exe shell sh /data/local/tmp/shizuku.dex
```

### Enable Wireless Debugging (alternative to USB)

```powershell
# Pair with Shizuku app:
.\adb.exe shell settings put global development_settings_enabled 1
```

That's it. Everything else — disabling packages, changing settings, restricting background — is done inside the app.

---

## ❓ FAQ

**Will I lose data?** No. `pm disable-user` only hides apps.

**Does Google Wallet work?** Yes, completely independent of Samsung Pay.

**Samsung Pay?** Now disabled by default. Google Wallet works fine.

**Bixby doesn't work anymore?** Correct. If you need it, don't disable Bixby packages in the app.

**Hey Google / Gemini works?** Yes, preserved.

**Battery seems worse after reboot?** Normal. Android recalibrates for 1-2 charge cycles.

---

## 📂 File structure

```
s24-battery-optimizer/
├── releases/                   ← Executable files
│   └── s24-battery-optimizer.apk
├── android-app/                ← Android app source (Kotlin + Compose)
│   └── ...
├── README.md                   ← English (default)
├── README-ES.md
├── README-IT.md
└── README-PT.md
```

---

## 📜 License

MIT — use, modify, share.
