package com.codeforces.app.ui.screens.submissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeforces.app.data.api.SubmissionDto
import com.codeforces.app.data.repository.CodeforcesRepository
import com.codeforces.app.data.repository.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubmissionsUiState(
    val submissions: List<SubmissionDto> = emptyList(),
    val filtered: List<SubmissionDto> = emptyList(),
    val verdictFilter: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SubmissionsViewModel @Inject constructor(private val repo: CodeforcesRepository) : ViewModel() {

    private val _state = MutableStateFlow(SubmissionsUiState())
    val state: StateFlow<SubmissionsUiState> = _state.asStateFlow()

    fun load(handle: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = repo.getUserStatus(handle, 1, 200)
            when (result) {
                is Resource.Success -> _state.update { it.copy(submissions = result.data, isLoading = false) }
                is Resource.Error -> _state.update { it.copy(error = result.message, isLoading = false) }
                else -> {}
            }
            applyFilter()
        }
    }

    fun setFilter(verdict: String?) {
        _state.update { it.copy(verdictFilter = verdict) }
        applyFilter()
    }

    private fun applyFilter() {
        val s = _state.value
        _state.update {
            it.copy(filtered = if (s.verdictFilter == null) s.submissions
            else s.submissions.filter { sub -> sub.verdict == s.verdictFilter })
        }
    }
}
