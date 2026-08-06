package com.codeforces.app.ui.screens.problems

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeforces.app.data.api.ProblemDto
import com.codeforces.app.data.api.ProblemStatisticsDto
import com.codeforces.app.data.repository.CodeforcesRepository
import com.codeforces.app.data.repository.Resource
import com.codeforces.app.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProblemSection(
    val contestId: Int?,
    val problems: List<ProblemDto>
)

data class ProblemsUiState(
    val problems: List<ProblemDto> = emptyList(),
    val statistics: List<ProblemStatisticsDto> = emptyList(),
    val statisticsByProblem: Map<String, Int> = emptyMap(),
    val filteredProblems: List<ProblemDto> = emptyList(),
    val sections: List<ProblemSection> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedTags: Set<String> = emptySet(),
    val ratingFilterEnabled: Boolean = false,
    val minRating: Int = 800,
    val maxRating: Int = 3500,
    val contestNames: Map<Int, String> = emptyMap(),
    val bookmarks: Set<String> = emptySet(),
    val savedOnly: Boolean = false,
    val hasMore: Boolean = false
)

@HiltViewModel
class ProblemsViewModel @Inject constructor(
    private val repo: CodeforcesRepository,
    private val prefs: UserPreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProblemsUiState())
    val state: StateFlow<ProblemsUiState> = _state.asStateFlow()

    private var filterGen = 0
    private var displayCount = INITIAL_PAGE_SIZE
    private var allFilteredProblems: List<ProblemDto> = emptyList()

    companion object {
        private const val INITIAL_PAGE_SIZE = 80
        private const val PAGE_SIZE = 80
    }

    private val searchQueryFlow = MutableStateFlow("")

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
        viewModelScope.launch {
            searchQueryFlow
                .debounce(150)
                .distinctUntilChanged()
                .collect { applyFilters() }
        }
    }

    fun toggleBookmark(id: String) {
        viewModelScope.launch { prefs.toggleBookmark(id) }
    }

    fun setSavedOnly(enabled: Boolean) {
        _state.update { it.copy(savedOnly = enabled) }
        displayCount = INITIAL_PAGE_SIZE
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
        searchQueryFlow.value = query
    }

    fun toggleTag(tag: String) {
        _state.update { s ->
            val updated = s.selectedTags.toMutableSet()
            if (tag in updated) updated.remove(tag) else updated.add(tag)
            s.copy(selectedTags = updated)
        }
        displayCount = INITIAL_PAGE_SIZE
        applyFilters()
    }

    fun clearTags() {
        _state.update { it.copy(selectedTags = emptySet()) }
        applyFilters()
    }

    fun setRatingFilterEnabled(enabled: Boolean) {
        _state.update { it.copy(ratingFilterEnabled = enabled) }
        displayCount = INITIAL_PAGE_SIZE
        applyFilters()
    }

    fun setRatingRange(min: Int, max: Int) {
        _state.update { it.copy(minRating = min, maxRating = max) }
        displayCount = INITIAL_PAGE_SIZE
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
        displayCount = INITIAL_PAGE_SIZE
        applyFilters()
    }

    fun loadMore() {
        if (!_state.value.hasMore) return
        displayCount += PAGE_SIZE
        paginateCurrentFiltered()
    }

    private fun applyFilters() {
        val s = _state.value
        val gen = ++filterGen
        viewModelScope.launch(Dispatchers.Default) {
            val filtered = s.problems
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
            if (gen == filterGen) {
                allFilteredProblems = filtered
                displayCount = INITIAL_PAGE_SIZE
                paginateCurrentFiltered()
            }
        }
    }

    private fun paginateCurrentFiltered() {
        val paginated = allFilteredProblems.take(displayCount)
        val sections = paginated
            .groupBy { it.contestId }
            .map { (contestId, problems) -> ProblemSection(contestId, problems) }
        _state.update {
            it.copy(
                filteredProblems = allFilteredProblems,
                sections = sections,
                hasMore = displayCount < allFilteredProblems.size
            )
        }
    }
}
