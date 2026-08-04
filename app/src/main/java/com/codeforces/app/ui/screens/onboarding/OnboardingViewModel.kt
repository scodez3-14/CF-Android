package com.codeforces.app.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeforces.app.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _navigated = Channel<Unit>()
    val navigated: Flow<Unit> = _navigated.receiveAsFlow()

    fun saveHandle(handle: String) {
        viewModelScope.launch {
            _isLoading.value = true
            prefs.saveHandle(handle)
            _navigated.send(Unit)
            _isLoading.value = false
        }
    }
}
