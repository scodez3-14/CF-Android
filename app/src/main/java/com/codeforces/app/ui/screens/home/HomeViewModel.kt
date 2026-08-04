package com.codeforces.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeforces.app.data.api.ContestDto
import com.codeforces.app.data.api.RecentActionDto
import com.codeforces.app.data.api.SubmissionDto
import com.codeforces.app.data.api.UserDto
import com.codeforces.app.data.repository.CodeforcesRepository
import com.codeforces.app.data.repository.Resource
import com.codeforces.app.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val user: UserDto? = null,
    val upcomingContests: List<ContestDto> = emptyList(),
    val recentSubmissions: List<SubmissionDto> = emptyList(),
    val recentActions: List<RecentActionDto> = emptyList(),
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

    private fun loadAll(handle: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repo.getUserInfo(handle).collect { resource ->
                when (resource) {
                    is Resource.Success -> _state.update { it.copy(user = resource.data) }
                    is Resource.Error -> _state.update { it.copy(error = resource.message) }
                    else -> {}
                }
            }
        }
        viewModelScope.launch {
            repo.getContestList().collect { resource ->
                if (resource is Resource.Success) {
                    val upcoming = resource.data
                        .filter { it.phase == "BEFORE" }
                        .sortedBy { it.startTimeSeconds }
                        .take(5)
                    _state.update { it.copy(upcomingContests = upcoming, isLoading = false) }
                }
            }
        }
        viewModelScope.launch {
            val result = repo.getUserStatus(handle, 1, 10)
            if (result is Resource.Success) {
                _state.update { it.copy(recentSubmissions = result.data) }
            }
        }
        viewModelScope.launch {
            repo.getRecentActions(20).collect { resource ->
                if (resource is Resource.Success) {
                    _state.update { it.copy(recentActions = resource.data) }
                }
            }
        }
    }
}
