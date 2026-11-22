package com.nexus.ffultra.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.nexus.ffultra.core.Prefs
import com.nexus.ffultra.core.Severity
import com.nexus.ffultra.core.SnapshotBus
import com.nexus.ffultra.diagnostics.DeviceDiagnostics
import com.nexus.ffultra.monitor.SystemMonitor
import com.nexus.ffultra.notifications.PerfNotifier
import com.nexus.ffultra.overlay.FloatingPanel
import com.nexus.ffultra.profiles.Profiles
import com.nexus.ffultra.recommendations.RecommendationEngine
import kotlinx.coroutines.*

/** Serviço em primeiro plano (visível ao usuário via notificação) que monitora e emite alertas. */
class MonitorService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var job: Job? = null
    private var panel: FloatingPanel? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        PerfNotifier.ensureChannels(this)
        val n = PerfNotifier.foreground(this)
        if (Build.VERSION.SDK_INT >= 34) startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        else startForeground(1, n)
        SnapshotBus.running.value = true
        if (job == null) job = scope.launch { loop() }
        return START_STICKY
    }

    private suspend fun loop() {
        val monitor = SystemMonitor(this)
        val prefs = Prefs(this)
        val device = DeviceDiagnostics.run(this)
        while (currentCoroutineContext().isActive) {
            val profile = Profiles.active(this, device)
            val s = monitor.read()
            withContext(Dispatchers.Main) {
                val wantPanel = prefs.overlayEnabled && Settings.canDrawOverlays(this@MonitorService)
                if (wantPanel) {
                    if (panel == null) panel = FloatingPanel(this@MonitorService)
                    panel!!.show(); panel!!.update(s)
                } else {
                    panel?.hide()
                }
            }
            RecommendationEngine.evaluate(s, device, profile)
                .filter { it.severity != Severity.INFO }
                .forEach { PerfNotifier.alert(this, it.title, it.detail) }
            delay(profile.pollMs)
        }
    }

    override fun onDestroy() {
        scope.cancel()
        panel?.hide(); panel = null
        SnapshotBus.running.value = false
        super.onDestroy()
    }

    companion object {
        fun start(ctx: Context) = ContextCompat.startForegroundService(ctx, Intent(ctx, MonitorService::class.java))
        fun stop(ctx: Context) { ctx.stopService(Intent(ctx, MonitorService::class.java)) }
    }
}
