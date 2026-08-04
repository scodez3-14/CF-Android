package com.codeforces.app.data.repository

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

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val HANDLE_KEY = stringPreferencesKey("cf_handle")
        val API_KEY_KEY = stringPreferencesKey("cf_api_key")
        val API_SECRET_KEY = stringPreferencesKey("cf_api_secret")
    }

    val handle: Flow<String?> = context.dataStore.data.map { it[HANDLE_KEY] }
    val apiKey: Flow<String?> = context.dataStore.data.map { it[API_KEY_KEY] }

    suspend fun saveHandle(handle: String) {
        context.dataStore.edit { it[HANDLE_KEY] = handle }
    }

    suspend fun saveApiCredentials(apiKey: String, apiSecret: String) {
        context.dataStore.edit {
            it[API_KEY_KEY] = apiKey
            it[API_SECRET_KEY] = apiSecret
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
