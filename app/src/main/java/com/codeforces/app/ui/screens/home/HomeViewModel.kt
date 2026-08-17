package com.codeforces.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeforces.app.data.api.ContestDto
import com.codeforces.app.data.api.ProblemDto
import com.codeforces.app.data.api.RecentActionDto
import com.codeforces.app.data.api.SubmissionDto
import com.codeforces.app.data.api.UserDto
import com.codeforces.app.data.repository.CodeforcesRepository
import com.codeforces.app.data.repository.Resource
import com.codeforces.app.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val user: UserDto? = null,
    val upcomingContests: List<ContestDto> = emptyList(),
    val recentSubmissions: List<SubmissionDto> = emptyList(),
    val recentActions: List<RecentActionDto> = emptyList(),
    val dailyProblem: ProblemDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: CodeforcesRepository,
    private val prefs: UserPreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    val handle: Flow<String?> = prefs.handle

    init {
        viewModelScope.launch {
            prefs.handle.collect { h ->
                if (!h.isNullOrBlank()) loadAll(h)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            prefs.handle.first()?.let { if (it.isNotBlank()) loadAll(it) }
        }
    }

    /** Flip the global theme state instantly (whole app restyles live) + persist. */
    fun toggleTheme() {
        val dark = !com.codeforces.app.ui.theme.CfThemeState.isDark
        com.codeforces.app.ui.theme.CfThemeState.isDark = dark
        viewModelScope.launch { prefs.setDarkTheme(dark) }
    }

    private fun loadAll(handle: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            supervisorScope {
                launch {
                    repo.getUserInfo(handle).collect { resource ->
                        when (resource) {
                            is Resource.Success -> _state.update { it.copy(user = resource.data) }
                            is Resource.Error -> _state.update { it.copy(error = resource.message) }
                            else -> {}
                        }
                    }
                }
                launch {
                    repo.getContestList().collect { resource ->
                        if (resource is Resource.Success) {
                            val upcoming = resource.data
                                .filter { it.phase == "BEFORE" }
                                .sortedBy { it.startTimeSeconds }
                                .take(5)
                            _state.update { it.copy(upcomingContests = upcoming) }
                        }
                    }
                }
                launch {
                    val result = repo.getUserStatus(handle, 1, 10)
                    if (result is Resource.Success) {
                        _state.update { it.copy(recentSubmissions = result.data) }
                    }
                }
                launch {
                    repo.getRecentActions(20).collect { resource ->
                        if (resource is Resource.Success) {
                            _state.update { it.copy(recentActions = resource.data) }
                        }
                    }
                }
                launch {
                    val daily = repo.getDailyProblem()
                    _state.update { it.copy(dailyProblem = daily) }
                }
            }
            _state.update { it.copy(isLoading = false) }
        }
    }
}
