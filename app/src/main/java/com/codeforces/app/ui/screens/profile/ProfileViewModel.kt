package com.codeforces.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeforces.app.data.api.RatingChangeDto
import com.codeforces.app.data.api.SubmissionDto
import com.codeforces.app.data.api.UserDto
import com.codeforces.app.data.repository.CodeforcesRepository
import com.codeforces.app.data.repository.Resource
import com.codeforces.app.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: UserDto? = null,
    val ratingHistory: List<RatingChangeDto> = emptyList(),
    val recentSubmissions: List<SubmissionDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repo: CodeforcesRepository,
    private val prefs: UserPreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    val savedHandle: Flow<String?> = prefs.handle

    fun loadProfile(handle: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repo.getUserInfo(handle).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.update { it.copy(isLoading = true) }
                    is Resource.Success -> _state.update { it.copy(user = resource.data, isLoading = false) }
                    is Resource.Error -> _state.update { it.copy(error = resource.message, isLoading = false) }
                }
            }
        }
        viewModelScope.launch {
            val ratingResult = repo.getUserRating(handle)
            if (ratingResult is Resource.Success) {
                _state.update { it.copy(ratingHistory = ratingResult.data) }
            }
        }
        viewModelScope.launch {
            val submResult = repo.getUserStatus(handle, 1, 10)
            if (submResult is Resource.Success) {
                _state.update { it.copy(recentSubmissions = submResult.data) }
            }
        }
    }
}
