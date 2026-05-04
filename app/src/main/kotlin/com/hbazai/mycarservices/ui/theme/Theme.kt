package com.hbazai.mycarservices.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary          = PrimaryYellow,
    onPrimary        = OnPrimary,
    primaryContainer = PrimaryYellowDark,
    onPrimaryContainer = OnPrimary,
    secondary        = PrimaryYellowDark,
    onSecondary      = OnPrimary,
    background       = BackgroundDark,
    onBackground     = TextPrimary,
    surface          = BackgroundCard,
    onSurface        = TextPrimary,
    surfaceVariant   = BackgroundSurface,
    onSurfaceVariant = TextSecondary,
    error            = StatusOverdue,
    onError          = Color.White,
    outline          = TextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary          = PrimaryYellow,
    onPrimary        = OnPrimary,
    primaryContainer = PrimaryYellowDark,
    onPrimaryContainer = OnPrimary,
    secondary        = PrimaryYellowDark,
    onSecondary      = OnPrimary,
    background       = LightBackground,
    onBackground     = LightOnBackground,
    surface          = LightSurface,
    onSurface        = LightOnBackground,
    surfaceVariant   = Color(0xFFEEEEEE),
    onSurfaceVariant = Color(0xFF555555),
    error            = StatusOverdue,
    onError          = Color.White,
    outline          = Color(0xFF999999)
)

@Composable
fun MyCarServicesTheme(
    darkTheme: Boolean = true,   // default dark — yellow on black
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}