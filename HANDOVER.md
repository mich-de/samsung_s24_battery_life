# HANDOVER // HOSTAI & S24 TECHNICAL MINIMAL APPLIANCE

**Data aggiornamento**: 2026-10-25  
**Target Hardware**: Samsung Galaxy S24 (SM-S921B) • Exynos 2400 Deca-Core • GPU AMD Xclipse 940 (RDNA3) • 8 GB LPDDR5X • Android 14 (One UI 6.1)  
**Target Device ID**: `RFCX30T8WBL`  
**Git Repository**: `https://github.com/mich-de/samsung_s24_battery_life` (Branch: `main`)  
**Design System**: **Technical Minimal** (Nothing OS / Teenage Engineering inspired: Doto, Geist, Geist Mono, palette `#0E0E0E`, `#151515`, accenti arancioni `#FF5A1F`, bordi hairline 1px `#262626`, angoli sharp 4dp/6dp/8dp).

---

## 1. Stato Attuale dei Componenti

### A. HostAI Android Native App (`com.wannaphong.hostai`)
- **Versione**: v1.3.0 Technical Minimal Edition.
- **APK Rilasciato**: `releases/hostai-s24-exynos.apk` (firmato con debug keystore).
- **Interfaccia Nativa Ridisegnata**:
  - `MainActivity`: Header `HOSTAI.` dot-matrix, telemetry chip live `[Xclipse 940 | RAM Free | Temp]`, pulsanti action squadrati: `[ START INFERENCE ENGINE ]`, `[ WEB CHAT /chat ]`, `[ MODELS ]`, `[ CONSOLE ]`, `[ SESSIONS ]`, `[ CONFIG ]`.
  - `ModelManagementActivity` (`activity_model_management.xml` + `item_model.xml`): Header `// MODEL REPOSITORY`, chip telemetrico `[ 0 MODELS DETECTED ]`, FAB squadrato `+ IMPORT MODEL` sollevato a 90dp (sopra la navigation bar di sistema), card modelli sharp con pulsanti `[ VIEW METADATA ]` e `[ PURGE ]`.
  - `StoredCompletionsActivity` (`activity_stored_completions.xml` + `item_stored_completion.xml`): `// SESSION ARCHIVE`, pulsanti inferiori sollevati a 42dp `[ REFRESH ]`, `[ EXPORT ]`, `[ PURGE ]`.
  - `LogViewerActivity` (`activity_log_viewer.xml`): `// SYSTEM TELEMETRY CONSOLE`, terminal log monospace 11.5sp, pulsanti inferiori sollevati a 42dp `[ CLEAR ]`, `[ REFRESH ]`, `[ COPY ALL ]`.
  - `SettingsActivity` (`activity_settings.xml`): `// ENGINE CONFIGURATION`, card sharp 8dp per porta (`8080`), API key, concorrenza e max context.
  - `AndroidManifest.xml`: Tutte le activity secondarie esportate (`android:exported="true"`) per consentire navigazione e test diretti.

### B. HostAI Web Chat (Porta 8080 / `/chat`)
- **Percorso Live**: `/sdcard/Download/index.html` (servito con priorità da Javalin prima degli assets interni) e copia repository `hostai_chat_web.html`.
- **Caratteristiche UI / UX**:
  - Boot splash screen dot-matrix 5x7 animato (`HOSTAI.`).
  - Font Google Doto + Geist + Geist Mono caricati.
  - Sostituiti tutti i glifi Unicode (`☰`, `⚙`, `⚡`, `✕`) con **SVG vettoriali inline nitidi** a 1px (`stroke-width="1.8"`/`"2"`), eliminando i punti interrogativi tofu.
  - Risolto testo latenza: ora mostra chiaramente `READY` e `[latency] ms`.
  - Ottimizzazione mobile portrait per Galaxy S24: drawer sidebar a scomparsa con backdrop blurrato, viewport messaggi espanso al 95% della larghezza, dock input sottile con safe-area-inset per gesture bar.
  - Streaming SSE nativo collegato a `/v1/chat/completions` e selettore dinamico modelli `/v1/models`.

### C. OpenNovaLauncher (`com.michde.opennovalauncher`)
- **Versione**: v2.0 Technical Minimal Edition.
- **APK Rilasciato**: `releases/opennovalauncher-s24.apk`.
- **Engine Icone**: `TechnicalMinimalIconEngine.kt`:
  - 15 vettori handcrafted dedicati per app principali (HostAI, Settings, Fotocamera, Galleria, Browser, Terminale, ecc.).
  - Generatore dinamico badge Nothing-style: disco scuro `#151515`, bordo 1.5dp `#2E2E2E`, glifo desaturato ad alto contrasto, punto d'accento arancione `#FF5A1F`, cache LRU.
- **Gestione Sfondi**:
  - Supporto per impostare sia Homescreen (`FLAG_SYSTEM`) sia Lockscreen (`FLAG_LOCK`) tramite dialog di configurazione e Intent (`--es action set_all_wallpapers`).

### D. Sfondi Schermo (Homescreen & Lockscreen)
- **Homescreen**: `s24_technical_minimal_wallpaper.png` (1080x2340).
- **Lockscreen**: `s24_technical_minimal_lockscreen.png` (1080x2340):
  - Reticolo radar concentrico allineato al sensore d'impronte ultrasonico S24 (`[ TOUCH TO DECRYPT & UNLOCK ]`).
  - Frame telemetrico hardware per l'orologio One UI.
  - Hero centrale `HOSTAI.CORE.01` con anelli target e specifiche hardware.

### E. S24 Optimizer App (`com.s24optimizer`)
- **Versione**: v2.6 Technical Minimal Edition.
- **APK Rilasciato**: `releases/s24optimizer-v2.6.apk`.
- **Nuova Sezione HostAI Appliance (`HostAiScreen.kt`)**:
  - Scheda telemetrica live: GPU Xclipse 940 (Vulkan), NPU 17K MAC Dual, RAM disponibile.
  - Pulsante `[ 01 ] TRASFORMA IN APPLIANCE HOSTAI`: chiusura processi pesanti background (`am kill-all`), compilazione AOT speed (`cmd package compile -m speed com.wannaphong.hostai`), avvio motore.
  - Pulsante `[ 02 ] APRI WEB CHAT /chat (:8080)`.
  - Catalogo modelli on-device raccomandati per S24 (Llama 3.2 3B, Gemma 2 2B, Qwen 2.5 Coder 1.5B).
- **Stato Build**: Aggiornato ad AGP `9.3.1`, rimossi proxy non raggiungibili da `gradle.properties`.

### F. Ottimizzazione Sistema & RAM Galaxy S24
- **Dicio Assistant rimosso**: Disinstallato `org.stypox.dicio` con recupero immediato di **~472 MB - 616 MB di RAM** in background.
- **Samsung Galleria riabilitata**: `com.sec.android.gallery3d` riattivata (`pm enable`).
- **Sfondo e Stile riabilitato**: `com.samsung.android.app.dressroom` riattivato (`pm enable`).
- **RAM Libera Live**: **~3.8 GB disponibili** per inferenza LLM locale.

---

## 2. Percorsi File Chiave & Toolchain

| Componente | Percorso File |
|---|---|
| **HostAI APK Rebuilt** | `releases/hostai-s24-exynos.apk` |
| **OpenNova Launcher APK** | `releases/opennovalauncher-s24.apk` |
| **S24 Optimizer APK** | `releases/s24optimizer-v2.6.apk` |
| **HostAI Web Chat HTML** | `hostai_chat_web.html` (e `/sdcard/Download/index.html`) |
| **Homescreen Wallpaper** | `s24_technical_minimal_wallpaper.png` |
| **Lockscreen Wallpaper** | `s24_technical_minimal_lockscreen.png` |
| **Apktool Jar** | `/home/michde/.gemini/antigravity-ide/brain/bec4bbe5-ed34-427e-a386-d0e3bd13d2a5/scratch/apktool.jar` |
| **Android Build Tools 35** | `/home/michde/Android/Sdk/build-tools/35.0.0/` (`zipalign`, `apksigner`) |
| **Java JDK 17** | `/home/michde/Documenti/snav-app/scratch/jdk/jdk-17.0.19+10/bin/java` |

---

## 3. Comandi Rapidi di Manutenzione

### Ricompilazione e firma manuale HostAI APK:
```bash
JAVA_HOME=/home/michde/Documenti/snav-app/scratch/jdk/jdk-17.0.19+10 \
$JAVA_HOME/bin/java -jar /home/michde/.gemini/antigravity-ide/brain/bec4bbe5-ed34-427e-a386-d0e3bd13d2a5/scratch/apktool.jar b /tmp/hostai_decompiled -o /tmp/hostai_rebuilt.apk

/home/michde/Android/Sdk/build-tools/35.0.0/zipalign -p -f -v 4 /tmp/hostai_rebuilt.apk /tmp/hostai_aligned.apk
/home/michde/Android/Sdk/build-tools/35.0.0/apksigner sign --ks ~/.android/debug.keystore --ks-pass pass:android --key-pass pass:android --out releases/hostai-s24-exynos.apk /tmp/hostai_aligned.apk
adb -s RFCX30T8WBL install -r releases/hostai-s24-exynos.apk
```

### Aggiornamento Web Chat su dispositivo:
```bash
adb -s RFCX30T8WBL push hostai_chat_web.html /sdcard/Download/index.html
```

### Compilazione e installazione S24 Optimizer:
```bash
cd android-app
JAVA_HOME=/home/michde/Documenti/snav-app/scratch/jdk/jdk-17.0.19+10 ./gradlew assembleDebug
adb -s RFCX30T8WBL install -r app/build/outputs/apk/debug/app-debug.apk
cp app/build/outputs/apk/debug/app-debug.apk ../releases/s24optimizer-v2.6.apk
```

---

## 4. Stato Git
Tutte le modifiche, gli asset SVG/PNG, i file sorgente e i binari APK aggiornati sono sincronizzati e pushati su `origin/main` fino al commit `38e4d0c`.
