package com.s24optimizer.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SyncDisabled
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Apps
import androidx.compose.ui.graphics.vector.ImageVector

object Optimizations {

    fun getAll(): List<Optimization> = listOf(
        // ── SAMSUNG ──
        opt("bixby_agent", Optimization.Category.BLOAT,
            "Bixby Voice Assistant", "Assistente Vocale Bixby",
            "Main Bixby process. Stays in memory 24/7 listening for 'Hi Bixby'. Drains CPU and RAM.",
            "Processo principale Bixby. Resta in memoria 24/7 in attesa di 'Hi Bixby'. Consuma CPU e RAM.",
            "pm disable-user --user 0 com.samsung.android.bixby.agent",
            "pm enable com.samsung.android.bixby.agent",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.bixby.agent"""",
            group = "Bixby"),

        opt("bixby_wakeup", Optimization.Category.BLOAT,
            "Bixby Wake-up Detection", "Rilevamento Sveglia Bixby",
            "Always-listens for 'Hi Bixby' via microphone. Main standby drain on One UI 8.",
            "Ascolta sempre 'Hi Bixby' via microfono. Principale drain in standby su One UI 8.",
            "pm disable-user --user 0 com.samsung.android.bixby.wakeup",
            "pm enable com.samsung.android.bixby.wakeup",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.bixby.wakeup"""",
            group = "Bixby"),

        opt("bixby_vision", Optimization.Category.BLOAT,
            "Bixby Vision", "Bixby Vision",
            "AI image analysis: text translation from photos, product search, QR scanning.",
            "Analisi immagini AI: traduzione testo da foto, ricerca prodotti, scansione QR.",
            "pm disable-user --user 0 com.samsung.android.bixbyvision.framework",
            "pm enable com.samsung.android.bixbyvision.framework",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.bixbyvision.framework"""",
            group = "Bixby"),

        opt("bixby_langpacks", Optimization.Category.BLOAT,
            "Bixby Offline Language Models", "Modelli Linguistici Offline Bixby",
            "13 language packs (~300MB total). Only needed if you use Bixby Voice offline.",
            "13 pacchetti lingua (~300MB). Servono solo se usi Bixby Voice offline.",
            "pm disable-user --user 0 com.samsung.android.bixby.ondevice.arae; "
                + "pm disable-user --user 0 com.samsung.android.bixby.ondevice.dede; "
                + "pm disable-user --user 0 com.samsung.android.bixby.ondevice.enus; "
                + "pm disable-user --user 0 com.samsung.android.bixby.ondevice.eses; "
                + "pm disable-user --user 0 com.samsung.android.bixby.ondevice.esmx; "
                + "pm disable-user --user 0 com.samsung.android.bixby.ondevice.itit; "
                + "pm disable-user --user 0 com.samsung.android.bixby.ondevice.plpl; "
                + "pm disable-user --user 0 com.samsung.android.bixby.ondevice.ptbr; "
                + "pm disable-user --user 0 com.samsung.android.bixby.ondevice.roro; "
                + "pm disable-user --user 0 com.samsung.android.bixby.ondevice.ruxx; "
                + "pm disable-user --user 0 com.samsung.android.bixby.ondevice.svse; "
                + "pm disable-user --user 0 com.samsung.android.bixby.ondevice.trtr; "
                + "pm disable-user --user 0 com.samsung.android.bixby.ondevice.zhhk",
            "pm enable com.samsung.android.bixby.ondevice.arae; "
                + "pm enable com.samsung.android.bixby.ondevice.dede; "
                + "pm enable com.samsung.android.bixby.ondevice.enus; "
                + "pm enable com.samsung.android.bixby.ondevice.eses; "
                + "pm enable com.samsung.android.bixby.ondevice.esmx; "
                + "pm enable com.samsung.android.bixby.ondevice.itit; "
                + "pm enable com.samsung.android.bixby.ondevice.plpl; "
                + "pm enable com.samsung.android.bixby.ondevice.ptbr; "
                + "pm enable com.samsung.android.bixby.ondevice.roro; "
                + "pm enable com.samsung.android.bixby.ondevice.ruxx; "
                + "pm enable com.samsung.android.bixby.ondevice.svse; "
                + "pm enable com.samsung.android.bixby.ondevice.trtr; "
                + "pm enable com.samsung.android.bixby.ondevice.zhhk",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.bixby.ondevice.itit"""",
            group = "Bixby"),

        opt("vision_intel", Optimization.Category.BLOAT,
            "Vision Intelligence", "Intelligenza Visiva",
            "Samsung visual search (like Google Lens). Camera works fine without it.",
            "Ricerca visiva Samsung (simile a Google Lens). Fotocamera funziona senza.",
            "pm disable-user --user 0 com.samsung.android.visionintelligence",
            "pm enable com.samsung.android.visionintelligence",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.visionintelligence"""",
            group = "Bixby"),

        opt("game_tools", Optimization.Category.BLOAT,
            "Game Tools", "Game Tools",
            "In-game overlay: screenshot, recording, DND. Games work fine without it.",
            "Overlay in-game: screenshot, registrazione, DND. Giochi funzionano senza.",
            "pm disable-user --user 0 com.samsung.android.game.gametools",
            "pm enable com.samsung.android.game.gametools",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.game.gametools"""",
            group = "Gaming"),

        opt("gos", Optimization.Category.BLOAT,
            "Game Optimizing Service (GOS)", "Game Optimizing Service (GOS)",
            "Limits gaming performance to prevent overheating. Safe to disable: games may run faster but with higher battery/heat. No app or system features break.",
            "Limita le prestazioni nei giochi per evitare surriscaldamento. Disabilitabile: i giochi girano più fluidi ma scaldano e consumano di più. Nessuna app o funzione si rompe.",
            "pm disable-user --user 0 com.samsung.android.game.gos",
            "pm enable com.samsung.android.game.gos",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.game.gos"""",
            group = "Gaming"),

        opt("sam_daily", Optimization.Category.BLOAT,
            "Samsung Daily / Members", "Samsung Daily / Members",
            "Removes Samsung side panel (news/offers) AND Samsung Customization Service which keeps Play Services awake on movement. Loss: side panel, Samsung Members benefits. Gain: less standby drain.",
            "Rimuove pannello laterale Samsung (notizie/offerte) E Customization Service che tiene sveglio Play Services in movimento. Perdi: pannello laterale, vantaggi Samsung Members. Guadagni: meno drain in standby.",
            "pm disable-user --user 0 com.samsung.android.rubin.app; pm revoke com.samsung.android.rubin.app android.permission.ACTIVITY_RECOGNITION",
            "pm enable com.samsung.android.rubin.app; pm grant com.samsung.android.rubin.app android.permission.ACTIVITY_RECOGNITION",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.rubin.app"""",
            group = "Assistants & Suggestions", groupIt = "Assistenti e suggerimenti"),

        opt("smart_suggest", Optimization.Category.BLOAT,
            "Smart Suggestions", "Suggerimenti Intelligenti",
            "Predictive app/text suggestions in keyboard, share sheet, and notification bar. Loss: app suggestions, smart copy-paste. Keyboard predictions unaffected.",
            "Suggerimenti predittivi di app/testo in tastiera, menu condividi e barra notifiche. Perdi: suggerimenti app, copia-incolla intelligente. Predizioni tastiera non toccate.",
            "pm disable-user --user 0 com.samsung.android.smartsuggestions",
            "pm enable com.samsung.android.smartsuggestions",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.smartsuggestions"""",
            group = "Assistants & Suggestions", groupIt = "Assistenti e suggerimenti"),

        opt("aware", Optimization.Category.BLOAT,
            "Contextual Awareness", "Consapevolezza Contestuale",
            "Detects driving/walking using sensors (not GPS). Powers driving mode, walking focus. Loss: automatic driving/walking detection. Modes & Routines triggers for activity may stop.",
            "Rileva guida/camminata usando sensori (non GPS). Alimenta modalità guida, focus camminata. Perdi: rilevamento automatico guida/camminata. Attivatori attività in Modalità e Routine potrebbero non funzionare.",
            "pm disable-user --user 0 com.samsung.android.aware.service",
            "pm enable com.samsung.android.aware.service",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.aware.service"""",
            group = "Assistants & Suggestions", groupIt = "Assistenti e suggerimenti"),

        opt("bbc_agent", Optimization.Category.BLOAT,
            "Bixby Briefing Agent", "Agente Bixby Briefing",
            "Provides news/suggestions content to Bixby. Useless if Bixby is disabled.",
            "Fornisce notizie/suggerimenti a Bixby. Inutile se Bixby è disabilitato.",
            "pm disable-user --user 0 com.samsung.android.bbc.bbcagent",
            "pm enable com.samsung.android.bbc.bbcagent",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.bbc.bbcagent"""",
            group = "Assistants & Suggestions", groupIt = "Assistenti e suggerimenti"),

        opt("reminder", Optimization.Category.BLOAT,
            "Samsung Reminder", "Promemoria Samsung",
            "Native Samsung reminders. Use Google Keep instead.",
            "Promemoria nativi Samsung. Usa Google Keep invece.",
            "pm disable-user --user 0 com.samsung.android.app.reminder",
            "pm enable com.samsung.android.app.reminder",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.app.reminder"""",
            group = "Assistants & Suggestions", groupIt = "Assistenti e suggerimenti"),

        opt("routines", Optimization.Category.BLOAT,
            "Samsung Routines", "Routine Samsung",
            "Automations like 'when I arrive home, turn WiFi on'. Use Tasker/IFTTT instead.",
            "Automazioni come 'quando arrivo a casa, accendi WiFi'. Usa Tasker/IFTTT.",
            "pm disable-user --user 0 com.samsung.android.app.routines",
            "pm enable com.samsung.android.app.routines",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.app.routines"""",
            group = "Assistants & Suggestions", groupIt = "Assistenti e suggerimenti"),

        opt("live_effect", Optimization.Category.BLOAT,
            "Live Effect Service", "Servizio Effetti Live",
            "AR filters and beautification effects for photos/videos.",
            "Filtri AR ed effetti beautificazione per foto/video.",
            "pm disable-user --user 0 com.samsung.android.liveeffectservice",
            "pm enable com.samsung.android.liveeffectservice",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.liveeffectservice"""",
            group = "Assistants & Suggestions", groupIt = "Assistenti e suggerimenti"),

        opt("one_connect", Optimization.Category.BLOAT,
            "Samsung One Connect", "Samsung One Connect",
            "SmartThings remote control for Samsung TVs, appliances, IoT devices. Loss: you cannot control smart home/TV from your phone. Use physical remotes instead.",
            "Controllo remoto SmartThings per TV Samsung, elettrodomestici, IoT. Perdi: non puoi controllare smart home/TV dal telefono. Usa telecomandi fisici.",
            "pm disable-user --user 0 com.samsung.android.oneconnect",
            "pm enable com.samsung.android.oneconnect",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.oneconnect"""",
            group = "SmartThings"),

        opt("st_platform", Optimization.Category.BLOAT,
            "Samsung Pass / Security Platform", "Samsung Pass / Piattaforma Sicurezza",
            "Samsung Pass: biometric login to apps/sites, autofill passwords. Loss: Samsung Pass, biometric login to Samsung apps. Keep if you use Samsung Pass instead of Google Password Manager.",
            "Samsung Pass: login biometrico a app/siti, autocompilazione password. Perdi: Samsung Pass, login biometrico app Samsung. Tienilo se usi Samsung Pass invece di Google Password Manager.",
            "pm disable-user --user 0 com.samsung.android.service.stplatform",
            "pm enable com.samsung.android.service.stplatform",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.service.stplatform"""",
            group = "SmartThings"),

        opt("kids_mode", Optimization.Category.BLOAT,
            "Samsung Kids Mode", "Modalità Bambini Samsung",
            "Kids Home: child-safe environment with app timer.",
            "Kids Home: ambiente sicuro per bambini con timer app.",
            "pm disable-user --user 0 com.samsung.android.forest",
            "pm enable com.samsung.android.forest",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.forest"""",
            group = "Samsung Apps", groupIt = "App Samsung"),

        opt("my_galaxy", Optimization.Category.BLOAT,
            "Samsung Members / My Galaxy", "Samsung Members / My Galaxy",
            "Support, offers, Samsung community. Marketing bloatware.",
            "Supporto, offerte, community Samsung. Bloatware marketing.",
            "pm disable-user --user 0 com.mygalaxy.service",
            "pm enable com.mygalaxy.service",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.mygalaxy.service"""",
            group = "Samsung Apps", groupIt = "App Samsung"),

        opt("buds_mgr", Optimization.Category.BLOAT,
            "Galaxy Buds/Watch Manager", "Gestore Galaxy Buds/Watch",
            "Manages Galaxy Buds, Watch, and accessories. WARNING: disabling breaks Galaxy Wearable app — no firmware updates, no battery readout, no Buds/Watch settings.",
            "Gestisce Galaxy Buds, Watch e accessori. ATTENZIONE: disabilitarlo rompe l'app Galaxy Wearable — niente aggiornamenti firmware, niente lettura batteria, niente impostazioni Buds/Watch.",
            "pm disable-user --user 0 com.samsung.accessory.budsunitemgr",
            "pm enable com.samsung.accessory.budsunitemgr",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.accessory.budsunitemgr"""",
            group = "Samsung Apps", groupIt = "App Samsung"),

        opt("sam_health", Optimization.Category.BLOAT,
            "Samsung Health", "Samsung Health",
            "Step counter, sleep tracking, workouts. Keep if you use it!",
            "Contapassi, monitoraggio sonno, allenamenti. Tienilo se lo usi!",
            "pm disable-user --user 0 com.sec.android.app.shealth",
            "pm enable com.sec.android.app.shealth",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.sec.android.app.shealth"""",
            group = "Samsung Apps", groupIt = "App Samsung"),

        opt("chrome_custom", Optimization.Category.BLOAT,
            "Samsung Chrome Customizations", "Personalizzazioni Chrome Samsung",
            "Samsung's Chrome modifications (default home page, bookmarks). Chrome works fine.",
            "Modifiche Samsung a Chrome (home page, segnalibri). Chrome funziona normale.",
            "pm disable-user --user 0 com.sec.android.app.chromecustomizations",
            "pm enable com.sec.android.app.chromecustomizations",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.sec.android.app.chromecustomizations"""",
            group = "Samsung Apps", groupIt = "App Samsung"),

        opt("partner_bkm", Optimization.Category.BLOAT,
            "Partner Bookmarks", "Segnalibri Partner",
            "Pre-installed bookmarks from carriers/manufacturers.",
            "Segnalibri preinstallati da gestori/produttori.",
            "pm disable-user --user 0 com.android.providers.partnerbookmarks",
            "pm enable com.android.providers.partnerbookmarks",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.android.providers.partnerbookmarks"""",
            group = "Samsung Apps", groupIt = "App Samsung"),

        opt("smart_switch", Optimization.Category.BLOAT,
            "Smart Switch", "Smart Switch",
            "Transfer data from old phone. Useless after initial migration.",
            "Trasferimento dati da vecchio telefono. Inutile dopo la migrazione.",
            "pm disable-user --user 0 com.sec.android.easyMover",
            "pm enable com.sec.android.easyMover",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.sec.android.easyMover"""",
            group = "Samsung Apps", groupIt = "App Samsung"),

        opt("emergency", Optimization.Category.BLOAT,
            "Emergency Launcher", "Lanciatore Emergenza",
            "SOS launcher (5x power press → call 112). Keep if you travel!",
            "SOS emergenza (5x pressione → chiama 112). Tienilo se viaggi!",
            "pm disable-user --user 0 com.sec.android.emergencylauncher",
            "pm enable com.sec.android.emergencylauncher",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.sec.android.emergencylauncher"""",
            group = "Samsung Apps", groupIt = "App Samsung"),

        opt("knox_zt", Optimization.Category.BLOAT,
            "Knox Zero Touch", "Knox Zero Touch",
            "Enterprise device management framework. Safe to disable for personal phones.",
            "Framework gestione aziendale. Sicuro disabilitare su telefoni personali.",
            "pm disable-user --user 0 com.samsung.android.knox.zt.framework",
            "pm enable com.samsung.android.knox.zt.framework",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.knox.zt.framework"""",
            group = "Knox & Telemetry", groupIt = "Knox e telemetria"),

        opt("knox_matrix", Optimization.Category.BLOAT,
            "Knox Matrix (battery fix)", "Knox Matrix (fix batteria)",
            "Knox Matrix security suite — main battery drain after April 2026 update. Disables: knnr, kpecore, attestation, analytics.",
            "Suite sicurezza Knox Matrix — principale causa drain batteria dopo aggiornamento Aprile 2026. Disabilita: knnr, kpecore, attestation, analytics.",
            "pm disable-user --user 0 com.samsung.android.knox.knnr; "
                + "appops set com.samsung.android.knox.knnr RUN_IN_BACKGROUND deny; "
                + "appops set com.samsung.android.knox.knnr RUN_ANY_IN_BACKGROUND deny; "
                + "appops set com.samsung.android.knox.knnr WAKE_LOCK deny; "
                + "appops set com.samsung.android.knox.knnr START_FOREGROUND deny; "
                + "am force-stop com.samsung.android.knox.knnr; "
                + "pm disable-user --user 0 com.samsung.android.knox.kpecore; "
                + "pm disable-user --user 0 com.samsung.android.knox.attestation; "
                + "pm disable-user --user 0 com.samsung.android.knox.analytics.uploader",
            "pm enable com.samsung.android.knox.knnr; "
                + "appops set com.samsung.android.knox.knnr RUN_IN_BACKGROUND allow; "
                + "appops set com.samsung.android.knox.knnr RUN_ANY_IN_BACKGROUND allow; "
                + "appops set com.samsung.android.knox.knnr WAKE_LOCK allow; "
                + "appops set com.samsung.android.knox.knnr START_FOREGROUND allow; "
                + "pm enable com.samsung.android.knox.kpecore; "
                + "pm enable com.samsung.android.knox.attestation; "
                + "pm enable com.samsung.android.knox.analytics.uploader",
            // All four, not just knnr. On this device the other three were disabled while
            // knnr was still running, and a check reading one package reported that as the
            // state of the whole entry.
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.knox.knnr"; """
                + """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.knox.kpecore"; """
                + """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.knox.attestation"; """
                + """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.knox.analytics.uploader"""",
            group = "Knox & Telemetry", groupIt = "Knox e telemetria"),

        opt("samsung_pay", Optimization.Category.BLOAT,
            "Samsung Pay", "Samsung Pay",
            "Samsung's payment service. Uses Knox for NFC transactions. Safe to disable if you use Google Wallet or nothing.",
            "Servizio pagamenti Samsung. Usa Knox per transazioni NFC. Disabilitabile se usi Google Wallet o niente.",
            "pm disable-user --user 0 com.samsung.android.spayfw",
            "pm enable com.samsung.android.spayfw",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.spayfw"""",
            group = "Samsung Apps", groupIt = "App Samsung"),

        opt("app_update_center", Optimization.Category.BLOAT,
            "App Update Center", "Centro Aggiornamenti",
            "Pushes recommended app installations and updates. Behind 'Esperienza' suggestions after system updates.",
            "Spinge installazioni e aggiornamenti app consigliati. Dietro i suggerimenti 'Esperienza' dopo aggiornamenti.",
            "pm disable-user --user 0 com.samsung.android.app.updatecenter",
            "pm enable com.samsung.android.app.updatecenter",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.app.updatecenter"""",
            group = "Samsung Apps", groupIt = "App Samsung"),

        opt("device_protection", Optimization.Category.BLOAT,
            "Device Protection (Score)", "Protezione Dispositivo",
            "Background device scoring process. ⚠️ Disabling breaks battery settings page (empty).",
            "Processo di punteggio dispositivo in background. ⚠️ Disabilitare rompe pagina impostazioni batteria (vuota).",
            "pm disable-user --user 0 com.samsung.android.lool",
            "pm enable com.samsung.android.lool",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.lool"""",
            group = "Knox & Telemetry", groupIt = "Knox e telemetria"),

        opt("scpm", Optimization.Category.BLOAT,
            "Smart Contextual Platform", "Piattaforma Contestuale",
            "Analyzes usage patterns for Samsung recommendations and personalization. ⚠️ May break battery settings page.",
            "Analizza pattern d'uso per raccomandazioni e personalizzazione Samsung. ⚠️ Può rompere pagina impostazioni batteria.",
            "pm disable-user --user 0 com.samsung.android.scpm",
            "pm enable com.samsung.android.scpm",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.scpm"""",
            group = "Knox & Telemetry", groupIt = "Knox e telemetria"),

        opt("samsung_statsd", Optimization.Category.BLOAT,
            "Samsung Analytics (statsd)", "Analytics Samsung (statsd)",
            "Samsung telemetry and usage statistics collection. ⚠️ May break battery settings page.",
            "Raccolta telemetria e statistiche d'uso Samsung. ⚠️ Può rompere pagina impostazioni batteria.",
            "pm disable-user --user 0 com.samsung.android.statsd",
            "pm enable com.samsung.android.statsd",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.statsd"""",
            group = "Knox & Telemetry", groupIt = "Knox e telemetria"),

        opt("theme_designer", Optimization.Category.BLOAT,
            "Theme Designer", "Theme Designer",
            "Standalone Samsung theme creation tool. Separate from Theme Store. Sits idle in background.",
            "Strumento creazione temi Samsung separato dal Theme Store. Resta inattivo in background.",
            "pm disable-user --user 0 com.samsung.android.themedesigner",
            "pm enable com.samsung.android.themedesigner",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.themedesigner"""",
            group = "Samsung Apps", groupIt = "App Samsung"),

        opt("share_live", Optimization.Category.BLOAT,
            "Share Live", "Share Live",
            "Real-time AR photo/video sharing service. Uses camera and network. Rarely used directly.",
            "Servizio condivisione AR foto/video in tempo reale. Usa fotocamera e rete. Usato raramente.",
            "pm disable-user --user 0 com.samsung.android.app.sharelive",
            "pm enable com.samsung.android.app.sharelive",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.app.sharelive"""",
            group = "Samsung Apps", groupIt = "App Samsung"),

        opt("easy_setup", Optimization.Category.BLOAT,
            "Easy Setup", "Easy Setup",
            "Initial device setup / transfer wizard. Useless after first configuration.",
            "Procedura configurazione/trasferimento iniziale. Inutile dopo prima configurazione.",
            "pm disable-user --user 0 com.samsung.android.easysetup",
            "pm enable com.samsung.android.easysetup",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.easysetup"""",
            group = "Samsung Apps", groupIt = "App Samsung"),

        opt("smartthings_find", Optimization.Category.BLOAT,
            "SmartThings Find", "SmartThings Find",
            "Locate Samsung devices, tags, and accessories on a map. Background location scanning.",
            "Localizza dispositivi Samsung, tag e accessori su mappa. Scansione posizione in background.",
            "pm disable-user --user 0 com.samsung.android.app.find",
            "pm enable com.samsung.android.app.find",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.app.find"""",
            group = "SmartThings"),

        opt("galaxy_appbooster", Optimization.Category.BLOAT,
            "Galaxy App Booster", "Galaxy App Booster",
            "Optimizes app performance by pre-compiling. Runs once then sits idle. Part of Good Guardians but often pushed by Samsung Experience.",
            "Ottimizza prestazioni app pre-compilando. Esegue una volta poi resta inattivo. Spesso spinto da Esperienza Samsung.",
            "pm disable-user --user 0 com.samsung.android.appbooster",
            "pm enable com.samsung.android.appbooster",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.appbooster"""",
            group = "Samsung Apps", groupIt = "App Samsung"),

        // ── GOOGLE ──
        opt("g_photos", Optimization.Category.BLOAT,
            "Google Photos", "Google Foto",
            "Cloud backup, editing, organization. Samsung Gallery works fine.",
            "Backup cloud, modifica, organizzazione. Galleria Samsung funziona.",
            "pm disable-user --user 0 com.google.android.apps.photos",
            "pm enable com.google.android.apps.photos",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.google.android.apps.photos"""",
            group = "Google"),

        opt("g_messages", Optimization.Category.BLOAT,
            "Google Messages", "Google Messaggi",
            "SMS/RCS app. Keep if it's your only SMS app!",
            "App SMS/RCS. Tienila se è la tua unica app SMS!",
            "pm disable-user --user 0 com.google.android.apps.messaging",
            "pm enable com.google.android.apps.messaging",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.google.android.apps.messaging"""",
            group = "Google"),

        opt("g_docs", Optimization.Category.BLOAT,
            "Google Docs", "Google Documenti",
            "Online document editor. Web version works in browser.",
            "Editor documenti online. Versione web funziona nel browser.",
            "pm disable-user --user 0 com.google.android.apps.docs.editors.docs",
            "pm enable com.google.android.apps.docs.editors.docs",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.google.android.apps.docs.editors.docs"""",
            group = "Google"),

        opt("g_search", Optimization.Category.BLOAT,
            "Google Search / Assistant", "Google Search / Assistente",
            "Google Assistant, Discover, Search widget. NEEDED for 'Hey Google'!",
            "Google Assistant, Discover, widget ricerca. SERVE per 'Hey Google'!",
            "pm disable-user --user 0 com.google.android.googlequicksearchbox",
            "pm enable com.google.android.googlequicksearchbox",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.google.android.googlequicksearchbox"""",
            group = "Google"),

        opt("g_cast", Optimization.Category.BLOAT,
            "Google Cast", "Google Cast",
            "Screen/content casting to Chromecast/Google TV.",
            "Trasmissione schermo/contenuti a Chromecast/Google TV.",
            "pm disable-user --user 0 com.google.android.apps.chromecast.app",
            "pm enable com.google.android.apps.chromecast.app",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.google.android.apps.chromecast.app"""",
            group = "Google"),

        opt("g_feedback", Optimization.Category.BLOAT,
            "Google Feedback", "Google Feedback",
            "Bug report tool. Never used directly.",
            "Strumento segnalazione bug. Mai usato direttamente.",
            "pm disable-user --user 0 com.google.android.feedback",
            "pm enable com.google.android.feedback",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.google.android.feedback"""",
            group = "Google"),

        opt("g_maps", Optimization.Category.BLOAT,
            "Google Maps", "Google Maps",
            "Navigation, traffic, POI search. Keep if you use it for driving!",
            "Navigazione, traffico, ricerca POI. Tienilo se guidi con Maps!",
            "pm disable-user --user 0 com.google.android.apps.maps",
            "pm enable com.google.android.apps.maps",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.google.android.apps.maps"""",
            group = "Google"),

        opt("g_supervision", Optimization.Category.BLOAT,
            "Google Family Link", "Google Family Link",
            "Parental controls. Keep if you use it for your children!",
            "Controllo parentale. Tienilo se lo usi per i figli!",
            "pm disable-user --user 0 com.google.android.gms.supervision",
            "pm enable com.google.android.gms.supervision",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.google.android.gms.supervision"""",
            group = "Google"),

        // ── FACEBOOK ──
        opt("fb_main", Optimization.Category.BLOAT,
            "Facebook App", "App Facebook",
            "Main Facebook app. Biggest known battery drainer: 15min system alarms, constant GPS, FG service.",
            "App principale Facebook. Maggior drainer di batteria: sveglie 15min, GPS costante, FG service.",
            "pm disable-user --user 0 com.facebook.katana",
            "pm enable com.facebook.katana",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.facebook.katana"""",
            group = "Meta"),

        opt("fb_messenger", Optimization.Category.BLOAT,
            "Facebook Messenger", "Facebook Messenger",
            "Chat and calls. Runs background service for push notifications.",
            "Chat e chiamate. Esegue servizio in background per notifiche push.",
            "pm disable-user --user 0 com.facebook.orca",
            "pm enable com.facebook.orca",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.facebook.orca"""",
            group = "Meta"),

        // ── MICROSOFT ──
        opt("ms_edge", Optimization.Category.BLOAT,
            "Microsoft Edge", "Microsoft Edge",
            "Web browser (~150MB). Redundant if you use Chrome/Firefox.",
            "Browser (~150MB). Ridondante se usi Chrome/Firefox.",
            "pm disable-user --user 0 com.microsoft.emmx",
            "pm enable com.microsoft.emmx",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.microsoft.emmx"""",
            group = "Microsoft"),

        opt("ms_excel", Optimization.Category.BLOAT,
            "Microsoft Excel", "Microsoft Excel",
            "Spreadsheet editor (~150MB). Use Google Sheets instead.",
            "Foglio di calcolo (~150MB). Usa Google Sheets.",
            "pm disable-user --user 0 com.microsoft.office.excel",
            "pm enable com.microsoft.office.excel",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.microsoft.office.excel"""",
            group = "Microsoft"),

        opt("ms_word", Optimization.Category.BLOAT,
            "Microsoft Word", "Microsoft Word",
            "Document editor (~150MB). Use Google Docs instead.",
            "Editor documenti (~150MB). Usa Google Docs.",
            "pm disable-user --user 0 com.microsoft.office.word",
            "pm enable com.microsoft.office.word",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.microsoft.office.word"""",
            group = "Microsoft"),

        opt("ms_rdc", Optimization.Category.BLOAT,
            "Microsoft Remote Desktop", "Microsoft Remote Desktop",
            "Remote PC connection. Use AnyDesk/TeamViewer instead.",
            "Connessione PC remoto. Usa AnyDesk/TeamViewer.",
            "pm disable-user --user 0 com.microsoft.rdc.androidx",
            "pm enable com.microsoft.rdc.androidx",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.microsoft.rdc.androidx"""",
            group = "Microsoft"),

        opt("knox_reset", Optimization.Category.MAINTENANCE,
            "Knox Matrix Reset", "Reset Knox Matrix",
            "Fixes April 2026 update battery drain loop. Clears cache (doesn't uninstall). Data auto-repopulates.",
            "Risolve loop drain da aggiornamento Aprile 2026. Pulisce cache (non disinstalla). Dati si ripopolano.",
            "pm clear com.samsung.android.knox.kpecore; pm clear com.samsung.android.knox.attestation; pm clear com.samsung.android.knox.pushmanager; pm clear com.samsung.android.knox.containercore; pm clear com.samsung.android.knox.analytics.uploader",
            "",
            group = "Resets", groupIt = "Reset"),

        opt("gms_reset", Optimization.Category.MAINTENANCE,
            "Google Play Services Reset", "Reset Google Play Services",
            "Resets stuck foreground services. You'll need to re-login to Google Pay, Smart Lock, loyalty cards. Gmail/Drive auto-sync.",
            "Resetta FG services bloccati. Dovrai rifare login Google Pay, Smart Lock, carte fedeltà. Gmail/Drive si risincronizzano.",
            "am force-stop com.google.android.gms; pm clear com.google.android.gms",
            "",
            group = "Resets", groupIt = "Reset"),

        // These four checks used to grep for a whole line equal to "deny"/"allow"/the
        // package name. Neither command prints that. `appops get <pkg> RUN_ANY_IN_BACKGROUND`
        // prints "RUN_ANY_IN_BACKGROUND: deny" (sometimes with a "; time=..." tail), and
        // `cmd deviceidle whitelist` prints "user,com.whatsapp,10423". So every one of them
        // returned 0 forever: the box could never tick, no matter what the device did.
        opt("bg_instagram", Optimization.Category.MAINTENANCE,
            "Instagram Background Restrict", "Limita Background Instagram",
            "Instagram used 771 mAh in 15h with 329K data packets in background. Blocks background activity, app works when opened.",
            "Instagram usava 771 mAh in 15h con 329K pacchetti dati in background. Blocca attività bg, app funziona quando aperta.",
            "appops set com.instagram.android RUN_ANY_IN_BACKGROUND deny",
            "appops set com.instagram.android RUN_ANY_IN_BACKGROUND allow",
            """appops get com.instagram.android RUN_ANY_IN_BACKGROUND | grep -c ": deny"""",
            group = "Background"),

        // The single biggest consumer in the 12 August measurement: 690 mAh, 41% of the
        // whole discharge, of which 247 mAh was radio with the screen off — 106 MB pulled
        // down overnight by ClipsTabBackgroundPrefetchWorker and friends. appops alone did
        // not stop it; the netpolicy blacklist denies background metered data outright, and
        // it does so whether or not Data Saver is on.
        //
        // The UID is looked up at run time rather than baked in: it is assigned at install
        // and changes if the app is reinstalled.
        opt("ig_bg_data", Optimization.Category.MAINTENANCE,
            "Instagram Background Data OFF", "Dati Background Instagram OFF",
            "Ticked: Instagram is denied mobile data while it is in the background. Stops the overnight prefetch (106 MB in one night). The feed still refreshes when you open the app, and WiFi is unaffected.",
            "Spuntato: a Instagram sono negati i dati mobili quando è in background. Ferma il prefetch notturno (106 MB in una notte). Il feed si aggiorna comunque quando apri l'app, e il WiFi non è toccato.",
            """cmd netpolicy add restrict-background-blacklist "$(pm list packages -U | grep -w "package:com.instagram.android" | sed "s/.*uid://")"""",
            """cmd netpolicy remove restrict-background-blacklist "$(pm list packages -U | grep -w "package:com.instagram.android" | sed "s/.*uid://")"""",
            """cmd netpolicy list restrict-background-blacklist | grep -cw "$(pm list packages -U | grep -w "package:com.instagram.android" | sed "s/.*uid://")"""",
            icon = Icons.Default.NetworkCheck,
            group = "Background"),

        // The tick follows the live bucket, which the system re-evaluates on its own: while
        // the phone is charging or the app was opened recently it reads 5 (exempted) no
        // matter what was set. So this box unticks itself under those conditions — that is
        // the device's answer, not a failed write.
        opt("ig_standby_restricted", Optimization.Category.MAINTENANCE,
            "Instagram Restricted Bucket", "Instagram Bucket Limitato",
            "Puts Instagram in the RESTRICTED standby bucket: its jobs and alarms get the smallest quota Android hands out. The system re-evaluates the bucket, so it reads as exempt while charging or just after you use the app.",
            "Mette Instagram nel bucket standby RESTRICTED: job e sveglie ricevono la quota più bassa prevista da Android. Il sistema rivaluta il bucket, quindi risulta esente mentre il telefono è in carica o subito dopo aver usato l'app.",
            "am set-standby-bucket com.instagram.android restricted",
            "am set-standby-bucket com.instagram.android active",
            """am get-standby-bucket com.instagram.android | grep -cFx "45"""",
            icon = Icons.Default.Pause,
            group = "Background"),

        // Removed by hand on 12 August and back in the list by 13 August: Huawei Health
        // re-adds itself. Worth an entry precisely because it does not stay done.
        opt("huawei_doze_remove", Optimization.Category.MAINTENANCE,
            "Huawei Health Doze Exempt OFF", "Huawei Health Esente Doze OFF",
            "Ticked: Huawei Health is out of the Doze whitelist, so it sleeps like everything else. It drew 38 mAh overnight. The app re-adds itself, so expect to apply this again.",
            "Spuntato: Huawei Health è fuori dalla whitelist Doze, quindi dorme come tutto il resto. Consumava 38 mAh a notte. L'app si re-inserisce da sola, quindi aspettati di doverla riapplicare.",
            "cmd deviceidle whitelist -com.huawei.health",
            "cmd deviceidle whitelist +com.huawei.health",
            // Counts the matches, then counts how many of those counts are zero: 1 when the
            // package is gone from the list. An `if` would need a `; `, which is the
            // separator opt() splits commands on.
            """cmd deviceidle whitelist 2>/dev/null | grep -c ",com.huawei.health," | grep -cFx "0"""",
            icon = Icons.Default.Bedtime,
            group = "Background"),

        opt("bg_tandem", Optimization.Category.MAINTENANCE,
            "Tandem Background Restrict", "Limita Background Tandem",
            "The Tandem language-exchange app, using 80.6 mAh in background. Blocks background activity; it still works when opened.",
            "L'app di scambio linguistico Tandem, con 80.6 mAh in background. Blocca l'attività in background; funziona comunque quando aperta.",
            "appops set net.tandem RUN_ANY_IN_BACKGROUND deny",
            "appops set net.tandem RUN_ANY_IN_BACKGROUND allow",
            """appops get net.tandem RUN_ANY_IN_BACKGROUND | grep -c ": deny"""",
            group = "Background"),

        opt("wa_background_unrestricted", Optimization.Category.MAINTENANCE,
            "WhatsApp Unrestricted Battery", "WhatsApp Batteria No Limit",
            "Sets WhatsApp battery to 'Unrestricted' via appops. Prevents One UI from killing WhatsApp in background during calls. Equivalent to: Settings > Apps > WhatsApp > Battery > Unrestricted.",
            "Imposta batteria WhatsApp su 'Nessuna restrizione' via appops. Impedisce a One UI di uccidere WhatsApp in background durante le chiamate. Equivalente: Impostazioni > App > WhatsApp > Batteria > Nessuna restrizione.",
            "appops set com.whatsapp RUN_ANY_IN_BACKGROUND allow",
            "appops set com.whatsapp RUN_ANY_IN_BACKGROUND deny",
            """appops get com.whatsapp RUN_ANY_IN_BACKGROUND | grep -c ": allow"""",
            group = "WhatsApp"),

        opt("wa_doze_whitelist", Optimization.Category.MAINTENANCE,
            "WhatsApp Doze Exempt", "WhatsApp Esente Doze",
            "Whitelists WhatsApp from Android Doze mode. Prevents system from delaying WhatsApp's network access when device is idle. Critical for reliable VoIP calls.",
            "Whitelista WhatsApp da Doze mode. Impedisce al sistema di ritardare l'accesso di rete di WhatsApp quando il dispositivo è in idle. Essenziale per chiamate VoIP affidabili.",
            "cmd deviceidle whitelist +com.whatsapp",
            "cmd deviceidle whitelist -com.whatsapp",
            """cmd deviceidle whitelist 2>/dev/null | grep -c ",com.whatsapp,"""",
            group = "WhatsApp"),

        // ── SYSTEM ──
        opt("auto_restrict", Optimization.Category.SYSTEM,
            "App Auto Restriction ON", "Auto Limitazione App ON",
            "Ticked: apps unused for 3+ days are put into standby automatically. More aggressive than Adaptive Battery. Unverified: the key holds the value but nothing on this device was seen to read it.",
            "Spuntato: le app non usate da 3+ giorni vengono messe in standby automaticamente. Più aggressiva della Batteria Adattiva. Non verificata: la chiave conserva il valore ma non risulta letta da nulla su questo dispositivo.",
            "settings put global app_auto_restriction_enabled 1",
            "settings put global app_auto_restriction_enabled 0",
            """settings get global app_auto_restriction_enabled | grep -cFx "1"""",
            verified = false,
            group = "Battery", groupIt = "Batteria"),

        // `battery_saver_mode` was invented by this app: the settings row had no default
        // and its first write came from the shell. `low_power` is the one PowerManager
        // reads — writing it makes the framework echo the value straight back, and
        // `dumpsys power` reports mSettingBatterySaverEnabled accordingly.
        opt("batt_saver", Optimization.Category.SYSTEM,
            "Battery Saver ON", "Risparmio Energetico ON",
            "Ticked: Battery Saver is on — lower brightness, slower CPU, background limited. Minor performance impact.",
            "Spuntato: il Risparmio Energetico è attivo — luminosità ridotta, CPU rallentata, background limitato. Leggero impatto sulle prestazioni.",
            "settings put global low_power 1",
            "settings put global low_power 0",
            """settings get global low_power | grep -cFx "1"""",
            group = "Battery", groupIt = "Batteria"),

        opt("cpu_resp", Optimization.Category.SYSTEM,
            "Enhanced CPU Responsiveness OFF", "Risposta CPU Migliorata OFF",
            "Ticked: the CPU is no longer held at a high frequency for touch and gestures. Note: this device already ships with it off, so the tick reflects the factory state rather than a change made here.",
            "Spuntato: la CPU non viene più tenuta a frequenza alta per tocco e gesti. Nota: su questo dispositivo è già spenta di fabbrica, quindi la spunta riflette lo stato di serie, non una modifica fatta qui.",
            "settings put global sem_enhanced_cpu_responsiveness 0",
            "settings put global sem_enhanced_cpu_responsiveness 1",
            """settings get global sem_enhanced_cpu_responsiveness | grep -cFx "0"""",
            group = "Battery", groupIt = "Batteria"),

        opt("enhanced_proc", Optimization.Category.SYSTEM,
            "Enhanced Processing OFF", "Elaborazione Migliorata OFF",
            "Ticked: CPU boosting for heavy apps (games, video) is off, for a quieter standby. Unverified: no system process was ever seen writing this key on this device, so the tick may mean nothing.",
            "Spuntato: il boosting CPU per app pesanti (giochi, video) è disattivato, per uno standby più tranquillo. Non verificata: nessun processo di sistema ha mai scritto questa chiave su questo dispositivo, quindi la spunta potrebbe non significare nulla.",
            "settings put global enhanced_processing 0",
            "settings put global enhanced_processing 1",
            """settings get global enhanced_processing | grep -cFx "0"""",
            verified = false,
            group = "Battery", groupIt = "Batteria"),

        opt("ble_scan", Optimization.Category.SYSTEM,
            "BLE Always-Scanning OFF", "Scansione BLE Continua OFF",
            "Ticked: continuous Bluetooth Low Energy scanning is off. One of the larger standby savings.",
            "Spuntato: la scansione Bluetooth Low Energy continua è disattivata. Uno dei risparmi maggiori in standby.",
            "settings put global ble_scan_always_enabled 0",
            "settings put global ble_scan_always_enabled 1",
            """settings get global ble_scan_always_enabled | grep -cFx "0"""",
            group = "Network", groupIt = "Rete"),

        opt("aod", Optimization.Category.SYSTEM,
            "Always On Display OFF", "Always On Display OFF",
            "Ticked: Always On Display is off, so the idle screen no longer shows clock and notifications. Saves ~3-5% battery/day.",
            "Spuntato: Always On Display disattivato, lo schermo a riposo non mostra più ora e notifiche. Risparmia ~3-5% di batteria al giorno.",
            // `global always_on_display_enabled` used to be written alongside this and was
            // dropped: it is a row this app created, while `system aod_mode` is owned by
            // com.samsung.android.app.aodservice and is what the panel actually reads.
            "settings put system aod_mode 0",
            "settings put system aod_mode 1",
            """settings get system aod_mode | grep -cFx "0"""",
            group = "Display", groupIt = "Schermo"),

        opt("screen_timeout", Optimization.Category.SYSTEM,
            "Screen Timeout 30s", "Timeout Schermo 30s",
            "Screen turns off after 30 seconds of inactivity.",
            "Schermo si spegne dopo 30 secondi di inattività.",
            "settings put system screen_off_timeout 30000",
            "settings put system screen_off_timeout 60000",
            """settings get system screen_off_timeout | grep -cFx "30000"""",
            group = "Display", groupIt = "Schermo"),

        opt("notif_led", Optimization.Category.SYSTEM,
            "Notification LED OFF", "LED Notifiche OFF",
            "Ticked: the notification LED no longer blinks. Minimal saving.",
            "Spuntato: il LED notifiche non lampeggia più. Risparmio minimo.",
            "settings put system notification_light_pulse 0",
            "settings put system notification_light_pulse 1",
            """settings get system notification_light_pulse | grep -cFx "0"""",
            group = "Display", groupIt = "Schermo"),

        // No "Deep Doze Mode" entry: it wrote `secure doze_enabled`, which in AOSP is the
        // ambient-display pulse (its neighbours are doze_pulse_on_double_tap and
        // doze_quick_pickup_gesture), not the Doze idle state machine. It also had no
        // default row on this device. Real Doze tuning is the `quick_doze` entry below,
        // which writes device_idle_constants — the keys DeviceIdleController parses.

        opt("animations", Optimization.Category.SYSTEM,
            "Reduce Animations (0.5x)", "Riduci Animazioni (0.5x)",
            "Halves animation speed. Saves GPU power. Phone feels slightly less smooth.",
            "Riduce velocità animazioni a metà. Risparmia GPU. Telefono leggermente meno fluido.",
            "settings put global window_animation_scale 0.5; settings put global transition_animation_scale 0.5; settings put global animator_duration_scale 0.5",
            "settings put global window_animation_scale 1; settings put global transition_animation_scale 1; settings put global animator_duration_scale 1",
            """settings get global window_animation_scale | grep -cFx "0.5"""",
            group = "Display", groupIt = "Schermo"),

        // protect_battery is the key that actually decides what happens: 0 = off,
        // 1 = Maximum (charging stops at battery_protection_threshold), 3 = Basic
        // (charges to 100%, waits for 95% before topping up). Verified from the One UI
        // screen itself. battery_protection_default_value, which this used to write, is
        // not read by the charger: the cap was reported as applied while the phone kept
        // charging to 100%.
        opt("batt_protect", Optimization.Category.SYSTEM,
            "Battery Protection 80%", "Protezione Batteria 80%",
            "Stops charging at 80% (One UI \"Maximum\"). Extends battery lifespan. Replaces adaptive charging (needs ACTIVITY_RECOGNITION).",
            "Ferma ricarica a 80% (\"Massima\" in One UI). Prolunga vita batteria. Sostituisce ricarica adattiva (che serve ACTIVITY_RECOGNITION).",
            "settings put global protect_battery 1; settings put global battery_protection_threshold 80",
            "settings put global protect_battery 0",
            """settings get global protect_battery | grep -cFx "1"; settings get global battery_protection_threshold | grep -cFx "80"""",
            group = "Battery", groupIt = "Batteria"),

        // No "WiFi Power Save" entry: `global wifi_power_save` is a real key, but the
        // system ships it at 120 — it is a duration, not a switch. Writing 1 and 0 into it
        // replaced a tuning value with a meaningless one and reported that as an
        // optimization. Reverting it means restoring 120, not 0.

        opt("fast_charge", Optimization.Category.SYSTEM,
            "Adaptive Fast Charging OFF", "Ricarica Rapida Adattiva OFF",
            "Ticked: adaptive fast wired charging is off, so the phone charges slower and cooler. May extend battery lifespan when charging overnight.",
            "Spuntato: la ricarica rapida adattiva via cavo è disattivata, il telefono carica più lentamente e più freddo. Può allungare la vita della batteria caricando di notte.",
            "settings put system adaptive_fast_charging 0",
            "settings put system adaptive_fast_charging 1",
            """settings get system adaptive_fast_charging | grep -cFx "0"""",
            group = "Battery", groupIt = "Batteria"),

        opt("extra_brightness", Optimization.Category.SYSTEM,
            "Extra Brightness OFF", "Luminosità Extra OFF",
            "Ticked: brightness can no longer go above the normal maximum. Saves power outdoors.",
            "Spuntato: la luminosità non può più superare il massimo normale. Risparmia energia all'aperto.",
            "settings put secure screen_extra_brightness 0",
            "settings put secure screen_extra_brightness 1",
            """settings get secure screen_extra_brightness | grep -cFx "0"""",
            group = "Display", groupIt = "Schermo"),

        // `system auto_brightness` is not a row this device has; the real one is
        // `screen_brightness_mode` (0 manual, 1 automatic), and `dumpsys display` reports
        // the resulting autoBrightness state.
        opt("auto_brightness_off", Optimization.Category.SYSTEM,
            "Auto Brightness OFF", "Luminosità Automatica OFF",
            "Ticked: automatic brightness is off, so the level stays where you put it. Saves the light sensor's draw, at the cost of adjusting by hand.",
            "Spuntato: la luminosità automatica è disattivata, il livello resta quello che imposti. Risparmia il consumo del sensore di luce, al prezzo di regolarla a mano.",
            "settings put system screen_brightness_mode 0",
            "settings put system screen_brightness_mode 1",
            """settings get system screen_brightness_mode | grep -cFx "0"""",
            group = "Display", groupIt = "Schermo"),

        opt("battery_percent", Optimization.Category.SYSTEM,
            "Show Battery Percentage", "Mostra Percentuale Batteria",
            "Shows battery percentage inside the status bar icon.",
            "Mostra percentuale batteria dentro l'icona della barra di stato.",
            "settings put system display_battery_percentage 1",
            "settings put system display_battery_percentage 0",
            """settings get system display_battery_percentage | grep -cFx "1"""",
            group = "Display", groupIt = "Schermo"),

        // The values were the wrong way round. UiModeManager reads 1 as NIGHT_NO and 2 as
        // NIGHT_YES — `dumpsys uimode` on this device prints mNightMode=1 (no) while the
        // entry claimed 1 meant dark. Applying "Dark Mode" forced light mode.
        opt("dark_mode", Optimization.Category.SYSTEM,
            "Dark Mode (permanent)", "Modalità Scura (permanente)",
            "Forces dark mode at all times. AMOLED black pixels save battery on the display.",
            "Forza modalità scura sempre. I pixel neri AMOLED risparmiano batteria sul display.",
            "settings put secure ui_night_mode 2",
            "settings put secure ui_night_mode 1",
            """settings get secure ui_night_mode | grep -cFx "2"""",
            group = "Display", groupIt = "Schermo"),

        opt("double_tap_wake", Optimization.Category.SYSTEM,
            "Double Tap to Wake OFF", "Doppio Tocco per Accendere OFF",
            "Ticked: double-tapping no longer wakes the screen. Removes the touch digitizer's standby draw.",
            "Spuntato: il doppio tocco non accende più lo schermo. Elimina il consumo in standby del digitizer.",
            // The AOSP key is `secure double_tap_to_wake`, and writing it looks like it works:
            // the row exists, the write succeeds, the check reads back what it wrote. But the
            // gesture keeps waking the screen, because One UI reads its own key — spelled
            // `double_tab_to_wake_up`, with Samsung's typo — inserted under `system` by
            // `android` at first boot. That is the one that decides.
            "settings put system double_tab_to_wake_up 0",
            "settings put system double_tab_to_wake_up 1",
            """settings get system double_tab_to_wake_up | grep -cFx "0"""",
            group = "Gestures", groupIt = "Gesti"),

        // ── NEW: Audio & Calls ──
        opt("call_extra_vol", Optimization.Category.SYSTEM,
            "Call Extra Volume OFF", "Volume Chiamata Extra OFF",
            "Ticked: the extra loud call volume option is off. Slight speaker protection benefit.",
            "Spuntato: l'opzione volume extra in chiamata è disattivata. Leggera protezione dell'altoparlante.",
            "settings put system call_extra_volume 0",
            "settings put system call_extra_volume 1",
            """settings get system call_extra_volume | grep -cFx "0"""",
            group = "Calls", groupIt = "Chiamate"),

        opt("call_noise_off", Optimization.Category.SYSTEM,
            "Call Noise Reduction OFF", "Riduzione Rumore OFF",
            "Ticked: microphone noise suppression during calls is off. May improve battery on long calls.",
            "Spuntato: la soppressione del rumore del microfono in chiamata è disattivata. Può migliorare la batteria nelle chiamate lunghe.",
            "settings put system call_noise_reduction 0",
            "settings put system call_noise_reduction 1",
            """settings get system call_noise_reduction | grep -cFx "0"""",
            group = "Calls", groupIt = "Chiamate"),

        opt("call_vib_off", Optimization.Category.SYSTEM,
            "Call Connect/Vibrate OFF", "Vibrazione Chiamata OFF",
            "Ticked: the phone no longer vibrates when a call connects or ends. Saves a tiny amount of battery per call.",
            "Spuntato: il telefono non vibra più quando una chiamata si connette o finisce. Risparmio minimo per chiamata.",
            "settings put system call_answer_vib 0; settings put system call_end_vib 0",
            "settings put system call_answer_vib 1; settings put system call_end_vib 1",
            """settings get system call_answer_vib | grep -cFx "0"""",
            group = "Calls", groupIt = "Chiamate"),

        // ── NEW: Gestures & Navigation ──
        opt("double_tap_sleep", Optimization.Category.SYSTEM,
            "Double Tap to Sleep", "Doppio Tocco per Spegnere",
            "Double-tap on home screen or lock screen to turn screen off.",
            "Doppio tocco sulla schermata home o blocco per spegnere schermo.",
            // AOSP keeps this under `secure`, but One UI owns its own copy under `system`
            // (default 1, written by Samsung at first boot). The `secure` row does not exist
            // here, so the old commands created one nothing reads.
            "settings put system double_tap_to_sleep 1",
            "settings put system double_tap_to_sleep 0",
            """settings get system double_tap_to_sleep | grep -cFx "1"""",
            group = "Gestures", groupIt = "Gesti"),

        opt("lift_wake_off", Optimization.Category.SYSTEM,
            "Lift to Wake OFF", "Solleva per Accendere OFF",
            "Ticked: picking the phone up no longer turns the screen on. Removes the accelerometer's standby draw.",
            "Spuntato: sollevare il telefono non accende più lo schermo. Elimina il consumo in standby dell'accelerometro.",
            "settings put system lift_to_wake 0",
            "settings put system lift_to_wake 1",
            """settings get system lift_to_wake | grep -cFx "0"""",
            group = "Gestures", groupIt = "Gesti"),

        opt("smart_stay_off", Optimization.Category.SYSTEM,
            "Smart Stay (Eye Tracking) OFF", "Smart Stay (Tracciamento Occhi) OFF",
            "Ticked: front-camera eye tracking no longer keeps the screen on while you look at it. Saves ~5% battery/day.",
            "Spuntato: il tracciamento occhi della fotocamera frontale non tiene più acceso lo schermo mentre guardi. Risparmia ~5% di batteria al giorno.",
            "settings put system intelligent_sleep_mode 0",
            "settings put system intelligent_sleep_mode 1",
            """settings get system intelligent_sleep_mode | grep -cFx "0"""",
            group = "Gestures", groupIt = "Gesti"),

        opt("one_handed_off", Optimization.Category.SYSTEM,
            "One-Handed Mode OFF", "Modalità Una Mano OFF",
            "Ticked: the one-handed mode shortcut is off. Fewer accidental triggers, and one less background service.",
            "Spuntato: la scorciatoia della modalità una mano è disattivata. Meno attivazioni accidentali e un servizio in background in meno.",
            "settings put secure one_handed_mode_enabled 0",
            "settings put secure one_handed_mode_enabled 1",
            """settings get secure one_handed_mode_enabled | grep -cFx "0"""",
            group = "Gestures", groupIt = "Gesti"),

        // ── ONE UI 8.5 ──
        // `adaptive_battery_enabled` does not exist on this device — the row One UI keeps
        // (and Diagnostics already reads) is `adaptive_battery_management_enabled`.
        opt("adaptive_batt_off", Optimization.Category.SYSTEM,
            "Adaptive Battery OFF", "Batteria Adattiva OFF",
            "Ticked: Adaptive Battery is off, so One UI no longer limits background activity on its own. Can fix delayed notifications and VoIP call issues. Unverified: this row did not exist until the app wrote it, and no system package has ever touched it — One UI most likely keeps this switch outside the settings provider.",
            "Spuntato: la Batteria Adattiva è disattivata, One UI non limita più da sola l'attività in background. Può risolvere notifiche ritardate e problemi con le chiamate VoIP. Non verificata: questa riga non esisteva prima che la scrivesse l'app e nessun pacchetto di sistema l'ha mai toccata — One UI con ogni probabilità tiene questo interruttore fuori dal settings provider.",
            "settings put global adaptive_battery_management_enabled 0",
            "settings put global adaptive_battery_management_enabled 1",
            """settings get global adaptive_battery_management_enabled | grep -cFx "0"""",
            verified = false,
            group = "Battery", groupIt = "Batteria"),

        // No "Network Battery Saver OFF" and no "Personal Data Intelligence OFF" entries:
        // neither `network_battery_saver_enabled` nor `pdi_usage_enabled` exists in any
        // namespace on this device. Both were One UI 8.5 features named from release notes
        // and guessed at as key names; the writes only ever created rows for themselves.

        // `ram_plus_size` was never a row here either. The one NandswapManager and
        // com.samsung.android.lool write is `ram_expand_size`, and its factory default is
        // 4096 — so reverting restores 4096, not "delete the key".
        opt("ram_plus_off", Optimization.Category.SYSTEM,
            "RAM Plus OFF", "RAM Plus OFF",
            "Ticked: RAM Plus (virtual RAM on storage) is off. Frees storage and reduces flash wear — Samsung's virtual RAM can slow the device down by using storage as swap. Needs a reboot to take effect.",
            "Spuntato: RAM Plus (RAM virtuale su storage) è disattivata. Libera spazio e riduce l'usura della flash — la RAM virtuale Samsung può rallentare il dispositivo usando lo storage come swap. Richiede un riavvio.",
            "settings put global ram_expand_size 0",
            "settings put global ram_expand_size 4096",
            """settings get global ram_expand_size | grep -cFx "0"""",
            group = "Battery", groupIt = "Batteria"),

        opt("error_reports_off", Optimization.Category.SYSTEM,
            "Samsung Error Reports OFF", "Segnalazioni Errori OFF",
            "Ticked: app crash and diagnostic reports are no longer sent to Samsung's servers. No impact on how the phone works.",
            "Spuntato: i report di crash e diagnostici non vengono più inviati ai server Samsung. Nessun impatto sul funzionamento del telefono.",
            "settings put global send_action_app_error 0",
            "settings put global send_action_app_error 1",
            """settings get global send_action_app_error | grep -cFx "0"""",
            group = "Privacy"),

        opt("hqm_telemetry_off", Optimization.Category.SYSTEM,
            "Samsung HQM Telemetry OFF", "Telemetria HQM Samsung OFF",
            "Ticked: High Quality Monitoring — Samsung's background usage data collection — is off. Pure telemetry, zero impact on features.",
            "Spuntato: High Quality Monitoring — la raccolta dati d'uso in background di Samsung — è disattivato. Solo telemetria, zero impatto sulle funzioni.",
            "settings put system samsung_eula_agree_hqm 0; settings put system samsung_errorlog_agree 0",
            "settings put system samsung_eula_agree_hqm 1; settings put system samsung_errorlog_agree 1",
            """settings get system samsung_eula_agree_hqm | grep -cFx "0"""",
            group = "Privacy"),

        opt("nearby_scan_off", Optimization.Category.SYSTEM,
            "Nearby Scan OFF", "Scansione Vicinanza OFF",
            "Ticked: Nearby Device Scanning is off, so the phone stops scanning over Bluetooth for Samsung devices. Significant saving if you do not use Galaxy Buds/Watch daily. Unverified: the key holds the value but no reader for it was found on this device.",
            "Spuntato: la Scansione Dispositivi Vicini è disattivata, il telefono smette di cercare dispositivi Samsung via Bluetooth. Risparmio notevole se non usi Galaxy Buds/Watch ogni giorno. Non verificata: la chiave conserva il valore ma non è stato trovato nulla che la legga.",
            "settings put global nearby_scanning_enabled 0",
            "settings put global nearby_scanning_enabled 1",
            """settings get global nearby_scanning_enabled | grep -cFx "0"""",
            verified = false,
            group = "Network", groupIt = "Rete"),

        // ── NEW: Connectivity ──
        opt("wifi_scan_throttle", Optimization.Category.SYSTEM,
            "WiFi Scan Throttle ON", "Limite Scansione WiFi ON",
            "Ticked: WiFi scan frequency is capped. Less drain from constant network scanning. Unverified: the key holds the value but no reader for it was found on this device.",
            "Spuntato: la frequenza di scansione WiFi è limitata. Meno consumo dalla scansione di rete costante. Non verificata: la chiave conserva il valore ma non è stato trovato nulla che la legga.",
            "settings put global wifi_scan_throttle_enabled 1",
            "settings put global wifi_scan_throttle_enabled 0",
            """settings get global wifi_scan_throttle_enabled | grep -cFx "1"""",
            verified = false,
            group = "Network", groupIt = "Rete"),

        // Half of this entry wrote `wifi_switch_to_better_wifi_enabled`, a row that does not
        // exist here — and the check read that half, so the box never ticked even when the
        // other half had applied. Only the key this device actually has is left.
        opt("wifi_switch_off", Optimization.Category.SYSTEM,
            "Auto WiFi Switch OFF", "Commuta WiFi Auto OFF",
            "Ticked: the phone no longer drops WiFi for mobile data when the connection is weak. Less background scanning.",
            "Spuntato: il telefono non passa più dal WiFi ai dati mobili quando la connessione è debole. Meno scansione in background.",
            "settings put global wifi_switch_to_mobile_data_ins 0",
            "settings put global wifi_switch_to_mobile_data_ins 1",
            """settings get global wifi_switch_to_mobile_data_ins | grep -cFx "0"""",
            group = "Network", groupIt = "Rete"),

        // ── NEW: Touch ──
        opt("touch_sensitivity", Optimization.Category.SYSTEM,
            "Touch Sensitivity (Low)", "Sensibilità Tocco (Bassa)",
            "Reduces touch sensitivity. May prevent accidental touches. Slight digitizer power saving.",
            "Riduce sensibilità tocco. Previene tocchi accidentali. Leggero risparmio digitizer.",
            "settings put system auto_adjust_touch 0",
            "settings put system auto_adjust_touch 1",
            """settings get system auto_adjust_touch | grep -cFx "0"""",
            group = "Display", groupIt = "Schermo"),

        // ── NEW: System ──
        // No shell commands: `global auto_sync` is a row this device does not have, so the
        // old apply/revert created one for themselves and the check read it straight back.
        // Master sync only moves through ContentResolver — see [MasterSync].
        opt("auto_sync_off", Optimization.Category.SYSTEM,
            "Auto Sync OFF", "Sincronizzazione Auto OFF",
            "Ticked: automatic account sync is off, so apps no longer refresh in the background. One of the larger savings.",
            "Spuntato: la sincronizzazione automatica degli account è disattivata, le app non si aggiornano più in background. Uno dei risparmi maggiori.",
            "", "", "",
            icon = Icons.Default.SyncDisabled,
            local = Optimization.LocalAction.MASTER_SYNC_OFF,
            group = "Network", groupIt = "Rete"),

        // ── REDDIT JULY 2026: Samsung Customization Service Fix ──
        opt("customization_svc_activity", Optimization.Category.SYSTEM,
            "Customization Service Fix", "Fix Customization Service",
            "Samsung Customization Service wakes Play Services every 15min via Physical Activity tracking. Revoking ACTIVITY_RECOGNITION stops this drain loop. Biggest standby fix found on Reddit July 2026.",
            "Customization Service sveglia Play Services ogni 15min tramite Physical Activity. Revocare ACTIVITY_RECOGNITION ferma il loop. Più grande fix standby trovato su Reddit luglio 2026.",
            "pm revoke com.samsung.android.rubin.app android.permission.ACTIVITY_RECOGNITION",
            "pm grant com.samsung.android.rubin.app android.permission.ACTIVITY_RECOGNITION",
            "",
            group = "Privacy"),

        // No "Meta App Manager" entry: it targeted com.facebook.appmanager and
        // com.facebook.system, and `pm list packages` shows neither is installed for user 0
        // on this device. The advice is real for phones that ship them — this one does not.

        // No "Usage & Diagnostics OFF" entry: `global usage_setting` does not exist in any
        // namespace here. The Settings toggle it was named after is backed by something else.

        // ── REFRESH RATE (from Galaxy Max Hz) ──
        opt("adaptive_120", Optimization.Category.REFRESH_RATE,
            "Adaptive 120Hz", "120Hz Adattivo",
            "Variable refresh rate 24-120Hz: 120Hz when touching, drops to 24Hz when idle. Best balance of smoothness and battery. Equivalent to Galaxy Max Hz Adaptive mode.",
            "Frequenza variabile 24-120Hz: 120Hz al tocco, scende a 24Hz quando fermo. Miglior bilanciamento tra fluidità e batteria. Equivalente alla modalità Adattiva di Galaxy Max Hz.",
            "settings put secure refresh_rate_mode 2; settings put system peak_refresh_rate 120.0; settings put system user_refresh_rate 120.0; settings put system min_refresh_rate 24.0",
            "settings put secure refresh_rate_mode 0; settings put system peak_refresh_rate 60.0; settings put system user_refresh_rate 60.0; settings put system min_refresh_rate 60.0",
            """settings get secure refresh_rate_mode | grep -cFx "2"""",
            icon = Icons.AutoMirrored.Filled.ShowChart),

        opt("high_96", Optimization.Category.REFRESH_RATE,
            "High 96Hz (Battery Save)", "96Hz Fisso (Risparmio)",
            "Fixed 96Hz refresh rate. Nearly as smooth as 120Hz but uses ~15% less GPU power. Equivalent to Galaxy Max Hz High mode. Best for all-day battery without sacrificing smoothness.",
            "Frequenza fissa 96Hz. Quasi fluido come 120Hz ma usa ~15% meno GPU. Equivalente alla modalità Alta di Galaxy Max Hz. Ideale per batteria giornaliera senza sacrificare fluidità.",
            "settings put secure refresh_rate_mode 1; settings put system peak_refresh_rate 96.0; settings put system user_refresh_rate 96.0; settings put system min_refresh_rate 96.0",
            "settings put secure refresh_rate_mode 0; settings put system peak_refresh_rate 60.0; settings put system user_refresh_rate 60.0; settings put system min_refresh_rate 60.0",
            """settings get secure refresh_rate_mode | grep -cFx "1"""",
            icon = Icons.AutoMirrored.Filled.TrendingUp),

        opt("standard_60", Optimization.Category.REFRESH_RATE,
            "Standard 60Hz", "Standard 60Hz",
            "Fixed 60Hz refresh rate. Maximum battery savings. Not as smooth but significantly extends battery life. Equivalent to Galaxy Max Hz Standard mode.",
            "Frequenza fissa 60Hz. Massimo risparmio batteria. Meno fluido ma allunga significativamente la batteria. Equivalente alla modalità Standard di Galaxy Max Hz.",
            "settings put secure refresh_rate_mode 0; settings put system peak_refresh_rate 60.0; settings put system user_refresh_rate 60.0; settings put system min_refresh_rate 60.0",
            "settings put secure refresh_rate_mode 2; settings put system peak_refresh_rate 120.0; settings put system user_refresh_rate 120.0; settings put system min_refresh_rate 24.0",
            """settings get secure refresh_rate_mode | grep -cFx "0"""",
            icon = Icons.Default.Pause),

        opt("psm_hz_override", Optimization.Category.REFRESH_RATE,
            "High Hz on Power Saving", "Alta Frequenza in PSM",
            "Samsung locks refresh rate to 60Hz when Power Saving Mode is on. This override keeps your selected high refresh rate active even in PSM. Equivalent to Galaxy Max Hz 'Keep Motion Smoothness on PSM'.",
            "Samsung blocca a 60Hz quando il Risparmio Energetico è attivo. Questa mod mantiene l'alta frequenza anche in PSM. Equivalente a 'Mantieni Fluidità in PSM' di Galaxy Max Hz.",
            "settings put global pms_settings_refresh_rate_enabled 0; settings put global psm_refresh_rate_tag 0",
            "settings delete global pms_settings_refresh_rate_enabled; settings delete global psm_refresh_rate_tag",
            """settings get global pms_settings_refresh_rate_enabled | grep -cFx "0"""",
            icon = Icons.Default.PowerSettingsNew),

        opt("quick_doze", Optimization.Category.SYSTEM,
            "Quick Doze 30s ON", "Doze Rapido 30s ON",
            "Ticked: Doze starts 30 seconds after the screen goes off, instead of the stock 60+ minutes. Large standby drain reduction.",
            "Spuntato: il Doze parte 30 secondi dopo lo spegnimento dello schermo, invece dei 60+ minuti di serie. Grande riduzione del consumo in standby.",
            "settings put global device_idle_constants inactive_to=30000,sensing_to=30000,idle_after_inactive_to=3000,idle_pending_to=30000,max_idle_pending_to=30000,max_idle_to=14400000",
            "settings delete global device_idle_constants",
            """settings get global device_idle_constants | grep -c "inactive_to=30000"""",
            icon = Icons.Default.Bedtime,
            group = "Battery", groupIt = "Batteria"),

        opt("perf_restrict", Optimization.Category.SYSTEM,
            "Unlock CPU in PSM", "Sblocca CPU in PSM",
            "Power Saving Mode normally restricts CPU performance. Keeps full CPU speed while still saving battery on screen/app behavior.",
            "Il Risparmio Energetico normalmente limita la CPU. Mantiene la CPU piena risparmiando comunque su schermo/app.",
            "settings put global restricted_device_performance 0,1",
            "settings delete global restricted_device_performance",
            """settings get global restricted_device_performance | grep -cFx "0,1"""",
            icon = Icons.Default.Bolt,
            group = "Battery", groupIt = "Batteria"),

        // ── PER-APP REFRESH RATE ──
        opt("per_app_refresh_rate", Optimization.Category.PER_APP_RR,
            "Per-App Refresh Rate", "Frequenza Personalizzata per App",
            "Assign different refresh rates (60/96/120Hz) to individual apps. Uses AccessibilityService to detect foreground app and apply its rate. Requires enabling in System Settings → Accessibility → Installed Apps.",
            "Assegna frequenze diverse (60/96/120Hz) a singole app. Usa AccessibilityService per rilevare l'app in primo piano. Richiede attivazione in Impostazioni → Accessibilità → App installate.",
            "settings put secure s24opt_per_app_rr_enabled 1",
            "settings delete secure s24opt_per_app_rr_enabled",
            """settings get secure s24opt_per_app_rr_enabled | grep -cFx "1"""",
            icon = Icons.Default.Apps),

        // ── ADVANCED GMH FEATURES ──
        opt("resolution_720p", Optimization.Category.ADVANCED,
            "720p Resolution (Battery Save)", "Risoluzione 720p (Risparmio)",
            "Sets display to 720p. Fewer pixels = less GPU work = significant battery savings. Good for media consumption. Uses wm size command.",
            "Imposta display a 720p. Meno pixel = meno GPU = notevole risparmio batteria. Ideale per video. Usa comando wm size.",
            "wm size 720x1560; wm density 320",
            "wm size reset; wm density reset",
            """wm size | grep -c "720x1560"""",
            icon = Icons.Default.AspectRatio,
            group = "Display", groupIt = "Schermo"),

        // No "Reset to Native Resolution" entry: it was the inverse of the 720p entry above,
        // apply and revert swapped, so the two fought each other — and its check counted
        // "Override" lines from `wm size`, which appear exactly when the resolution is *not*
        // native. It ticked when it was off and unticked when it was on. Unticking the 720p
        // entry already runs `wm size reset; wm density reset`.

        opt("net_speed", Optimization.Category.ADVANCED,
            "Network Speed Indicator ON", "Indicatore Velocità Rete ON",
            "Ticked: the status bar shows real-time network speed. Equivalent to Galaxy Max Hz's Net Speed indicator. Unverified: the key holds the value but this One UI build's status bar was not seen to read it.",
            "Spuntato: la barra di stato mostra la velocità di rete in tempo reale. Equivalente all'indicatore Net Speed di Galaxy Max Hz. Non verificata: la chiave conserva il valore ma la barra di stato di questa build One UI non risulta leggerla.",
            "settings put secure sysui_net_speed 1",
            "settings put secure sysui_net_speed 0",
            """settings get secure sysui_net_speed | grep -cFx "1"""",
            icon = Icons.Default.NetworkCheck,
            verified = false,
            group = "Network", groupIt = "Rete"),

        opt("force_resizable", Optimization.Category.ADVANCED,
            "Force Resizable Activities ON", "Forza Attività Ridimensionabili ON",
            "Ticked: every app can be used in split-screen and multi-window, including the ones that declare they do not support it. Equivalent to Galaxy Max Hz's 'Keep Force Resizable Activities'.",
            "Spuntato: tutte le app possono essere usate in split-screen e multi-finestra, anche quelle che dichiarano di non supportarlo. Equivalente a 'Keep Force Resizable Activities' di Galaxy Max Hz.",
            "settings put global force_resizable_activities 1",
            "settings put global force_resizable_activities 0",
            """settings get global force_resizable_activities | grep -cFx "1"""",
            icon = Icons.Default.Crop,
            group = "Display", groupIt = "Schermo"),

        opt("anim_off", Optimization.Category.ADVANCED,
            "Disable Animations (0x)", "Annulla Animazioni (0x)",
            "Turns off all window/transition animations. Maximum GPU savings but phone feels instant/jarring. Equivalent to Galaxy Max Hz Animation Mod.",
            "Spegne tutte le animazioni. Massimo risparmio GPU ma il telefono sembra immediato/scattoso. Equivalente al Mod Animazioni di Galaxy Max Hz.",
            "settings put global window_animation_scale 0; settings put global transition_animation_scale 0; settings put global animator_duration_scale 0",
            "settings put global window_animation_scale 1; settings put global transition_animation_scale 1; settings put global animator_duration_scale 1",
            """settings get global window_animation_scale | grep -cFx "0"""",
            icon = Icons.Default.Speed,
            group = "Display", groupIt = "Schermo"),

        opt("batt_protect_85", Optimization.Category.ADVANCED,
            "Battery Protection 85%", "Protezione Batteria 85%",
            "Stops charging at 85%. More usable capacity than 80% while still protecting battery lifespan.",
            "Ferma ricarica a 85%. Più capacità utilizzabile dell'80% proteggendo comunque la batteria.",
            "settings put global protect_battery 1; settings put global battery_protection_threshold 85",
            "settings put global protect_battery 0",
            """settings get global protect_battery | grep -cFx "1"; settings get global battery_protection_threshold | grep -cFx "85"""",
            icon = Icons.Default.BatteryChargingFull,
            group = "Battery Protection", groupIt = "Protezione batteria"),

        opt("batt_protect_90", Optimization.Category.ADVANCED,
            "Battery Protection 90%", "Protezione Batteria 90%",
            "Stops charging at 90%. Good balance for daily use while still extending battery lifespan.",
            "Ferma ricarica a 90%. Buon bilanciamento per uso quotidiano allungando comunque la vita batteria.",
            "settings put global protect_battery 1; settings put global battery_protection_threshold 90",
            "settings put global protect_battery 0",
            """settings get global protect_battery | grep -cFx "1"; settings get global battery_protection_threshold | grep -cFx "90"""",
            icon = Icons.Default.BatteryChargingFull,
            group = "Battery Protection", groupIt = "Protezione batteria"),

        opt("batt_protect_95", Optimization.Category.ADVANCED,
            "Battery Protection 95%", "Protezione Batteria 95%",
            "Stops charging at 95%. Minimal protection but maximum daily range.",
            "Ferma ricarica a 95%. Protezione minima ma massima autonomia giornaliera.",
            "settings put global protect_battery 1; settings put global battery_protection_threshold 95",
            "settings put global protect_battery 0",
            """settings get global protect_battery | grep -cFx "1"; settings get global battery_protection_threshold | grep -cFx "95"""",
            icon = Icons.Default.BatteryChargingFull,
            group = "Battery Protection", groupIt = "Protezione batteria"),

        // ── SCREEN-OFF MODS (require persistent service) ──
        opt("screen_off_low_hz", Optimization.Category.ADVANCED,
            "Screen-Off Low Hz", "Bassa Hz Schermo Spento",
            "Forces the lowest refresh rate when screen turns off (60Hz). Samsung stock behavior locks to HIGHEST Hz on screen-off — this overrides it. Requires background service (notification will appear). Equivalent to Galaxy Max Hz Screen-Off Refresh Mod.",
            "Forza 60Hz a schermo spento. Samsung normalmente forza la MASSIMA frequenza — questa mod la annulla. Richiede servizio in background (apparirà notifica). Equivalente a Galaxy Max Hz.",
            "settings put secure s24opt_screen_off_low_hz 1",
            "settings delete secure s24opt_screen_off_low_hz",
            "settings get secure s24opt_screen_off_low_hz",
            icon = Icons.Default.DarkMode,
            group = "Screen-Off", groupIt = "Schermo spento"),

        opt("screen_off_psm", Optimization.Category.ADVANCED,
            "Auto PSM on Screen-Off", "PSM Automatico a Schermo Spento",
            "Automatically enables Power Saving Mode when screen turns off and disables it when screen turns on. Requires background service (notification will appear). Equivalent to Galaxy Max Hz Auto PSM.",
            "Attiva PSM a schermo spento, lo disattiva a schermo acceso. Richiede servizio in background (apparirà notifica). Equivalente a Galaxy Max Hz.",
            "settings put secure s24opt_screen_off_psm 1",
            "settings delete secure s24opt_screen_off_psm",
            "settings get secure s24opt_screen_off_psm",
            icon = Icons.Default.BatteryFull,
            group = "Screen-Off", groupIt = "Schermo spento"),

        opt("screen_off_sync", Optimization.Category.ADVANCED,
            "Auto Sync Off on Screen-Off", "Sincr. Off a Schermo Spento",
            "Disables account sync when screen turns off, re-enables when screen turns on. Saves sync drain. Requires background service (notification will appear). Equivalent to Galaxy Max Hz.",
            "Disattiva sync a schermo spento, riattiva a schermo acceso. Richiede servizio in background (apparirà notifica). Equivalente a Galaxy Max Hz.",
            "settings put secure s24opt_screen_off_sync 1",
            "settings delete secure s24opt_screen_off_sync",
            "settings get secure s24opt_screen_off_sync",
            icon = Icons.Default.SyncDisabled,
            group = "Screen-Off", groupIt = "Schermo spento"),
    )

    private fun opt(
        id: String, cat: Optimization.Category,
        titleEn: String, titleIt: String,
        descEn: String, descIt: String,
        apply: String, revert: String, check: String = "",
        icon: ImageVector? = null,
        /** Sub-heading. Passing one string uses it for both languages — right for the
         *  many headings that are brand names and identical either way. */
        group: String = "", groupIt: String = group,
        verified: Boolean = true,
        local: Optimization.LocalAction? = null,
    ) = Optimization(id, titleEn, titleIt, descEn, descIt, cat,
        apply.split("; ").filter { it.isNotBlank() },
        revert.split("; ").filter { it.isNotBlank() },
        check.split("; ").filter { it.isNotBlank() },
        icon, group, groupIt, verified, local)

    fun byCategory(): Map<Optimization.Category, List<Optimization>> =
        getAll().groupBy { it.category }
}
