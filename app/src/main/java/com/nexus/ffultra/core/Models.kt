package com.nexus.ffultra.core

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow

enum class Tier(val label: String) { BASIC("Básico"), MID("Intermediário"), HIGH("Avançado") }
enum class Severity { INFO, WARN, CRITICAL }

data class Snapshot(
    val tempC: Float?,            // temperatura da bateria (°C), se o aparelho informar
    val thermalStatus: Int?,      // PowerManager.THERMAL_STATUS_* (API 29+)
    val ramTotalMb: Long,
    val ramAvailMb: Long,
    val lowMemory: Boolean,
    val cpuLoadPct: Int?,         // ESTIMATIVA via frequência atual/máxima; null se o kernel bloquear
    val cpuCores: Int,
    val batteryPct: Int,
    val charging: Boolean,
    val storageFreeGb: Float,
    val storageTotalGb: Float,
    val refreshHz: Float,
    val powerSave: Boolean,
) {
    val ramUsedPct: Int get() = if (ramTotalMb > 0) (100 - ramAvailMb * 100 / ramTotalMb).toInt() else 0
}

/** Estado compartilhado entre o serviço em segundo plano e a UI. */
object SnapshotBus { val running = MutableStateFlow(false) }

/** Estado persistido (necessário para restaurar configurações). */
class Prefs(ctx: Context) {
    private val sp = ctx.applicationContext.getSharedPreferences("nexus_state", Context.MODE_PRIVATE)
    var overlayEnabled: Boolean
        get() = sp.getBoolean("overlay", false)
        set(v) = sp.edit().putBoolean("overlay", v).apply()
    var boostActive: Boolean
        get() = sp.getBoolean("boost_active", false)
        set(v) = sp.edit().putBoolean("boost_active", v).apply()
    var savedInterruptionFilter: Int
        get() = sp.getInt("saved_filter", -1)
        set(v) = sp.edit().putInt("saved_filter", v).apply()
    var serviceStartedByBoost: Boolean
        get() = sp.getBoolean("svc_by_boost", false)
        set(v) = sp.edit().putBoolean("svc_by_boost", v).apply()
    var profileOverride: String?
        get() = sp.getString("profile_override", null)
        set(v) = sp.edit().putString("profile_override", v).apply()
}
