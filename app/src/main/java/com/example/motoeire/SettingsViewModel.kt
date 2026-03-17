package com.example.motoeire

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

    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            settingsDataStore.updateThemeMode(themeMode)
        }
    }

    fun updateViewMode(viewMode: ViewMode) {
        viewModelScope.launch {
            settingsDataStore.updateViewMode(viewMode)
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            // Delete all cars from database
            // Note: We'll need to add this to the repository
            repository.deleteAllCars()
            // Reset settings
            settingsDataStore.resetAllSettings()
        }
    }
}