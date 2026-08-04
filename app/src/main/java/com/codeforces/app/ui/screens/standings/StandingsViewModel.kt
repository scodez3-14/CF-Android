package com.codeforces.app.ui.screens.standings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeforces.app.data.api.StandingsDto
import com.codeforces.app.data.repository.CodeforcesRepository
import com.codeforces.app.data.repository.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StandingsUiState(
    val standings: StandingsDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class StandingsViewModel @Inject constructor(private val repo: CodeforcesRepository) : ViewModel() {
    private val _state = MutableStateFlow(StandingsUiState())
    val state: StateFlow<StandingsUiState> = _state.asStateFlow()

    fun load(contestId: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = repo.getContestStandings(contestId)) {
                is Resource.Success -> _state.update { it.copy(standings = result.data, isLoading = false) }
                is Resource.Error -> _state.update { it.copy(error = result.message, isLoading = false) }
                else -> {}
            }
        }
    }
}
