# Samsung Galaxy S24 (SM-S921B) — Report Stato Pre-Patch (5 Agosto 2026)

**Data Rilievo**: Baseline consolidata al 12 Agosto / 3 Settembre 2026  
**Dispositivo**: Samsung Galaxy S24 Base (SM-S921B)  
**SoC**: Samsung Exynos 2400 (`s5e9945`)  
**Firmware Pre-Patch**: Android 16 (SDK 36) | One UI 8.5  
**Livello Patch di Sicurezza Attuale**: `2026-06-05`  
**Aggiornamento in corso**: Patch di sicurezza del `2026-08-05` (SMR Agosto 2026)

---

## 1. Stato Hardware e Salute Batteria

I parametri fisici della cella e della gestione energetica prima dell'installazione della patch:

| Parametro | Valore Rilevato | Note |
|---|---|---|
| **Salute Batteria (ASOC)** | **94%** | Cella in ottimo stato di conservazione |
| **Capacità Appresa** | 3.900 mAh | Rilevata via dumpsys batterystats |
| **Capacità Nominale (Rated)** | 3.880 mAh | Specifiche standard del produttore |
| **Utilizzo Cumulativo (LLB)** | 84.669 | Età dispositivo: 126 giorni |
| **Temperatura Max Storica** | 58.1°C | Picco registrato in ricarica rapida/stress |
| **Protezione Batteria Attiva** | Modalità 1 (Tetto 80%) / Modalità 3 (Base 100%) | Gestita dall'app `s24optimizer` |

---

## 2. Metriche dei Consumi Pre-Patch (Baseline di Riferimento)

La misurazione pre-patch fa riferimento alla scarica completa registrata l'11–12 agosto su una finestra di oltre 13 ore:

### 2.1 Sintesi Drain Orario
* **Drain a Schermo Spento (Standby)**: **59.2 mAh/h** (`1.52%/h`)
  * *Evoluzione storica*: 118 mAh/h (13 Luglio) $\rightarrow$ 107 mAh/h (11 Agosto) $\rightarrow$ **59.2 mAh/h** (12 Agosto, **-45%**).
  * *Target ideale*: 30–50 mAh/h (attualmente ~18% sopra la soglia ideale).
* **Drain a Schermo Acceso**: **401 mAh/h** (`10.3%/h`)
  * Schermo acceso totale: 2h 53m (22% del tempo)
  * Schermo spento totale: 10h 16m (78% del tempo)
* **Comportamento Deep Doze**:
  * Tempo in Deep Idle: **8h 55m (67.8% del tempo a schermo spento)**, sessione più lunga: 3h 04m.
  * Light Idle: 37m (4.8%).

### 2.2 Ripartizione Hardware del Consumo Globale
Su un consumo totale di **1.680 mAh** registrato nel ciclo:

```
[CPU]              ■■■■■■■■■■■■■■■■■■■■■■■■  760 mAh (45.2%)
[Radio Mobile]     ■■■■■■■■■■■■■■            443 mAh (26.4%)
[Schermo Display]  ■■■■■■■■■                 277 mAh (16.5%)
[Audio System]     ■■■■■                     146 mAh (8.7%)
[Wakelocks]        ■■                         49 mAh (2.9%)
[Bluetooth]        ■                          27.4 mAh (1.6%)
[Wi-Fi]            ■                          14.1 mAh (0.8%)
[Sensori/GNSS/Cam] ■                          17.0 mAh (1.0%)
```

---

## 3. Top Drainer & Anomalie Identificate Pre-Patch

Nel quadro pre-patch sono state isolate due anomalie responsabili del **56% dell'intero drain notturno a schermo spento**:

### 1. Instagram (`com.instagram.android`) — #1 Assoluto
* **Consumo totale**: **690 mAh** (41% dell'intera scarica).
* **Anomalia**: Prefetch aggressivo e continuo in background tramite `ClipsTabBackgroundPrefetchWorker`, `PendingMediaWorker` e `IgBgFetchSchedulerService`.
* **Impatto Radio**: 106.36 MB scaricati a schermo spento, radio mobile attiva per 48m 10s (costo: **247 mAh** solo di modem a display spento).

### 2. WhatsApp (`com.whatsapp`) — AudioTrack Leak
* **Consumo totale**: **203 mAh**.
* **Anomalia**: Traccia audio rimasta aperta e agganciata per **5h 21m 53s consecutive** mentre l'app era in stato `CACHED` e a schermo spento (`+audio +state=10423`).
* **Costo anomalia**: **96.2 mAh** sprecati inutilmente nel sonno.

### 3. Altri Processi Rilevanti
* **System (uid 1000)**: 248 mAh (di cui 7h 35m di Wi-Fi multicast wakelock per discovery mDNS/Cast).
* **Kernel / Root (uid 0)**: 143 mAh.
* **Google Play Services (`com.google.android.gms`)**: 54.3 mAh (Foreground service continuo di 12h 21m).
* **Huawei Health (`com.huawei.health`)**: 38.3 mAh (tendenza ricorrente a re-inserirsi nella Doze whitelist utente).

---

## 4. Assetto Software e Ottimizzazioni Applicate Pre-Patch

Prima dell'aggiornamento, il dispositivo si trova con la seguente configurazione custom attiva:

### 4.1 Bloatware Disabilitati (32 pacchetti)
* **26 Servizi Samsung**: Bixby (`agent`, `wakeup`, `bixbyvision`, modelli offline), Samsung Rubin (`com.samsung.android.rubin.app`), Live Effect Service, Game Optimizing Service (GOS), Air Command, DeX Launcher (`com.sec.android.app.desktoplauncher`), Samsung Positioning, SmartThings Platform.
* **6 Servizi Terze Parti**: Facebook App Manager & Katana, Microsoft Edge, Word, Excel.
* **Knox Matrix**: Cache e dati resettati per impedire cicli anomali di wake loop.

### 4.2 Restrizioni di Background Applicate (ADB / AppOps)
* **Instagram**:
  * `am set-standby-bucket com.instagram.android restricted`
  * `cmd netpolicy add restrict-background-blacklist 10510` (blocco dati mobili in background)
  * `cmd appops set com.instagram.android RUN_ANY_IN_BACKGROUND deny`
* **Huawei Health**:
  * Rimossa dalla Doze whitelist (`cmd deviceidle whitelist -com.huawei.health`).

### 4.3 Programma di Ricarica Notturna (`s24optimizer`)
* **Meccanismo**: Servizio interno basato su Shizuku e allarme esatto `RTC_WAKEUP` (senza ricorrere al consenso di Samsung Rubin o alle Routine proprietarie).
* **Orari**:
  * **19:00 $\rightarrow$ 07:00**: Tetto all'80% (`protect_battery 1`, `threshold 80`).
  * **07:00 $\rightarrow$ 19:00**: Rilascio a modalità Base (`protect_battery 3`, carica fino al 100%).
* **Watchdog Shizuku**: Script attivo in background in `/data/local/tmp/shizuku_watchdog.sh` (controllo ogni 60s per riavviare `shizuku_server` in caso di crash a caldo).

---

## 5. Punti Critici da Verificare Post-Patch (Post 5 Agosto 2026)

Con l'installazione della patch di sicurezza e il riavvio del firmware:

1. **Riavvio del Server Shizuku**:
   * *Criticità*: Un riavvio di sistema termina tutti i processi con `uid shell`. Il watchdog in `/data/local/tmp` non sopravvive al boot.
   * *Azione*: Riavviare manualmente il server Shizuku (via debug wireless o tramite script ADB) affinché l'app `s24optimizer` e il programma di ricarica automatica tornino operativi.
2. **Persistenza delle Policy di Rete e AppOps**:
   * *Criticità*: L'aggiornamento del framework Android potrebbe ripulire le blacklist di `netpolicy` o resettare le autorizzazioni modificate di `com.instagram.android`.
   * *Azione*: Verificare con `cmd netpolicy list restrict-background-blacklist` e `cmd appops get com.instagram.android RUN_ANY_IN_BACKGROUND`.
3. **Controllo Doze Whitelist**:
   * *Criticità*: Verificare che Google Play Services o Huawei Health non abbiano forzato nuovamente il reintegro nella lista di esclusione da Doze.
4. **Fase di Assestamento ART / Dexopt (24–48h)**:
   * *Avvertenza*: Nelle prime 24 ore post-update il drain a schermo spento salirà temporaneamente sopra i 59 mAh/h per la ricompilazione AOT delle applicazioni. Non prendere misurazioni baseline prima di 48 ore dal riavvio.
