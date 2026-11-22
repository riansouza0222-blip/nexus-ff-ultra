# NEXUS FF ULTRA

Otimizador gamer para Free Fire (Android 8.0+, Kotlin + Jetpack Compose). Tema preto e vermelho.

## O que faz (e o que NÃO faz)
**Faz:** monitora temperatura, RAM, CPU (estimada), bateria, armazenamento e taxa do display; diagnostica o aparelho e o
classifica (Básico / Intermediário / Avançado); aplica perfis com limites de alerta e dicas; envia notificações de desempenho;
painel flutuante opcional; **MEGA BOOST** (Não Perturbe + monitoramento) com **Restaurar**.

**Não faz:** cheats, aimbot, leitura/escrita de memória, injeção de código, modificação do Free Fire, limpeza de RAM de outros
apps (o Android não permite) nem promessa de FPS acima do hardware. O FPS interno do jogo não é acessível a apps comuns; o painel
exibe a taxa de quadros do display.

## Arquitetura (pacotes independentes em `app/src/main/java/com/nexus/ffultra/`)
| Pacote | Responsabilidade |
|---|---|
| `core` | Modelos, `Prefs` (estado p/ restauração), `SnapshotBus` |
| `monitor` | `SystemMonitor`, `CpuReader` |
| `diagnostics` | `DeviceDiagnostics` (classificação do aparelho) |
| `profiles` | `Profiles` (limites e dicas por nível) |
| `recommendations` | `RecommendationEngine` (regras puras, testáveis) |
| `optimizer` | `MegaBoost` (aplicar/restaurar) |
| `gamemode` | `GameLauncher` (abre Free Fire / MAX) |
| `notifications` | `PerfNotifier` (canais, alertas com cooldown) |
| `service` | `MonitorService` (foreground service) |
| `overlay` | `FloatingPanel` (opcional) |
| `ui` | Compose, ViewModel, tema |

Hoje é um único módulo Gradle (`:app`) com pacotes de baixo acoplamento; cada pacote pode virar um módulo Gradle separado.

## Permissões
`POST_NOTIFICATIONS`, `FOREGROUND_SERVICE` (+`SPECIAL_USE`), `ACCESS_NOTIFICATION_POLICY` (opcional, p/ Não Perturbar),
`SYSTEM_ALERT_WINDOW` (opcional, p/ painel flutuante). Nenhuma permissão de internet, localização, contatos ou armazenamento.

## Gerar o APK
**A – GitHub Actions (sem instalar nada):** suba a pasta para um repositório; em *Actions → Build APK*, baixe o
artefato `NEXUS-FF-ULTRA-apk` (`app-debug.apk` / `app-release.apk`).

**B – Android Studio:** *File → Open* nesta pasta → *Build → Build APK(s)*.

**C – Linha de comando** (JDK 17 + Android SDK 34, Gradle 8.9):
`gradle wrapper --gradle-version 8.9 && ./gradlew assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk`

Para instalar: permita "instalar apps de fontes desconhecidas" e abra o APK. O release usa a chave debug (apenas sideload);
para publicar na Play Store, configure sua própria keystore.

## Uso
1. Abra o app e conceda notificações. 2. (Opcional) permita Não Perturbar e painel flutuante.
3. Toque em **MEGA BOOST** antes de jogar. 4. Ao terminar, **Restaurar configurações**.
