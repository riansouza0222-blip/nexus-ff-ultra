package com.nexus.ffultra.monitor

import java.io.File

/** Leitura best-effort do sysfs. Android 8+ bloqueia /proc/stat, então a carga é estimada por frequência. */
object CpuReader {
    private fun cpuDirs(): List<File> =
        File("/sys/devices/system/cpu").listFiles { f -> f.name.matches(Regex("cpu[0-9]+")) }?.sortedBy { it.name } ?: emptyList()

    private fun readLong(f: File): Long? = try { f.readText().trim().toLong() } catch (_: Exception) { null }

    fun cores(): Int = Runtime.getRuntime().availableProcessors()

    fun maxFreqMhz(): Int? =
        cpuDirs().mapNotNull { readLong(File(it, "cpufreq/cpuinfo_max_freq")) }.maxOrNull()?.let { (it / 1000).toInt() }

    fun loadPct(): Int? {
        val ratios = cpuDirs().mapNotNull {
            val cur = readLong(File(it, "cpufreq/scaling_cur_freq"))
            val max = readLong(File(it, "cpufreq/cpuinfo_max_freq"))
            if (cur != null && max != null && max > 0) cur.toFloat() / max else null
        }
        return if (ratios.isEmpty()) null else (ratios.average() * 100).toInt().coerceIn(0, 100)
    }
}
