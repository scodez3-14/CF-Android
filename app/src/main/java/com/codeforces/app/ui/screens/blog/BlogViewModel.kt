package com.codeforces.app.ui.screens.blog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeforces.app.data.api.BlogEntryDto
import com.codeforces.app.data.repository.CodeforcesRepository
import com.codeforces.app.data.repository.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BlogUiState(
    val entries: List<BlogEntryDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class BlogViewModel @Inject constructor(private val repo: CodeforcesRepository) : ViewModel() {
    private val _state = MutableStateFlow(BlogUiState())
    val state: StateFlow<BlogUiState> = _state.asStateFlow()

    fun load(handle: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val res = repo.getUserBlogEntries(handle)) {
                is Resource.Success -> _state.update { it.copy(entries = res.data, isLoading = false) }
                is Resource.Error -> _state.update { it.copy(error = res.message, isLoading = false) }
                else -> {}
            }
        }
    }
}
