package com.example.casioplayer.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("casio_player_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_THEME = "selected_theme"
        private const val KEY_HAPTICS = "enable_haptics"
    }

    fun getTheme(): CasioThemeMode {
        val name = prefs.getString(KEY_THEME, CasioThemeMode.GREEN.name)
        return try {
            CasioThemeMode.valueOf(name ?: CasioThemeMode.GREEN.name)
        } catch (_: Exception) {
            CasioThemeMode.GREEN
        }
    }

    fun setTheme(theme: CasioThemeMode) {
        prefs.edit().putString(KEY_THEME, theme.name).apply()
    }

    fun isHapticEnabled(): Boolean {
        return prefs.getBoolean(KEY_HAPTICS, true)
    }

    fun setHapticEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTICS, enabled).apply()
    }
}