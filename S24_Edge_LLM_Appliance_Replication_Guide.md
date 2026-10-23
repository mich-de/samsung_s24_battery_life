# Samsung Galaxy S24 (Exynos 2400) Edge LLM Appliance: Replication Runbook

Questo documento contiene la guida tecnica completa, i comandi esatti e le configurazioni per replicare da zero l'intero setup su un secondo Samsung Galaxy S24 (SM-S921B, Exynos 2400 / GPU AMD Xclipse 940, 8 GB RAM, Android 16/17 One UI).

---

## 1. Architettura e Obiettivo
Convertire il dispositivo in un server locale autonomo per modelli linguistici (LLM on-device) conforme alle API OpenAI (`http://127.0.0.1:8080/v1/chat/completions`), massimizzando la RAM fisica libera (~4.4 - 4.8 GB liberi) e sfruttando l'accelerazione GPU OpenCL con zero consumo per servizi non necessari.

```
       +--------------------------------------------------------+
       |             Samsung Galaxy S24 (SM-S921B)              |
       |             Exynos 2400 · AMD Xclipse 940 GPU          |
       +--------------------------------------------------------+
                               |
            +------------------+------------------+
            |                                     |
   [ Edge AI Engine ]                    [ Zero-Bloat OS ]
   · Google Edge Gallery                 · 240 app rimosse
   · HostAI v1.3.0 (S24 Mod)             · 68 pacchetti congelati
   · LiteRT-LM (OpenCL GPU)              · Knox/NFC disattivati
   · Auto-Sync Daemon                    · RAM compattata (~4.5 GB free)
```

---

## 2. Prerequisiti & Setup Iniziale del Dispositivo

1. **Opzioni Sviluppatore**:
   - Impostazioni > Informazioni sul telefono > Informazioni software > tocca 7 volte su *Versione build*.
   - Abilita **Debug USB**.
   - Collega via cavo al PC e accetta il popup RSA del computer.
2. **Installazione Shizuku**:
   - Scarica e installa `Shizuku v13.6.0+` (`moe.shizuku.privileged.api`).
   - Avvia il demone via ADB:
     ```powershell
     $apk = (adb shell pm path moe.shizuku.privileged.api).Replace("package:","").Trim()
     $dir = $apk.Substring(0, $apk.LastIndexOf('/'))
     adb shell "$dir/lib/arm64/libshizuku.so --apk=$apk"
     ```

---

## 3. Fase 1: Deep Bloatware Purge (Disinstallazione & Freeze)

### A. Rimozione App Terze Parti (Spazio Utente)
Eseguire via PowerShell per rimuovere le app precaricate non essenziali per l'appliance:
```powershell
$appsToRemove = @(
    "com.facebook.orca", "com.facebook.katana", "com.microsoft.office.word",
    "com.microsoft.office.excel", "com.microsoft.office.powerpoint",
    "com.microsoft.office.outlook", "com.microsoft.skydrive",
    "com.spotify.music", "com.netflix.mediaclient", "com.linkedin.android",
    "com.amazon.mShop.android.shopping"
)
foreach ($app in $appsToRemove) {
    adb shell pm uninstall --user 0 $app
}
```

### B. Congelamento Servizi di Sistema Samsung & Google
Su One UI, le app di sistema (`/system`, `/product`) bloccano `pm uninstall` a livello Knox. Si congelano via `pm disable-user` combinato ad AppOps deny (zero processi in RAM, zero consumi):
```powershell
$systemBloat = @(
    # Bixby & Vision
    "com.samsung.android.bixby.agent", "com.samsung.android.bixby.wakeup",
    "com.samsung.android.visionintelligence", "com.samsung.android.bixbyvision.framework",
    # Samsung Cloud & Account Sync
    "com.samsung.android.scloud", "com.samsung.android.visual.cloudcore",
    # Pass & Wallet
    "com.samsung.android.samsungpass", "com.samsung.android.samsungpassautofill", "com.samsung.android.spayfw",
    # Samsung Duplicates
    "com.samsung.android.messaging", "com.sec.android.daemonapp", "com.sec.android.app.samsungapps",
    "com.samsung.android.app.routines", "com.samsung.android.app.sketchbook",
    # Telemetria & Diagnostica
    "com.sec.android.diagmonagent", "com.sec.spp.push", "com.samsung.android.rubin.app",
    "com.samsung.android.smartsuggestions", "com.samsung.android.scpm", "com.dti.tim",
    # Google Assistant & GMS Bloat
    "com.google.android.googlequicksearchbox", "com.google.android.apps.bard",
    "com.google.android.apps.tachyon", "com.google.android.apps.messaging",
    "com.google.android.apps.carrier.carrierwifi", "com.google.android.syncadapters.calendar",
    "com.google.android.apps.maps", "com.google.android.gm"
)

foreach ($pkg in $systemBloat) {
    adb shell pm disable-user --user 0 $pkg
    adb shell appops set $pkg RUN_ANY_IN_BACKGROUND deny
    adb shell appops set $pkg WAKE_LOCK deny
    adb shell am force-stop $pkg
}
```

### C. Disattivazione Knox Background Deamons (Non necessari per LLM)
```powershell
$knoxDeamons = @(
    "com.samsung.android.knox.analytics.uploader",
    "com.samsung.android.knox.attestation",
    "com.samsung.android.knox.zt.framework",
    "com.samsung.android.knox.kpecore"
)
foreach ($pkg in $knoxDeamons) {
    adb shell pm disable-user --user 0 $pkg
    adb shell appops set $pkg RUN_ANY_IN_BACKGROUND deny
    adb shell am force-stop $pkg
}
```

---

## 4. Fase 2: RAM & System Squeeze

1. **Disattivazione Hardware Non Usato**:
   ```powershell
   adb shell svc nfc disable
   adb shell settings put global ble_scan_always_enabled 0
   adb shell settings put secure voice_interaction_service '""'
   adb shell settings put secure assistant '""'
   ```
2. **Display Standby (Evita schermo sempre acceso sotto carica)**:
   ```powershell
   adb shell settings put global stay_on_while_plugged_in 0
   adb shell settings put system screen_off_timeout 30000
   ```
3. **Ottimizzazione ActivityManager (Priorita' assoluta a HostAI)**:
   ```powershell
   adb shell device_config put activity_manager max_cached_processes 2
   adb shell device_config put activity_manager max_phantom_processes 2
   adb shell device_config put activity_manager proactive_kills_enabled true
   adb shell cmd activity compact system
   ```

---

## 5. Fase 3: Storage Purge & Auto-Sync Modelli

1. **Pulizia Cestino & File Residui**:
   ```powershell
   adb shell "find /sdcard/Download -mindepth 1 -maxdepth 1 ! -name 'models' -exec rm -rf {} +"
   adb shell "rm -rf /sdcard/.trash-storage /sdcard/.Trash /sdcard/.trashed"
   adb shell "rm -f /sdcard/*.xml /sdcard/*.png /sdcard/*.txt"
   adb shell "rm -rf /sdcard/Music/.thumbnails /sdcard/Pictures/.thumbnails /sdcard/Movies/.thumbnails"
   adb shell pm trim-caches 999999999999
   adb shell sm fstrim
   ```
2. **Demone Auto-Sync da Edge Gallery a HostAI**:
   Crea lo script `/data/local/tmp/edge_model_sync.sh` sul dispositivo:
   ```bash
   #!/system/bin/sh
   SRC_DIR="/storage/emulated/0/Android/data/com.google.ai.edge.gallery/files"
   DST_DIR="/storage/emulated/0/Download/models"
   mkdir -p "$DST_DIR"

   while true; do
       if [ -d "$SRC_DIR" ]; then
           for model in $(find "$SRC_DIR" -type f -name "*.litertlm" 2>/dev/null); do
               fname=$(basename "$model")
               dst_file="$DST_DIR/$fname"
               if [ ! -f "$dst_file" ] || [ "$model" -nt "$dst_file" ]; then
                   cp "$model" "$dst_file"
                   chmod 664 "$dst_file"
               fi
           done
       fi
       sleep 10
   done
   ```
   Rendilo eseguibile e avvialo:
   ```powershell
   adb shell chmod 755 /data/local/tmp/edge_model_sync.sh
   adb shell "nohup /data/local/tmp/edge_model_sync.sh > /dev/null 2>&1 &"
   ```

---

## 6. Fase 4: HostAI S24 Custom Build & Deploy

Il pacchetto precompilato e' disponibile in `releases/hostai-s24-exynos.apk`.

### Modifiche Chiave del Codice Sorgente (`wannaphong/android-hostai`)
1. **Target ABI (`app/build.gradle.kts`)**:
   `ndk { abiFilters += listOf("arm64-v8a") }`
2. **GPU Acceleration (`SettingsManager.kt`)**:
   Default su `BACKEND_GPU` (OpenCL su GPU AMD Xclipse 940).
3. **Auto-Discovery Modelli (`ModelManager.kt`)**:
   Scansione automatica di `/storage/emulated/0/Download/models` con auto-selezione all'avvio.
4. **Permessi (`AndroidManifest.xml`)**:
   Aggiunta `MANAGE_EXTERNAL_STORAGE` ed `android:exported="true"` per `ApiServerService`.
5. **Zero-Scroll Obsidian Dark UI (`activity_main.xml` & `MainActivity.kt`)**:
   - Layout a schermata singola (senza scrolling per raggiungere Exit).
   - Telemetria live: GPU AMD Xclipse 940, RAM fisica libera, temperatura batteria.
   - Carosello di selezione rapida a 1 tocco (`Gemma 4 E2B`, `Embedding Gemma`, `TinyGarden`, `MobileActions`).
   - Sottotitolo: `(Recompiled for S24 OneUI 9)`
   - Crediti: `Original: Wannaphong Phatthiyaphaibun · Mod: mich-de`.

### Installazione & Configurazione
```powershell
adb install -r "releases/hostai-s24-exynos.apk"
adb shell appops set com.wannaphong.hostai MANAGE_EXTERNAL_STORAGE allow
adb shell pm grant com.wannaphong.hostai android.permission.POST_NOTIFICATIONS
adb shell cmd deviceidle whitelist +com.wannaphong.hostai
adb shell am start -n com.wannaphong.hostai/.MainActivity
```

---

## 7. Fase 5: Verifica Funzionale dell'API Server

1. **Port Forwarding**:
   ```powershell
   adb forward tcp:8080 tcp:8080
   ```
2. **Controllo Stato**:
   ```powershell
   curl.exe http://127.0.0.1:8080/health
   # Risposta attesa: {"status":"ok","model_loaded":true}
   ```
3. **Elenco Modelli Disponibili**:
   ```powershell
   curl.exe http://127.0.0.1:8080/v1/models
   ```
4. **Test di Inferenza (Compatibile OpenAI)**:
   ```powershell
   $body = '{"model":"gemma-4-E2B-it.litertlm","messages":[{"role":"user","content":"Hello!"}],"max_tokens":20}'
   [System.IO.File]::WriteAllText("$env:TEMP\prompt.json", $body, [System.Text.Encoding]::UTF8)
   curl.exe -X POST http://127.0.0.1:8080/v1/chat/completions -H "Content-Type: application/json" -d "@$env:TEMP\prompt.json"
   ```

---

## 8. Benchmark e Metriche di Riferimento su S24 Exynos
- **RAM Disponibile a riposo**: ~4.45 GB
- **RAM con Gemma 4 (2.4 GB) caricato su GPU**: ~2.85 GB liberi residui
- **Tempo di prima risposta (TTFT)**: ~1.2s - 1.7s
- **Temperatura Batteria**: 26°C - 29°C (profilo termico freddo)
- **Spazio UFS 4.0 disponibile**: ~200 GB
