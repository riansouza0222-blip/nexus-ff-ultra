package com.nexus.ffultra.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.ffultra.core.Severity
import com.nexus.ffultra.core.Snapshot
import com.nexus.ffultra.core.Tier
import com.nexus.ffultra.gamemode.GameLauncher

@Composable
fun Dashboard(vm: MainViewModel, onOverlayPermission: () -> Unit, onDndPermission: () -> Unit, onLaunchGame: (String) -> Unit) {
    val ctx = LocalContext.current
    val d = vm.device
    LazyColumn(
        Modifier.fillMaxSize().background(NxBlack).statusBarsPadding().navigationBarsPadding().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("NEXUS FF ULTRA", color = NxRed, fontSize = 28.sp, fontWeight = FontWeight.Black)
            Text("Otimizador de desempenho para Free Fire", color = NxMuted, fontSize = 13.sp)
        }
        item {
            NxCard {
                Text(d.model, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("${d.androidVersion} • ${d.abi}", color = NxMuted, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                Text("Classificação: ${d.tier.label}", color = NxRed, fontWeight = FontWeight.Bold)
                Text(d.reasons.joinToString(" • "), color = NxMuted, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Text("Perfil ativo: ${vm.profile.tier.label}", fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Chip("Auto", vm.override == null) { vm.selectTier(null) }
                    Tier.entries.forEach { t -> Chip(t.label, vm.override == t) { vm.selectTier(t) } }
                }
            }
        }
        item { vm.snapshot?.let { Metrics(it, vm) } }
        item {
            NxCard {
                Button(
                    onClick = { vm.runBoost() }, enabled = !vm.boostActive,
                    modifier = Modifier.fillMaxWidth().height(64.dp), shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NxRed, disabledContainerColor = NxRedDark),
                ) { Text(if (vm.boostActive) "MEGA BOOST ATIVO" else "MEGA BOOST", fontSize = 22.sp, fontWeight = FontWeight.Black) }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { vm.restore() }, enabled = vm.boostActive, modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, NxRed)) { Text("Restaurar configurações", color = NxText) }
                if (!vm.dndGranted()) TextButton(onClick = onDndPermission) { Text("Permitir 'Não Perturbar' (opcional)", color = NxRed) }
                vm.log.forEach { Text(it, fontSize = 12.sp, color = NxMuted) }
            }
        }
        item {
            NxCard {
                ToggleRow("Monitoramento em segundo plano", vm.serviceRunning) { vm.setService(it) }
                ToggleRow("Painel flutuante", vm.overlayEnabled) { if (!vm.setOverlay(it)) onOverlayPermission() }
            }
        }
        item { Text("Recomendações", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
        items(vm.recs) { r ->
            val c = when (r.severity) { Severity.CRITICAL -> NxRed; Severity.WARN -> NxWarn; Severity.INFO -> NxMuted }
            NxCard(border = c) { Text(r.title, color = c, fontWeight = FontWeight.Bold); Text(r.detail, fontSize = 13.sp) }
        }
        item {
            NxCard {
                Text("Dicas para o perfil ${vm.profile.tier.label}", fontWeight = FontWeight.Bold)
                vm.profile.gameTips.forEach { Text("• $it", fontSize = 13.sp, color = NxMuted) }
            }
        }
        item {
            val games = GameLauncher.installed(ctx)
            NxCard {
                if (games.isEmpty()) Text("Free Fire não encontrado neste aparelho.", color = NxMuted)
                games.forEach { (pkg, name) ->
                    Button(onClick = { onLaunchGame(pkg) }, modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NxRedDark)) { Text("Abrir $name") }
                }
            }
        }
        item {
            Text("O NEXUS FF ULTRA não modifica o Free Fire, não usa cheats nem acessa a memória do jogo. " +
                "Ele não aumenta o FPS acima da capacidade do aparelho: apenas monitora, recomenda e aplica ajustes seguros e reversíveis. " +
                "O FPS interno do jogo não está disponível para apps comuns; o painel mostra a taxa do display.",
                color = NxMuted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun Metrics(s: Snapshot, vm: MainViewModel) {
    val p = vm.profile
    NxCard {
        Meter("Temperatura", s.tempC?.let { "%.1f°C".format(it) } ?: "indisponível", s.tempC?.let { it / 50f }, (s.tempC ?: 0f) >= p.tempWarnC)
        Meter("RAM", "${s.ramUsedPct}% (${s.ramAvailMb} MB livres)", s.ramUsedPct / 100f, s.ramUsedPct >= p.ramWarnPct)
        Meter("CPU (estimada)", s.cpuLoadPct?.let { "$it% • ${s.cpuCores} núcleos" } ?: "indisponível neste aparelho", s.cpuLoadPct?.div(100f), false)
        Meter("Bateria", "${s.batteryPct}%${if (s.charging) " (carregando)" else ""}", s.batteryPct / 100f, !s.charging && s.batteryPct <= 15)
        Meter("Armazenamento", "%.1f GB livres de %.0f GB".format(s.storageFreeGb, s.storageTotalGb),
            1f - s.storageFreeGb / s.storageTotalGb, s.storageFreeGb < 2f)
        Text("Display: ${s.refreshHz.toInt()} Hz • FPS do jogo: indisponível sem root", color = NxMuted, fontSize = 12.sp)
    }
}

@Composable
private fun Meter(label: String, value: String, progress: Float?, warn: Boolean) {
    Column(Modifier.padding(vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 13.sp, color = NxMuted); Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { (progress ?: 0f).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(6.dp),
            color = if (warn) NxWarn else NxRed, trackColor = Color(0xFF2A2A2A),
        )
    }
}

@Composable
private fun NxCard(border: Color = Color(0xFF2A2A2A), content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NxSurface), border = BorderStroke(1.dp, border)) {
        Column(Modifier.padding(14.dp), content = content)
    }
}

@Composable
private fun Chip(text: String, selected: Boolean, onClick: () -> Unit) = FilterChip(
    selected = selected, onClick = onClick, label = { Text(text, fontSize = 12.sp) },
    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NxRed, selectedLabelColor = Color.White),
)

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) =
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp)
        Switch(checked = checked, onCheckedChange = onChange,
            colors = SwitchDefaults.colors(checkedTrackColor = NxRed, checkedThumbColor = Color.White))
    }
