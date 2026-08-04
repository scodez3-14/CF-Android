package com.codeforces.app.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeforces.app.data.api.UserDto
import com.codeforces.app.data.repository.CodeforcesRepository
import com.codeforces.app.data.repository.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val result: UserDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(private val repo: CodeforcesRepository) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChanged(query: String) {
        _state.update { it.copy(query = query, error = null) }
        searchJob?.cancel()
        if (query.length >= 2) {
            searchJob = viewModelScope.launch {
                delay(600) // debounce
                search(query)
            }
        } else {
            _state.update { it.copy(result = null) }
        }
    }

    private suspend fun search(handle: String) {
        _state.update { it.copy(isLoading = true) }
        repo.getUserInfo(handle).collect { resource ->
            when (resource) {
                is Resource.Success -> _state.update { it.copy(result = resource.data, isLoading = false) }
                is Resource.Error -> _state.update { it.copy(error = resource.message, isLoading = false, result = null) }
                else -> {}
            }
        }
    }
}
