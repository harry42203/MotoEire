package com.example.motoeire

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Create DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_settings"
)

class SettingsDataStore(private val context: Context) {

    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val VIEW_MODE_KEY = stringPreferencesKey("view_mode")
    }

    // Read settings as Flow
    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data.map { preferences ->
        val themeMode = preferences[THEME_MODE_KEY]?.let { ThemeMode.valueOf(it) } ?: ThemeMode.SYSTEM
        val viewMode = preferences[VIEW_MODE_KEY]?.let { ViewMode.valueOf(it) } ?: ViewMode.GRID
        UserSettings(themeMode = themeMode, viewMode = viewMode)
    }

    // Update theme mode
    suspend fun updateThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = themeMode.name
        }
    }

    // Update view mode
    suspend fun updateViewMode(viewMode: ViewMode) {
        context.dataStore.edit { preferences ->
            preferences[VIEW_MODE_KEY] = viewMode.name
        }
    }

    // Reset all settings
    suspend fun resetAllSettings() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}