package com.mis.parentapp.shared

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel

enum class ThemeMode { LIGHT, DARK, SYSTEM }

class AppSettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    var themeMode by mutableStateOf(
        ThemeMode.valueOf(prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
    )
        private set

    var pushNotificationsEnabled by mutableStateOf(prefs.getBoolean("push_notifications", true))
        private set

    var privacyEnabled by mutableStateOf(prefs.getBoolean("privacy_enabled", false))
        private set

    fun setTheme(mode: ThemeMode) {
        themeMode = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun setPushNotifications(enabled: Boolean) {
        pushNotificationsEnabled = enabled
        prefs.edit().putBoolean("push_notifications", enabled).apply()
    }

    fun setPrivacy(enabled: Boolean) {
        privacyEnabled = enabled
        prefs.edit().putBoolean("privacy_enabled", enabled).apply()
    }
}
