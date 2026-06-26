package com.hbazai.mycarservices.util

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Theme options exposed in Settings. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * App-wide theme selection, persisted in SharedPreferences and exposed as a
 * Compose state so any change recomposes the whole UI instantly.
 */
object ThemePreference {
    private const val PREFS_NAME = "car_service_prefs"
    private const val KEY_THEME  = "theme_mode"

    private var loaded = false
    private var _mode by mutableStateOf(ThemeMode.SYSTEM)

    /** Current mode, observed by Compose. */
    val mode: ThemeMode get() = _mode

    fun load(context: Context) {
        if (loaded) return
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_THEME, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        _mode = runCatching { ThemeMode.valueOf(raw) }.getOrDefault(ThemeMode.SYSTEM)
        loaded = true
    }

    fun setMode(context: Context, mode: ThemeMode) {
        _mode = mode
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_THEME, mode.name).apply()
    }
}
