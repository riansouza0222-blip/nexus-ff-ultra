package com.nexus.ffultra.overlay

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.view.Choreographer
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.TextView
import com.nexus.ffultra.core.Snapshot

/**
 * Painel flutuante opcional (exige permissão "Exibir sobre outros apps", concedida pelo usuário).
 * A taxa de quadros mostrada é a do DISPLAY medida por este app; o FPS interno do Free Fire
 * não é acessível a apps comuns sem root/ferramentas de depuração.
 */
class FloatingPanel(private val ctx: Context) {
    private val wm = ctx.getSystemService(WindowManager::class.java)
    private var view: TextView? = null
    private val choreo = Choreographer.getInstance()
    private var frames = 0
    private var lastNs = 0L
    private var displayFps = 0
    private val cb = object : Choreographer.FrameCallback {
        override fun doFrame(t: Long) {
            if (lastNs == 0L) lastNs = t
            frames++
            if (t - lastNs >= 1_000_000_000L) { displayFps = frames; frames = 0; lastNs = t }
            if (view != null) choreo.postFrameCallback(this)
        }
    }

    fun show() {
        if (view != null) return
        val d = ctx.resources.displayMetrics.density
        val tv = TextView(ctx).apply {
            setTextColor(Color.WHITE); textSize = 11f; typeface = android.graphics.Typeface.MONOSPACE
            setPadding((8 * d).toInt(), (6 * d).toInt(), (8 * d).toInt(), (6 * d).toInt())
            background = GradientDrawable().apply {
                setColor(0xCC000000.toInt()); setStroke((1.5f * d).toInt(), 0xFFE50914.toInt()); cornerRadius = 10 * d
            }
            text = "NEXUS FF..."
        }
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT,
        ).apply { gravity = Gravity.TOP or Gravity.START; x = (12 * d).toInt(); y = (100 * d).toInt() }
        var sx = 0; var sy = 0; var tx = 0f; var ty = 0f
        tv.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> { sx = lp.x; sy = lp.y; tx = e.rawX; ty = e.rawY }
                MotionEvent.ACTION_MOVE -> { lp.x = sx + (e.rawX - tx).toInt(); lp.y = sy + (e.rawY - ty).toInt(); wm.updateViewLayout(tv, lp) }
            }
            true
        }
        wm.addView(tv, lp)
        view = tv
        lastNs = 0L; frames = 0
        choreo.postFrameCallback(cb)
    }

    fun update(s: Snapshot) {
        view?.text = buildString {
            append("NEXUS FF ULTRA\n")
            append("Temp ${s.tempC?.let { "%.1f°C".format(it) } ?: "--"}\n")
            append("RAM ${s.ramUsedPct}%  CPU ${s.cpuLoadPct?.let { "~$it%" } ?: "--"}\n")
            append("Bat ${s.batteryPct}%${if (s.charging) " +" else ""}\n")
            append("Display $displayFps fps (${s.refreshHz.toInt()} Hz)")
        }
    }

    fun hide() {
        view?.let { runCatching { wm.removeView(it) } }
        view = null
    }
}
