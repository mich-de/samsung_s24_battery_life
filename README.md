# S24 Battery Optimizer

🌐 **English | [Español](README-ES.md) | [Italiano](README-IT.md) | [Português](README-PT.md)**

> **Restore your Galaxy S24 battery life** — disable bloatware, stop known drain bugs, and optimize system settings, **no root required**.

---

## 📋 Compatibility

| Model | One UI / Android |
|-------|------------------|
| SM-S921B (S24 base) | One UI 7 / Android 16 (tested on exynos, untested on snapdragon)|
| SM-S926B (S24+) | One UI 7 / Android 16 (untested)|
| SM-S928B (S24 Ultra) | One UI 7 / Android 16 (untested)|
| S23, S22, S21 series | One UI 5+ / Android 13+ (untested) |

---

## 🔧 What it does

### Disabled bloatware (24 packages)

| App | Package | Reason |
|-----|---------|--------|
| **Bixby Voice** | `bixby.agent` | Always-listening assistant |
| **Bixby Wakeup** | `bixby.wakeup` | "Hi Bixby" hotword |
| **Bixby Vision** | `bixbyvision.framework` | Camera AI recognition |
| **Vision Intelligence** | `visionintelligence` | Scene analysis AI |
| **Game Tools** | `game.gametools` | Game overlay panel |
| **Game Optimizing** | `game.gos` | CPU/GPU throttling |
| **Smart Suggestions** | `smartsuggestions` | Contextual suggestions |
| **Aware Service** | `aware.service` | Activity detection |
| **BBC Agent** | `bbc.bbcagent` | Bloatware push updates |
| **Reminder** | `app.reminder` | Samsung reminders |
| **Routines** | `app.routines` | Bixby automations |
| **Routine Plus** | `app.routineplus` | Doze wakelock (confirmed drain) |
| **Live Effect** | `liveeffectservice` | Video call effects |
| **OneConnect** | `oneconnect` | SmartThings Find |
| **STPlatform** | `service.stplatform` | Knox background service |
| **Forest** | `forest` | Digital wellbeing |
| **Buds Manager** | `accessory.budsunitemgr` | Galaxy Buds plugin |
| **Rubin App** | `rubin.app` | Customization Service (Play Services loop) |
| **Facebook** | `facebook.katana` | Social battery drain |
| **Messenger** | `facebook.orca` | Chat wakelocks |
| **Edge** | `microsoft.emmx` | Alternative browser |
| **Excel** | `office.excel` | Office sync |
| **Word** | `office.word` | Office sync |
| **Remote Desktop** | `rdc.androidx` | RDP client |

### 🔄 Knox Matrix Reset (5 packages)

Resets Knox services affected by the **April 2026 attestation loop bug** (continuous CPU drain). `kpecore`, `attestation`, `pushmanager`, `containercore`, `analytics.uploader`.

### ⚙️ System settings (23)

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
| | MCF Quick Share | OFF |
| **Display** | AOD / Always On | OFF |
| | Screen Timeout | 30s |
| | Lift to Wake | OFF |
| | Smart Stay | OFF |
| | Doze | ON |
| **Performance** | Animations | 0.5x |
| | RAM Plus | 2 GB |
| **Protection** | Max charge | 80% |

### 🚫 Background restricted (3 apps)

`appops set RUN_ANY_IN_BACKGROUND deny` on:
- **Instagram** — continuous wakelocks, background sync
- **WhatsApp** — 499 mAh in 8h in our test (16% of total)
- **Tandem** (edit package name if needed)

---

## 📊 Expected results

### Discharge test — Before optimization

```
Duration:    8h 42m (80% → 19% = 61% used)
Capacity:    3,114 mAh drained
Average:     358 mA/h
Est. life:   ~10.9h (mixed usage)

Top consumers (mAh):
  CPU                    1,252 (40%)
  Display                 542 (17%)
  Radio 5G                440 (14%)
  WhatsApp                499 (16%)
  System                  386 (12%)
  Instagram               314 (10%)
  Google Play Services    288 (9%)
  Kernel                  186 (6%)
```

### After optimization (data being collected)

Community reports indicate:
- **15-25%** reduction in standby drain
- **+2-4 hours** extra in mixed usage
- WhatsApp and Instagram no longer drain in background

---

## 🚀 How to use

### Requirements
- Windows with ADB configured (`platform-tools`)
- USB Debugging enabled on phone
- Phone connected to PC

### Installation

```powershell
git clone https://github.com/tuouser/s24-battery-optimizer
cd s24-battery-optimizer
```

### Customize ADB path

Edit `s24-optimize.bat` (line 22):

```batch
set ADB="C:\your-path\platform-tools\adb.exe"
```

### Run

```powershell
.\s24-optimize.bat
```

The phone will show a debugging prompt — **always confirm**.

### Undo everything

```powershell
.\s24-restore.bat
```

---

## 👆 Manual settings (S24 Exynos 2400 · One UI 7)

These settings **cannot** be automated via ADB. Paths are **exact** for One UI 7 on S24 (Exynos 2400, Android 16).

### ⚡ Exynos 2400 — 5G Modem & CPU

The Exynos 5300 modem (integrated in the 2400) has higher idle drain on **5G SA** (Standalone) with weak signal. On One UI 7:

| Setting | Exact path (One UI 7) | Recommendation |
|---------|----------------------|----------------|
| **5G Network preference** | Settings > Connections > Mobile networks > Network mode | "5G/LTE/3G/2G (auto connect)" — BUT switch to "LTE/3G/2G (auto connect)" if you're in a weak 5G area. The Exynos 5300 consumes 2-3x more when hunting for unstable 5G |
| **Standalone 5G** | Settings > Connections > Mobile networks | If you see "5G SA", set it OFF. NSA mode draws less because it uses 4G for control signaling |
| **Optimize connection** | Settings > Connections > WiFi > ⋯ > Intelligent > "Switch to mobile data" | OFF — prevents the WiFi/5G ping-pong that the Exynos modem handles poorly |
| **Advanced WiFi** | Same section > "Switch to better network" | OFF — stops periodic scanning |

Exynos 2400 CPU/GPU scheduler (G788):

| Setting | Exact path (One UI 7) | Recommendation |
|---------|----------------------|----------------|
| **Processing speed** | Settings > Device care > Battery > More battery settings > Processing speed | **"Optimized"** (NOT "High" or "Maximum" — those force the X4 cores to higher clocks unnecessarily) |
| **Auto brightness** | Settings > Display | ON — the S24's M13 OLED panel consumes significantly less below 50% brightness. Locking it at 70% doubles drain |
| **Resolution** | Settings > Display > Screen resolution | **FHD+** (2340×1080). On a 6.2" screen the difference from QHD+ is invisible, but GPU rendering costs the Exynos Xclipse 940 heavily |
| **Refresh rate** | Settings > Display > Motion smoothness | **"Adaptive"** (LTPO 1→24→120Hz). NOT "Standard" (60Hz) and NOT fixed 120Hz — LTPO drops to 1Hz on Home screen, 24Hz on video |

### 🧠 Good Guardians (free Galaxy Store apps)

Samsung provides official modules for the S24. Install **"Good Guardians"** from Galaxy Store (search "Good Guardians Samsung").

| Module | Install from | What it does |
|--------|-------------|--------------|
| **Battery Guardian** | Galaxy Store > search "Battery Guardian" | Blocks apps that keep CPU awake with screen off ("Apps preventing sleep" feature) |
| **Thermal Guardian** | Galaxy Store > search "Thermal Guardian" | Lowers Exynos 2400 thermal threshold by 1-2°C — prevents the chip from heating up and consuming more to cool down. Set "Cooling threshold" to -1°C or -2°C |
| **Memory Guardian** | Galaxy Store > search "Memory Guardian" | Frees RAM more aggressively. S24 has 8GB; keeping them clean avoids flash swap (battery drain) |
| **App Booster** | Galaxy Store > search "App Booster" | Optimizes apps compiled with the Exynos NPU. Run after every monthly update |

### 🔋 Good Lock — Camera Assistant & NavStar

Available from **Galaxy Store > Good Lock** (or NiceLock if restricted in your region).

| Module | Setting | Recommendation |
|--------|---------|----------------|
| **Camera Assistant** | "Automatic lens switching" | OFF — constant switching between wide/tele lenses drains the Exynos ISP |
| **Camera Assistant** | "Video HDR10+" | OFF unless you record HDR video (Exynos MFC encoding is heavy) |
| **Reduce animations** | Settings > Developer options (enable via About phone > Software info > Tap Build number 7×) > "Reduce animations" | ON — complements ADB script (0.5x already set). If Developer options aren't visible, ADB 0.5x covers this |

### ☁️ Cloud services & sync

| Setting | Exact path (One UI 7) | Recommendation |
|---------|----------------------|----------------|
| **Samsung account sync** | Settings > Accounts and backup > Manage accounts > Samsung Cloud > Sync | Disable **everything** except Contacts (if needed). Gallery, Notes, Samsung Calendar do periodic polling |
| **Galaxy Store auto-update** | Galaxy Store > Menu (⋮) > Settings > Auto update apps | "Over WiFi only" or "Never" |
| **Play Store auto-update** | Play Store > Profile icon > Settings > Network preferences > Auto-update apps | "Over WiFi only" |
| **Google Photos backup** | Google Photos > Profile icon > Photos settings > Backup | "Over WiFi only" or OFF if you sync manually |
| **Samsung Cloud backup** | Settings > Accounts and backup > Samsung Cloud | OFF unless you pay for the subscription (free 5GB ends quickly) |

### 📡 Hidden connectivity (S24 specific)

| Setting | Exact path (One UI 7) | Recommendation |
|---------|----------------------|----------------|
| **Find My Mobile** | Settings > Security and privacy > Find My Mobile > "Allow searching" | OFF — does periodic network scanning. You don't need remote wake |
| **Smart View** | Quick panel > Smart View toggle | Keep OFF if you don't use screen mirroring |
| **NFC** | Quick panel > NFC toggle | Keep OFF if you don't use Samsung/Google Pay often. Exynos NFC polls every 3-5s |
| **UWB** | Settings > Connections > Ultra-wideband | OFF if you don't use SmartThings Find with UWB tags |
| **Printing** | Settings > Advanced features > Printing | OFF if no printer connected |


### 🔳 Display & One UI 7

S24 M13 OLED 6.2" 2340×1080 (Dynamic AMOLED 2X):

| Setting | Exact path (One UI 7) | Recommendation |
|---------|----------------------|----------------|
| **Edge panels** | Settings > Display > Edge panels | OFF if unused (the edge processor keeps the touch sensor warm) |
| **Smart pop-up view** | Settings > Advanced features > Smart pop-up view | OFF |
| **App notification badges** | Settings > Display > App notification badges | "Without numbers" or OFF — each badge requires icon polling |
| **Navigation bar** | Settings > Display > Navigation bar | "Full screen gestures" (software buttons keep the display controller more active) |
| **Dark mode** | Settings > Display > Dark mode | ON permanently. On OLED it's the single most effective setting: black pixels = off pixels |

### 🎯 Specific apps — S24 Exynos

| App | Recommended action (S24) |
|-----|-------------------------|
| **Google Photos** (preinstalled) | Backup WiFi only. The Exynos NPU does AI analysis (face recognition) during photo backup — battery drain |
| **TikTok** | Block background: `adb shell appops set com.zhiliaoapp.musically RUN_ANY_IN_BACKGROUND deny` |
| **Facebook** (if re-enabled) | `adb shell appops set com.facebook.katana RUN_ANY_IN_BACKGROUND deny` |
| **LinkedIn** | `adb shell appops set com.linkedin.android RUN_ANY_IN_BACKGROUND deny` — frequent contact sync |
| **Samsung Health** | If you don't use fitness sensors, disable: `adb shell pm disable-user --user 0 com.sec.android.app.shealth`. The Exynos 2400 keeps the ISP active for the HR sensor |
| **Samsung Members** | Diagnostics. Disable: `adb shell pm disable-user --user 0 com.samsung.android.mobileservice` |
| **Maps** (offline) | Download offline maps for your area. The Exynos N9 GPS is heavy with streaming maps |

### 🧹 Exynos periodic maintenance

The Exynos 2400 accumulates cache in Samsung apps more than other chips. Once a month:

**Wipe cache partition (no data loss):**
1. Power off the phone
2. Hold **Volume UP + Power** (S24 boots to recovery)
3. Use volume keys to navigate to "Wipe cache partition"
4. Press Power to select
5. "Yes" → "Reboot system now"

**App Booster** (from Good Guardians or Device Care):
1. Open **Device Care**
2. Tap "Start check" or the eye icon
3. Scroll to bottom → "App Booster" → "Run"

**Clear specific app cache** (if an app is draining):
1. Settings > Apps > [offending app] > Storage > Clear cache
2. Do NOT touch "Clear data" (you'd lose login/preferences)

---

## 🔍 Technical notes

### 120Hz and 5G remain active
The optimization does *not* disable:
- 120Hz refresh rate (only Battery Saver set to "Maximum" would limit it)
- 5G network (NO commands modify network preferences)
- Google Maps, Gmail, Google Assistant / Gemini, navigation

### Rubin.app — the Google Play Services fix
`Samsung Customization Service` (`com.samsung.android.rubin.app`) is known to create a loop with Google Play Services: it continuously requests location/activity updates, preventing Play Services from sleeping. We disable it + revoke `ACTIVITY_RECOGNITION`.

### Knox April 2026 Drain Bug
Starting with the April 2026 security patch, some Knox services (especially `kpecore` and `attestation`) enter a continuous attestation loop. `pm clear` forces a clean reset.

### Routine Plus Wakelock
We discovered that `com.samsung.android.app.routineplus` (a separate service from Routines) periodically acquires `Doze (draw)` wakelocks even when Routines is disabled. This was missing from most guides.

### 0.5x Animations
Doubles UI transition speed — snappier feel and less GPU time. 120Hz stays active.

### RAM Plus 2GB
High values (4/6/8GB) consume battery due to continuous flash I/O on the UFS 4.0 storage. 2GB is the optimal balance.

### Google Play Services
If you still notice abnormal Play Services drain after optimization (check Settings > Battery), uncomment these lines in the script:

```batch
REM am force-stop com.google.android.gms
REM pm clear com.google.android.gms
```

⚠️ `pm clear` on GMS resets Google Wallet payment cards — last resort only.

---

## 📂 File structure

```
s24-battery-optimizer/
├── releases/                   ← Folder containing all executable files
│   ├── s24-optimize.bat        ← Windows Batch script (main)
│   ├── s24-restore.bat         ← Restore defaults for Windows
│   ├── s24-optimize.sh         ← Unix/Linux Shell script (main)
│   ├── s24-restore.sh          ← Restore defaults for Unix/Linux
│   └── s24-battery-optimizer.apk ← Native Android companion app (Kotlin)
├── android-app/                ← Source code for the companion app (Kotlin)
│   └── ...                     (Uses Shizuku for local ADB commands)
├── README.md                   ← English documentation (Default)
├── README-ES.md                ← Spanish documentation
├── README-IT.md                ← Italian documentation
└── README-PT.md                ← Portuguese documentation
```

---

## 📱 Android App (Shizuku)

The project includes a native Android application in Kotlin located in [android-app](file:///C:/Users/mdeangelis/Downloads/s24app/android-app). This app allows you to:
- Monitor and manage optimization status directly from your phone.
- Execute local ADB shell commands without connecting your device to a PC, leveraging the **Shizuku** application API.

---

## ❓ FAQ

**Will I lose data?** No. `pm disable-user` only hides apps. Run `s24-restore.bat` or `./s24-restore.sh` to get them back.

**Does Google Pay / Wallet work?** Yes, unless you run `pm clear` on GMS.

**Samsung Pay?** The script does NOT touch `spayfw`. If you disable it manually, Samsung Wallet won't work.

**Bixby doesn't work anymore?** Correct. If you need Bixby, remove the Bixby packages from the script.

**Hey Google / Gemini works?** Yes, preserved. The only disabled voice assistant is Bixby.

**Battery seems worse after reboot?** Normal. Android is recalibrating statistics. Give it 1-2 full charge cycles.

---

## 🤝 Contribute

PRs welcome. Ideas:
- Add models (S23, S22, S21, S20, Pixel)
- Improve the Shizuku Android app user interface

---

## 📜 License

MIT — use, modify, share.
