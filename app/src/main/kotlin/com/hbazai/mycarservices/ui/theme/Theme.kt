package com.hbazai.mycarservices.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.hbazai.mycarservices.util.ThemeMode
import com.hbazai.mycarservices.util.ThemePreference

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

/** Resolve the effective dark/light flag from the selected [ThemeMode]. */
@Composable
fun shouldUseDarkTheme(mode: ThemeMode = ThemePreference.mode): Boolean = when (mode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT  -> false
    ThemeMode.DARK   -> true
}

@Composable
fun MyCarServicesTheme(
    darkTheme: Boolean = shouldUseDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Keep the status bar in sync with the theme. The bar stays yellow (brand),
    // so icons are always dark; nav bar follows the background.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = PrimaryYellow.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val insets = WindowCompat.getInsetsController(window, view)
            // Yellow bar is light -> dark status icons.
            insets.isAppearanceLightStatusBars = true
            insets.isAppearanceLightNavigationBars =
                colorScheme.background.luminance() > 0.5f
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
