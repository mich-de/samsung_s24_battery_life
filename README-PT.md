# S24 Battery Optimizer

🌐 **[English](README.md) | [Español](README-ES.md) | [Italiano](README-IT.md) | Português**

> **Restaure a duração da bateria do seu Galaxy S24** — desative bloatware, interrompa erros conhecidos de consumo de bateria e otimize as configurações do sistema, **sem necessidade de root**.

<p align="center">
  <img src="current_android_screenshot.png" alt="S24 Battery Optimizer em Ação" width="300" />
</p>

---

## 📋 Compatibilidade

| Modelo | One UI / Android |
|--------|------------------|
| SM-S921B (S24 base) | One UI 7 / Android 16 (testado em Exynos, não testado em Snapdragon) |
| SM-S926B (S24+) | One UI 7 / Android 16 (não testado) |
| SM-S928B (S24 Ultra) | One UI 7 / Android 16 (não testado) |
| Séries S23, S22, S21 | One UI 5+ / Android 13+ (não testado) |

---

## 🔧 O que faz

### Bloatware desativado (24 pacotes)

| Aplicativo | Pacote | Razão |
|------------|--------|-------|
| **Bixby Voice** | `bixby.agent` | Assistente de voz sempre escutando |
| **Bixby Wakeup** | `bixby.wakeup` | Ativação por palavra "Hi Bixby" |
| **Bixby Vision** | `bixbyvision.framework` | Reconhecimento de IA na câmera |
| **Vision Intelligence** | `visionintelligence` | Análise de cenários por IA |
| **Game Tools** | `game.gametools` | Painel de sobreposição de jogos |
| **Game Optimizing** | `game.gos` | Limitação de CPU/GPU em segundo plano |
| **Smart Suggestions** | `smartsuggestions` | Sugestões contextuais |
| **Aware Service** | `aware.service` | Detecção de atividade física |
| **BBC Agent** | `bbc.bbcagent` | Atualizações push de bloatware |
| **Reminder** | `app.reminder` | Lembretes da Samsung |
| **Routines** | `app.routines` | Automações do Bixby |
| **Routine Plus** | `app.routineplus` | Wakelock de Doze (consumo confirmado) |
| **Live Effect** | `liveeffectservice` | Efeitos em chamadas de vídeo |
| **OneConnect** | `oneconnect` | SmartThings Find (busca constante) |
| **STPlatform** | `service.stplatform` | Serviço de segurança Knox em segundo plano |
| **Forest** | `forest` | Bem-estar digital |
| **Buds Manager** | `accessory.budsunitemgr` | Plugin dos Galaxy Buds (se não usados) |
| **Rubin App** | `rubin.app` | Serviço de personalização (causa loop no Play Services) |
| **Facebook** | `facebook.katana` | Grande consumidor de bateria |
| **Messenger** | `facebook.orca` | Wakelocks e notificações constantes |
| **Edge** | `microsoft.emmx` | Navegador da Microsoft redundante |
| **Excel** | `office.excel` | Sincronização do Office em segundo plano |
| **Word** | `office.word` | Sincronização do Office em segundo plano |
| **Remote Desktop** | `rdc.androidx` | Cliente RDP redundante |

### 🔄 Redefinição de Knox Matrix (5 pacotes)

Redefine os serviços Knox afetados pelo **loop de atestação de abril de 2026** (causa de consumo contínuo de CPU). `kpecore`, `attestation`, `pushmanager`, `containercore`, `analytics.uploader`.

### ⚙️ Ajustes do sistema (23)

| Categoria | Ajuste | Valor |
|-----------|--------|-------|
| **Bateria** | Adaptive Battery | ON |
| | App Auto Restriction | ON |
| | Battery Saver | ON |
| | Enhanced CPU Resp. | OFF |
| | Enhanced Processing | OFF |
| **Rádio** | BLE Scan Always | OFF |
| | Nearby Scanning | OFF |
| | WiFi Power Save | ON |
| | WiFi Wakeup | OFF |
| | MCF Quick Share | OFF |
| **Tela** | AOD / Always On | OFF |
| | Screen Timeout | 30s |
| | Lift to Wake | OFF |
| | Smart Stay | OFF |
| | Doze | ON |
| **Desempenho** | Animações | 0.5x |
| | RAM Plus | 2 GB |
| **Proteção** | Carga máxima | 80% |

### 🚫 Restrição de segundo plano (3 aplicativos)

`appops set RUN_ANY_IN_BACKGROUND deny` em:
- **Instagram** — wakelocks contínuos e sincronização
- **WhatsApp** — 499 mAh em 8 horas de teste (16% do total)
- **Tandem** (editar nome do pacote se necessário)

---

## 📊 Resultados esperados

### Teste de descarga — Antes da otimização

```
Duração:        8h 42m (80% → 19% = 61% usado)
Capacidade:     3.114 mAh consumidos
Média:          358 mA/h
Vida estimada:  ~10.9h (uso misto)

Principais consumidores (mAh):
  CPU                    1.252 (40%)
  Tela                    542 (17%)
  Rádio 5G                440 (14%)
  WhatsApp                499 (16%)
  Sistema                 386 (12%)
  Instagram               314 (10%)
  Google Play Services    288 (9%)
  Kernel                  186 (6%)
```

### Após a otimização (dados em coleta)

Relatórios da comunidade indicam:
- Redução de **15-25%** no consumo em standby.
- **+2-4 horas** extras em uso misto.
- WhatsApp e Instagram não consomem mais em segundo plano.

---

## 🚀 Como usar

### Requisitos
- PC com Windows/Linux e ADB configurado (`platform-tools`).
- Depuração USB ativada no celular.
- Celular conectado ao PC.

### Instalação

```bash
git clone https://github.com/mich-de/samsung_s24_battery_life
cd samsung_s24_battery_life
```

### Execução

**No Windows (PowerShell):**
```powershell
.\releases\s24-optimize.bat
```

**No Linux / macOS (Terminal):**
```bash
chmod +x ./releases/s24-optimize.sh
./releases/s24-optimize.sh
```

### Desfazer tudo

**No Windows (PowerShell):**
```powershell
.\releases\s24-restore.bat
```

**No Linux / macOS (Terminal):**
```bash
chmod +x ./releases/s24-restore.sh
./releases/s24-restore.sh
```

---

## 📂 Estrutura de arquivos

```
s24-battery-optimizer/
├── releases/                   ← Diretório contendo todos os executáveis
│   ├── s24-optimize.bat        ← Script do Windows Batch (principal)
│   ├── s24-restore.bat         ← Restauração para Windows
│   ├── s24-optimize.sh         ← Script do Unix/Linux Shell (principal)
│   ├── s24-restore.sh          ← Restauração para Unix/Linux
│   └── s24-battery-optimizer.apk ← Aplicativo companheiro nativo Android (Kotlin)
├── android-app/                ← Código fonte do aplicativo Android
│   └── ...                     (Usa Shizuku para comandos ADB locais)
├── README.md                   ← Documentação em inglês (Padrão)
└── README-IT.md                ← Documentación em italiano
```

---

## 📱 Aplicativo Android (Shizuku)

O projeto inclui um aplicativo nativo para Android em Kotlin localizado em [android-app](file:///C:/Users/mdeangelis/Downloads/s24app/android-app). Este aplicativo permite:
- Monitorar e gerenciar o status de otimização diretamente do seu telefone.
- Executar comandos de shell do ADB locais sem conectar seu dispositivo a um PC, aproveitando a API do aplicativo **Shizuku**.

### Como configurar o Shizuku (Android 16 / One UI 7)

Para executar o aplicativo Android complementar, você deve configurar a versão mais recente do Shizuku:

1. **Baixar o Shizuku**: 
   Baixe e instale a versão mais recente (**v13.6.0** ou posterior) diretamente das [Releases do GitHub do Shizuku](https://github.com/RikkaApps/Shizuku/releases) para garantir compatibilidade com as alterações do framework do Android 16 QPR1.
2. **Iniciar o Shizuku**:
   - Se iniciado via PC, use o novo caminho de execução do binário nativo recomendado dentro do aplicativo Shizuku (ex. `adb shell /data/app/.../lib/arm64/libshizuku.so`).
   - Se iniciado via **Depuração sem fio**, use o botão "Iniciar" no aplicativo Shizuku após o pareamento.
3. **Autorizar o aplicativo**:
   Abra o aplicativo Shizuku, vá em **Aplicativos autorizados** e ative a opção para o **S24 Battery Optimizer**.

---

## ❓ FAQ

**Vou perder meus dados?** Não. `pm disable-user` apenas oculta os aplicativos. Execute `s24-restore.bat` ou `./s24-restore.sh` para restaurá-los.

**O Google Pay / Wallet funciona?** Sim, a menos que você limpe os dados do GMS.

**O Samsung Pay funciona?** O script não toca no `spayfw`. Se você desativá-lo manualmente, o Samsung Wallet deixará de funcionar.

**O Bixby não funciona mais?** Correto. Se você precisa do Bixby, edite o script e remova os pacotes do Bixby.

**O Hey Google / Gemini funciona?** Sim. O único assistente desativado é o Bixby.

---

## 🤝 Contribuir

Contribuições são bem-vindas. Ideias:
- Adicionar modelos (S23, S22, S21, S20, Pixel)
- Melhorar a interface do usuário do app Android Shizuku

---

## 📜 Licença

MIT — use, modifique e compartilhe livremente.
