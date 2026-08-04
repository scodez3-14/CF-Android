package com.codeforces.app.ui.screens.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeforces.app.data.api.UserDto
import com.codeforces.app.data.repository.CodeforcesRepository
import com.codeforces.app.data.repository.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LeaderboardUiState(
    val users: List<UserDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val activeOnly: Boolean = true
)

@HiltViewModel
class LeaderboardViewModel @Inject constructor(private val repo: CodeforcesRepository) : ViewModel() {
    private val _state = MutableStateFlow(LeaderboardUiState())
    val state: StateFlow<LeaderboardUiState> = _state.asStateFlow()

    init { load() }

    fun load(activeOnly: Boolean = _state.value.activeOnly) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, activeOnly = activeOnly) }
            repo.getRatedList(activeOnly).collect { resource ->
                when (resource) {
                    is Resource.Success -> _state.update { it.copy(users = resource.data.take(200), isLoading = false) }
                    is Resource.Error -> _state.update { it.copy(error = resource.message, isLoading = false) }
                    else -> {}
                }
            }
        }
    }
}
