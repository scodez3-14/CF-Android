package com.codeforces.app.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeforces.app.data.repository.UserPreferencesRepository
import com.codeforces.app.data.scraper.CfSubmitter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class LoginUiState(
    val isLoggedIn: Boolean? = null,
    val loggedInHandle: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val submitter: CfSubmitter,
    private val prefs: UserPreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    /** Check the persisted web session and restore the last used handle. */
    fun refresh() {
        viewModelScope.launch {
            val loggedIn = withContext(Dispatchers.IO) { submitter.isLoggedIn() }
            if (loggedIn) {
                prefs.setSessionActive(true)
                val stored = prefs.savedLoginHandle()
                val handle = stored ?: prefs.handle.first()
                _state.value = _state.value.copy(
                    isLoggedIn = true,
                    loggedInHandle = handle
                )
            } else {
                // OkHttp can be blocked by Cloudflare even with a valid session
                // (TLS fingerprint differs from the WebView). If the browser
                // login confirmed a session earlier, keep trusting it.
                val sessionActive = withContext(Dispatchers.IO) { prefs.isSessionActive() }
                if (sessionActive) {
                    val handle = prefs.savedLoginHandle() ?: prefs.handle.first()
                    _state.value = _state.value.copy(
                        isLoggedIn = true,
                        loggedInHandle = handle
                    )
                } else {
                    _state.value = _state.value.copy(isLoggedIn = false, loggedInHandle = null)
                }
            }
        }
    }

    fun login(handle: String, password: String, remember: Boolean) {
        val trimmed = handle.trim()
        if (trimmed.isBlank() || password.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "Enter your handle and password")
            return
        }
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            val result = withContext(Dispatchers.IO) { submitter.login(trimmed, password) }
            if (result.success) {
                if (remember) prefs.saveLoginCredentials(trimmed, password)
                else prefs.clearLoginCredentials()
                // Pin the UA this session was minted under, so later restarts
                // restore the exact one cf_clearance is bound to.
                prefs.saveLoginUserAgent(submitter.userAgent)
                prefs.saveHandle(trimmed)
                prefs.setSessionActive(true)
                _state.value = _state.value.copy(
                    isLoggedIn = true,
                    loggedInHandle = trimmed,
                    isLoading = false,
                    successMessage = "Signed in as $trimmed"
                )
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { submitter.logout() }
            prefs.clearLoginCredentials()
            prefs.setSessionActive(false)
            _state.value = _state.value.copy(
                isLoggedIn = false,
                loggedInHandle = null,
                successMessage = null,
                errorMessage = null
            )
        }
    }
}
