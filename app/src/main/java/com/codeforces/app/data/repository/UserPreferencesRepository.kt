package com.codeforces.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.codeforces.app.data.auth.SecureCipher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val HANDLE_KEY = stringPreferencesKey("cf_handle")
        val API_KEY_KEY = stringPreferencesKey("cf_api_key")
        val API_SECRET_KEY = stringPreferencesKey("cf_api_secret")
        val REMINDERS_KEY = booleanPreferencesKey("contest_reminders")
        val BOOKMARKS_KEY = stringSetPreferencesKey("bookmarked_problem_ids")
        val LOGIN_HANDLE_KEY = stringPreferencesKey("cf_login_handle")
        val LOGIN_PASSWORD_ENC_KEY = stringPreferencesKey("cf_login_password_enc")
        val SESSION_ACTIVE_KEY = booleanPreferencesKey("cf_session_active")
        val LOGIN_USER_AGENT_KEY = stringPreferencesKey("cf_login_user_agent")
    }

    val handle: Flow<String?> = context.dataStore.data.map { it[HANDLE_KEY] }
    val apiKey: Flow<String?> = context.dataStore.data.map { it[API_KEY_KEY] }

    val sessionActive: Flow<Boolean> = context.dataStore.data.map { it[SESSION_ACTIVE_KEY] ?: false }
    val remindersEnabled: Flow<Boolean> = context.dataStore.data.map { it[REMINDERS_KEY] ?: false }
    val bookmarks: Flow<Set<String>> = context.dataStore.data.map { it[BOOKMARKS_KEY] ?: emptySet() }

    suspend fun saveHandle(handle: String) {
        context.dataStore.edit { it[HANDLE_KEY] = handle }
    }

    suspend fun setSessionActive(active: Boolean) {
        context.dataStore.edit { it[SESSION_ACTIVE_KEY] = active }
    }

    suspend fun isSessionActive(): Boolean =
        context.dataStore.data.map { it[SESSION_ACTIVE_KEY] ?: false }.first()

    /** UA the login WebView used; cf_clearance is bound to it, so any
     *  follow-up browser work (submit page) must present the same UA. */
    suspend fun saveLoginUserAgent(ua: String) {
        context.dataStore.edit { it[LOGIN_USER_AGENT_KEY] = ua }
    }

    suspend fun loginUserAgent(): String? =
        context.dataStore.data.map { it[LOGIN_USER_AGENT_KEY] }.first()

    suspend fun saveApiCredentials(apiKey: String, apiSecret: String) {
        context.dataStore.edit {
            it[API_KEY_KEY] = apiKey
            it[API_SECRET_KEY] = apiSecret
        }
    }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { it[REMINDERS_KEY] = enabled }
    }

    suspend fun toggleBookmark(id: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[BOOKMARKS_KEY] ?: emptySet()
            val updated = current.toMutableSet()
            if (!updated.add(id)) updated.remove(id)
            prefs[BOOKMARKS_KEY] = updated
        }
    }

    // ── Codeforces login credentials (password stored encrypted) ────────────

    suspend fun saveLoginCredentials(handle: String, password: String) {
        val encrypted = SecureCipher.encrypt(password)
        context.dataStore.edit {
            it[LOGIN_HANDLE_KEY] = handle
            if (encrypted != null) it[LOGIN_PASSWORD_ENC_KEY] = encrypted
            else it.remove(LOGIN_PASSWORD_ENC_KEY)
        }
    }

    suspend fun clearLoginCredentials() {
        context.dataStore.edit {
            it.remove(LOGIN_HANDLE_KEY)
            it.remove(LOGIN_PASSWORD_ENC_KEY)
        }
    }

    suspend fun savedLoginHandle(): String? =
        context.dataStore.data.map { it[LOGIN_HANDLE_KEY] }.first()

    suspend fun savedLoginPassword(): String? {
        val encrypted = context.dataStore.data.map { it[LOGIN_PASSWORD_ENC_KEY] }.first()
            ?: return null
        return SecureCipher.decrypt(encrypted)
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
