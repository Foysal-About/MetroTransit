package com.example.metrotransit.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

class SettingsViewModel : ViewModel() {
    var themeMode by mutableStateOf(ThemeMode.SYSTEM)
        private set

    fun setTheme(mode: ThemeMode) {
        themeMode = mode
    }
}
