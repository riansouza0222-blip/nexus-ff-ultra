package com.nexus.ffultra.notifications

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.nexus.ffultra.R
import com.nexus.ffultra.ui.MainActivity

object PerfNotifier {
    private const val CH_MON = "monitor"
    private const val CH_ALERT = "alerts"
    private val last = mutableMapOf<String, Long>()

    fun ensureChannels(ctx: Context) {
        val nm = ctx.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CH_MON, "Monitoramento", NotificationManager.IMPORTANCE_LOW))
        nm.createNotificationChannel(NotificationChannel(CH_ALERT, "Alertas de desempenho", NotificationManager.IMPORTANCE_DEFAULT))
    }

    private fun open(ctx: Context) = PendingIntent.getActivity(
        ctx, 0, Intent(ctx, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

    fun foreground(ctx: Context): Notification =
        NotificationCompat.Builder(ctx, CH_MON).setSmallIcon(R.drawable.ic_stat)
            .setContentTitle("NEXUS FF ULTRA").setContentText("Monitorando o desempenho do aparelho")
            .setOngoing(true).setContentIntent(open(ctx)).build()

    /** Alerta com intervalo mínimo de 3 min por tipo, para não incomodar durante o jogo. */
    @SuppressLint("MissingPermission")
    fun alert(ctx: Context, title: String, text: String) {
        val now = System.currentTimeMillis()
        if (now - (last[title] ?: 0L) < 180_000) return
        last[title] = now
        val n = NotificationCompat.Builder(ctx, CH_ALERT).setSmallIcon(R.drawable.ic_stat)
            .setContentTitle(title).setContentText(text).setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true).setContentIntent(open(ctx)).build()
        runCatching { NotificationManagerCompat.from(ctx).notify(title.hashCode(), n) }
    }
}
