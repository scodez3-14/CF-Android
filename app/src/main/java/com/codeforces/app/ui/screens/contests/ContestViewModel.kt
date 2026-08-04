package com.codeforces.app.ui.screens.contests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeforces.app.data.api.ContestDto
import com.codeforces.app.data.repository.CodeforcesRepository
import com.codeforces.app.data.repository.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContestUiState(
    val upcoming: List<ContestDto> = emptyList(),
    val past: List<ContestDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ContestViewModel @Inject constructor(private val repo: CodeforcesRepository) : ViewModel() {

    private val _state = MutableStateFlow(ContestUiState())
    val state: StateFlow<ContestUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repo.getContestList().collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.update { it.copy(isLoading = true) }
                    is Resource.Success -> {
                        val upcoming = resource.data.filter { it.phase == "BEFORE" }.sortedBy { it.startTimeSeconds }
                        val past = resource.data.filter { it.phase == "FINISHED" }.sortedByDescending { it.startTimeSeconds }
                        _state.update { it.copy(upcoming = upcoming, past = past, isLoading = false) }
                    }
                    is Resource.Error -> _state.update { it.copy(isLoading = false, error = resource.message) }
                }
            }
        }
    }
}
