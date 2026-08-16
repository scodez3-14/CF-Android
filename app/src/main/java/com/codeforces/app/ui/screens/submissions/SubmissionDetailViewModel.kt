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
    /** Cloudflare is challenging the WebView — offer the manual check. */
    val needsCheck: Boolean = false,
    /** User dismissed the manual check without solving it. */
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

    init {
        viewModelScope.launch {
            _state.update { it.copy(loginUa = prefs.loginUserAgent()) }
        }
    }

    /** Source delivered by the Cloudflare-safe WebView; null = challenged. */
    fun onWebViewSource(code: String?) {
        if (!code.isNullOrBlank()) {
            _state.update {
                it.copy(source = code, sourceLoading = false, needsCheck = false)
            }
        } else {
            _state.update { it.copy(needsCheck = true) }
        }
    }

    /** User closed the full-screen security check without completing it. */
    fun onCheckDismissed() {
        _state.update { it.copy(webViewGiveUp = true) }
    }

    fun load(contestId: String, submissionId: Long, handleArg: String) {
        viewModelScope.launch {
            // Metadata from the public API (falls back to the saved handle).
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
            // Source code needs the authenticated web session. cf_clearance is
            // bound to the User-Agent the session was minted under, and that
            // can be either the saved browser-login UA or the device default
            // (password login) — try both, re-authenticating if needed.
            launch {
                val src = withContext(Dispatchers.IO) {
                    var result: String? = null

                    val savedUa = prefs.loginUserAgent()
                    if (!savedUa.isNullOrBlank()) {
                        submitter.setUserAgent(savedUa)
                        result = submitter.fetchSubmissionSource(contestId, submissionId)
                    }

                    if (result == null) {
                        submitter.resetUserAgent()
                        if (!submitter.isLoggedIn()) {
                            val savedHandle = prefs.savedLoginHandle()
                            val savedPassword = prefs.savedLoginPassword()
                            if (!savedHandle.isNullOrBlank() && savedPassword != null) {
                                submitter.login(savedHandle, savedPassword)
                            }
                        }
                        result = submitter.fetchSubmissionSource(contestId, submissionId)
                    }

                    result
                }
                _state.update { it.copy(source = src, sourceLoading = false) }
            }
        }
    }
}
