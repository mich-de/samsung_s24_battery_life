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
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.bixby.agent""""),

        opt("bixby_wakeup", Optimization.Category.BLOAT,
            "Bixby Wake-up Detection", "Rilevamento Sveglia Bixby",
            "Always-listens for 'Hi Bixby' via microphone. Main standby drain on One UI 8.",
            "Ascolta sempre 'Hi Bixby' via microfono. Principale drain in standby su One UI 8.",
            "pm disable-user --user 0 com.samsung.android.bixby.wakeup",
            "pm enable com.samsung.android.bixby.wakeup",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.bixby.wakeup""""),

        opt("bixby_vision", Optimization.Category.BLOAT,
            "Bixby Vision", "Bixby Vision",
            "AI image analysis: text translation from photos, product search, QR scanning.",
            "Analisi immagini AI: traduzione testo da foto, ricerca prodotti, scansione QR.",
            "pm disable-user --user 0 com.samsung.android.bixbyvision.framework",
            "pm enable com.samsung.android.bixbyvision.framework",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.bixbyvision.framework""""),

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
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.bixby.ondevice.itit""""),

        opt("vision_intel", Optimization.Category.BLOAT,
            "Vision Intelligence", "Intelligenza Visiva",
            "Samsung visual search (like Google Lens). Camera works fine without it.",
            "Ricerca visiva Samsung (simile a Google Lens). Fotocamera funziona senza.",
            "pm disable-user --user 0 com.samsung.android.visionintelligence",
            "pm enable com.samsung.android.visionintelligence",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.visionintelligence""""),

        opt("game_tools", Optimization.Category.BLOAT,
            "Game Tools", "Game Tools",
            "In-game overlay: screenshot, recording, DND. Games work fine without it.",
            "Overlay in-game: screenshot, registrazione, DND. Giochi funzionano senza.",
            "pm disable-user --user 0 com.samsung.android.game.gametools",
            "pm enable com.samsung.android.game.gametools",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.game.gametools""""),

        opt("gos", Optimization.Category.BLOAT,
            "Game Optimizing Service (GOS)", "Game Optimizing Service (GOS)",
            "Limits gaming performance to prevent overheating. Safe to disable: games may run faster but with higher battery/heat. No app or system features break.",
            "Limita le prestazioni nei giochi per evitare surriscaldamento. Disabilitabile: i giochi girano più fluidi ma scaldano e consumano di più. Nessuna app o funzione si rompe.",
            "pm disable-user --user 0 com.samsung.android.game.gos",
            "pm enable com.samsung.android.game.gos",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.game.gos""""),

        opt("sam_daily", Optimization.Category.BLOAT,
            "Samsung Daily / Members", "Samsung Daily / Members",
            "Removes Samsung side panel (news/offers) AND Samsung Customization Service which keeps Play Services awake on movement. Loss: side panel, Samsung Members benefits. Gain: less standby drain.",
            "Rimuove pannello laterale Samsung (notizie/offerte) E Customization Service che tiene sveglio Play Services in movimento. Perdi: pannello laterale, vantaggi Samsung Members. Guadagni: meno drain in standby.",
            "pm disable-user --user 0 com.samsung.android.rubin.app; pm revoke com.samsung.android.rubin.app android.permission.ACTIVITY_RECOGNITION",
            "pm enable com.samsung.android.rubin.app; pm grant com.samsung.android.rubin.app android.permission.ACTIVITY_RECOGNITION",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.rubin.app""""),

        opt("smart_suggest", Optimization.Category.BLOAT,
            "Smart Suggestions", "Suggerimenti Intelligenti",
            "Predictive app/text suggestions in keyboard, share sheet, and notification bar. Loss: app suggestions, smart copy-paste. Keyboard predictions unaffected.",
            "Suggerimenti predittivi di app/testo in tastiera, menu condividi e barra notifiche. Perdi: suggerimenti app, copia-incolla intelligente. Predizioni tastiera non toccate.",
            "pm disable-user --user 0 com.samsung.android.smartsuggestions",
            "pm enable com.samsung.android.smartsuggestions",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.smartsuggestions""""),

        opt("aware", Optimization.Category.BLOAT,
            "Contextual Awareness", "Consapevolezza Contestuale",
            "Detects driving/walking using sensors (not GPS). Powers driving mode, walking focus. Loss: automatic driving/walking detection. Modes & Routines triggers for activity may stop.",
            "Rileva guida/camminata usando sensori (non GPS). Alimenta modalità guida, focus camminata. Perdi: rilevamento automatico guida/camminata. Attivatori attività in Modalità e Routine potrebbero non funzionare.",
            "pm disable-user --user 0 com.samsung.android.aware.service",
            "pm enable com.samsung.android.aware.service",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.aware.service""""),

        opt("bbc_agent", Optimization.Category.BLOAT,
            "Bixby Briefing Agent", "Agente Bixby Briefing",
            "Provides news/suggestions content to Bixby. Useless if Bixby is disabled.",
            "Fornisce notizie/suggerimenti a Bixby. Inutile se Bixby è disabilitato.",
            "pm disable-user --user 0 com.samsung.android.bbc.bbcagent",
            "pm enable com.samsung.android.bbc.bbcagent",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.bbc.bbcagent""""),

        opt("reminder", Optimization.Category.BLOAT,
            "Samsung Reminder", "Promemoria Samsung",
            "Native Samsung reminders. Use Google Keep instead.",
            "Promemoria nativi Samsung. Usa Google Keep invece.",
            "pm disable-user --user 0 com.samsung.android.app.reminder",
            "pm enable com.samsung.android.app.reminder",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.app.reminder""""),

        opt("routines", Optimization.Category.BLOAT,
            "Samsung Routines", "Routine Samsung",
            "Automations like 'when I arrive home, turn WiFi on'. Use Tasker/IFTTT instead.",
            "Automazioni come 'quando arrivo a casa, accendi WiFi'. Usa Tasker/IFTTT.",
            "pm disable-user --user 0 com.samsung.android.app.routines",
            "pm enable com.samsung.android.app.routines",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.app.routines""""),

        opt("live_effect", Optimization.Category.BLOAT,
            "Live Effect Service", "Servizio Effetti Live",
            "AR filters and beautification effects for photos/videos.",
            "Filtri AR ed effetti beautificazione per foto/video.",
            "pm disable-user --user 0 com.samsung.android.liveeffectservice",
            "pm enable com.samsung.android.liveeffectservice",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.liveeffectservice""""),

        opt("one_connect", Optimization.Category.BLOAT,
            "Samsung One Connect", "Samsung One Connect",
            "SmartThings remote control for Samsung TVs, appliances, IoT devices. Loss: you cannot control smart home/TV from your phone. Use physical remotes instead.",
            "Controllo remoto SmartThings per TV Samsung, elettrodomestici, IoT. Perdi: non puoi controllare smart home/TV dal telefono. Usa telecomandi fisici.",
            "pm disable-user --user 0 com.samsung.android.oneconnect",
            "pm enable com.samsung.android.oneconnect",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.oneconnect""""),

        opt("st_platform", Optimization.Category.BLOAT,
            "Samsung Pass / Security Platform", "Samsung Pass / Piattaforma Sicurezza",
            "Samsung Pass: biometric login to apps/sites, autofill passwords. Loss: Samsung Pass, biometric login to Samsung apps. Keep if you use Samsung Pass instead of Google Password Manager.",
            "Samsung Pass: login biometrico a app/siti, autocompilazione password. Perdi: Samsung Pass, login biometrico app Samsung. Tienilo se usi Samsung Pass invece di Google Password Manager.",
            "pm disable-user --user 0 com.samsung.android.service.stplatform",
            "pm enable com.samsung.android.service.stplatform",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.service.stplatform""""),

        opt("kids_mode", Optimization.Category.BLOAT,
            "Samsung Kids Mode", "Modalità Bambini Samsung",
            "Kids Home: child-safe environment with app timer.",
            "Kids Home: ambiente sicuro per bambini con timer app.",
            "pm disable-user --user 0 com.samsung.android.forest",
            "pm enable com.samsung.android.forest",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.forest""""),

        opt("my_galaxy", Optimization.Category.BLOAT,
            "Samsung Members / My Galaxy", "Samsung Members / My Galaxy",
            "Support, offers, Samsung community. Marketing bloatware.",
            "Supporto, offerte, community Samsung. Bloatware marketing.",
            "pm disable-user --user 0 com.mygalaxy.service",
            "pm enable com.mygalaxy.service",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.mygalaxy.service""""),

        opt("buds_mgr", Optimization.Category.BLOAT,
            "Galaxy Buds/Watch Manager", "Gestore Galaxy Buds/Watch",
            "Manages Galaxy Buds, Watch, and accessories. WARNING: disabling breaks Galaxy Wearable app — no firmware updates, no battery readout, no Buds/Watch settings.",
            "Gestisce Galaxy Buds, Watch e accessori. ATTENZIONE: disabilitarlo rompe l'app Galaxy Wearable — niente aggiornamenti firmware, niente lettura batteria, niente impostazioni Buds/Watch.",
            "pm disable-user --user 0 com.samsung.accessory.budsunitemgr",
            "pm enable com.samsung.accessory.budsunitemgr",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.accessory.budsunitemgr""""),

        opt("sam_health", Optimization.Category.BLOAT,
            "Samsung Health", "Samsung Health",
            "Step counter, sleep tracking, workouts. Keep if you use it!",
            "Contapassi, monitoraggio sonno, allenamenti. Tienilo se lo usi!",
            "pm disable-user --user 0 com.sec.android.app.shealth",
            "pm enable com.sec.android.app.shealth",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.sec.android.app.shealth""""),

        opt("chrome_custom", Optimization.Category.BLOAT,
            "Samsung Chrome Customizations", "Personalizzazioni Chrome Samsung",
            "Samsung's Chrome modifications (default home page, bookmarks). Chrome works fine.",
            "Modifiche Samsung a Chrome (home page, segnalibri). Chrome funziona normale.",
            "pm disable-user --user 0 com.sec.android.app.chromecustomizations",
            "pm enable com.sec.android.app.chromecustomizations",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.sec.android.app.chromecustomizations""""),

        opt("partner_bkm", Optimization.Category.BLOAT,
            "Partner Bookmarks", "Segnalibri Partner",
            "Pre-installed bookmarks from carriers/manufacturers.",
            "Segnalibri preinstallati da gestori/produttori.",
            "pm disable-user --user 0 com.android.providers.partnerbookmarks",
            "pm enable com.android.providers.partnerbookmarks",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.android.providers.partnerbookmarks""""),

        opt("smart_switch", Optimization.Category.BLOAT,
            "Smart Switch", "Smart Switch",
            "Transfer data from old phone. Useless after initial migration.",
            "Trasferimento dati da vecchio telefono. Inutile dopo la migrazione.",
            "pm disable-user --user 0 com.sec.android.easyMover",
            "pm enable com.sec.android.easyMover",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.sec.android.easyMover""""),

        opt("emergency", Optimization.Category.BLOAT,
            "Emergency Launcher", "Lanciatore Emergenza",
            "SOS launcher (5x power press → call 112). Keep if you travel!",
            "SOS emergenza (5x pressione → chiama 112). Tienilo se viaggi!",
            "pm disable-user --user 0 com.sec.android.emergencylauncher",
            "pm enable com.sec.android.emergencylauncher",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.sec.android.emergencylauncher""""),

        opt("knox_zt", Optimization.Category.BLOAT,
            "Knox Zero Touch", "Knox Zero Touch",
            "Enterprise device management framework. Safe to disable for personal phones.",
            "Framework gestione aziendale. Sicuro disabilitare su telefoni personali.",
            "pm disable-user --user 0 com.samsung.android.knox.zt.framework",
            "pm enable com.samsung.android.knox.zt.framework",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.knox.zt.framework""""),

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
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.knox.knnr""""),

        opt("samsung_pay", Optimization.Category.BLOAT,
            "Samsung Pay", "Samsung Pay",
            "Samsung's payment service. Uses Knox for NFC transactions. Safe to disable if you use Google Wallet or nothing.",
            "Servizio pagamenti Samsung. Usa Knox per transazioni NFC. Disabilitabile se usi Google Wallet o niente.",
            "pm disable-user --user 0 com.samsung.android.spayfw",
            "pm enable com.samsung.android.spayfw",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.samsung.android.spayfw""""),

        // ── GOOGLE ──
        opt("g_photos", Optimization.Category.BLOAT,
            "Google Photos", "Google Foto",
            "Cloud backup, editing, organization. Samsung Gallery works fine.",
            "Backup cloud, modifica, organizzazione. Galleria Samsung funziona.",
            "pm disable-user --user 0 com.google.android.apps.photos",
            "pm enable com.google.android.apps.photos",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.google.android.apps.photos""""),

        opt("g_messages", Optimization.Category.BLOAT,
            "Google Messages", "Google Messaggi",
            "SMS/RCS app. Keep if it's your only SMS app!",
            "App SMS/RCS. Tienila se è la tua unica app SMS!",
            "pm disable-user --user 0 com.google.android.apps.messaging",
            "pm enable com.google.android.apps.messaging",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.google.android.apps.messaging""""),

        opt("g_docs", Optimization.Category.BLOAT,
            "Google Docs", "Google Documenti",
            "Online document editor. Web version works in browser.",
            "Editor documenti online. Versione web funziona nel browser.",
            "pm disable-user --user 0 com.google.android.apps.docs.editors.docs",
            "pm enable com.google.android.apps.docs.editors.docs",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.google.android.apps.docs.editors.docs""""),

        opt("g_search", Optimization.Category.BLOAT,
            "Google Search / Assistant", "Google Search / Assistente",
            "Google Assistant, Discover, Search widget. NEEDED for 'Hey Google'!",
            "Google Assistant, Discover, widget ricerca. SERVE per 'Hey Google'!",
            "pm disable-user --user 0 com.google.android.googlequicksearchbox",
            "pm enable com.google.android.googlequicksearchbox",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.google.android.googlequicksearchbox""""),

        opt("g_cast", Optimization.Category.BLOAT,
            "Google Cast", "Google Cast",
            "Screen/content casting to Chromecast/Google TV.",
            "Trasmissione schermo/contenuti a Chromecast/Google TV.",
            "pm disable-user --user 0 com.google.android.apps.chromecast.app",
            "pm enable com.google.android.apps.chromecast.app",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.google.android.apps.chromecast.app""""),

        opt("g_feedback", Optimization.Category.BLOAT,
            "Google Feedback", "Google Feedback",
            "Bug report tool. Never used directly.",
            "Strumento segnalazione bug. Mai usato direttamente.",
            "pm disable-user --user 0 com.google.android.feedback",
            "pm enable com.google.android.feedback",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.google.android.feedback""""),

        opt("g_maps", Optimization.Category.BLOAT,
            "Google Maps", "Google Maps",
            "Navigation, traffic, POI search. Keep if you use it for driving!",
            "Navigazione, traffico, ricerca POI. Tienilo se guidi con Maps!",
            "pm disable-user --user 0 com.google.android.apps.maps",
            "pm enable com.google.android.apps.maps",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.google.android.apps.maps""""),

        opt("g_supervision", Optimization.Category.BLOAT,
            "Google Family Link", "Google Family Link",
            "Parental controls. Keep if you use it for your children!",
            "Controllo parentale. Tienilo se lo usi per i figli!",
            "pm disable-user --user 0 com.google.android.gms.supervision",
            "pm enable com.google.android.gms.supervision",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.google.android.gms.supervision""""),

        // ── FACEBOOK ──
        opt("fb_main", Optimization.Category.BLOAT,
            "Facebook App", "App Facebook",
            "Main Facebook app. Biggest known battery drainer: 15min system alarms, constant GPS, FG service.",
            "App principale Facebook. Maggior drainer di batteria: sveglie 15min, GPS costante, FG service.",
            "pm disable-user --user 0 com.facebook.katana",
            "pm enable com.facebook.katana",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.facebook.katana""""),

        opt("fb_messenger", Optimization.Category.BLOAT,
            "Facebook Messenger", "Facebook Messenger",
            "Chat and calls. Runs background service for push notifications.",
            "Chat e chiamate. Esegue servizio in background per notifiche push.",
            "pm disable-user --user 0 com.facebook.orca",
            "pm enable com.facebook.orca",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.facebook.orca""""),

        // ── MICROSOFT ──
        opt("ms_edge", Optimization.Category.BLOAT,
            "Microsoft Edge", "Microsoft Edge",
            "Web browser (~150MB). Redundant if you use Chrome/Firefox.",
            "Browser (~150MB). Ridondante se usi Chrome/Firefox.",
            "pm disable-user --user 0 com.microsoft.emmx",
            "pm enable com.microsoft.emmx",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.microsoft.emmx""""),

        opt("ms_excel", Optimization.Category.BLOAT,
            "Microsoft Excel", "Microsoft Excel",
            "Spreadsheet editor (~150MB). Use Google Sheets instead.",
            "Foglio di calcolo (~150MB). Usa Google Sheets.",
            "pm disable-user --user 0 com.microsoft.office.excel",
            "pm enable com.microsoft.office.excel",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.microsoft.office.excel""""),

        opt("ms_word", Optimization.Category.BLOAT,
            "Microsoft Word", "Microsoft Word",
            "Document editor (~150MB). Use Google Docs instead.",
            "Editor documenti (~150MB). Usa Google Docs.",
            "pm disable-user --user 0 com.microsoft.office.word",
            "pm enable com.microsoft.office.word",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.microsoft.office.word""""),

        opt("ms_rdc", Optimization.Category.BLOAT,
            "Microsoft Remote Desktop", "Microsoft Remote Desktop",
            "Remote PC connection. Use AnyDesk/TeamViewer instead.",
            "Connessione PC remoto. Usa AnyDesk/TeamViewer.",
            "pm disable-user --user 0 com.microsoft.rdc.androidx",
            "pm enable com.microsoft.rdc.androidx",
            """pm list packages -d 2>/dev/null | grep -cFx "package:com.microsoft.rdc.androidx""""),

        opt("knox_reset", Optimization.Category.MAINTENANCE,
            "Knox Matrix Reset", "Reset Knox Matrix",
            "Fixes April 2026 update battery drain loop. Clears cache (doesn't uninstall). Data auto-repopulates.",
            "Risolve loop drain da aggiornamento Aprile 2026. Pulisce cache (non disinstalla). Dati si ripopolano.",
            "pm clear com.samsung.android.knox.kpecore; pm clear com.samsung.android.knox.attestation; pm clear com.samsung.android.knox.pushmanager; pm clear com.samsung.android.knox.containercore; pm clear com.samsung.android.knox.analytics.uploader",
            ""),

        opt("gms_reset", Optimization.Category.MAINTENANCE,
            "Google Play Services Reset", "Reset Google Play Services",
            "Resets stuck foreground services. You'll need to re-login to Google Pay, Smart Lock, loyalty cards. Gmail/Drive auto-sync.",
            "Resetta FG services bloccati. Dovrai rifare login Google Pay, Smart Lock, carte fedeltà. Gmail/Drive si risincronizzano.",
            "am force-stop com.google.android.gms; pm clear com.google.android.gms",
            ""),

        opt("bg_instagram", Optimization.Category.MAINTENANCE,
            "Instagram Background Restrict", "Limita Background Instagram",
            "Instagram used 771 mAh in 15h with 329K data packets in background. Blocks background activity, app works when opened.",
            "Instagram usava 771 mAh in 15h con 329K pacchetti dati in background. Blocca attività bg, app funziona quando aperta.",
            "appops set com.instagram.android RUN_ANY_IN_BACKGROUND deny",
            "appops set com.instagram.android RUN_ANY_IN_BACKGROUND allow",
            """appops get com.instagram.android RUN_ANY_IN_BACKGROUND | grep -cFx "deny""""),

        opt("bg_tandem", Optimization.Category.MAINTENANCE,
            "net.tandem Background Restrict", "Limita Background net.tandem",
            "System network service using 80.6 mAh. May affect dual-SIM reception.",
            "Servizio di rete sistema con 80.6 mAh. Può influenzare ricezione doppia SIM.",
            "appops set net.tandem RUN_ANY_IN_BACKGROUND deny",
            "appops set net.tandem RUN_ANY_IN_BACKGROUND allow",
            """appops get net.tandem RUN_ANY_IN_BACKGROUND | grep -cFx "deny""""),

        opt("wa_background_unrestricted", Optimization.Category.MAINTENANCE,
            "WhatsApp Unrestricted Battery", "WhatsApp Batteria No Limit",
            "Sets WhatsApp battery to 'Unrestricted' via appops. Prevents One UI from killing WhatsApp in background during calls. Equivalent to: Settings > Apps > WhatsApp > Battery > Unrestricted.",
            "Imposta batteria WhatsApp su 'Nessuna restrizione' via appops. Impedisce a One UI di uccidere WhatsApp in background durante le chiamate. Equivalente: Impostazioni > App > WhatsApp > Batteria > Nessuna restrizione.",
            "appops set com.whatsapp RUN_ANY_IN_BACKGROUND allow",
            "appops set com.whatsapp RUN_ANY_IN_BACKGROUND deny",
            """appops get com.whatsapp RUN_ANY_IN_BACKGROUND | grep -cFx "allow""""),

        opt("wa_doze_whitelist", Optimization.Category.MAINTENANCE,
            "WhatsApp Doze Exempt", "WhatsApp Esente Doze",
            "Whitelists WhatsApp from Android Doze mode. Prevents system from delaying WhatsApp's network access when device is idle. Critical for reliable VoIP calls.",
            "Whitelista WhatsApp da Doze mode. Impedisce al sistema di ritardare l'accesso di rete di WhatsApp quando il dispositivo è in idle. Essenziale per chiamate VoIP affidabili.",
            "cmd deviceidle whitelist +com.whatsapp",
            "cmd deviceidle whitelist -com.whatsapp",
            """cmd deviceidle whitelist 2>/dev/null | grep -cFx "com.whatsapp""""),

        // ── SYSTEM ──
        opt("auto_restrict", Optimization.Category.SYSTEM,
            "App Auto Restriction", "Auto Limitazione App",
            "Auto-standby for apps unused for 3+ days. More aggressive than Adaptive Battery.",
            "Standby automatico per app non usate da 3+ giorni. Più aggressivo di Batteria Adattiva.",
            "settings put global app_auto_restriction_enabled 1",
            "settings put global app_auto_restriction_enabled 0",
            """settings get global app_auto_restriction_enabled | grep -cFx "1""""),

        opt("batt_saver", Optimization.Category.SYSTEM,
            "Battery Saver Mode", "Modalità Risparmio Energetico",
            "Reduces brightness, slows CPU, limits background. Minor performance impact.",
            "Riduce luminosità, rallenta CPU, limita background. Leggero impatto prestazioni.",
            "settings put global battery_saver_mode 1",
            "settings put global battery_saver_mode 0",
            """settings get global battery_saver_mode | grep -cFx "1""""),

        opt("cpu_resp", Optimization.Category.SYSTEM,
            "Enhanced CPU Responsiveness", "Risposta CPU Migliorata",
            "Keeps CPU at high frequency for touch/gestures. OFF saves battery.",
            "Mantiene CPU a frequenza alta per tocco/gesti. OFF risparmia batteria.",
            "settings put global sem_enhanced_cpu_responsiveness 0",
            "settings put global sem_enhanced_cpu_responsiveness 1",
            """settings get global sem_enhanced_cpu_responsiveness | grep -cFx "0""""),

        opt("enhanced_proc", Optimization.Category.SYSTEM,
            "Enhanced Processing", "Elaborazione Migliorata",
            "CPU boosting for heavy apps (games, video). OFF = smoother standby.",
            "Boosting CPU per app pesanti (giochi, video). OFF = standby più lungo.",
            "settings put global enhanced_processing 0",
            "settings put global enhanced_processing 1",
            """settings get global enhanced_processing | grep -cFx "0""""),

        opt("ble_scan", Optimization.Category.SYSTEM,
            "BLE Always Scanning", "Scansione BLE Sempre Attiva",
            "Constant Bluetooth Low Energy scanning. Saves significant battery when OFF.",
            "Scansione Bluetooth BLE costante. OFF risparmia batteria significativamente.",
            "settings put global ble_scan_always_enabled 0",
            "settings put global ble_scan_always_enabled 1",
            """settings get global ble_scan_always_enabled | grep -cFx "0""""),

        opt("aod", Optimization.Category.SYSTEM,
            "Always On Display", "Always On Display",
            "Screen always shows clock/notifications at low brightness. Saves ~3-5% battery/day when OFF.",
            "Schermo mostra sempre ora/notifiche a bassa luminosità. OFF risparmia ~3-5% batteria/giorno.",
            "settings put system aod_mode 0; settings put global always_on_display_enabled 0",
            "settings put system aod_mode 1; settings put global always_on_display_enabled 1",
            """settings get system aod_mode | grep -cFx "0""""),

        opt("screen_timeout", Optimization.Category.SYSTEM,
            "Screen Timeout 30s", "Timeout Schermo 30s",
            "Screen turns off after 30 seconds of inactivity.",
            "Schermo si spegne dopo 30 secondi di inattività.",
            "settings put system screen_off_timeout 30000",
            "settings put system screen_off_timeout 60000",
            """settings get system screen_off_timeout | grep -cFx "30000""""),

        opt("notif_led", Optimization.Category.SYSTEM,
            "Notification LED", "LED Notifiche",
            "LED blinking for notifications. Saves minimal battery when OFF.",
            "Lampeggio LED per notifiche. Risparmio minimo OFF.",
            "settings put system notification_light_pulse 0",
            "settings put system notification_light_pulse 1",
            """settings get system notification_light_pulse | grep -cFx "0""""),

        opt("doze", Optimization.Category.SYSTEM,
            "Deep Doze Mode", "Doze Profondo",
            "Deep sleep when phone is still + screen off. Huge standby drain reduction.",
            "Sleep profondo con telefono fermo + schermo spento. Riduzione enorme drain standby.",
            "settings put secure doze_enabled 1",
            "settings put secure doze_enabled 0",
            """settings get secure doze_enabled | grep -cFx "1""""),

        opt("animations", Optimization.Category.SYSTEM,
            "Reduce Animations (0.5x)", "Riduci Animazioni (0.5x)",
            "Halves animation speed. Saves GPU power. Phone feels slightly less smooth.",
            "Riduce velocità animazioni a metà. Risparmia GPU. Telefono leggermente meno fluido.",
            "settings put global window_animation_scale 0.5; settings put global transition_animation_scale 0.5; settings put global animator_duration_scale 0.5",
            "settings put global window_animation_scale 1; settings put global transition_animation_scale 1; settings put global animator_duration_scale 1",
            """settings get global window_animation_scale | grep -cFx "0.5""""),

        opt("batt_protect", Optimization.Category.SYSTEM,
            "Battery Protection 80%", "Protezione Batteria 80%",
            "Stops charging at 80%. Extends battery lifespan. Replaces adaptive charging (needs ACTIVITY_RECOGNITION).",
            "Ferma ricarica a 80%. Prolunga vita batteria. Sostituisce ricarica adattiva (che serve ACTIVITY_RECOGNITION).",
            "settings put global battery_protection_default_value 3; settings put global battery_protection_threshold 80",
            "settings put global battery_protection_default_value 1",
            """settings get global battery_protection_threshold | grep -cFx "80""""),

        opt("wifi_power", Optimization.Category.SYSTEM,
            "WiFi Power Save", "Risparmio Energetico WiFi",
            "Reduces WiFi card power in standby. Slightly slower downloads.",
            "Riduce potenza scheda WiFi in standby. Download leggermente più lenti.",
            "settings put global wifi_power_save 1",
            "settings put global wifi_power_save 0",
            """settings get global wifi_power_save | grep -cFx "1""""),

        opt("fast_charge", Optimization.Category.SYSTEM,
            "Adaptive Fast Charging", "Ricarica Rapida Adattiva",
            "Enables faster wired charging when screen is off. Disabling may extend battery lifespan overnight.",
            "Abilita ricarica più veloce via cavo a schermo spento. Disattivare può allungare la vita della batteria di notte.",
            "settings put system adaptive_fast_charging 0",
            "settings put system adaptive_fast_charging 1",
            """settings get system adaptive_fast_charging | grep -cFx "0""""),

        opt("extra_brightness", Optimization.Category.SYSTEM,
            "Extra Brightness", "Luminosità Extra",
            "Allows brightness above normal max. Consumes more battery. OFF saves power.",
            "Permette luminosità oltre il massimo normale. Consuma più batteria. OFF risparmia energia.",
            "settings put secure screen_extra_brightness 0",
            "settings put secure screen_extra_brightness 1",
            """settings get secure screen_extra_brightness | grep -cFx "0""""),

        opt("auto_brightness_off", Optimization.Category.SYSTEM,
            "Auto Brightness OFF", "Luminosità Automatica OFF",
            "Disables auto brightness. Manual control saves sensor power but requires manual adjustment.",
            "Disattiva luminosità automatica. Controllo manuale risparmia sensore ma richiede regolazione manuale.",
            "settings put system auto_brightness 0",
            "settings put system auto_brightness 1",
            """settings get system auto_brightness | grep -cFx "0""""),

        opt("battery_percent", Optimization.Category.SYSTEM,
            "Show Battery Percentage", "Mostra Percentuale Batteria",
            "Shows battery percentage inside the status bar icon.",
            "Mostra percentuale batteria dentro l'icona della barra di stato.",
            "settings put system display_battery_percentage 1",
            "settings put system display_battery_percentage 0",
            """settings get system display_battery_percentage | grep -cFx "1""""),

        opt("dark_mode", Optimization.Category.SYSTEM,
            "Dark Mode (permanent)", "Modalità Scura (permanente)",
            "Forces dark mode at all times. AMOLED black pixels save battery on the display.",
            "Forza modalità scura sempre. I pixel neri AMOLED risparmiano batteria sul display.",
            "settings put secure ui_night_mode 1",
            "settings put secure ui_night_mode 2",
            """settings get secure ui_night_mode | grep -cFx "1""""),

        opt("double_tap_wake", Optimization.Category.SYSTEM,
            "Double Tap to Wake", "Doppio Tocco per Accendere",
            "Wake screen by double-tapping. Convenient but uses touch digitizer standby power.",
            "Accende schermo con doppio tocco. Comodo ma usa standby del digitizer.",
            "settings put secure double_tap_to_wake 0",
            "settings put secure double_tap_to_wake 1",
            """settings get secure double_tap_to_wake | grep -cFx "0""""),

        // ── NEW: Audio & Calls ──
        opt("call_extra_vol", Optimization.Category.SYSTEM,
            "Call Extra Volume OFF", "Volume Chiamata Extra OFF",
            "Disables the extra loud call volume option. Slight speaker protection benefit.",
            "Disattiva l'opzione volume extra in chiamata. Leggera protezione altoparlante.",
            "settings put system call_extra_volume 0",
            "settings put system call_extra_volume 1",
            """settings get system call_extra_volume | grep -cFx "0""""),

        opt("call_noise_off", Optimization.Category.SYSTEM,
            "Call Noise Reduction OFF", "Riduzione Rumore OFF",
            "Disables microphone noise suppression during calls. May improve battery during long calls.",
            "Disattiva soppressione rumore microfono in chiamata. Può migliorare batteria in chiamate lunghe.",
            "settings put system call_noise_reduction 0",
            "settings put system call_noise_reduction 1",
            """settings get system call_noise_reduction | grep -cFx "0""""),

        opt("call_vib_off", Optimization.Category.SYSTEM,
            "Call Connect/Vibrate OFF", "Vibrazione Chiamata OFF",
            "Disables vibration on call connect and end. Saves a tiny amount of battery per call.",
            "Disattiva vibrazione all'inizio e fine chiamata. Risparmio minimo per chiamata.",
            "settings put system call_answer_vib 0; settings put system call_end_vib 0",
            "settings put system call_answer_vib 1; settings put system call_end_vib 1",
            """settings get system call_answer_vib | grep -cFx "0""""),

        // ── NEW: Gestures & Navigation ──
        opt("double_tap_sleep", Optimization.Category.SYSTEM,
            "Double Tap to Sleep", "Doppio Tocco per Spegnere",
            "Double-tap on home screen or lock screen to turn screen off.",
            "Doppio tocco sulla schermata home o blocco per spegnere schermo.",
            "settings put secure double_tap_to_sleep 1",
            "settings put secure double_tap_to_sleep 0",
            """settings get secure double_tap_to_sleep | grep -cFx "1""""),

        opt("lift_wake_off", Optimization.Category.SYSTEM,
            "Lift to Wake OFF", "Solleva per Accendere OFF",
            "Disables screen-on when picking up the phone. Saves accelerometer-based battery drain.",
            "Disattiva accensione schermo quando si solleva il telefono. Risparmia batteria dell'accelerometro.",
            "settings put system lift_to_wake 0",
            "settings put system lift_to_wake 1",
            """settings get system lift_to_wake | grep -cFx "0""""),

        opt("smart_stay_off", Optimization.Category.SYSTEM,
            "Smart Stay (Eye Tracking) OFF", "Smart Stay (Tracciamento Occhi) OFF",
            "Disables front camera eye tracking that keeps screen on while you look at it. Saves ~5% battery/day.",
            "Disattiva tracciamento occhi fotocamera frontale. Risparmia ~5% batteria/giorno.",
            "settings put system intelligent_sleep_mode 0",
            "settings put system intelligent_sleep_mode 1",
            """settings get system intelligent_sleep_mode | grep -cFx "0""""),

        opt("one_handed_off", Optimization.Category.SYSTEM,
            "One-Handed Mode OFF", "Modalità Una Mano OFF",
            "Disables one-handed mode shortcut. Reduces accidental triggers and background service.",
            "Disattiva scorciatoia modalità una mano. Riduce attivazioni accidentali e servizio in background.",
            "settings put secure one_handed_mode_enabled 0",
            "settings put secure one_handed_mode_enabled 1",
            """settings get secure one_handed_mode_enabled | grep -cFx "0""""),

        // ── ONE UI 8.5 ──
        opt("adaptive_batt_off", Optimization.Category.SYSTEM,
            "Adaptive Battery OFF", "Batteria Adattiva OFF",
            "Disables Adaptive Battery. Prevents One UI from automatically limiting background activity for apps. Can fix delayed notifications and VoIP call issues.",
            "Disattiva Batteria Adattiva. Impedisce a One UI di limitare automaticamente le app in background. Risolve notifiche ritardate e problemi chiamate VoIP.",
            "settings put global adaptive_battery_enabled 0",
            "settings put global adaptive_battery_enabled 1",
            """settings get global adaptive_battery_enabled | grep -cFx "0""""),

        opt("network_batt_saver_off", Optimization.Category.SYSTEM,
            "Network Battery Saver OFF", "Risparmio Rete OFF",
            "Disables One UI 8.5 Network Battery Saver (Personal Data Intelligence). Prevents Samsung from limiting network performance based on usage patterns. New in One UI 8.5.",
            "Disattiva il Risparmio Rete di One UI 8.5. Impedisce a Samsung di limitare le prestazioni di rete in base ai pattern d'uso. Nuovo in One UI 8.5.",
            "settings put global network_battery_saver_enabled 0",
            "settings put global network_battery_saver_enabled 1",
            """settings get global network_battery_saver_enabled | grep -cFx "0""""),

        opt("pdi_off", Optimization.Category.SYSTEM,
            "Personal Data Intel OFF", "Intel. Dati Pers. OFF",
            "Disables Personal Data Intelligence. Stops Samsung from analyzing usage patterns for battery features. Saves background CPU. Network Battery Saver depends on this.",
            "Disattiva Personal Data Intelligence. Ferma l'analisi dei pattern d'uso da parte di Samsung. Risparmia CPU in background. Network Battery Saver dipende da questo.",
            "settings put global pdi_usage_enabled 0",
            "settings put global pdi_usage_enabled 1",
            """settings get global pdi_usage_enabled | grep -cFx "0""""),

        opt("ram_plus_off", Optimization.Category.SYSTEM,
            "RAM Plus OFF", "RAM Plus OFF",
            "Disables RAM Plus (virtual RAM via storage). Frees up storage space and reduces flash wear. Samsung's virtual RAM can slow down the device when storage is used as swap.",
            "Disattiva RAM Plus (RAM virtuale su storage). Libera spazio e riduce usura flash. La RAM virtuale Samsung può rallentare il dispositivo usando storage come swap.",
            "settings put global ram_plus_size 0",
            "settings delete global ram_plus_size",
            """settings get global ram_plus_size | grep -cFx "0""""),

        opt("nearby_scan_off", Optimization.Category.SYSTEM,
            "Nearby Scan OFF", "Scansione Vicinanza OFF",
            "Disables Nearby Device Scanning. Prevents constant Bluetooth scanning for Samsung devices. Significant battery saving if you don't use Galaxy Buds/Watch daily.",
            "Disattiva Scansione Dispositivi Vicini. Impedisce scansione Bluetooth costante. Notevole risparmio batteria se non usi Galaxy Buds/Watch ogni giorno.",
            "settings put global nearby_scanning_enabled 0",
            "settings put global nearby_scanning_enabled 1",
            """settings get global nearby_scanning_enabled | grep -cFx "0""""),

        // ── NEW: Connectivity ──
        opt("wifi_scan_throttle", Optimization.Category.SYSTEM,
            "WiFi Scan Throttle", "Limite Scansione WiFi",
            "Limits WiFi scan frequency. Reduces battery drain from constant network scanning.",
            "Limita frequenza scansione WiFi. Riduce consumo batteria da scansione rete costante.",
            "settings put global wifi_scan_throttle_enabled 1",
            "settings put global wifi_scan_throttle_enabled 0",
            """settings get global wifi_scan_throttle_enabled | grep -cFx "1""""),

        opt("wifi_switch_off", Optimization.Category.SYSTEM,
            "Auto WiFi Switch OFF", "Commuta WiFi Auto OFF",
            "Disables automatic switching to better WiFi networks. Reduces background scanning.",
            "Disattiva commutazione automatica a reti WiFi migliori. Riduce scansione in background.",
            "settings put global wifi_switch_to_better_wifi_enabled 0; settings put global wifi_switch_to_mobile_data_ins 0",
            "settings put global wifi_switch_to_better_wifi_enabled 1; settings put global wifi_switch_to_mobile_data_ins 1",
            """settings get global wifi_switch_to_better_wifi_enabled | grep -cFx "0""""),

        // ── NEW: Touch ──
        opt("touch_sensitivity", Optimization.Category.SYSTEM,
            "Touch Sensitivity (Low)", "Sensibilità Tocco (Bassa)",
            "Reduces touch sensitivity. May prevent accidental touches. Slight digitizer power saving.",
            "Riduce sensibilità tocco. Previene tocchi accidentali. Leggero risparmio digitizer.",
            "settings put system auto_adjust_touch 0",
            "settings put system auto_adjust_touch 1",
            """settings get system auto_adjust_touch | grep -cFx "0""""),

        // ── NEW: System ──
        opt("auto_sync_off", Optimization.Category.SYSTEM,
            "Auto Sync OFF", "Sincronizzazione Auto OFF",
            "Disables automatic account sync. Saves significant battery. Apps won't refresh in background.",
            "Disattiva sincronizzazione automatica account. Notevole risparmio batteria. App non si aggiornano in bg.",
            "settings put global auto_sync 0",
            "settings put global auto_sync 1",
            """settings get global auto_sync | grep -cFx "0""""),

        opt("quick_doze", Optimization.Category.SYSTEM,
            "Quick Doze (Aggressive)", "Doze Rapido (Aggressivo)",
            "Forces Android Doze mode to activate within 30 seconds of screen-off (default: 60+ min). Huge standby drain reduction.",
            "Forza Doze di Android a 30 secondi dallo spegnimento schermo (default: 60+ minuti). Riduzione enorme drain standby.",
            "settings put global device_idle_constants inactive_to=30000,sensing_to=30000,idle_after_inactive_to=3000,idle_pending_to=30000,max_idle_pending_to=30000,max_idle_to=14400000",
            "settings delete global device_idle_constants",
            """settings get global device_idle_constants | grep -c "inactive_to=30000"""",
            icon = Icons.Default.Bedtime),

        opt("perf_restrict", Optimization.Category.SYSTEM,
            "Unlock CPU in PSM", "Sblocca CPU in PSM",
            "Power Saving Mode normally restricts CPU performance. Keeps full CPU speed while still saving battery on screen/app behavior.",
            "Il Risparmio Energetico normalmente limita la CPU. Mantiene la CPU piena risparmiando comunque su schermo/app.",
            "settings put global restricted_device_performance 0,1",
            "settings delete global restricted_device_performance",
            """settings get global restricted_device_performance | grep -cFx "0,1"""",
            icon = Icons.Default.Bolt),

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
            "Quick Doze (Aggressive)", "Doze Rapido (Aggressivo)",
            "Forces Android Doze mode to activate within 30 seconds of screen-off (default: 60+ min). Huge standby drain reduction.",
            "Forza Doze di Android a 30 secondi dallo spegnimento schermo (default: 60+ minuti). Riduzione enorme drain standby.",
            "settings put global device_idle_constants inactive_to=30000,sensing_to=30000,idle_after_inactive_to=3000,idle_pending_to=30000,max_idle_pending_to=30000,max_idle_to=14400000",
            "settings delete global device_idle_constants",
            """settings get global device_idle_constants | grep -c "inactive_to=30000"""",
            icon = Icons.Default.Bedtime),

        opt("perf_restrict", Optimization.Category.SYSTEM,
            "Unlock CPU in PSM", "Sblocca CPU in PSM",
            "Power Saving Mode normally restricts CPU performance. Keeps full CPU speed while still saving battery on screen/app behavior.",
            "Il Risparmio Energetico normalmente limita la CPU. Mantiene la CPU piena risparmiando comunque su schermo/app.",
            "settings put global restricted_device_performance 0,1",
            "settings delete global restricted_device_performance",
            """settings get global restricted_device_performance | grep -cFx "0,1"""",
            icon = Icons.Default.Bolt),

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
            icon = Icons.Default.AspectRatio),

        opt("resolution_fhd", Optimization.Category.ADVANCED,
            "Reset to Native Resolution", "Ripristina Risoluzione Nativa",
            "Resets display resolution and density to device native defaults. Reverts 720p mode.",
            "Ripristina risoluzione e densità ai valori nativi del dispositivo. Annulla la modalità 720p.",
            "wm size reset; wm density reset",
            "wm size 720x1560; wm density 320",
            """wm size | grep -c "Override"""",
            icon = Icons.Default.AspectRatio),

        opt("net_speed", Optimization.Category.ADVANCED,
            "Network Speed Indicator", "Indicatore Velocità Rete",
            "Shows real-time network speed in the status bar. Equivalent to Galaxy Max Hz Net Speed indicator.",
            "Mostra velocità di rete in tempo reale nella barra di stato. Equivalente all'indicatore Net Speed di Galaxy Max Hz.",
            "settings put secure sysui_net_speed 1",
            "settings put secure sysui_net_speed 0",
            """settings get secure sysui_net_speed | grep -cFx "1"""",
            icon = Icons.Default.NetworkCheck),

        opt("force_resizable", Optimization.Category.ADVANCED,
            "Force Resizable Activities", "Forza Attività Ridimensionabili",
            "Allows all apps to be used in split-screen/multi-window, even those that don't support it. Equivalent to Galaxy Max Hz 'Keep Force Resizable Activities'.",
            "Permette a tutte le app di essere usate in split-screen/multi-finestra, anche quelle che non lo supportano. Equivalente a Galaxy Max Hz.",
            "settings put global force_resizable_activities 1",
            "settings put global force_resizable_activities 0",
            """settings get global force_resizable_activities | grep -cFx "1"""",
            icon = Icons.Default.Crop),

        opt("anim_off", Optimization.Category.ADVANCED,
            "Disable Animations (0x)", "Annulla Animazioni (0x)",
            "Turns off all window/transition animations. Maximum GPU savings but phone feels instant/jarring. Equivalent to Galaxy Max Hz Animation Mod.",
            "Spegne tutte le animazioni. Massimo risparmio GPU ma il telefono sembra immediato/scattoso. Equivalente al Mod Animazioni di Galaxy Max Hz.",
            "settings put global window_animation_scale 0; settings put global transition_animation_scale 0; settings put global animator_duration_scale 0",
            "settings put global window_animation_scale 1; settings put global transition_animation_scale 1; settings put global animator_duration_scale 1",
            """settings get global window_animation_scale | grep -cFx "0"""",
            icon = Icons.Default.Speed),

        opt("batt_protect_85", Optimization.Category.ADVANCED,
            "Battery Protection 85%", "Protezione Batteria 85%",
            "Stops charging at 85%. More usable capacity than 80% while still protecting battery lifespan.",
            "Ferma ricarica a 85%. Più capacità utilizzabile dell'80% proteggendo comunque la batteria.",
            "settings put global battery_protection_default_value 3; settings put global battery_protection_threshold 85",
            "settings put global battery_protection_default_value 1",
            """settings get global battery_protection_threshold | grep -cFx "85"""",
            icon = Icons.Default.BatteryChargingFull),

        opt("batt_protect_90", Optimization.Category.ADVANCED,
            "Battery Protection 90%", "Protezione Batteria 90%",
            "Stops charging at 90%. Good balance for daily use while still extending battery lifespan.",
            "Ferma ricarica a 90%. Buon bilanciamento per uso quotidiano allungando comunque la vita batteria.",
            "settings put global battery_protection_default_value 3; settings put global battery_protection_threshold 90",
            "settings put global battery_protection_default_value 1",
            """settings get global battery_protection_threshold | grep -cFx "90"""",
            icon = Icons.Default.BatteryChargingFull),

        opt("batt_protect_95", Optimization.Category.ADVANCED,
            "Battery Protection 95%", "Protezione Batteria 95%",
            "Stops charging at 95%. Minimal protection but maximum daily range.",
            "Ferma ricarica a 95%. Protezione minima ma massima autonomia giornaliera.",
            "settings put global battery_protection_default_value 3; settings put global battery_protection_threshold 95",
            "settings put global battery_protection_default_value 1",
            """settings get global battery_protection_threshold | grep -cFx "95"""",
            icon = Icons.Default.BatteryChargingFull),

        // ── SCREEN-OFF MODS (require persistent service) ──
        opt("screen_off_low_hz", Optimization.Category.ADVANCED,
            "Screen-Off Low Hz", "Bassa Hz Schermo Spento",
            "Forces the lowest refresh rate when screen turns off (60Hz). Samsung stock behavior locks to HIGHEST Hz on screen-off — this overrides it. Requires background service (notification will appear). Equivalent to Galaxy Max Hz Screen-Off Refresh Mod.",
            "Forza 60Hz a schermo spento. Samsung normalmente forza la MASSIMA frequenza — questa mod la annulla. Richiede servizio in background (apparirà notifica). Equivalente a Galaxy Max Hz.",
            "settings put secure s24opt_screen_off_low_hz 1",
            "settings delete secure s24opt_screen_off_low_hz",
            "",
            icon = Icons.Default.DarkMode),

        opt("screen_off_psm", Optimization.Category.ADVANCED,
            "Auto PSM on Screen-Off", "PSM Automatico a Schermo Spento",
            "Automatically enables Power Saving Mode when screen turns off and disables it when screen turns on. Requires background service (notification will appear). Equivalent to Galaxy Max Hz Auto PSM.",
            "Attiva PSM a schermo spento, lo disattiva a schermo acceso. Richiede servizio in background (apparirà notifica). Equivalente a Galaxy Max Hz.",
            "settings put secure s24opt_screen_off_psm 1",
            "settings delete secure s24opt_screen_off_psm",
            "",
            icon = Icons.Default.BatteryFull),

        opt("screen_off_sync", Optimization.Category.ADVANCED,
            "Auto Sync Off on Screen-Off", "Sincr. Off a Schermo Spento",
            "Disables account sync when screen turns off, re-enables when screen turns on. Saves sync drain. Requires background service (notification will appear). Equivalent to Galaxy Max Hz.",
            "Disattiva sync a schermo spento, riattiva a schermo acceso. Richiede servizio in background (apparirà notifica). Equivalente a Galaxy Max Hz.",
            "settings put secure s24opt_screen_off_sync 1",
            "settings delete secure s24opt_screen_off_sync",
            "",
            icon = Icons.Default.SyncDisabled),
    )

    private fun opt(
        id: String, cat: Optimization.Category,
        titleEn: String, titleIt: String,
        descEn: String, descIt: String,
        apply: String, revert: String, check: String = "",
        icon: ImageVector? = null,
        group: String = "",
    ) = Optimization(id, titleEn, titleIt, descEn, descIt, cat,
        apply.split("; ").filter { it.isNotBlank() },
        revert.split("; ").filter { it.isNotBlank() },
        check.split("; ").filter { it.isNotBlank() },
        icon, group)

    fun byCategory(): Map<Optimization.Category, List<Optimization>> =
        getAll().groupBy { it.category }
}
