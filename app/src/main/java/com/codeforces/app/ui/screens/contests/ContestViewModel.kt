package com.codeforces.app.ui.screens.contests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeforces.app.data.api.ContestDto
import com.codeforces.app.data.api.ProblemDto
import com.codeforces.app.data.repository.CodeforcesRepository
import com.codeforces.app.data.repository.Resource
import com.codeforces.app.data.scraper.CfScraper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import javax.inject.Inject

data class ContestUiState(
    val upcoming: List<ContestDto> = emptyList(),
    val past: List<ContestDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val problems: List<ProblemDto> = emptyList(),
    val problemsLoading: Boolean = false,
    val problemsError: String? = null,
    val editorialHtml: String? = null,
    val isEditorialLoading: Boolean = false,
    val editorialLoadAttempted: Boolean = false
)

@HiltViewModel
class ContestViewModel @Inject constructor(
    private val repo: CodeforcesRepository,
    okHttpClient: OkHttpClient
) : ViewModel() {

    private val _state = MutableStateFlow(ContestUiState())
    val state: StateFlow<ContestUiState> = _state.asStateFlow()

    private val scraper = CfScraper(okHttpClient)

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

    fun loadContestProblems(contestId: Int) {
        if (_state.value.problemsLoading) return
        viewModelScope.launch {
            _state.update { it.copy(problemsLoading = true, problemsError = null) }
            when (val result = repo.getContestStandings(contestId)) {
                is Resource.Success -> _state.update {
                    it.copy(problems = result.data.problems, problemsLoading = false)
                }
                is Resource.Error -> _state.update {
                    it.copy(problemsError = result.message, problemsLoading = false)
                }
                else -> _state.update { it.copy(problemsLoading = false) }
            }
        }
    }

    fun loadEditorial(contestId: Int) {
        if (_state.value.isEditorialLoading || _state.value.editorialLoadAttempted) return
        _state.update { it.copy(isEditorialLoading = true, editorialLoadAttempted = true) }
        viewModelScope.launch {
            val html = withContext(Dispatchers.IO) { scraper.fetchEditorial(contestId.toString()) }
            _state.update { it.copy(editorialHtml = html, isEditorialLoading = false) }
        }
    }
}
