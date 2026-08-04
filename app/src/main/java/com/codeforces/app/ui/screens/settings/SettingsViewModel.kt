package com.codeforces.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeforces.app.data.repository.CodeforcesRepository
import com.codeforces.app.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository,
    private val repo: CodeforcesRepository
) : ViewModel() {

    val handle: Flow<String?> = prefs.handle

    fun saveHandle(newHandle: String) {
        viewModelScope.launch { prefs.saveHandle(newHandle) }
    }

    fun logout() {
        viewModelScope.launch {
            prefs.clearAll()
            repo.clearAllCache()
        }
    }
}
