package com.codeforces.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeforces.app.data.repository.UserPreferencesRepository
import com.codeforces.app.notifications.ContestReminderManager
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private val _handle = MutableStateFlow<String?>(null)
    val handle: StateFlow<String?> = _handle

    init {
        reminderManager.init()
        viewModelScope.launch {
            // Keep the loading gate up until the very first handle value has
            // arrived. DataStore reads are async, so without this the saved
            // handle is still null while isLoading is already false, flashing
            // the onboarding/login screen for a frame on every app start.
            prefs.handle.collect { value ->
                _handle.value = value
                _isLoading.value = false
            }
        }
    }
}
