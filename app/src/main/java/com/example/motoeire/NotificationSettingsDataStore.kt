package com.example.motoeire

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.notificationDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "notification_settings"
)

class NotificationSettingsDataStore(private val context: Context) {
    private val dataStore = context.notificationDataStore

    val notificationSettings: Flow<NotificationSettings> = dataStore.data
        .map { preferences ->
            NotificationSettings(
                nctNotificationsEnabled = preferences[PreferenceKeys.NCT_ENABLED] ?: true,
                taxNotificationsEnabled = preferences[PreferenceKeys.TAX_ENABLED] ?: true,
                insuranceNotificationsEnabled = preferences[PreferenceKeys.INSURANCE_ENABLED] ?: true,
                nctReminders = preferences[PreferenceKeys.NCT_REMINDERS]?.split(",")?.mapNotNull { it.toIntOrNull() } ?: listOf(90, 30, 7, 1),
                taxReminders = preferences[PreferenceKeys.TAX_REMINDERS]?.split(",")?.mapNotNull { it.toIntOrNull() } ?: listOf(30, 7, 1),
                insuranceReminders = preferences[PreferenceKeys.INSURANCE_REMINDERS]?.split(",")?.mapNotNull { it.toIntOrNull() } ?: listOf(30, 7, 1)
            )
        }

    suspend fun updateNctEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.NCT_ENABLED] = enabled
        }
    }

    suspend fun updateTaxEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.TAX_ENABLED] = enabled
        }
    }

    suspend fun updateInsuranceEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.INSURANCE_ENABLED] = enabled
        }
    }

    suspend fun updateNctReminders(reminders: List<Int>) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.NCT_REMINDERS] = reminders.joinToString(",")
        }
    }

    suspend fun updateTaxReminders(reminders: List<Int>) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.TAX_REMINDERS] = reminders.joinToString(",")
        }
    }

    suspend fun updateInsuranceReminders(reminders: List<Int>) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.INSURANCE_REMINDERS] = reminders.joinToString(",")
        }
    }

    suspend fun enableAll() {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.NCT_ENABLED] = true
            preferences[PreferenceKeys.TAX_ENABLED] = true
            preferences[PreferenceKeys.INSURANCE_ENABLED] = true
        }
    }

    suspend fun disableAll() {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.NCT_ENABLED] = false
            preferences[PreferenceKeys.TAX_ENABLED] = false
            preferences[PreferenceKeys.INSURANCE_ENABLED] = false
        }
    }

    object PreferenceKeys {
        val NCT_ENABLED = booleanPreferencesKey("nct_notifications_enabled")
        val TAX_ENABLED = booleanPreferencesKey("tax_notifications_enabled")
        val INSURANCE_ENABLED = booleanPreferencesKey("insurance_notifications_enabled")
        val NCT_REMINDERS = stringPreferencesKey("nct_reminders")
        val TAX_REMINDERS = stringPreferencesKey("tax_reminders")
        val INSURANCE_REMINDERS = stringPreferencesKey("insurance_reminders")
    }
}