package com.codeforces.app.data.tracker

import android.content.Context
import com.codeforces.app.AppForegroundState
import com.codeforces.app.data.api.CodeforcesApiService
import com.codeforces.app.data.api.SubmissionDto
import com.codeforces.app.notifications.NotificationHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** UI-facing projection of one submission being tracked. */
data class SubmissionView(
    val id: Long,
    val verdict: String?,
    val passedTestCount: Int,
    val timeMillis: Int,
    val memoryBytes: Long,
    val language: String,
    val creationTimeSeconds: Long,
    val isRunning: Boolean = false
)

fun SubmissionDto.toView(isRunning: Boolean = false): SubmissionView = SubmissionView(
    id = id,
    verdict = verdict,
    passedTestCount = passedTestCount,
    timeMillis = timeConsumedMillis,
    memoryBytes = memoryConsumedBytes,
    language = programmingLanguage,
    creationTimeSeconds = creationTimeSeconds,
    isRunning = isRunning
)

enum class TrackStage {
    /** The hidden WebView is posting the solution form. */
    SUBMITTING,
    /** Submission accepted by Codeforces, waiting for a judge. */
    IN_QUEUE,
    /** Judge is running the solution on tests. */
    TESTING,
    /** Final verdict received. */
    FINAL,
    /** Polling budget exhausted without a final verdict. */
    TIMED_OUT
}

data class TrackedSubmission(
    val submissionId: Long?,
    val contestId: String,
    val problemIndex: String,
    val problemName: String,
    val language: String,
    val handle: String,
    val view: SubmissionView?,
    val stage: TrackStage,
    val finalVerdict: String? = null
) {
    val isRunning: Boolean
        get() = stage == TrackStage.SUBMITTING || stage == TrackStage.IN_QUEUE || stage == TrackStage.TESTING
}

/**
 * App-wide live submission tracker. Lives outside any ViewModel so verdict
 * polling survives navigating away from the problem screen, and can fire a
 * system notification when the verdict arrives while the app is backgrounded.
 */
@Singleton
class SubmissionTracker @Inject constructor(
    private val api: CodeforcesApiService,
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _active = MutableStateFlow<TrackedSubmission?>(null)
    val active: StateFlow<TrackedSubmission?> = _active.asStateFlow()

    private val _events = MutableSharedFlow<TrackedSubmission>(extraBufferCapacity = 4)
    val events: SharedFlow<TrackedSubmission> = _events.asSharedFlow()

    private var pollJob: Job? = null

    /** A new submission flow is starting (form is being posted). */
    fun begin(
        handle: String,
        contestId: String,
        problemIndex: String,
        problemName: String,
        language: String
    ) {
        pollJob?.cancel()
        _active.value = TrackedSubmission(
            submissionId = null,
            contestId = contestId,
            problemIndex = problemIndex,
            problemName = problemName,
            language = language,
            handle = handle,
            view = null,
            stage = TrackStage.SUBMITTING
        )
    }

    /** The WebView POST went through (or may have); [submissionId] comes from
     *  the redirect page when it could be extracted. Starts (or continues)
     *  verdict polling — the public API is the source of truth for whether
     *  the submission actually landed. */
    fun onDispatched(submissionId: Long?) {
        val t = _active.value ?: return
        if (t.stage == TrackStage.FINAL || t.stage == TrackStage.TIMED_OUT) return
        _active.value = t.copy(submissionId = submissionId ?: t.submissionId)
        startPolling()
    }

    /** The submit attempt failed outright — drop tracking if judging hasn't begun. */
    fun fail() {
        val t = _active.value ?: return
        if (t.isRunning && t.stage != TrackStage.SUBMITTING) return
        pollJob?.cancel()
        _active.value = null
    }

    /** Hide a finished / timed-out tracking card. */
    fun dismiss() {
        val t = _active.value ?: return
        if (t.isRunning) return
        _active.value = null
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = scope.launch {
            val startSec = System.currentTimeMillis() / 1000
            repeat(MAX_POLLS) {
                delay(POLL_INTERVAL_MS)
                val t = _active.value ?: return@launch
                try {
                    val resp = api.getUserStatus(t.handle, 1, 30)
                    val subs = resp.takeIf { it.status == "OK" }?.result.orEmpty()
                    val match = matchSubmission(subs, t, startSec)
                    if (match != null) {
                        val verdict = match.verdict
                        val stage = when {
                            verdict == "TESTING" -> TrackStage.TESTING
                            verdict == null || verdict == "IN_QUEUE" -> TrackStage.IN_QUEUE
                            else -> TrackStage.FINAL
                        }
                        val finished = _active.value?.copy(
                            view = match.toView(isRunning = stage != TrackStage.FINAL),
                            stage = stage,
                            finalVerdict = if (stage == TrackStage.FINAL) verdict else null
                        ) ?: return@launch
                        _active.value = finished
                        if (stage == TrackStage.FINAL) {
                            if (!AppForegroundState.isForeground) {
                                NotificationHelper.showVerdict(
                                    context,
                                    submissionId = match.id,
                                    contestId = t.contestId,
                                    problemLabel = "${t.contestId}${t.problemIndex}",
                                    verdict = verdict
                                )
                            }
                            _events.tryEmit(finished)
                            return@launch
                        }
                    }
                } catch (_: Exception) {
                    // Transient network error; keep polling.
                }
            }
            // No final verdict within the budget — surface it instead of
            // silently giving up.
            _active.value = _active.value?.copy(stage = TrackStage.TIMED_OUT)
        }
    }

    /** Match the API list against what we submitted: by id when available,
     *  otherwise the most recent submission on this exact problem. */
    private fun matchSubmission(
        subs: List<SubmissionDto>,
        t: TrackedSubmission,
        startSec: Long
    ): SubmissionDto? {
        val byId = t.submissionId
            ?.let { id -> subs.firstOrNull { it.id == id } }
            // Guard against an id scraped from a stale submissions page.
            ?.takeIf { startSec - it.creationTimeSeconds < 300 }
        if (byId != null) return byId
        return subs
            .filter {
                it.contestId?.toString() == t.contestId &&
                    it.problem.index == t.problemIndex &&
                    it.creationTimeSeconds >= startSec - 300
            }
            .maxByOrNull { it.creationTimeSeconds }
    }

    private companion object {
        const val POLL_INTERVAL_MS = 2000L
        const val MAX_POLLS = 90
    }
}
