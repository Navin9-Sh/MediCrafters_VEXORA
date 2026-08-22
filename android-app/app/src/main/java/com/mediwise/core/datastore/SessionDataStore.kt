package com.mediwise.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "clinical_session")

@Singleton
class SessionDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_ROLE = stringPreferencesKey("user_role")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val PUSH_NOTIF = androidx.datastore.preferences.core.booleanPreferencesKey("push_notif")
        val APPT_REMINDER = androidx.datastore.preferences.core.booleanPreferencesKey("appt_reminder")
        val CHAT_NOTIF = androidx.datastore.preferences.core.booleanPreferencesKey("chat_notif")
        val MARKETING_EMAIL = androidx.datastore.preferences.core.booleanPreferencesKey("marketing_email")
        val BIOMETRIC = androidx.datastore.preferences.core.booleanPreferencesKey("biometric")
        val DARK_THEME = androidx.datastore.preferences.core.booleanPreferencesKey("dark_theme")
    }

    val accessToken: Flow<String?> = context.dataStore.data.map { it[Keys.ACCESS_TOKEN] }
    val refreshToken: Flow<String?> = context.dataStore.data.map { it[Keys.REFRESH_TOKEN] }
    val userId: Flow<String?> = context.dataStore.data.map { it[Keys.USER_ID] }
    val userEmail: Flow<String?> = context.dataStore.data.map { it[Keys.USER_EMAIL] }
    val userRole: Flow<String?> = context.dataStore.data.map { it[Keys.USER_ROLE] }
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { it[Keys.ACCESS_TOKEN] != null }

    val pushNotifications: Flow<Boolean> = context.dataStore.data.map { it[Keys.PUSH_NOTIF] ?: true }
    val apptReminders: Flow<Boolean> = context.dataStore.data.map { it[Keys.APPT_REMINDER] ?: true }
    val chatNotifications: Flow<Boolean> = context.dataStore.data.map { it[Keys.CHAT_NOTIF] ?: true }
    val marketingEmails: Flow<Boolean> = context.dataStore.data.map { it[Keys.MARKETING_EMAIL] ?: false }
    val biometricLogin: Flow<Boolean> = context.dataStore.data.map { it[Keys.BIOMETRIC] ?: false }
    val darkMode: Flow<Boolean> = context.dataStore.data.map { it[Keys.DARK_THEME] ?: false }

    suspend fun saveSetting(key: String, value: Boolean) {
        context.dataStore.edit { prefs ->
            when (key) {
                "push" -> prefs[Keys.PUSH_NOTIF] = value
                "reminders" -> prefs[Keys.APPT_REMINDER] = value
                "chat" -> prefs[Keys.CHAT_NOTIF] = value
                "marketing" -> prefs[Keys.MARKETING_EMAIL] = value
                "biometric" -> prefs[Keys.BIOMETRIC] = value
                "dark" -> prefs[Keys.DARK_THEME] = value
            }
        }
    }

    suspend fun saveSession(accessToken: String, refreshToken: String, userId: String, role: String, email: String) {
        context.dataStore.edit {
            it[Keys.ACCESS_TOKEN] = accessToken
            it[Keys.REFRESH_TOKEN] = refreshToken
            it[Keys.USER_ID] = userId
            it[Keys.USER_ROLE] = role
            it[Keys.USER_EMAIL] = email
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
