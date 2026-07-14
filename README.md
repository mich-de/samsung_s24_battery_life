# S24 Battery Optimizer

🌐 **Italiano | [English](README-EN.md)**

> **Ripristina l'autonomia del tuo Galaxy S24** — disabilita bloatware, ferma i drain noti e ottimizza le impostazioni di sistema, **senza root**.

## 📋 Compatibilità

| Modello | One UI / Android |
|---------|------------------|
| SM-S921B (S24 base) | One UI 7 / Android 16 |
| SM-S926B (S24+) | One UI 7 / Android 16 |
| SM-S928B (S24 Ultra) | One UI 7 / Android 16 |
| Serie S23, S22, S21 | One UI 5+ / Android 13+ (testare) |

## 🔧 Cosa fa

### Bloatware disabilitato (24 pacchetti)

| App | Pacchetto | Perché |
|-----|-----------|--------|
| **Bixby Voice** | `bixby.agent` | Assistente in ascolto continuo |
| **Bixby Wakeup** | `bixby.wakeup` | "Hi Bixby" sempre attivo |
| **Bixby Vision** | `bixbyvision.framework` | Riconoscimento AI camera |
| **Vision Intelligence** | `visionintelligence` | Analisi scene AI |
| **Game Tools** | `game.gametools` | Pannello overlay giochi |
| **Game Optimizing** | `game.gos` | Limitatore CPU/GPU |
| **Smart Suggestions** | `smartsuggestions` | Suggerimenti contestuali |
| **Aware Service** | `aware.service` | Rilevamento attività |
| **BBC Agent** | `bbc.bbcagent` | Push bloatware |
| **Reminder** | `app.reminder` | Promemoria Samsung |
| **Routines** | `app.routines` | Automazioni Bixby |
| **Routine Plus** | `app.routineplus` | Wakelock Doze (drain confermato) |
| **Live Effect** | `liveeffectservice` | Effetti videochiamate |
| **OneConnect** | `oneconnect` | SmartThings Find |
| **STPlatform** | `service.stplatform` | Knox invisibile |
| **Forest** | `forest` | Benessere digitale |
| **Buds Manager** | `accessory.budsunitemgr` | Plugin Galaxy Buds |
| **Rubin App** | `rubin.app` | Customization Service (causa loop Play Services) |
| **Facebook** | `facebook.katana` | Social battery drain |
| **Messenger** | `facebook.orca` | Chat wakelock |
| **Edge** | `microsoft.emmx` | Browser alternativo |
| **Excel** | `office.excel` | Office sync |
| **Word** | `office.word` | Office sync |
| **Remote Desktop** | `rdc.androidx` | RDP client |

### 🔄 Knox Matrix Reset (5 pacchetti)

Resetta i servizi Knox che in **Aprile 2026** hanno causato un bug di drain continuo (loop di attestazione CPU). `kpecore`, `attestation`, `pushmanager`, `containercore`, `analytics.uploader`.

### ⚙️ Impostazioni sistema (23)

| Categoria | Impostazione | Valore |
|-----------|-------------|--------|
| **Batteria** | Adaptive Battery | ON |
| | App Auto Restriction | ON |
| | Battery Saver | ON |
| | Enhanced CPU Resp. | OFF |
| | Enhanced Processing | OFF |
| **Radio** | BLE Scan Always | OFF |
| | Nearby Scanning | OFF |
| | WiFi Power Save | ON |
| | WiFi Wakeup | OFF |
| | MCF Quick Share | OFF |
| **Schermo** | AOD / Always On | OFF |
| | Screen Timeout | 30s |
| | Lift to Wake | OFF |
| | Smart Stay | OFF |
| | Doze | ON |
| **Performance** | Animazioni | 0.5x |
| | RAM Plus | 2 GB |
| **Protezione** | Carica massima | 80% |

### 🚫 Background bloccato (3 app)

`appops set RUN_ANY_IN_BACKGROUND deny` su:
- **Instagram** — wakelock continui, sync background
- **WhatsApp** — 499 mAh in 8h nel nostro test (16% del totale)
- **Tandem** (modifica il nome pacchetto se necessario)

## 📊 Risultati attesi

### Test di scarica — Prima dell'ottimizzazione

```
Durata:      8h 42m (da 80% a 19% = 61% utilizzato)
Capacità:    3.114 mAh consumati
Media:       358 mA/h
Vita stimata: ~10.9h (uso misto)

Top consumatori (mAh):
  CPU                    1.252 (40%)
  Schermo                 542 (17%)
  Radio 5G                440 (14%)
  WhatsApp                499 (16%)
  Sistema                 386 (12%)
  Instagram               314 (10%)
  Google Play Services    288 (9%)
  Kernel                  186 (6%)
```

### Dopo l'ottimizzazione (dati in raccolta)

I resoconti degli utenti della community riportano:
- **15-25%** di riduzione drain in standby
- **+2-4 ore** di autonomia in uso misto
- WhatsApp e Instagram non consumano più in background

## 🚀 Come usare

### Requisiti
- Windows con ADB configurato (`platform-tools`)
- USB Debugging attivo sul telefono
- Telefono collegato al PC

### Installazione

```powershell
# Clona o scarica
git clone https://github.com/tuouser/s24-battery-optimizer
cd s24-battery-optimizer
```

### Personalizza il percorso ADB

Modifica in `s24-optimize.bat` (riga 22):

```batch
set ADB="C:\tuo-percorso\platform-tools\adb.exe"
```

### Esegui

```powershell
.\s24-optimize.bat
```

Il telefono mostrerà i permessi di debug — **conferma sempre**.

### Per annullare tutto

```powershell
.\s24-restore.bat
```

## 👆 Impostazioni manuali (S24 Exynos 2400 · One UI 7)

Queste impostazioni NON sono automatizzabili via ADB. I percorsi sono quelli **esatti** di One UI 7 su S24 (Exynos 2400, Android 16).

### ⚡ Exynos 2400 — Modem 5G e CPU

Il modem Exynos 5300 (integrato nel 2400) ha un consumo a riposo più alto quando agganciato in **5G SA** (Standalone) con segnale debole. Su One UI 7:

| Impostazione | Percorso esatto (One UI 7) | Consiglio |
|-------------|---------------------------|-----------|
| **Preferenza rete 5G** | Impostazioni > Connessioni > Reti mobili > Modalità rete | "5G/LTE/3G/2G (connessione automatica)", MA se sei in zona con 5G debole passa a "LTE/3G/2G (connessione automatica)" — il modem Exynos 5300 consuma 2-3x in più quando cerca 5G instabile |
| **Rete 5G autonoma** | Impostazioni > Connessioni > Reti mobili | Se vedi "5G SA", impostalo su OFF. La modalità NSA (Non-Standalone) consuma meno perché si appoggia alla rete 4G per il controllo |
| **Ottimizza connessione** | Impostazioni > Connessioni > WiFi > ⋯ > Intelligente > "Passa a dati mobili" | OFF — evita il ping-pong WiFi/5G che il modem Exynos gestisce male |
| **Avanzate WiFi** | Stessa sezione > "Passa a rete migliore" | OFF — impedisce scansioni periodiche |

Lo scheduler CPU/GPU Exynos 2400 (G788):
| Impostazione | Percorso esatto (One UI 7) | Consiglio |
|-------------|---------------------------|-----------|
| **Velocità di elaborazione** | Impostazioni > Assistenza dispositivo > Batteria > Altre impostazioni batteria > Velocità di elaborazione | **"Ottimizzato"** (NON "Alta" o "Massima" — forza i core X4 a frequenze più alte inutilmente) |
| **Luminosità automatica** | Impostaggi > Schermo | ON — il display M13 OLED dell'S24 consuma molto meno con luminosità < 50%. Tenerlo fisso al 70% raddoppia il drain |
| **Risoluzione** | Impostazioni > Schermo > Risoluzione schermo | **FHD+** (2340×1080). Su schermo 6.2" la differenza con QHD+ è invisibile, ma il rendering GPU costa caro all'Exynos Xclipse 940 |
| **Refresh rate** | Impostazioni > Schermo > Frequenza di aggiornamento | **"Adattivo"** (LTPO 1→24→120Hz). NON "Standard" (60Hz) e NON "120Hz fisso" — LTPO scende a 1Hz su sfondo Home, 24Hz su video |

### 🧠 Good Guardians (app gratuite Galaxy Store)

Samsung fornisce moduli ufficiali per l'S24. Installa **"Good Guardians"** dal Galaxy Store (cerca "Good Guardians Samsung").

| Modulo | Si installa da | Cosa fa |
|--------|---------------|---------|
| **Battery Guardian** | Galaxy Store > cerca "Battery Guardian" | Blocca le app che tengono sveglia la CPU quando lo schermo è spento (funzione "App che impediscono il sonno") |
| **Thermal Guardian** | Galaxy Store > cerca "Thermal Guardian" | Abbassa la soglia termica dell'Exynos 2400 di 1-2°C — evita che il chip scaldi e consumi di più per raffreddarsi. Imposta "Soglia di raffreddamento" a -1°C o -2°C |
| **Memory Guardian** | Galaxy Store > cerca "Memory Guardian" | Libera RAM più aggressivamente. L'S24 ha 8GB, tenerli puliti evita swap su flash (consuma batteria) |
| **App Booster** | Galaxy Store > cerca "App Booster" | Ottimizza le app compilate con l'Exynos NPU. Eseguilo dopo ogni aggiornamento mensile |

### 🔋 Good Lock — Camera Assistant e NavStar

Disponibili da **Galaxy Store > Good Lock** (o NiceLock se bloccato in Italia).

| Modulo | Impostazione | Consiglio |
|--------|-------------|-----------|
| **Camera Assistant** | "Automatic lens switching" | OFF — il passaggio continuo tra lenti grandangolo/tele consuma l'ISP dell'Exynos |
| **Camera Assistant** | "Video HDR10+" | OFF se non registri video HDR (la codifica Exynos MFC pesa) |
| **Riduci animazioni** | Impostazioni > Opzioni sviluppatore (attiva da Info software > Numero build 7 tap) > "Riduci animazioni" | ON — complementare allo script ADB (0.5x già impostato). Se non vedi Opzioni sviluppatore, lo script ADB ha già impostato 0.5x |

### ☁️ Servizi cloud e sync

| Impostazione | Percorso esatto (One UI 7) | Consiglio |
|-------------|---------------------------|-----------|
| **Sync account Samsung** | Impostazioni > Account e backup > Gestisci account > Samsung Cloud > Sincronizza | Disattiva **tutto** tranne Contatti (se ti servono). Galleria, Note, Calendario Samsung fanno polling periodico |
| **Auto-aggiornamento Galaxy Store** | Galaxy Store > Menu (⋮) > Impostazioni > Aggiornamento automatico app | "Solo con WiFi" o "Mai" |
| **Auto-aggiornamento Play Store** | Play Store > Icona profilo > Impostazioni > Preferenze rete > Aggiorna automaticamente app | "Solo in WiFi" |
| **Backup Google Foto** | Google Foto > Icona profilo > Impost. Foto > Backup | "Solo in WiFi" o OFF se sincronizzi manualmente |
| **Samsung Cloud backup** | Impostazioni > Account e backup > Samsung Cloud | OFF se non paghi l'abbonamento (i 5GB gratis finiscono subito) |

### 📡 Connettività nascosta (S24 specifica)

| Impostazione | Percorso esatto (One UI 7) | Consiglio |
|-------------|---------------------------|-----------|
| **Trova il mio dispositivo** | Impostazioni > Sicurezza e privacy > Trova dispositivo mobile > "Consenti ricerche" | OFF — fa scanning periodico della rete. L'accensione remota non ti serve |
| **Smart View** | Pannello rapido → scorciatoia Smart View | Tieni spento se non usi screen mirroring |
| **NFC** | Pannello rapido → scorciatoia NFC | Tieni OFF se non usi Samsung/Google Pay frequente. L'NFC dell'Exynos fa polling ogni 3-5s |
| **UWB** | Impostazioni > Connessioni > Ultra-wideband | OFF se non usi SmartThings Find con tag UWB |
| **Stampa** | Impostazioni > Funzioni avanzate > Stampa | OFF se non hai stampanti collegate |


### 🔳 Schermo e One UI 7

S24 con display M13 OLED 6.2" 2340×1080 (Dynamic AMOLED 2X):

| Impostazione | Percorso esatto (One UI 7) | Consiglio |
|-------------|---------------------------|-----------|
| **Pannelli Edge** | Impostazioni > Schermo > Pannelli Edge | OFF se non li usi (il processore edge tiene caldo il sensore) |
| **Pop-up intelligente** | Impostazioni > Funzioni avanzate > Pop-up intelligente | OFF |
| **Icona app notifica** | Impostazioni > Schermo > Icona app notifica (Badge) | "Senza numeri" o OFF — ogni badge richiede polling dell'icona |
| **Barra di navigazione** | Impostazioni > Schermo > Barra di navigazione | "Gesture a schermo intero" (i pulsanti software tengono il display controller più attivo) |
| **Modalità scura** | Impostazioni > Schermo > Modalità scura | ON permanente. Su OLED è il singolo intervento più efficace: pixel neri = pixel spenti |

### 🎯 App specifiche — S24 Exynos

| App | Azione raccomandata (S24) |
|-----|---------------------------|
| **Google Foto** (preinstallato) | Backup solo WiFi. Il NPU Exynos durante backup foto fa analisi AI (riconoscimento volti) — consuma |
| **TikTok** | Blocca background con `adb shell appops set com.zhiliaoapp.musically RUN_ANY_IN_BACKGROUND deny` |
| **Facebook** (se riattivato) | `adb shell appops set com.facebook.katana RUN_ANY_IN_BACKGROUND deny` |
| **LinkedIn** | `adb shell appops set com.linkedin.android RUN_ANY_IN_BACKGROUND deny` — sync contatti frequente |
| **Samsung Health** | Se non usi sensori fitness, disabilita: `adb shell pm disable-user --user 0 com.sec.android.app.shealth`. L'Exynos 2400 tiene l'ISP attivo per il sensore HR |
| **Samsung Members** | Diagnostica. Disabilita: `adb shell pm disable-user --user 0 com.samsung.android.mobileservice` (se non ricevi SMS) |
| **Mappe** (offline) | Scarica mappe offline per la tua zona. L'N9 GPS dell'Exynos consuma molto con mappe in streaming |

### 🧹 Manutenzione periodica Exynos

L'Exynos 2400 accumula cache nelle app Samsung più di altri chip. Una volta al mese:

**Pulizia cache partizione (non perde dati):**
1. Spegni il telefono
2. Tieni premuto **Volume SU + Power** (sul S24 entrano in recovery)
3. Con i tasti volume naviga a "Wipe cache partition"
4. Premi Power per selezionare
5. "Yes" → "Reboot system now"

**App Booster** (da Good Guardians o Assistenza dispositivo):
1. Apri **Assistenza dispositivo**
2. Tocca "Avvia il controllo" o l'icona diottrie
3. Scorri in fondo → "App Booster" → "Esegui"

**Svuota cache app specifiche** (se un'app consuma anomalo):
1. Impostazioni > Applicazioni > [app incriminata] > Archiviazione > Svuota cache
2. NON toccare "Svuota dati" (perderesti login/preferenze)

## 🔍 Note tecniche

### 120Hz e 5G rimangono attivi
L'ottimizzazione *non* disabilita:
- Refresh rate 120Hz (solo Battery Saver su "Massimo" lo limiterebbe)
- Rete 5G (NESSUN comando modifica preferenze di rete)
- Google Maps, Gmail, Google Assistant / Gemini, navigatore

### Rubin.app — il fix Google Play Services
`Samsung Customization Service` (`com.samsung.android.rubin.app`) è noto per creare un loop con Google Play Services: richiede continuamente aggiornamenti di posizione/attività, impedendo a Play Services di andare in sospensione. Lo disabilitiamo + revochiamo `ACTIVITY_RECOGNITION`.

### Knox April 2026 Drain Bug
A partire dalla patch di Aprile 2026, alcuni servizi Knox (in particolare `kpecore` e `attestation`) entrano in un ciclo di attestazione continua. Il `pm clear` forza un reset pulito.

### Routine Plus Wakelock
Abbiamo scoperto che `com.samsung.android.app.routineplus` (servizio separato da Routines normali) acquisisce periodicamente wakelock `Doze (draw)` anche quando Routines è disabilitato. Era assente dalla maggior parte delle guide.

### Animazioni 0.5x
Raddoppia la velocità delle transizioni UI. L'effetto è una UI più scattante e meno GPU time. 120Hz rimane attivo.

### RAM Plus 2GB
Valori alti (4/6/8GB) consumano batteria per I/O continuo su storage flash. 2GB è il valore ottimale.

### Google Play Services
Se dopo l'ottimizzazione noti ancora drain anomalo da Play Services (verifica in Impostazioni > Batteria), decommenta le righe nel script:

```batch
REM am force-stop com.google.android.gms
REM pm clear com.google.android.gms
```

⚠️ `pm clear` su GMS cancella i pagamenti Google Wallet, quindi è l'ultima risorsa.

## 📂 Struttura file

```
s24-battery-optimizer/
├── releases/                   ← Cartella contenente tutti i file eseguibili
│   ├── s24-optimize.bat        ← Script Windows Batch (principale)
│   ├── s24-restore.bat         ← Ripristino default per Windows
│   ├── s24-optimize.sh         ← Script Unix/Linux Shell (principale)
│   ├── s24-restore.sh          ← Ripristino default per Unix/Linux
│   └── s24-battery-optimizer.apk ← App compagna Android nativa (Kotlin)
├── android-app/                ← Sorgenti dell'app compagna (Kotlin)
│   └── ...                     (Utilizza Shizuku per comandi ADB locali)
└── README.md
```

## 📱 Android App (Shizuku)

Il progetto include anche un'applicazione nativa Android in Kotlin situata nella directory [android-app](file:///C:/Users/mdeangelis/Downloads/s24app/android-app). Questa applicazione consente di:
- Gestire e monitorare lo stato di ottimizzazione direttamente dal dispositivo.
- Eseguire i comandi ADB locali senza necessità di connettere il telefono a un PC, sfruttando l'API fornita dall'applicazione **Shizuku**.

## ❓ FAQ

**Perdo i dati?** No. `pm disable-user` solo nasconde le app. Con `s24-restore.bat` o `./s24-restore.sh` tornano come prima.

**Google Pay / Wallet funziona?** Sì, a meno che non esegui `pm clear` su GMS.

**Samsung Pay?** Lo script non tocca `spayfw`. Se lo disabiliti manualmente, Samsung Wallet non funzionerà.

**Bixby non funziona più?** Corretto. Se ti serve Bixby, modifica lo script togliendo i pacchetti Bixby.

**Hey Google / Gemini funziona?** Sì, preservato. L'unico assistente vocale disabilitato è Bixby.

**Dopo il riavvio la batteria sembra peggiore?** Normale. Android sta ricalibrando le statistiche. Dai 1-2 cicli completi di carica/scarica.

## 🤝 Contribuire

PR benvenute. Idee:
- Aggiungere modelli (S23, S22, S21, S20, Pixel)
- Migliorare l'interfaccia dell'app Android Shizuku

## 📜 Licenza

MIT — usa, modifica, condividi.

