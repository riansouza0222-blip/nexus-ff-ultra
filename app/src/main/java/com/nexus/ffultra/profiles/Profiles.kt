package com.nexus.ffultra.profiles

import android.content.Context
import com.nexus.ffultra.core.Prefs
import com.nexus.ffultra.core.Tier
import com.nexus.ffultra.diagnostics.DeviceInfo

data class OptimizationProfile(
    val tier: Tier, val pollMs: Long, val ramWarnPct: Int,
    val tempWarnC: Float, val tempCritC: Float, val useDnd: Boolean,
    val gameTips: List<String>,
)

object Profiles {
    fun forTier(t: Tier): OptimizationProfile = when (t) {
        Tier.BASIC -> OptimizationProfile(t, 4000, 80, 38f, 42f, true, listOf(
            "No Free Fire: gráficos 'Suave' e FPS 'Padrão'.",
            "Desative sombras e efeitos de alta qualidade nas configurações do jogo.",
            "Feche apps pesados manualmente antes de jogar (o Android não permite que outro app faça isso por você).",
            "Mantenha pelo menos 3 GB livres para o jogo e suas atualizações."))
        Tier.MID -> OptimizationProfile(t, 3000, 85, 40f, 44f, true, listOf(
            "No Free Fire: gráficos 'Padrão' e FPS 'Alto' se a opção existir no seu aparelho.",
            "Se o aparelho esquentar, volte para FPS 'Padrão' – o calor reduz o desempenho.",
            "Em Configurações > Bateria, desative a economia de energia durante as partidas."))
        Tier.HIGH -> OptimizationProfile(t, 2000, 90, 42f, 46f, true, listOf(
            "No Free Fire: use a maior qualidade/FPS que o jogo liberar para o seu modelo.",
            "Evite jogar carregando em carregador rápido: o calor pode causar throttling.",
            "Se seu Android tiver 'Modo Jogo'/'Game Dashboard', ative para o Free Fire."))
    }

    fun active(ctx: Context, device: DeviceInfo): OptimizationProfile {
        val o = Prefs(ctx).profileOverride?.let { runCatching { Tier.valueOf(it) }.getOrNull() }
        return forTier(o ?: device.tier)
    }
}
