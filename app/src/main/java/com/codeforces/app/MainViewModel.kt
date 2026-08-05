package com.codeforces.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeforces.app.data.repository.UserPreferencesRepository
import com.codeforces.app.notifications.ContestReminderManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository,
    private val reminderManager: ContestReminderManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    val handle: Flow<String?> = prefs.handle

    init {
        reminderManager.init()
        viewModelScope.launch {
            delay(500) // allow splash screen
            _isLoading.value = false
        }
    }
}
