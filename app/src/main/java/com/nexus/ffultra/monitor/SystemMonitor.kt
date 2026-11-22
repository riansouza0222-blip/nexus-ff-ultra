package com.nexus.ffultra.monitor

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.display.DisplayManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import android.view.Display
import com.nexus.ffultra.core.Snapshot

class SystemMonitor(private val ctx: Context) {
    fun read(): Snapshot {
        val am = ctx.getSystemService(ActivityManager::class.java)
        val mi = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
        val bi = ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = bi?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = bi?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val rawTemp = bi?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val st = bi?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val charging = st == BatteryManager.BATTERY_STATUS_CHARGING || st == BatteryManager.BATTERY_STATUS_FULL
        val pm = ctx.getSystemService(PowerManager::class.java)
        val sf = StatFs(Environment.getDataDirectory().path)
        val gb = 1024f * 1024f * 1024f
        val hz = ctx.getSystemService(DisplayManager::class.java).getDisplay(Display.DEFAULT_DISPLAY)?.refreshRate ?: 60f
        return Snapshot(
            tempC = if (rawTemp > 0) rawTemp / 10f else null,
            thermalStatus = if (Build.VERSION.SDK_INT >= 29) pm.currentThermalStatus else null,
            ramTotalMb = mi.totalMem / (1024 * 1024),
            ramAvailMb = mi.availMem / (1024 * 1024),
            lowMemory = mi.lowMemory,
            cpuLoadPct = CpuReader.loadPct(),
            cpuCores = CpuReader.cores(),
            batteryPct = if (level >= 0) level * 100 / scale else 0,
            charging = charging,
            storageFreeGb = sf.availableBytes / gb,
            storageTotalGb = sf.totalBytes / gb,
            refreshHz = hz,
            powerSave = pm.isPowerSaveMode,
        )
    }
}
