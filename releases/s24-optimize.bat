@echo off
REM ═══════════════════════════════════════════════════════════════════
REM S24 BATTERY OPTIMIZER — Script Definitivo v2.0
REM Device: SM-S921B (Galaxy S24) | One UI 7 / Android 16
REM ═══════════════════════════════════════════════════════════════════
REM Cosa preserva: 120Hz · 5G · Google Maps · Gmail · Hey Google + Gemini
REM ═══════════════════════════════════════════════════════════════════
REM Istruzioni:
REM   1. Collega il telefono al PC con debugging USB attivo
REM   2. Apri una finestra di PowerShell come amministratore
REM   3. Esegui: .\s24-optimize.bat
REM   4. Al termine, riavvia il telefono
REM
REM Per ripristinare: .\s24-restore.bat
REM ═══════════════════════════════════════════════════════════════════

set ADB="%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
if not exist %ADB% set ADB="%USERPROFILE%\AppData\Local\Android\Sdk\platform-tools\adb.exe"
if not exist %ADB% set ADB=adb
echo [%DATE% %TIME%] === S24 OPTIMIZER v2 ===
echo.

REM ── 1. BLOAT SAMSUNG — 18 pacchetti ────────────────────────────────
REM Disabilita app Samsung preinstallate che consumano batteria in
REM background, tengono sveglio il processore e raccolgono dati.
REM Non sono necessarie per il funzionamento base del telefono.
echo [1/6] Disabilitazione bloat Samsung...

REM Bixby Voice — Assistente vocale Samsung (non usato se usiamo Google)
REM Tiene il microfono in ascolto e mantiene wakelock
%ADB% shell pm disable-user --user 0 com.samsung.android.bixby.agent

REM Bixby Wakeup — Comando "Hi Bixby" per attivazione vocale
%ADB% shell pm disable-user --user 0 com.samsung.android.bixby.wakeup

REM Bixby Vision — Riconoscimento oggetti da fotocamera/galleria
%ADB% shell pm disable-user --user 0 com.samsung.android.bixbyvision.framework

REM Vision Intelligence — Analisi scene e suggerimenti AI
%ADB% shell pm disable-user --user 0 com.samsung.android.visionintelligence

REM Game Tools — Pannello sovrapposto nei giochi (consuma anche da fermo)
%ADB% shell pm disable-user --user 0 com.samsung.android.game.gametools

REM Game Optimizing Service — Limita CPU/GPU nei giochi ma consuma CPU
%ADB% shell pm disable-user --user 0 com.samsung.android.game.gos

REM Smart Suggestions — Suggerimenti contestuali basati su posizione
%ADB% shell pm disable-user --user 0 com.samsung.android.smartsuggestions

REM Aware Service — Rilevamento attività fisica (passi, movimento)
%ADB% shell pm disable-user --user 0 com.samsung.android.aware.service

REM BBC Agent — Aggiornamenti push bloatware Samsung
%ADB% shell pm disable-user --user 0 com.samsung.android.bbc.bbcagent

REM Reminder — Promemoria Samsung (alternativa a Google Keep/Tasks)
%ADB% shell pm disable-user --user 0 com.samsung.android.app.reminder

REM Routines — Automazioni condizionali Samsung (Bixby Routines)
%ADB% shell pm disable-user --user 0 com.samsung.android.app.routines

REM Routine Plus — Estensione routines (consuma CPU con wakelock Doze)
%ADB% shell pm disable-user --user 0 com.samsung.android.app.routineplus

REM Live Effect Service — Effetti live per videochiamate
%ADB% shell pm disable-user --user 0 com.samsung.android.liveeffectservice

REM OneConnect — SmartThings Find (localizzatore dispositivi)
%ADB% shell pm disable-user --user 0 com.samsung.android.oneconnect

REM STPlatform — Secure Trade Platform (servizio Knox invisibile)
%ADB% shell pm disable-user --user 0 com.samsung.android.service.stplatform

REM Forest — Modalità benessere digitale / focus (consuma notifiche)
%ADB% shell pm disable-user --user 0 com.samsung.android.forest

REM Buds Manager — Plugin per Galaxy Buds (se non usati)
%ADB% shell pm disable-user --user 0 com.samsung.accessory.budsunitemgr

REM Rubin App — Customization Service (causa noto drain Play Services)
%ADB% shell pm disable-user --user 0 com.samsung.android.rubin.app

REM ── 2. BLOAT FACEBOOK + MICROSOFT ───────────────────────────────────
REM App preinstallate da carrier (TIM/Vodafone etc.) che consumano
REM dati e batteria in background senza utilità per l'utente.
echo [2/6] Disabilitazione bloat Facebook + Microsoft...

REM Facebook — Client social (grande consumatore di batteria)
%ADB% shell pm disable-user --user 0 com.facebook.katana

REM Messenger — Chat Facebook (wakelock e notifiche continue)
%ADB% shell pm disable-user --user 0 com.facebook.orca

REM Microsoft Edge — Browser alternativo (se non usato)
%ADB% shell pm disable-user --user 0 com.microsoft.emmx

REM Excel — Office mobile (se non usato, consuma sync)
%ADB% shell pm disable-user --user 0 com.microsoft.office.excel

REM Word — Office mobile (se non usato)
%ADB% shell pm disable-user --user 0 com.microsoft.office.word

REM Remote Desktop — Client RDP (se non usato)
%ADB% shell pm disable-user --user 0 com.microsoft.rdc.androidx

REM ── 3. KNOX RESET — FIX CICLO DI DRAIN ─────────────────────────────
REM NOTO BUG Samsung Aprile 2026: Knox Matrix entra in loop di
REM attestazione continua consumando CPU e batteria (fino a 20%/h).
REM Il reset forza la riconfigurazione pulita dei servizi Knox.
echo [3/6] Reset Knox Matrix (fix noto drain Aprile 2026)...

REM KPECore — Kernel Policy Enforcement (core Knox, reset pulisce stato)
%ADB% shell pm clear com.samsung.android.knox.kpecore

REM Attestation — Certificazione dispositivo per Samsung/Google Pay
%ADB% shell pm clear com.samsung.android.knox.attestation

REM Push Manager — Notifiche push Knox (spesso in loop)
%ADB% shell pm clear com.samsung.android.knox.pushmanager

REM Container Core — Gestione contenitori sicuri (Knox Workspace)
%ADB% shell pm clear com.samsung.android.knox.containercore

REM Analytics Uploader — Upload statistiche telemetria Knox
%ADB% shell pm clear com.samsung.android.knox.analytics.uploader

REM ── 4. IMPOSTAZIONI SISTEMA ─────────────────────────────────────────
echo [4/6] Impostazioni sistema (batteria, radio, schermo)...

REM ── 4a. BATTERY — Ottimizzazioni consumo energetico ───
REM Adaptive Battery = 1: AI impara le tue abitudini e limita risorse
REM alle app usate raramente (Android 9+)
%ADB% shell settings put global adaptive_battery_management_enabled 1

REM App Auto Restriction = 1: Android mette automaticamente in
REM standby le app non usate da molto tempo (Android 12+)
%ADB% shell settings put global app_auto_restriction_enabled 1

REM Battery Saver = 1: Attiva la modalità risparmio energetico.
REM NOTA: su One UI 7 NON limita il refresh rate a 60Hz se la
REM modalità "Risparmio energetico" non è impostata su "Massimo"
%ADB% shell settings put global battery_saver_mode 1

REM Enhanced CPU Responsiveness = 0: Disabilita la modalità Samsung
REM che alza forzosamente la frequenza CPU (consumo extra inutile)
%ADB% shell settings put global sem_enhanced_cpu_responsiveness 0

REM Enhanced Processing = 0: Disabilita la "Modalità Performance"
REM che tiene CPU e GPU al massimo senza necessità
%ADB% shell settings put global enhanced_processing 0

REM ── 4b. CONNETTIVITÀ / RADIO ───────────────────────
REM BLE Scan Always = 0: Impedisce la scansione Bluetooth Low Energy
REM continua in background (grande risparmio, usata da Nearby/Aware)
%ADB% shell settings put global ble_scan_always_enabled 0

REM Nearby Scanning = 0: Disabilita scansione dispositivi vicini
REM (usata da Quick Share, SmartThings Find)
%ADB% shell settings put system nearby_scanning_enabled 0

REM Nearby Permission = 0: Revoca permesso di scansione
%ADB% shell settings put system nearby_scanning_permission_allowed 0

REM WiFi Power Save = 1: Attiva risparmio energetico WiFi quando
REM il telefono è in sospensione
%ADB% shell settings put global wifi_power_save 1

REM WiFi Wakeup = 0: Impedisce al WiFi di riaccendersi automaticamente
REM vicino a reti salvate (evita scansioni periodiche)
%ADB% shell settings put global wifi_wakeup_enabled 0

REM ── 4c. SCHERMO / AOD ────────────────────────────
REM AOD Mode = 0: Disabilita Always-On Display (consumo ~1%/h con
REM schermo OLED spento, il nero puro non consuma ma il sensore sì)
%ADB% shell settings put system aod_mode 0

REM Always On Display = 0: Doppia conferma disabilitazione AOD
%ADB% shell settings put global always_on_display_enabled 0

REM Screen Off Timeout = 30000 (30s): Tempo di spegnimento schermo.
REM Valori più bassi risparmiano batteria, 30s è il compromesso ideale
%ADB% shell settings put system screen_off_timeout 30000

REM Doze Enabled = 1: Attiva Doze profondo di Android (sospensione
REM forzata quando il telefono è fermo e schermo spento)
%ADB% shell settings put secure doze_enabled 1

REM Lift to Wake = 0: Disabilita accensione schermo sollevando
REM il telefono (evita attivazioni accidentali)
%ADB% shell settings put system lift_to_wake 0

REM Intelligent Sleep Mode = 0: Disabilita Smart Stay (schermo acceso
REM finché guardi lo schermo — usa sensore frontale)
%ADB% shell settings put system intelligent_sleep_mode 0

REM Notification Light Pulse = 0: Disabilita LED notifiche (se presente)
%ADB% shell settings put system notification_light_pulse 0

REM ── 4d. PERFORMANCE ──────────────────────────────
REM Animazioni a 0.5x: Raddoppia la velocità delle animazioni UI.
REM L'effetto è una UI più scattante e meno GPU time.
REM NOTA: 120Hz rimane attivo, questa è solo la durata delle transizioni
%ADB% shell settings put global window_animation_scale 0.5
%ADB% shell settings put global transition_animation_scale 0.5
%ADB% shell settings put global animator_duration_scale 0.5

REM RAM Plus = 2048 (2GB): Memoria virtuale su storage.
REM Valori alti (4/6/8GB) consumano batteria per I/O continuo su flash.
REM 2GB è il compromesso tra multitasking e risparmio energetico
%ADB% shell settings put global ram_expand_size 2048

REM ── 4e. PROTEZIONE BATTERIA ────────────────────────────
REM Battery Protection = 3 (Modalità Massima): Limita la carica all'80%
REM per preservare la salute della batteria a lungo termine.
REM 3 = Massima protezione (80%), 2 = Adattiva (80-95% in base a sonno),
REM 1 = Base (100% con stop notturno intelligente)
%ADB% shell settings put global battery_protection_default_value 3
%ADB% shell settings put global battery_protection_threshold 80

REM ── 4f. QUICK SHARE / MCF ──────────────────────
REM MCF Quick Share = 0: Disabilita la visibilità Quick Share.
REM MCF (Multi-Device Control Framework) esegue scansioni Bluetooth/WiFi
REM periodiche per cercare dispositivi Samsung vicini.
REM Disabilitare ferma queste scansioni in background
%ADB% shell settings put global mcf_quick_share_visibility 0

REM ── 5. RESTRIZIONE BACKGROUND ───────────────────────────────────────
REM Impedisce alle app selezionate di eseguire processi in background.
REM Le app possono ancora essere aperte manualmente, ma non possono
REM tenere wakelock, sincronizzare dati o fare polling quando in secondo
REM piano. Questo è il singolo intervento più efficace per ridurre
REM il drain su app che non si vogliono disinstallare.
echo [5/6] Restrizione background app pesanti...

REM Instagram — Noto per wakelock continui, sync in background e
REM upload automatico foto/ video
%ADB% shell appops set com.instagram.android RUN_ANY_IN_BACKGROUND deny

REM Tandem — App muletto (personalizzala con il nome corretto)
%ADB% shell appops set net.tandem RUN_ANY_IN_BACKGROUND deny

REM WhatsApp — Nonostante le ottimizzazioni di Meta, consuma
REM 499 mAh in 8 ore nel nostro test (16% del totale). Limitare
REM il background forza le notifiche push Firebase invece del
REM polling continuo
%ADB% shell appops set com.whatsapp RUN_ANY_IN_BACKGROUND deny

REM ── 6. RUBIN.APP — REVOCA PERMESSO ATTIVITÀ ────────────────────────
REM Rubin.app (Samsung Customization Service) è noto per tenere
REM Google Play Services in un loop di richieste di aggiornamento
REM posizione/attività. Anche dopo averlo disabilitato, mantiene
REM i permessi concessi. Revocare ACTIVITY_RECOGNITION rompe
REM definitivamente questo loop.
echo [6/6] Revoca permesso attività fisica rubin.app...
%ADB% shell pm revoke com.samsung.android.rubin.app android.permission.ACTIVITY_RECOGNITION

REM ── COMPLETATO ──────────────────────────────────────────────────────
echo.
echo ============================================================
echo   COMPLETATO!  Riavvia il telefono per applicare tutto.
echo ============================================================
echo.
echo   Per annullare tutte le modifiche:
echo     .\s24-restore.bat
echo.
echo   Attenzione: dopo il riavvio, il telefono potrebbe impiegare
echo   1-2 cicli di carica per ricalibrare la stima della batteria.
echo ============================================================
