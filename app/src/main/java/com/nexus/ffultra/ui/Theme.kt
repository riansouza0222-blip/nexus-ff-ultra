package com.nexus.ffultra.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val NxBlack = Color(0xFF000000)
val NxSurface = Color(0xFF111111)
val NxRed = Color(0xFFE50914)
val NxRedDark = Color(0xFF7A0409)
val NxText = Color(0xFFF2F2F2)
val NxMuted = Color(0xFF9A9A9A)
val NxWarn = Color(0xFFFFB020)

@Composable
fun NexusTheme(content: @Composable () -> Unit) = MaterialTheme(
    colorScheme = darkColorScheme(primary = NxRed, onPrimary = Color.White, background = NxBlack,
        surface = NxSurface, onSurface = NxText, onBackground = NxText),
    content = content,
)
