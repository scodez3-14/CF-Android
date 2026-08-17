package com.codeforces.app.ui.screens.submissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeforces.app.data.api.SubmissionDto
import com.codeforces.app.data.repository.CodeforcesRepository
import com.codeforces.app.data.repository.Resource
import com.codeforces.app.data.repository.UserPreferencesRepository
import com.codeforces.app.data.scraper.CfSubmitter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SubmissionsUiState(
    val submissions: List<SubmissionDto> = emptyList(),
    val filtered: List<SubmissionDto> = emptyList(),
    val verdictFilter: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SubmissionsViewModel @Inject constructor(
    private val repo: CodeforcesRepository,
    private val submitter: CfSubmitter,
    private val prefs: UserPreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SubmissionsUiState())
    val state: StateFlow<SubmissionsUiState> = _state.asStateFlow()

    /** null = still checking; false = signed out (list stays hidden). */
    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn.asStateFlow()

    private var currentHandle: String? = null
    private var livePollJob: Job? = null

    fun load(handle: String) {
        currentHandle = handle
        refreshLogin()
    }

    /**
     * Submissions of the account are login-gated. Re-checks the session on
     * ON_RESUME (e.g. after returning from the login screen) and loads the
     * list only while signed in; signing out clears everything shown.
     */
    fun refreshLogin() {
        val handle = currentHandle ?: return
        viewModelScope.launch {
            _isLoggedIn.value = null
            val loggedIn = withContext(Dispatchers.IO) { submitter.isLoggedIn() }
                || withContext(Dispatchers.IO) { prefs.isSessionActive() }
            _isLoggedIn.value = loggedIn
            if (loggedIn) {
                if (_state.value.submissions.isEmpty()) fetchSubmissions(handle)
            } else {
                livePollJob?.cancel()
                _state.update {
                    it.copy(
                        submissions = emptyList(),
                        filtered = emptyList(),
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }

    private fun fetchSubmissions(handle: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = repo.getUserStatus(handle, 1, 200)
            when (result) {
                is Resource.Success -> _state.update { it.copy(submissions = result.data, isLoading = false) }
                is Resource.Error -> _state.update { it.copy(error = result.message, isLoading = false) }
                else -> {}
            }
            applyFilter()
            startLivePolling(handle)
        }
    }

    fun setFilter(verdict: String?) {
        _state.update { it.copy(verdictFilter = verdict) }
        applyFilter()
    }

    /** Silent refresh loop: keeps the list live while any listed submission
     *  is queued or being judged, then stops once everything is final. */
    private fun startLivePolling(handle: String) {
        livePollJob?.cancel()
        livePollJob = viewModelScope.launch {
            while (isActive) {
                val anyRunning = _state.value.submissions.any { sub ->
                    sub.verdict == null || sub.verdict == "TESTING" || sub.verdict == "IN_QUEUE"
                }
                if (!anyRunning) break
                delay(4000)
                val result = repo.getUserStatus(handle, 1, 200)
                if (result is Resource.Success) {
                    _state.update { it.copy(submissions = result.data) }
                    applyFilter()
                }
            }
        }
    }

    private fun applyFilter() {
        val s = _state.value
        _state.update {
            it.copy(filtered = if (s.verdictFilter == null) s.submissions
            else s.submissions.filter { sub -> sub.verdict == s.verdictFilter })
        }
    }
}
