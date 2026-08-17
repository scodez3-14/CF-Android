package com.codeforces.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeforces.app.BuildConfig
import com.codeforces.app.data.repository.CodeforcesRepository
import com.codeforces.app.data.repository.UserPreferencesRepository
import com.codeforces.app.data.scraper.CfSubmitter
import com.codeforces.app.data.update.ReleaseInfo
import com.codeforces.app.data.update.UpdateChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository,
    private val repo: CodeforcesRepository,
    private val submitter: CfSubmitter,
    private val updateChecker: UpdateChecker
) : ViewModel() {

    val handle: Flow<String?> = prefs.handle
    val remindersEnabled: Flow<Boolean> = prefs.remindersEnabled

    // ── Manual update check (no startup auto-popup; user triggers it) ──

    private val _isCheckingUpdate = MutableStateFlow(false)
    val isCheckingUpdate: StateFlow<Boolean> = _isCheckingUpdate.asStateFlow()

    private val _updateInfo = MutableStateFlow<ReleaseInfo?>(null)
    val updateInfo: StateFlow<ReleaseInfo?> = _updateInfo.asStateFlow()

    private val _upToDate = MutableStateFlow(false)
    val upToDate: StateFlow<Boolean> = _upToDate.asStateFlow()

    fun checkForUpdates() {
        if (_isCheckingUpdate.value) return
        _isCheckingUpdate.value = true
        _upToDate.value = false
        viewModelScope.launch {
            val info = updateChecker.checkForUpdate(BuildConfig.VERSION_NAME)
            _updateInfo.value = info
            _upToDate.value = info == null
            _isCheckingUpdate.value = false
        }
    }

    fun dismissUpdate() {
        _updateInfo.value = null
    }

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
