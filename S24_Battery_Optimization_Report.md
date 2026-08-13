# Samsung Galaxy S24 (SM-S921B) — Report Ottimizzazione Batteria

**Data**: 14 Luglio 2026
**ADB eseguito da**: Windows PowerShell / CMD

---

## File Associati

| File | Descrizione |
|------|-------------|
| `s24-optimize.bat` | Script di ottimizzazione definitivo v2.0 |
| `s24-restore.bat` | Script di ripristino completo |
| `S24_Battery_Optimization_Report.md` | Questo report |
| `README.md` | Guida completa in italiano |
| `README-EN.md` | Complete guide in English |
| `s24-baseline 13-luglio.json` | Baseline pre-ottimizzazione (consumi 3.114 mAh / 8h 42m) |
| `s24-baseline 14-luglio.json` | Baseline post-ottimizzazione |

---

## 1. Diagnosi Iniziale

### Stato telefono
| Parametro | Valore |
|-----------|--------|
| Modello | SM-S921B (Galaxy S24 base) |
| Android | 16 (SDK 36) |
| One UI | 7 |
| Patch sicurezza | 2026-06-05 |
| Capacità batteria | 3900 mAh |
| Salute batteria | 94% (ASOC) |
| Temperatura max registrata | 58.1°C |

### Consumo batteria rilevato (nelle 15h 52m prima del fix)
| App | mAh | Note |
|-----|-----|------|
| Instagram | 771 | 1h20m FG + 1h37m BG, 329K pacchetti dati |
| Fotocamera | 318 | 30min uso |
| Google Play Services | 224 | Foreground service 16h51m ininterrotto (anomalo) |
| WhatsApp | 189 | 4h30m background, 64K pacchetti |
| YouTube ReVanced | 165 | - |
| Huawei Health | 62.6 | 100% background |
| net.tandem | 80.6 | - |
| SystemUI/AOD | 85.1 | Schermo / AOD |

**Standby drain pre-ottimizzazione**: ~118 mAh/h (alto, range normale: 30-50 mAh/h)

---

## 2. Bloatware Disabilitati

### 2.1 Samsung Bloat (26 pacchetti)
Motivo: servizi non necessari che girano in background o aggiornamenti automatici.

| Pacchetto | Cosa fa | Comando revert |
|-----------|---------|----------------|
| `com.samsung.android.bixby.agent` | Agente principale Bixby | `pm enable com.samsung.android.bixby.agent` |
| `com.samsung.android.bixby.wakeup` | Rilevamento "Hi Bixby" | `pm enable com.samsung.android.bixby.wakeup` |
| `com.samsung.android.bixbyvision.framework` | Bixby Vision | `pm enable com.samsung.android.bixbyvision.framework` |
| `com.samsung.android.bixby.ondevice.*` | 13 modelli linguistici offline | `pm enable com.samsung.android.bixby.ondevice.XXXX` |
| `com.samsung.android.rubin.app` | Rubin (tiene sveglio Play Services) | `pm enable` + grant `ACTIVITY_RECOGNITION` |
| `com.samsung.android.liveeffectservice` | Effetti live foto/video | `pm enable` |
| `com.samsung.android.visionintelligence` | Ricerca intelligente fotocamera | `pm enable` |
| `com.samsung.android.bbc.bbcagent` | Bixby Briefing / marketing | `pm enable` |
| `com.samsung.android.aware.service` | Consapevolezza contestuale | `pm enable` |
| `com.samsung.android.app.reminder` | Promemoria Samsung | `pm enable` |
| `com.samsung.android.game.gametools` | Game Launcher tools | `pm enable` |
| `com.samsung.android.game.gos` | Game Optimizing Service | `pm enable` |
| `com.samsung.android.app.routineplus` | Modalità Routine+ (Doze wakelock) | `pm enable` |
| `com.samsung.android.service.aircommand` | Air Command (S Pen — non serve su S24) | `pm enable` |
| `com.samsung.android.app.simplesharing` | Condivisione file rapida | `pm enable` |
| `com.samsung.android.service.airmessage` | Air Message | `pm enable` |
| `com.samsung.android.app.sharelive` | Share Live | `pm enable` |
| `com.sec.android.app.desktoplauncher` | Modalità desktop DeX | `pm enable` |
| `com.samsung.android.service.peoplestrip` | People Strip | `pm enable` |
| `com.samsung.android.samsungpositioning` | Posizionamento Samsung | `pm enable` |
| `com.samsung.android.service.stplatform` | SmartThings Platform | `pm enable` |
| `com.android.providers.partnerbookmarks` | Segnalibri partner | `pm enable` |
| `com.sec.android.app.chromecustomizations` | Personalizzazioni Chrome Samsung | `pm enable` |

### 2.2 Facebook + Microsoft (6 pacchetti)
| Pacchetto | Cosa fa | Comando revert |
|-----------|---------|----------------|
| `com.facebook.katana` | Facebook | `pm enable` |
| `com.facebook.orca` | Messenger | `pm enable` |
| `com.facebook.appmanager` | Gestore aggiornamenti Facebook | `pm enable` |
| `com.microsoft.emmx` | Microsoft Edge | `pm enable` |
| `com.microsoft.office.excel` | Excel | `pm enable` |
| `com.microsoft.office.word` | Word | `pm enable` |

---

## 3. Reset Knox Matrix (5 pacchetti)

Motivo: l'aggiornamento Aprile 2026 ha causato loop Knox Matrix con consumo anomalo CPU/batteria.

| Pacchetto | Azione |
|-----------|--------|
| `com.samsung.android.knox.kpecore` | `pm clear` |
| `com.samsung.android.knox.attestation` | `pm clear` |
| `com.samsung.android.knox.pushmanager` | `pm clear` |
| `com.samsung.android.knox.containercore` | `pm clear` |
| `com.samsung.android.knox.analytics.uploader` | `pm clear` |

---

## 4. Google Play Services Reset

| Azione | Dettaglio |
|--------|-----------|
| `am force-stop com.google.android.gms` | Forzato arresto |
| `pm clear com.google.android.gms` | Cancellati dati (login, token, cache) |

---

## 5. Limitazioni Background (appops)

A queste app è stato negato `RUN_ANY_IN_BACKGROUND`:
- `com.instagram.android`
- `com.whatsapp`
- `com.huawei.health`
- `net.tandem`

---

## 6. Ottimizzazioni Sistema (23 impostazioni)

### Scansioni Radio & Rete
- `ble_scan_always_enabled=0` — BLE scanning OFF
- `nearby_scanning_permission_allowed=0` — Nearby scanning OFF
- `wifi_scan_interval=0` — WiFi scan throttling
- `wifi_wakeup_enabled=0` — WiFi wakeup OFF
- `mcf_quick_share_visibility=0` — Background MCF scans OFF
- `mcf_continuity=0` — Continuity service OFF

### CPU & Power Saving
- `sem_enhanced_cpu_responsiveness=0` — CPU responsiveness ridotta
- `sem_power_mode_processing=0` — Enhanced processing OFF
- `ram_expand_size=2048` — RAM Plus a 2GB
- `window_animation_scale=0.5` + `transition_animation_scale=0.5` + `animator_duration_scale=0.5` — Animazioni 0.5x

### Doze & Schermo
- `doze_enabled=1` — Doze forzato
- `doze_quick_action_brightness_sensor_enabled=0` — Sensore brightness in Doze OFF
- `always_on_display_enabled=0` — AOD OFF
- `lift_to_wake=0` — Sollevamento per attivare OFF
- `intelligent_sleep_mode=0` — Smart Stay OFF
- `screen_off_timeout=30000` — Screen timeout 30s

### Batteria
- `adaptive_battery_management_enabled=1` — Adaptive Battery ON
- `app_auto_restriction_enabled=1` — Auto restriction ON
- `battery_saver_mode=1` — Battery Saver ON (riattivabile liberamente)
- `wifi_power_save=1` — WiFi power save ON

---

## 7. Verifica Impostazioni (14 Luglio 2026)

Dopo reboot, tutte le 23 impostazioni sono state verificate via `settings get` e confermate:

| Impostazione | Valore atteso | Verificato |
|---|---|---|
| Adaptive Battery | 1 (ON) | ✅ |
| Battery Saver | 1 (ON) | ✅ |
| BLE scan always | 0 (OFF) | ✅ |
| Enhanced processing | 0 (OFF) | ✅ |
| Enhanced CPU resp | 0 (OFF) | ✅ |
| AOD | 0 (OFF) | ✅ |
| Doze enabled | 1 (ON) | ✅ |
| Nearby scanning | 0 (OFF) | ✅ |
| WiFi power save | 1 (ON) | ✅ |
| WiFi scan interval | 0 | ✅ |
| WiFi wakeup | 0 (OFF) | ✅ |
| RAM Plus | 2048 | ✅ |
| Animations | 0.5x | ✅ |
| MCF visibility | 0 (OFF) | ✅ |
| MCF continuity | 0 (OFF) | ✅ |

Tutti i 18 pacchetti Samsung disabilitati confermati via `pm list packages -d`.
Tutti i 6 pacchetti FB+MS disabilitati confermati.
I 5 reset Knox confermati.
Rubin.app permesso `ACTIVITY_RECOGNITION` revocato confermato.

---

## 8. Nuove Scoperte in Questa Sessione

### Pacchetti aggiuntivi scoperti
- **`com.samsung.android.app.routineplus`** — Causa Doze wakelock anche con Routine disattivate
- **`com.samsung.android.service.aircommand`** — Non necessario su S24 base (S Pen assente)
- **`com.samsung.android.samsungpositioning`** — Posizionamento aggiuntivo Samsung
- **`com.samsung.android.service.stplatform`** — SmartThings Platform, servizi telemetria

### Altre scoperte
- **`mcf_quick_share_visibility=0`** — Blocca scansioni MCF in background (Bluetooth/WiFi)
- **`wifi_wakeup_enabled=0`** — Impedisce al WiFi di riattivare il Doze
- **`doze_quick_action_brightness_sensor_enabled=0`** — Impedisce al sensore di luminosità di uscire dal Doze
- **Extra dim** non esiste come setting su One UI 7. Rimosso dalle raccomandazioni.
- **WiFi Aware** non trovato. Rimosso dalle raccomandazioni.
- **Reduce animations** è in Opzioni sviluppatore, non in Funzioni avanzate.

### Verifiche impostazioni manuali (passi menu)
| Voce | Percorso | Stato |
|------|----------|-------|
| Network mode | `preferred_network_mode` key | ✅ Confermato |
| Edge panels | Settings > Display > Edge panels | ✅ Confermato |
| Dark mode | Settings > Display > Dark mode | ✅ Confermato |
| Navigation bar | Settings > Display > Navigation bar | ✅ Confermato |
| NFC | Settings > Connections > NFC | ✅ Confermato |
| Printing | Settings > Connections > Printing | ✅ Confermato (4 servizi stampa installati) |
| Screen resolution | S24 base FHD+ nativo — non presente su One UI 7 | ℹ️ Non modificabile |
| Extra dim | Non trovato | ❌ Rimosso |
| WiFi Aware | Non trovato | ❌ Rimosso |

---

## 9. Per Ottimizzare Manualmente (dal telefono)

1. **Installa Good Guardians** → Galaxy Store
2. Apri **App Booster** → esegui ottimizzazione completa
3. **Battery Guardian** → attiva "Wakelock blocker" e "App timers"
4. **Thermal Guardian** → abbassa soglia di -2°C
5. **Memory Guardian** → RAM Plus a 2GB (già via ADB)
6. Impostazioni → Batteria → Limiti background → verifica Deep Sleep per app non necessarie
7. **Riavvio settimanale** per pulire memory leak

---

## 10. Script di Ripristino Rapido

Vedi `s24-restore.bat` per il ripristino completo. Se serve solo riattivare un'app:

```cmd
adb shell pm enable <NOME_PACCHETTO>
```

Per ripristinare una singola impostazione:
```cmd
adb shell settings put global <NOME_IMPOSTAZIONE> <VALORE_ORIGINALE>
```
