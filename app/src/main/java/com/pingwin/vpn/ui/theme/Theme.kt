package com.pingwin.vpn.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF10285A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE7FF),
    onPrimaryContainer = Color(0xFF0B2049),

    secondary = Color(0xFF667085),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE9EDF6),
    onSecondaryContainer = Color(0xFF344054),

    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF17191F),

    surface = Color(0xFFFAFAFA),
    onSurface = Color(0xFF17191F),

    surfaceVariant = Color(0xFFF0F2F5),
    onSurfaceVariant = Color(0xFF667085)
)

@Composable
fun PingwinTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
