package com.example.motoeire

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsDataStore: SettingsDataStore,
    private val repository: CarRepository
) : ViewModel() {

    // Expose settings as StateFlow
    val userSettings: StateFlow<UserSettings> = settingsDataStore.userSettingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings()
        )

    // ✅ NEW - Error state for reset
    var resetError by mutableStateOf<String?>(null)
        private set

    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            Log.d("SettingsViewModel", "Theme changed to: ${themeMode.name}")
            settingsDataStore.updateThemeMode(themeMode)
        }
    }

    fun updateViewMode(viewMode: ViewMode) {
        viewModelScope.launch {
            Log.d("SettingsViewModel", "View mode changed to: ${viewMode.name}")
            settingsDataStore.updateViewMode(viewMode)
        }
    }

    // ✅ UPDATED - Add error handling
    fun resetAllData() {
        viewModelScope.launch {
            try {
                Log.d("SettingsViewModel", "Starting data reset...")
                repository.deleteAllCars()
                settingsDataStore.resetAllSettings()
                resetError = null  // Clear any previous errors
                Log.d("SettingsViewModel", "All data reset successfully")
            } catch (e: Exception) {
                resetError = "Failed to reset data: ${e.message}"
                Log.e("SettingsViewModel", "Error resetting data", e)
            }
        }
    }

    // ✅ NEW - Clear error message
    fun clearResetError() {
        resetError = null
    }
}