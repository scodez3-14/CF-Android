package com.codeforces.app.ui.screens.submissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeforces.app.data.api.SubmissionDto
import com.codeforces.app.data.repository.CodeforcesRepository
import com.codeforces.app.data.repository.Resource
import com.codeforces.app.data.repository.UserPreferencesRepository
import com.codeforces.app.data.scraper.CfSubmitter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SubmissionDetailUiState(
    val meta: SubmissionDto? = null,
    val isLoading: Boolean = true,
    val source: String? = null,
    val sourceLoading: Boolean = true,
    /** UA the browser login used — the hidden WebView must present it too. */
    val loginUa: String? = null,
    /** Cloudflare is challenging — the web session needs renewal via login. */
    val needsCheck: Boolean = false,
    /** User chose not to renew — show the unavailable state. */
    val webViewGiveUp: Boolean = false
)

@HiltViewModel
class SubmissionDetailViewModel @Inject constructor(
    private val submitter: CfSubmitter,
    private val repo: CodeforcesRepository,
    private val prefs: UserPreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SubmissionDetailUiState())
    val state: StateFlow<SubmissionDetailUiState> = _state.asStateFlow()

    /** null = still checking; false = signed out. */
    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn.asStateFlow()

    private var contestId: String = ""
    private var submissionId: Long = 0L

    init {
        viewModelScope.launch {
            _state.update { it.copy(loginUa = prefs.loginUserAgent()) }
        }
    }

    fun load(contestId: String, submissionId: Long, handleArg: String) {
        this.contestId = contestId
        this.submissionId = submissionId
        viewModelScope.launch {
            // Gate behind login — source code requires an active CF session.
            _isLoggedIn.value = null
            val loggedIn = withContext(Dispatchers.IO) { submitter.isLoggedIn() }
                || withContext(Dispatchers.IO) { prefs.isSessionActive() }
            _isLoggedIn.value = loggedIn

            launch {
                try {
                    val handle = handleArg.ifBlank {
                        prefs.savedLoginHandle() ?: prefs.handle.first().orEmpty()
                    }
                    if (handle.isNotBlank()) {
                        val res = repo.getUserStatus(handle, 1, 2000)
                        val match = (res as? Resource.Success)?.data
                            ?.firstOrNull { it.id == submissionId }
                        _state.update { it.copy(meta = match) }
                    }
                } catch (_: Exception) {
                }
                _state.update { it.copy(isLoading = false) }
            }
            if (loggedIn) fetchSource()
        }
    }

    /** Re-run the source fetch (e.g. after renewing the web session). */
    fun retrySource() {
        if (_state.value.source != null || contestId.isBlank()) return
        fetchSource()
    }

    /** Source delivered by the Cloudflare-safe WebView; null = challenged. */
    fun onWebViewSource(code: String?) {
        if (!code.isNullOrBlank()) {
            _state.update {
                it.copy(source = code, sourceLoading = false, needsCheck = false)
            }
            // The WebView has a valid session — sync its cookies into OkHttp
            // so the *next* source fetch can succeed without the WebView.
            viewModelScope.launch(Dispatchers.IO) {
                submitter.syncCookiesFromSystem()
            }
        } else {
            _state.update { it.copy(needsCheck = true) }
        }
    }

    /** User declined to renew the session. */
    fun onRenewalDismissed() {
        _state.update { it.copy(webViewGiveUp = true) }
    }

    private fun fetchSource() {
        viewModelScope.launch {
            _state.update {
                it.copy(sourceLoading = true, source = null, needsCheck = false, webViewGiveUp = false)
            }
            val src = withContext(Dispatchers.IO) {
                // ① Always pull the latest cookies from the system WebView
                //    session into OkHttp's PersistentCookieJar.  This is the
                //    key fix: the WebView login (or hidden WebView) may have
                //    refreshed cf_clearance / JSESSIONID since the last time
                //    OkHttp touched them.
                submitter.syncCookiesFromSystem()

                var result: String? = null

                // ② Try with the saved WebView UA (cf_clearance is UA-bound)
                val savedUa = prefs.loginUserAgent()
                if (!savedUa.isNullOrBlank()) {
                    submitter.setUserAgent(savedUa)
                    result = submitter.fetchSubmissionSource(contestId, submissionId)
                }

                // ③ Fallback: try with the default UA
                if (result == null) {
                    submitter.resetUserAgent()
                    result = submitter.fetchSubmissionSource(contestId, submissionId)
                }

                // ④ If still null, attempt a re-login with saved credentials
                //    then retry once more.
                if (result == null) {
                    val savedHandle = prefs.savedLoginHandle()
                    val savedPassword = prefs.savedLoginPassword()
                    if (!savedHandle.isNullOrBlank() && savedPassword != null) {
                        submitter.login(savedHandle, savedPassword)
                        // Re-sync cookies after login (login sets new ones)
                        submitter.syncCookiesFromSystem()
                        result = submitter.fetchSubmissionSource(contestId, submissionId)
                    }
                }

                result
            }
            _state.update { it.copy(source = src, sourceLoading = false) }
        }
    }
}
