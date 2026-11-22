package com.nexus.ffultra.optimizer

import android.app.NotificationManager
import android.content.Context
import com.nexus.ffultra.core.Prefs
import com.nexus.ffultra.core.SnapshotBus
import com.nexus.ffultra.profiles.OptimizationProfile
import com.nexus.ffultra.service.MonitorService

/**
 * MEGA BOOST: aplica apenas ações permitidas pelo Android, todas reversíveis:
 *  1. Não Perturbar (prioritário) - somente se o usuário concedeu acesso; o estado anterior é salvo.
 *  2. Inicia o monitoramento em segundo plano (alertas de temperatura/RAM/bateria).
 * NÃO limpa RAM de outros apps: o Android não permite isso a apps comuns, e "limpadores" costumam piorar o desempenho.
 */
class MegaBoost(private val ctx: Context) {
    private val prefs = Prefs(ctx)
    private val nm = ctx.getSystemService(NotificationManager::class.java)

    fun dndGranted() = nm.isNotificationPolicyAccessGranted

    fun run(profile: OptimizationProfile): List<String> {
        if (prefs.boostActive) return listOf("O Mega Boost já está ativo. Use 'Restaurar' para desfazer.")
        val log = mutableListOf<String>()
        if (profile.useDnd) {
            if (dndGranted()) {
                prefs.savedInterruptionFilter = nm.currentInterruptionFilter
                nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                log += "✔ Não Perturbe (prioritário) ativado - menos interrupções na partida."
            } else log += "– Não Perturbe ignorado: permissão não concedida (opcional)."
        }
        prefs.serviceStartedByBoost = !SnapshotBus.running.value
        MonitorService.start(ctx)
        log += "✔ Monitoramento em segundo plano ativo (perfil ${profile.tier.label})."
        prefs.boostActive = true
        log += "ℹ Dica: feche apps pesados manualmente e ajuste gráficos no jogo (veja as dicas do perfil)."
        return log
    }

    fun restore(): List<String> {
        val log = mutableListOf<String>()
        val saved = prefs.savedInterruptionFilter
        if (saved != -1 && dndGranted()) {
            nm.setInterruptionFilter(saved)
            log += "✔ Modo Não Perturbe restaurado ao estado anterior."
        }
        prefs.savedInterruptionFilter = -1
        if (prefs.serviceStartedByBoost) { MonitorService.stop(ctx); log += "✔ Monitoramento iniciado pelo Boost foi encerrado." }
        prefs.serviceStartedByBoost = false
        prefs.boostActive = false
        if (log.isEmpty()) log += "Nada para restaurar."
        return log
    }
}
