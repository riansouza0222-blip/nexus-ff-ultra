package com.nexus.ffultra.gamemode

import android.content.Context
import android.content.Intent

object GameLauncher {
    val packages = mapOf("com.dts.freefireth" to "Free Fire", "com.dts.freefiremax" to "Free Fire MAX")

    fun installed(ctx: Context): List<Pair<String, String>> =
        packages.filter { ctx.packageManager.getLaunchIntentForPackage(it.key) != null }.toList()

    /** Apenas abre o jogo pelo ícone oficial. Nada no jogo é lido ou modificado. */
    fun launch(ctx: Context, pkg: String): Boolean {
        val i = ctx.packageManager.getLaunchIntentForPackage(pkg) ?: return false
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(i)
        return true
    }
}
