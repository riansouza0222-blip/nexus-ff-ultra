package com.nexus.ffultra.ui

import android.app.Application
import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.nexus.ffultra.core.Prefs
import com.nexus.ffultra.core.Snapshot
import com.nexus.ffultra.core.SnapshotBus
import com.nexus.ffultra.core.Tier
import com.nexus.ffultra.diagnostics.DeviceDiagnostics
import com.nexus.ffultra.diagnostics.DeviceInfo
import com.nexus.ffultra.monitor.SystemMonitor
import com.nexus.ffultra.optimizer.MegaBoost
import com.nexus.ffultra.profiles.OptimizationProfile
import com.nexus.ffultra.profiles.Profiles
import com.nexus.ffultra.recommendations.Recommendation
import com.nexus.ffultra.recommendations.RecommendationEngine
import com.nexus.ffultra.service.MonitorService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val ctx: Context = app
    private val monitor = SystemMonitor(ctx)
    private val prefs = Prefs(ctx)
    private val boost = MegaBoost(ctx)

    val device: DeviceInfo = DeviceDiagnostics.run(ctx)
    var profile by mutableStateOf(Profiles.active(ctx, device)); private set
    var override by mutableStateOf(prefs.profileOverride?.let { runCatching { Tier.valueOf(it) }.getOrNull() }); private set
    var snapshot by mutableStateOf<Snapshot?>(null); private set
    var recs by mutableStateOf<List<Recommendation>>(emptyList()); private set
    var log by mutableStateOf<List<String>>(emptyList()); private set
    var boostActive by mutableStateOf(prefs.boostActive); private set
    var serviceRunning by mutableStateOf(false); private set
    var overlayEnabled by mutableStateOf(prefs.overlayEnabled); private set

    suspend fun tick() {
        val s = withContext(Dispatchers.Default) { monitor.read() }
        snapshot = s
        recs = RecommendationEngine.evaluate(s, device, profile)
        serviceRunning = SnapshotBus.running.value
        boostActive = prefs.boostActive
    }

    fun selectTier(t: Tier?) {
        prefs.profileOverride = t?.name
        override = t
        profile = Profiles.active(ctx, device)
    }

    fun runBoost() { log = boost.run(profile); boostActive = prefs.boostActive }
    fun restore() { log = boost.restore(); boostActive = prefs.boostActive }
    fun dndGranted() = boost.dndGranted()

    fun setService(on: Boolean) { if (on) MonitorService.start(ctx) else MonitorService.stop(ctx) }

    /** Retorna false se faltar a permissão de sobreposição (a UI então abre a tela de configurações). */
    fun setOverlay(on: Boolean): Boolean {
        if (on && !Settings.canDrawOverlays(ctx)) return false
        prefs.overlayEnabled = on
        overlayEnabled = on
        if (on && !SnapshotBus.running.value) MonitorService.start(ctx)
        return true
    }
}
