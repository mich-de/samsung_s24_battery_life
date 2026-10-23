# Samsung Galaxy S24 (SM-S921B) — Report Stato Post-Patch (5 Agosto 2026)

**Data Rilievo**: 3 Settembre 2026 — Ore 12:21  
**Dispositivo**: Samsung Galaxy S24 Base (`SM-S921B`, Serial: `RFCX30T8WBL`)  
**SoC**: Samsung Exynos 2400 (`s5e9945`)  
**Nuovo Firmware Attivo**: `BP4A.251205.006.S921BXXSHDZH3`  
**Livello Patch di Sicurezza Installato**: **`2026-08-05`** *(aggiornato da `2026-06-05`)*  
**Uptime Dispositivo**: ~1h 14m (Riavvio post-aggiornamento avvenuto alle ~11:07)  

---

## 1. Esito Installazione Firmware e Stato Sistema

L'aggiornamento della patch di sicurezza di agosto 2026 è stato applicato con successo senza errori di partizione o bootloop.

| Proprietà di Sistema | Valore Rilevato Pre-Patch | Valore Attuale Post-Patch | Esito |
|---|---|---|---|
| **Security Patch Level** | `2026-06-05` | **`2026-08-05`** | ✅ Aggiornato con successo |
| **Build Display ID** | N/A | `BP4A.251205.006.S921BXXSHDZH3` | ✅ Release ufficiale Samsung |
| **Versione Android / SDK** | 16 (SDK 36) | 16 (SDK 36) | Invariata |
| **Uptime Post-Reboot** | N/A | `up 1:14` | Sistema operativo avviato e stabile |
| **Carico CPU (Load Average)** | N/A | `~15.4` (transitorio) | ⚠️ Attività post-OTA in corso |

> [!NOTE]
> Il carico di sistema elevato (`load average ~15.4`) rilevato nell'ora successiva al riavvio è fisiologico: il servizio `android/com.android.server.art.BackgroundDexoptJobService` e i moduli di indicizzazione di sistema (Google Play Services, Samsung Media Storage) stanno completando la ricompilazione DEX/AOT post-firmware.

---

## 2. Stato della Batteria Post-Aggiornamento

Il dispositivo è stato rilevato collegato alla carica di mantenimento:

| Parametro | Valore Live Rilevato | Note |
|---|---|---|
| **Percentuale Carica** | **100%** | In carica AC (`status: 5 - FULL`) |
| **Tensione Cella** | 4.358 mV | Livello nominale a piena carica |
| **Temperatura Batteria** | **27.0°C** | Temperatura termica eccellente |
| **Stato di Salute (BSOH)** | **98%** | Backup chip fuel-gauge |
| **Capacità Appresa (cc)** | 3.897 mAh | Allineata alla baseline pre-patch (3.900 mAh) |
| **Protect Battery Mode** | **3** (Modalità Base) | Tetto 100% con rilascio diurno corretto |
| **Threshold Memorizzata** | **80%** | Configurazione per la fascia notturna |

---

## 3. Verifica Persistenza Tweak & Ottimizzazioni (Audit Live)

È stato condotto un audit approfondito tramite ADB per verificare se l'aggiornamento Samsung avesse ripristinato configurazioni predefinite:

### 3.1 Bloatware Disabilitati (87 pacchetti totali)
* **Stato**: ✅ **TUTTI I PACCHETTI DISABILITATI SONO STATI PRESERVATI**.
* Nessuno dei 26 pacchetti Samsung (Bixby, Rubin, GOS, DeX, AirCommand) né delle app partner (Facebook Katana/Orca, Microsoft Office) è stato riattivato dal processo di OTA.

### 3.2 Restrizioni Instagram (`com.instagram.android`)
* **NetPolicy Blacklist**: ✅ **Preservata**. L'UID `10510` risulta ancora presente in `restrict-background-blacklist`.
* **AppOps Background**: ✅ **Preservata**. `RUN_ANY_IN_BACKGROUND: deny`.
* **Processo**: Al momento del controllo l'app non è in esecuzione in background (`0` processi attivi).

### 3.3 Shizuku Framework & Watchdog Locale
* **Server Privilegiato**: ✅ **Attivo**. Processo `shizuku_server` rilevato regolarmente in esecuzione sotto `uid shell` (PID: `19346`).
* **Watchdog Locale (`/data/local/tmp/shizuku_watchdog.sh`)**:
  * *Rilevamento post-reboot*: Come previsto, il demone script non era attivo subito dopo il boot del sistema operativo.
  * *Azione eseguita*: **Riavviato con successo in background** (PID: `9139`, log confermato alle 12:21:41).

### 3.4 Companion App `s24optimizer` & Programma Ricarica
* **Stato App**: ✅ **In esecuzione** (PID: `17297`).
* **Allarmi Registrati in AlarmManager**:
  * `com.s24optimizer.action.CHARGE_SCHEDULE_TICK`: ✅ **Armato con `RTC_WAKEUP` esatto** (gestirà la transizione al tetto dell'80% alle 19:00).
  * `com.s24optimizer.action.RESTART_SCHEDULE_TICK`: ✅ **Armato con `RTC_WAKEUP`** (schedulato per le 02:00 del mattino).

### 3.5 Doze Whitelist (Lista Esclusioni Risparmio Energetico)
* **Anomalia rilevata**: `com.huawei.health` (UID: `10497`) risulta nuovamente inclusa nella lista bianca utente (`user,com.huawei.health`), segno che l'app o il servizio Bluetooth ne hanno forzato il reinserimento al primo boot.
* **Altre app in whitelist**: `com.whatsapp`, `com.s24optimizer`, `com.azure.authenticator`, `it.pixelsync`.

---

## 4. Monitoraggio Anomalie Pre-Patch

| Anomalia Pre-Patch | Comportamento Attuale Post-Patch | Verdetto Preliminare |
|---|---|---|
| **WhatsApp AudioTrack Leak** | Processo `com.whatsapp` attivo (PID: `9051`), ma **nessuna traccia AudioTrack aperta** in `media.audio_flinger`. | Normale. Da ricontrollare durante la notte. |
| **Instagram Background Radio Drain** | Nessun processo attivo. Blocco dati mobili e AppOps `deny` operativi. | Sotto controllo. |
| **Surriscaldamento / Thermal Throttling** | Cella a 27.0°C dopo 1h dal completamento dell'installazione. | Ottimo comportamento termico. |

---

## 5. Prossimi Passi Consigliati

1. **Lasciare completare la finestra di 24–48 ore**: Evitare di valutare i consumi oggi, poiché la compilazione ART in background e l'aggiornamento dei cataloghi di sistema aumenteranno transitoriamente i risvegli della CPU.
2. **Rimozione Huawei Health da Doze (opzionale)**: Se non strettamente necessario per le notifiche dello smartwatch in tempo reale a display spento, rimuoverla nuovamente con:
   ```bash
   adb shell cmd deviceidle whitelist -com.huawei.health
   ```
3. **Verifica passaggio Notturno (Ore 19:00)**: Monitorare alle 19:00 che l'allarme di `s24optimizer` imposti correttamente `protect_battery` a `1` (Massima / Tetto 80%).
4. **Nuova Baseline di Scarica**: Eseguire una misurazione della scarica notturna tra 24–48 ore per confrontarla con i `59.2 mAh/h` pre-patch.
