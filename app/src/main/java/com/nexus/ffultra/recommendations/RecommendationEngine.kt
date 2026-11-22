package com.nexus.ffultra.recommendations

import com.nexus.ffultra.core.Severity
import com.nexus.ffultra.core.Snapshot
import com.nexus.ffultra.diagnostics.DeviceInfo
import com.nexus.ffultra.profiles.OptimizationProfile

data class Recommendation(val severity: Severity, val title: String, val detail: String)

object RecommendationEngine {
    fun evaluate(s: Snapshot, d: DeviceInfo, p: OptimizationProfile): List<Recommendation> {
        val r = mutableListOf<Recommendation>()
        s.tempC?.let {
            if (it >= p.tempCritC) r += Recommendation(Severity.CRITICAL, "Temperatura crítica", "%.1f°C. Faça uma pausa, tire a capinha e reduza gráficos/FPS.".format(it))
            else if (it >= p.tempWarnC) r += Recommendation(Severity.WARN, "Aparelho aquecendo", "%.1f°C. O sistema pode reduzir o desempenho (throttling).".format(it))
        }
        s.thermalStatus?.let {
            if (it >= 3) r += Recommendation(Severity.CRITICAL, "Limitação térmica severa", "O Android está reduzindo o desempenho para proteger o hardware.")
            else if (it == 2) r += Recommendation(Severity.WARN, "Limitação térmica moderada", "Reduza a qualidade gráfica do jogo.")
        }
        if (s.lowMemory) r += Recommendation(Severity.CRITICAL, "Memória baixa", "O sistema está sob pressão de RAM. Feche apps em segundo plano manualmente.")
        else if (s.ramUsedPct >= p.ramWarnPct) r += Recommendation(Severity.WARN, "RAM alta (${s.ramUsedPct}%)", "Feche apps abertos antes de iniciar a partida.")
        if (s.powerSave) r += Recommendation(Severity.WARN, "Economia de bateria ativa", "Ela limita CPU/GPU. Desative para jogar com desempenho total.")
        if (!s.charging && s.batteryPct <= 15) r += Recommendation(Severity.WARN, "Bateria baixa (${s.batteryPct}%)", "Conecte o carregador; o desempenho pode cair em bateria baixa.")
        if (s.storageFreeGb < 2f) r += Recommendation(Severity.WARN, "Pouco armazenamento", "Apenas %.1f GB livres. Libere espaço para evitar travamentos e falhas de atualização.".format(s.storageFreeGb))
        if (s.refreshHz > 60.5f && d.tier.ordinal == 0) r += Recommendation(Severity.INFO, "Tela de ${s.refreshHz.toInt()} Hz", "Em aparelhos básicos, 60 Hz economiza bateria e calor.")
        if (r.isEmpty()) r += Recommendation(Severity.INFO, "Tudo em ordem", "Nenhum problema detectado. Bom jogo!")
        return r
    }
}
