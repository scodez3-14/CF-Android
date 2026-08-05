package com.codeforces.app.ui.screens.problems

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeforces.app.data.api.ProblemDto
import com.codeforces.app.data.api.ProblemStatisticsDto
import com.codeforces.app.data.repository.CodeforcesRepository
import com.codeforces.app.data.repository.Resource
import com.codeforces.app.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProblemsUiState(
    val problems: List<ProblemDto> = emptyList(),
    val statistics: List<ProblemStatisticsDto> = emptyList(),
    val statisticsByProblem: Map<String, Int> = emptyMap(),
    val filteredProblems: List<ProblemDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedTags: Set<String> = emptySet(),
    val ratingFilterEnabled: Boolean = false,
    val minRating: Int = 800,
    val maxRating: Int = 3500,
    val contestNames: Map<Int, String> = emptyMap(),
    val bookmarks: Set<String> = emptySet(),
    val savedOnly: Boolean = false
)

@HiltViewModel
class ProblemsViewModel @Inject constructor(
    private val repo: CodeforcesRepository,
    private val prefs: UserPreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProblemsUiState())
    val state: StateFlow<ProblemsUiState> = _state.asStateFlow()

    val allTags = listOf(
        "dp", "greedy", "implementation", "math", "brute force", "data structures",
        "graphs", "trees", "strings", "sorting", "binary search", "number theory",
        "combinatorics", "geometry", "dfs and similar", "bitmasks", "two pointers",
        "shortest paths", "hashing", "divide and conquer", "constructive algorithms",
        "interactive", "games", "flows", "probabilities", "matrices", "fft", "schedules"
    )

    init {
        loadProblems()
        loadContestNames()
        viewModelScope.launch {
            prefs.bookmarks.collect { bookmarks ->
                _state.update { it.copy(bookmarks = bookmarks) }
                applyFilters()
            }
        }
    }

    fun toggleBookmark(id: String) {
        viewModelScope.launch { prefs.toggleBookmark(id) }
    }

    fun setSavedOnly(enabled: Boolean) {
        _state.update { it.copy(savedOnly = enabled) }
        applyFilters()
    }

    private fun loadContestNames() {
        viewModelScope.launch {
            repo.getContestList().collect { resource ->
                if (resource is Resource.Success) {
                    val names = resource.data.associate { it.id to it.name }
                    _state.update { it.copy(contestNames = names) }
                }
            }
        }
    }

    fun loadProblems(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repo.getProblems(forceRefresh = forceRefresh).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.update { it.copy(isLoading = true) }
                    is Resource.Success -> {
                        _state.update { s ->
                            s.copy(
                                problems = resource.data.problems,
                                statistics = resource.data.problemStatistics,
                                statisticsByProblem = resource.data.problemStatistics.associate {
                                    "${it.contestId}_${it.index}" to it.solvedCount
                                },
                                isLoading = false
                            )
                        }
                        applyFilters()
                    }
                    is Resource.Error -> _state.update { it.copy(isLoading = false, error = resource.message) }
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun toggleTag(tag: String) {
        _state.update { s ->
            val updated = s.selectedTags.toMutableSet()
            if (tag in updated) updated.remove(tag) else updated.add(tag)
            s.copy(selectedTags = updated)
        }
        applyFilters()
    }

    fun clearTags() {
        _state.update { it.copy(selectedTags = emptySet()) }
        applyFilters()
    }

    fun setRatingFilterEnabled(enabled: Boolean) {
        _state.update { it.copy(ratingFilterEnabled = enabled) }
        applyFilters()
    }

    fun setRatingRange(min: Int, max: Int) {
        _state.update { it.copy(minRating = min, maxRating = max) }
        applyFilters()
    }

    fun clearFilters() {
        _state.update {
            it.copy(
                searchQuery = "",
                selectedTags = emptySet(),
                ratingFilterEnabled = false,
                minRating = 800,
                maxRating = 3500
            )
        }
        applyFilters()
    }

    private fun applyFilters() {
        val s = _state.value
        _state.update {
            it.copy(filteredProblems = s.problems
                .filter { p ->
                    val id = "${p.contestId}_${p.index}"
                    val matchesSaved = !s.savedOnly || id in s.bookmarks
                    if (!matchesSaved) return@filter false
                    val matchesSearch = s.searchQuery.isBlank() ||
                            p.name.contains(s.searchQuery, ignoreCase = true) ||
                            p.contestId?.toString()?.startsWith(s.searchQuery) == true
                    val matchesTags = s.selectedTags.isEmpty() ||
                            s.selectedTags.all { tag -> tag in p.tags }
                    val matchesRating = !s.ratingFilterEnabled ||
                            (p.rating != null && p.rating in s.minRating..s.maxRating)
                    matchesSearch && matchesTags && matchesRating
                }
                // Sort: newest contest first (highest contestId), then by index letter
                .sortedWith(compareByDescending<ProblemDto> { it.contestId ?: 0 }
                    .thenBy { it.index })
            )
        }
    }
}
