# S24 Battery Optimizer

🌐 **[English](README.md) | Español | [Italiano](README-IT.md) | [Português](README-PT.md)**

> **Restaura la duración de la batería de tu Galaxy S24** — deshabilita el bloatware, detiene los errores de drenaje conocidos y optimiza la configuración del sistema, **sin necesidad de root**.

---

## 📋 Compatibilidad

| Modelo | One UI / Android |
|--------|------------------|
| SM-S921B (S24 base) | One UI 7 / Android 16 |
| SM-S926B (S24+) | One UI 7 / Android 16 |
| SM-S928B (S24 Ultra) | One UI 7 / Android 16 |
| Series S23, S22, S21 | One UI 5+ / Android 13+ (no probado) |

---

## 🔧 Qué hace

### Bloatware deshabilitado (24 paquetes)

| Aplicación | Paquete | Razón |
|------------|---------|-------|
| **Bixby Voice** | `bixby.agent` | Asistente siempre en escucha |
| **Bixby Wakeup** | `bixby.wakeup` | Palabra clave "Hi Bixby" siempre activa |
| **Bixby Vision** | `bixbyvision.framework` | Reconocimiento de IA en cámara |
| **Vision Intelligence** | `visionintelligence` | Análisis de escenas por IA |
| **Game Tools** | `game.gametools` | Panel de superposición de juegos |
| **Game Optimizing** | `game.gos` | Limitación de CPU/GPU en segundo plano |
| **Smart Suggestions** | `smartsuggestions` | Sugerencias contextuales |
| **Aware Service** | `aware.service` | Detección de actividad física |
| **BBC Agent** | `bbc.bbcagent` | Actualizaciones push de bloatware |
| **Reminder** | `app.reminder` | Recordatorios de Samsung |
| **Routines** | `app.routines` | Automatizaciones de Bixby |
| **Routine Plus** | `app.routineplus` | Bloqueo Doze (drenaje confirmado) |
| **Live Effect** | `liveeffectservice` | Efectos para videollamadas |
| **OneConnect** | `oneconnect` | SmartThings Find (escaneo constante) |
| **STPlatform** | `service.stplatform` | Plataforma segura Knox en segundo plano |
| **Forest** | `forest` | Bienestar digital |
| **Buds Manager** | `accessory.budsunitemgr` | Plugin de Galaxy Buds (si no se usan) |
| **Rubin App** | `rubin.app` | Servicio de personalización (causa bucle en Play Services) |
| **Facebook** | `facebook.katana` | Gran consumidor de batería |
| **Messenger** | `facebook.orca` | Wakelocks y notificaciones constantes |
| **Edge** | `microsoft.emmx` | Navegador de Microsoft redundante |
| **Excel** | `office.excel` | Sincronización de Office en segundo plano |
| **Word** | `office.word` | Sincronización de Office en segundo plano |
| **Remote Desktop** | `rdc.androidx` | Cliente RDP redundante |

### 🔄 Restablecimiento de Knox Matrix (5 paquetes)

Restablece los servicios de Knox afectados por el **bucle de atestación de abril de 2026** (causa de uso continuo de CPU). `kpecore`, `attestation`, `pushmanager`, `containercore`, `analytics.uploader`.

### ⚙️ Ajustes del sistema (23)

| Categoría | Ajuste | Valor |
|-----------|--------|-------|
| **Batería** | Adaptive Battery | ON |
| | App Auto Restriction | ON |
| | Battery Saver | ON |
| | Enhanced CPU Resp. | OFF |
| | Enhanced Processing | OFF |
| **Radio** | BLE Scan Always | OFF |
| | Nearby Scanning | OFF |
| | WiFi Power Save | ON |
| | WiFi Wakeup | OFF |
| | MCF Quick Share | OFF |
| **Pantalla** | AOD / Always On | OFF |
| | Screen Timeout | 30s |
| | Lift to Wake | OFF |
| | Smart Stay | OFF |
| | Doze | ON |
| **Rendimiento** | Animaciones | 0.5x |
| | RAM Plus | 2 GB |
| **Protección** | Carga máxima | 80% |

### 🚫 Restricción de segundo plano (3 aplicaciones)

`appops set RUN_ANY_IN_BACKGROUND deny` en:
- **Instagram** — wakelocks continuos y sincronización
- **WhatsApp** — 499 mAh en 8 horas de prueba (16% del total)
- **Tandem** (editar nombre de paquete si es necesario)

---

## 📊 Resultados esperados

### Prueba de descarga — Antes de la optimización

```
Duración:       8h 42m (80% → 19% = 61% usado)
Capacidad:      3,114 mAh consumidos
Promedio:       358 mA/h
Vida estimada:  ~10.9h (uso mixto)

Principales consumidores (mAh):
  CPU                    1,252 (40%)
  Pantalla                542 (17%)
  Radio 5G                440 (14%)
  WhatsApp                499 (16%)
  Sistema                 386 (12%)
  Instagram               314 (10%)
  Google Play Services    288 (9%)
  Kernel                  186 (6%)
```

### Después de la optimización (datos en recopilación)

Reportes de la comunidad indican:
- Reducción del **15-25%** en el drenaje en espera (standby).
- **+2-4 horas** extra en uso mixto.
- WhatsApp e Instagram ya no consumen en segundo plano.

---

## 🚀 Cómo usar

### Requisitos
- PC con Windows/Linux y ADB configurado (`platform-tools`).
- Depuración USB activada en el teléfono.
- Teléfono conectado a la PC.

### Instalación

```bash
git clone https://github.com/mich-de/samsung_s24_battery_life
cd samsung_s24_battery_life
```

### Ejecución

**En Windows (PowerShell):**
```powershell
.\releases\s24-optimize.bat
```

**En Linux / macOS (Terminal):**
```bash
chmod +x ./releases/s24-optimize.sh
./releases/s24-optimize.sh
```

### Deshacer todo

**En Windows (PowerShell):**
```powershell
.\releases\s24-restore.bat
```

**En Linux / macOS (Terminal):**
```bash
chmod +x ./releases/s24-restore.sh
./releases/s24-restore.sh
```

---

## 📂 Estructura de archivos

```
s24-battery-optimizer/
├── releases/                   ← Directorio con todos los ejecutables
│   ├── s24-optimize.bat        ← Script de Windows Batch (principal)
│   ├── s24-restore.bat         ← Restauración para Windows
│   ├── s24-optimize.sh         ← Script de Unix/Linux Shell (principal)
│   ├── s24-restore.sh          ← Restauración para Unix/Linux
│   └── s24-battery-optimizer.apk ← Aplicación compañera para Android (Kotlin)
├── android-app/                ← Código fuente de la aplicación Android
│   └── ...                     (Usa Shizuku para comandos ADB locales)
├── README.md                   ← Documentación en inglés (Predeterminada)
└── README-IT.md                ← Documentación en italiano
```

---

## 📱 Aplicación Android (Shizuku)

El proyecto incluye una aplicación nativa para Android en Kotlin ubicada en [android-app](file:///C:/Users/mdeangelis/Downloads/s24app/android-app). Esta aplicación te permite:
- Monitorear y gestionar el estado de optimización directamente desde tu teléfono.
- Ejecutar comandos de shell de ADB locales sin conectar tu dispositivo a una PC, aprovechando la API de la aplicación **Shizuku**.

---

## ❓ FAQ

**¿Perderé datos?** No. `pm disable-user` solo oculta las aplicaciones. Ejecuta `s24-restore.bat` o `./s24-restore.sh` para restaurarlas.

**¿Funciona Google Pay / Wallet?** Sí, a menos que limpies los datos de GMS.

**¿Funciona Samsung Pay?** El script no toca `spayfw`. Si lo deshabilitas manualmente, Samsung Wallet dejará de funcionar.

**¿Bixby ya no funciona?** Correcto. Si necesitas Bixby, edita el script y elimina los paquetes de Bixby.

**¿Funciona Hey Google / Gemini?** Sí. El único asistente deshabilitado es Bixby.

---

## 🤝 Contribuir

Las contribuciones son bienvenidas. Ideas:
- Añadir modelos (S23, S22, S21, S20, Pixel)
- Mejorar la interfaz de usuario de la app Android Shizuku

---

## 📜 Licencia

MIT — usa, modifica y comparte de forma libre.
