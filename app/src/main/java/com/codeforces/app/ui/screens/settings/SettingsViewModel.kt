package com.codeforces.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeforces.app.data.repository.CodeforcesRepository
import com.codeforces.app.data.repository.UserPreferencesRepository
import com.codeforces.app.data.scraper.CfSubmitter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository,
    private val repo: CodeforcesRepository,
    private val submitter: CfSubmitter
) : ViewModel() {

    val handle: Flow<String?> = prefs.handle
    val remindersEnabled: Flow<Boolean> = prefs.remindersEnabled

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn

    private val _loggedInHandle = MutableStateFlow<String?>(null)
    val loggedInHandle: StateFlow<String?> = _loggedInHandle

    init {
        refreshLogin()
    }

    fun refreshLogin() {
        viewModelScope.launch {
            val loggedIn = withContext(Dispatchers.IO) { submitter.isLoggedIn() }
                || withContext(Dispatchers.IO) { prefs.isSessionActive() }
            _isLoggedIn.value = loggedIn
            if (loggedIn) {
                val handle = prefs.savedLoginHandle() ?: prefs.handle.first()
                _loggedInHandle.value = handle
            } else {
                _loggedInHandle.value = null
            }
        }
    }

    fun saveHandle(newHandle: String) {
        viewModelScope.launch { prefs.saveHandle(newHandle) }
    }

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.setRemindersEnabled(enabled) }
    }

    fun logoutCf() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { submitter.logout() }
            prefs.clearLoginCredentials()
            prefs.setSessionActive(false)
            _isLoggedIn.value = false
            _loggedInHandle.value = null
        }
    }

    fun logout() {
        viewModelScope.launch {
            prefs.clearAll()
            repo.clearAllCache()
        }
    }
}
