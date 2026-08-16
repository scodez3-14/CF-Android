package com.codeforces.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeforces.app.data.repository.UserPreferencesRepository
import com.codeforces.app.data.update.ReleaseInfo
import com.codeforces.app.data.update.UpdateChecker
import com.codeforces.app.notifications.ContestReminderManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository,
    private val reminderManager: ContestReminderManager,
    private val updateChecker: UpdateChecker
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _handle = MutableStateFlow<String?>(null)
    val handle: StateFlow<String?> = _handle

    private val _updateInfo = MutableStateFlow<ReleaseInfo?>(null)
    val updateInfo: StateFlow<ReleaseInfo?> = _updateInfo.asStateFlow()

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
        // Check GitHub for a newer release shortly after startup (non-blocking).
        viewModelScope.launch {
            delay(3_000) // Wait 3 s so the UI has settled first
            val current = BuildConfig.VERSION_NAME
            val info = updateChecker.checkForUpdate(current)
            _updateInfo.value = info
        }
    }

    fun dismissUpdate() {
        _updateInfo.value = null
    }
}
