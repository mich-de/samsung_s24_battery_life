# S24 Edge AI & Battery Appliance

🌐 **English | [Español](README-ES.md) | [Italiano](README-IT.md) | [Português](README-PT.md)**

> **Transform your Samsung Galaxy S24 into a dedicated Edge LLM AI Appliance and Battery-Optimized Workstation** — local on-device OpenAI-compatible LLM server, Exynos 2400 AMD Xclipse 940 GPU acceleration, custom ChatGPT-like Web UI, lightweight launcher, and complete bloatware purge with **no root required**.

<p align="center">
  <img src="https://github.com/mich-de/samsung_s24_battery_life/blob/main/hostai_s24_live.png" alt="HostAI S24 Edge AI Studio" width="450" />
</p>

---

## 🚀 1-Click Transformation: Galaxy S24 Edge AI Appliance

Transform your device in seconds into a local offline LLM server:

| OS | Quick Setup (Transform to AI Appliance) | Rollback (Restore Stock) |
|---|---|---|
| **Windows (CMD)** | `create_hostai_phone.bat` | `restore_hostai_phone.bat` |
| **Windows (PowerShell)** | `.\create_hostai_phone.ps1` | `.\restore_hostai_phone.ps1` |
| **macOS / Linux** | `bash create_hostai_phone.sh` | `bash restore_hostai_phone.sh` |

Full step-by-step documentation available in the **[S24 Edge LLM Appliance Replication Guide](S24_Edge_LLM_Appliance_Replication_Guide.md)**.

### 🌟 Key AI Appliance Features

1. **HostAI S24 Edition (`releases/hostai-s24-exynos.apk`)**:
   - **Local OpenAI API**: Full `/v1/chat/completions` and `/v1/models` endpoints running on port `8080`.
   - **Hardware Acceleration**: Optimized for Samsung Exynos 2400 with AMD Xclipse 940 GPU via OpenCL.
   - **On-Device Models**: Preconfigured for Google Gemma 4 E2B Instruct (2.6 GB), TinyGarden 270M (288 MB), and EmbeddingGemma.
   - **Zero Cloud Leakage**: 100% offline edge neural processing — zero personal data leaves the device.

2. **Built-in ChatGPT-like Web UI (`http://localhost:8080/chat`)**:
   - **Obsidian Dark & Clean Light themes** with 1-tap instant switch.
   - **Zero CDN Dependencies**: 100% self-contained vanilla HTML/JS/CSS, loads in <5ms with zero blank page issues.
   - **Live SSE Streaming**: Real-time token streaming with typing animation and Stop Generation button.
   - **Rich Markdown & Code**: Syntax-highlighted code blocks with 1-click "Copy" button.
   - **Session History**: Multi-chat sidebar with persistence in `localStorage`.

3. **OpenNovaLauncher S24 Edition (`releases/opennovalauncher-s24.apk`)**:
   - Dedicated minimal home launcher tailored for AI workflows.
   - Integrated HostAI Studio card & 1-tap Web Chat shortcut.
   - Native folder management, dynamic 3/4/5 column grid, hide apps manager, zero telemetry.
   - 3-button navigation bar insets compensated for smooth scrolling.

4. **Needle Router S24 Edition (`releases/needle-router-v1.2.apk`)**:
   - Zero-latency natural language intent router and local offline assistant for Samsung Galaxy S24.
   - Hardware control: Camera, Flashlight/Torch, Alarms, Clock, Wi-Fi, Bluetooth, Settings, App launch.
   - Integrates with local HostAI OpenAI server for complex queries.
   - **Offline Neural TTS**: Native binding to Kaldi Sherpa-ONNX (`com.k2fsa.sherpa.onnx.tts.engine`) with pre-packaged Italian VITS Piper voice for zero-cloud spoken feedback.

5. **De-Googled & Sealed Privacy Architecture**:
   - **Dicio Voice Assistant (`releases/dicio.apk`)**: Offline voice AI assistant bridge to HostAI LLM.
   - **MicroG-RE (`releases/microg-morphe-re.apk`)**: Open-source GMS replacement with zero background telemetry.
   - **App Stores**: F-Droid (`releases/f-droid.apk`) and Aurora Store (`releases/aurora-store.apk`) in anonymous mode.
   - **AppOps Cage for Gemini**: Sealed sandbox for `googlequicksearchbox` (0 MB idle RAM consumption).
   - **RAM Squeeze**: Neutralized 19 Knox/telemetry packages, freeing **>4.5 GB physical RAM** (over 60% memory free) for model weights and large context buffers.

---

## 📋 Compatibility

| Model | One UI / Android |
|-------|------------------|
| SM-S921B (S24 base) | One UI 8.5 / Android 16 & One UI 9.0 Beta / Android 17 (tested on Exynos) |
| SM-S926B (S24+) | One UI 8.5 / Android 16 & One UI 9.0 Beta (untested) |
| SM-S928B (S24 Ultra) | One UI 8.5 / Android 16 & One UI 9.0 Beta (untested) |
| S23, S22, S21 series | One UI 5+ / Android 13+ (untested) |

---

## 🔧 What it does

### Disabled bloatware (30+ packages)

| Category | Packages | Reason |
|----------|----------|--------|
| **Bixby** | agent, wakeup, vision, visionintelligence, 13 language packs | Always-listening assistant + AI |
| **Samsung Apps** | Game Tools, GOS, Smart Suggestions, Aware Service, BBC Agent, Reminder, Routines, Routine Plus, Live Effect, OneConnect, STPlatform, Forest, Buds Manager, Rubin App, My Galaxy, Samsung Health | Bloat + background drain |
| **Knox** | kpecore, attestation, pushmanager, container, analytics, zt framework | April 2026 attestation loop bug |
| **App Update Center** | updatecenter, scpm, statsd | Pushes "Experience" app recommendations + telemetry |
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
- ~~**Device Protection / lool**~~ — withdrawn on 14 August. `lool` owns the `com.samsung.android.sm.dcapi` provider that system_server calls when Sleep mode changes the power saving mode; disabled, it crashed system_server in a loop until RescueParty rebooted the phone and rolled back the settings this app had written. Do not re-add.
- **Smart Contextual Platform (scpm)** — usage pattern analytics
- **Samsung Analytics (statsd)** — telemetry collection
- **Theme Designer, Share Live, Easy Setup, SmartThings Find, Galaxy App Booster**

New system toggles: **Samsung Error Reports OFF**, **Samsung HQM Telemetry OFF**

### Scheduled Restart

A weekly (or daily) reboot at a time you pick, driven by an alarm and `svc power reboot` — Samsung's own "Auto restart" is off and its UI is disabled from the factory on this device. It only fires while the phone is idle: screen off, charging, above 30%, no call in progress. Any condition not met and it retries every 15 minutes for up to 2 hours, then waits for the next day. A notification gives 60 seconds of warning with **Cancel**.

Reboots done this way are tagged: `getprop ro.boot.bootreason` reads `reboot,s24opt_scheduled`, so they stay distinguishable from a crash-driven `reboot,rescueparty`.

⚠️ **Every reboot kills Shizuku** (its server runs as the shell user and cannot survive a boot), so everything that depends on it — the charge schedule included — stays frozen until Shizuku is started again. Weekly is safer than nightly until Shizuku starts on boot by itself.

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
├── create_hostai_phone.bat     ← 1-click: turn S24 into dedicated Edge LLM appliance
├── create_hostai_phone.ps1     ← PowerShell version
├── create_hostai_phone.sh      ← Linux / macOS version
├── restore_hostai_phone.bat    ← Full rollback / safety net: restores all stock Samsung & Knox apps
├── restore_hostai_phone.ps1    ← PowerShell rollback version
├── restore_hostai_phone.sh     ← Linux / macOS rollback version
├── releases/                   ← Executable files
│   ├── s24-battery-optimizer.apk
│   └── hostai-s24-exynos.apk   ← Edge LLM server (Exynos 2400 / AMD Xclipse 940)
├── S24_Edge_LLM_Appliance_Replication_Guide.md ← Edge LLM appliance replication runbook
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
