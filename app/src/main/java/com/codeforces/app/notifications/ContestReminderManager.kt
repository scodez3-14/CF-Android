package com.codeforces.app.notifications

import android.content.Context
import com.codeforces.app.data.repository.CodeforcesRepository
import com.codeforces.app.data.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContestReminderManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repo: CodeforcesRepository,
    private val prefs: UserPreferencesRepository
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val scheduler by lazy { ContestReminderScheduler(context.applicationContext) }

    @Volatile
    private var initialized = false

    fun init() {
        if (initialized) return
        initialized = true
        scope.launch {
            prefs.remindersEnabled.collect { enabled ->
                if (enabled) reschedule()
                else scheduler.cancelAll()
            }
        }
    }

    private suspend fun reschedule() {
        try {
            val resource = repo.getContestList().first()
            val contests = (resource as? com.codeforces.app.data.repository.Resource.Success)
                ?.data.orEmpty()
            scheduler.scheduleAll(contests.filter { it.phase == "BEFORE" })
        } catch (_: Exception) {
        }
    }
}
