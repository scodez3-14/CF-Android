package com.codeforces.app.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.codeforces.app.data.repository.UserPreferencesRepository
import com.codeforces.app.data.scraper.CfSubmitter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class WebLoginUiState(
    val isChecking: Boolean = false,
    val isLoggedIn: Boolean = false,
    val handle: String? = null
)

/**
 * Bridges a login performed inside a WebView (which can pass Codeforces'
 * Cloudflare challenge) into the app's persistent OkHttp session by importing
 * the browser cookies into [CfSubmitter]'s cookie jar.
 */
@HiltViewModel
class WebLoginViewModel @Inject constructor(
    private val submitter: CfSubmitter,
    private val prefs: UserPreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WebLoginUiState())
    val state: StateFlow<WebLoginUiState> = _state.asStateFlow()

    /**
     * Called when the WebView itself confirms a valid session (it can see the
     * logged-in user on the page). This is authoritative: unlike [checkSession]
     * it doesn't need OkHttp to pass Cloudflare's TLS checks.
     */
    fun confirmBrowserLogin(cookieHeader: String?, handle: String?, userAgent: String?) {
        Log.d("CFLOGIN", "confirmBrowserLogin handle=$handle cookies=${cookieHeader?.length} ua=$userAgent")
        if (_state.value.isLoggedIn) return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                userAgent?.let {
                    submitter.setUserAgent(it)
                    prefs.saveLoginUserAgent(it)
                }
                if (!cookieHeader.isNullOrBlank()) {
                    submitter.importCookies(cookieHeader)
                }
            }
            val h = handle ?: withContext(Dispatchers.IO) { submitter.currentHandle() }
            if (h != null) prefs.saveHandle(h)
            prefs.setSessionActive(true)
            _state.value = _state.value.copy(isChecking = false, isLoggedIn = true, handle = h)
        }
    }

    /**
     * Import the current WebView cookies and verify the session is valid.
     * [userAgent] must match the UA the WebView used, since `cf_clearance`
     * is bound to it.
     */
    fun checkSession(cookieHeader: String?, userAgent: String?) {
        Log.d("CFLOGIN", "checkSession cookies=${cookieHeader?.length}")
        if (_state.value.isChecking || _state.value.isLoggedIn) return
        _state.value = _state.value.copy(isChecking = true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                userAgent?.let {
                    submitter.setUserAgent(it)
                    prefs.saveLoginUserAgent(it)
                }
                if (!cookieHeader.isNullOrBlank()) {
                    submitter.importCookies(cookieHeader)
                }
            }
            val ok = withContext(Dispatchers.IO) { submitter.isLoggedIn() }
            Log.d("CFLOGIN", "checkSession okHttpIsLoggedIn=$ok")
            if (ok) {
                val handle = withContext(Dispatchers.IO) { submitter.currentHandle() }
                if (handle != null) prefs.saveHandle(handle)
                prefs.setSessionActive(true)
                _state.value = _state.value.copy(isChecking = false, isLoggedIn = true, handle = handle)
            } else {
                _state.value = _state.value.copy(isChecking = false)
            }
        }
    }
}
