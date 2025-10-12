package com.example.agrogesf.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "agro_gesf_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        val LOGGED_IN_USER_ID = longPreferencesKey("logged_in_user_id")
        val LOGGED_IN_USER_EMAIL = stringPreferencesKey("logged_in_user_email")
        val LOGGED_IN_USER_NAME = stringPreferencesKey("logged_in_user_name")
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
    }

    suspend fun saveUserSession(userId: Long, email: String, name: String) {
        context.dataStore.edit { prefs ->
            prefs[LOGGED_IN_USER_ID] = userId
            prefs[LOGGED_IN_USER_EMAIL] = email
            prefs[LOGGED_IN_USER_NAME] = name
        }
    }

    suspend fun clearUserSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(LOGGED_IN_USER_ID)
            prefs.remove(LOGGED_IN_USER_EMAIL)
            prefs.remove(LOGGED_IN_USER_NAME)
        }
    }

    val userSession: Flow<UserSession?> = context.dataStore.data.map { prefs ->
        val userId = prefs[LOGGED_IN_USER_ID]
        val email = prefs[LOGGED_IN_USER_EMAIL]
        val name = prefs[LOGGED_IN_USER_NAME]

        if (userId != null && email != null && name != null) {
            UserSession(userId, email, name)
        } else {
            null
        }
    }

    suspend fun updateLastSyncTime() {
        context.dataStore.edit { prefs ->
            prefs[LAST_SYNC_TIME] = System.currentTimeMillis()
        }
    }

    val lastSyncTime: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[LAST_SYNC_TIME] ?: 0L
    }
}

data class UserSession(
    val userId: Long,
    val email: String,
    val name: String
)