package com.nexus.ffultra.diagnostics

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import com.nexus.ffultra.core.Tier
import com.nexus.ffultra.monitor.CpuReader

data class DeviceInfo(
    val model: String, val androidVersion: String, val sdk: Int,
    val ramTotalMb: Long, val cores: Int, val maxFreqMhz: Int?, val abi: String,
    val tier: Tier, val reasons: List<String>,
)

object DeviceDiagnostics {
    fun run(ctx: Context): DeviceInfo {
        val mi = ActivityManager.MemoryInfo().also { ctx.getSystemService(ActivityManager::class.java).getMemoryInfo(it) }
        val ram = mi.totalMem / (1024 * 1024)
        val cores = CpuReader.cores()
        val freq = CpuReader.maxFreqMhz()
        val reasons = listOf("RAM: $ram MB", "Núcleos: $cores", "Freq. máx.: ${freq?.let { "$it MHz" } ?: "indisponível"}")
        val tier = when {
            ram < 3000 || cores < 6 -> Tier.BASIC
            ram >= 6500 && (freq ?: 0) >= 2300 -> Tier.HIGH
            else -> Tier.MID
        }
        return DeviceInfo(
            model = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
            androidVersion = "Android ${Build.VERSION.RELEASE}", sdk = Build.VERSION.SDK_INT,
            ramTotalMb = ram, cores = cores, maxFreqMhz = freq,
            abi = Build.SUPPORTED_ABIS.firstOrNull() ?: "?", tier = tier, reasons = reasons,
        )
    }
}
