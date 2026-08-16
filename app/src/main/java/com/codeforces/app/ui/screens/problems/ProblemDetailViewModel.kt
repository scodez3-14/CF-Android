package com.codeforces.app.ui.screens.problems

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeforces.app.data.api.CodeforcesApiService
import com.codeforces.app.data.repository.UserPreferencesRepository
import com.codeforces.app.data.scraper.CfScraper
import com.codeforces.app.data.scraper.CfSubmitter
import com.codeforces.app.data.tracker.SubmissionTracker
import com.codeforces.app.data.tracker.SubmissionView
import com.codeforces.app.data.tracker.TrackedSubmission
import com.codeforces.app.data.tracker.toView
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import javax.inject.Inject

data class SampleTest(val input: String, val output: String)

data class ProblemDetail(
    val name: String,
    val contestId: String,
    val index: String,
    val rating: Int?,
    val tags: List<String>,
    val timeLimit: String,
    val memoryLimit: String,
    /** Extracted HTML of the .problem-statement div */
    val statementHtml: String,
    val samples: List<SampleTest>,
    /** HTML content of the editorial/tutorial, null if not found */
    val editorialHtml: String? = null
)

data class SubmitLanguage(val id: String, val label: String)

data class SubmitRequest(val languageId: String, val code: String)

sealed interface LoginState {
    object Checking : LoginState
    data class LoggedIn(val handle: String?) : LoginState
    object LoggedOut : LoginState
}

data class ProblemDetailUiState(
    val detail: ProblemDetail? = null,
    val isLoading: Boolean = false,
    val isEditorialLoading: Boolean = false,
    val editorialLoadAttempted: Boolean = false,
    val error: String? = null,
    // Submit tab
    val loginState: LoginState = LoginState.Checking,
    val isAutoLoggingIn: Boolean = false,
    val submitUserAgent: String? = null,
    val languages: List<SubmitLanguage> = emptyList(),
    val languagesReloadTrigger: Int = 0,
    /** True while the hidden WebView is posting the form (submit in flight). */
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val submitRequest: SubmitRequest? = null,
    // Submission tab
    val submissions: List<SubmissionView> = emptyList(),
    val isSubmissionsLoading: Boolean = false
)

@HiltViewModel
class ProblemDetailViewModel @Inject constructor(
    okHttpClient: OkHttpClient,
    private val prefs: UserPreferencesRepository,
    private val submitter: CfSubmitter,
    private val api: CodeforcesApiService,
    private val tracker: SubmissionTracker
) : ViewModel() {

    private val _state = MutableStateFlow(ProblemDetailUiState())
    val state: StateFlow<ProblemDetailUiState> = _state.asStateFlow()

    private val scraper = CfScraper(okHttpClient)

    val bookmarks: Flow<Set<String>> = prefs.bookmarks

    /** Live tracking of this problem's submission (null when idle or for
     *  another problem). Survives tab switches and navigation. */
    val track: StateFlow<TrackedSubmission?> =
        combine(tracker.active, _state) { t, s ->
            t?.takeIf { s.detail?.contestId == it.contestId && s.detail?.index == it.problemIndex }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** Emits each submission that reaches a final verdict. */
    val finishedEvents: kotlinx.coroutines.flow.SharedFlow<TrackedSubmission> = tracker.events

    init {
        viewModelScope.launch {
            _state.value = _state.value.copy(submitUserAgent = prefs.loginUserAgent())
        }
        // Refresh the history tab whenever any tracked submission finishes.
        viewModelScope.launch {
            tracker.events.collect { finished ->
                val detail = _state.value.detail
                if (detail != null &&
                    finished.contestId == detail.contestId &&
                    finished.problemIndex == detail.index
                ) {
                    refreshSubmissions()
                }
            }
        }
    }

    fun load(contestId: String, index: String, name: String, rating: Int? = null, tags: List<String> = emptyList()) {
        if (_state.value.detail != null || _state.value.isLoading) return

        viewModelScope.launch {
            _state.value = ProblemDetailUiState(isLoading = true)
            try {
                val detail = withContext(Dispatchers.IO) {
                    scrape(contestId, index, name, rating, tags)
                }
                _state.value = ProblemDetailUiState(detail = detail)
            } catch (e: Exception) {
                _state.value = ProblemDetailUiState(error = e.message ?: "Failed to load problem")
            }
        }
    }

    fun toggleBookmark() {
        val detail = _state.value.detail ?: return
        viewModelScope.launch {
            prefs.toggleBookmark("${detail.contestId}_${detail.index}")
        }
    }

    /**
     * Re-check the Codeforces web session. If the session is gone but the user
     * previously chose "remember me", silently re-authenticate with the stored
     * (encrypted) password so submitting still works after restarts.
     */
    fun refreshLoginState(autoLogin: Boolean = true) {
        if (_state.value.isAutoLoggingIn) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isAutoLoggingIn = true)
            val loggedIn = withContext(Dispatchers.IO) { submitter.isLoggedIn() }
                || withContext(Dispatchers.IO) { prefs.isSessionActive() }
            val state = if (loggedIn) {
                val handle = prefs.savedLoginHandle() ?: prefs.handle.first()
                _state.value.copy(loginState = LoginState.LoggedIn(handle))
            } else if (autoLogin) {
                autoLogin()
            } else {
                _state.value.copy(loginState = LoginState.LoggedOut)
            }
            _state.value = state.copy(isAutoLoggingIn = false)
        }
    }

    private suspend fun autoLogin(): ProblemDetailUiState {
        val handle = prefs.savedLoginHandle()
        val password = prefs.savedLoginPassword()
        if (handle.isNullOrBlank() || password == null) {
            return _state.value.copy(loginState = LoginState.LoggedOut)
        }
        val result = withContext(Dispatchers.IO) { submitter.login(handle, password) }
        return if (result.success) {
            _state.value.copy(loginState = LoginState.LoggedIn(handle))
        } else {
            _state.value.copy(loginState = LoginState.LoggedOut)
        }
    }

    /** End the Codeforces web session and drop the saved credentials. */
    fun logout() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { submitter.logout() }
            prefs.clearLoginCredentials()
            prefs.setSessionActive(false)
            _state.value = _state.value.copy(loginState = LoginState.LoggedOut)
        }
    }

    // ── Languages & submit ────────────────────────────────────────────────────

    /** Refresh the login session and ask the hidden submit WebView to (re)load
     *  the submit page when the language list is still empty. */
    fun loadLanguages() {
        if (_state.value.languages.isEmpty()) {
            _state.value = _state.value.copy(languagesReloadTrigger = _state.value.languagesReloadTrigger + 1)
        }
        refreshLoginState(autoLogin = false)
    }

    /** Languages are read straight out of the (browser) submit page by the
     *  hidden WebView, which reports them here. */
    fun onLanguagesLoaded(languages: List<SubmitLanguage>) {
        if (languages.isNotEmpty()) {
            _state.value = _state.value.copy(languages = languages)
        }
    }

    /** Queue a submission. The actual form POST happens inside the WebView
     *  (which passes Cloudflare), driven by [submitRequest]. Verdict tracking
     *  continues app-wide via [SubmissionTracker]. */
    fun submitCode(languageId: String, code: String) {
        if (_state.value.isSubmitting) return
        if (_state.value.loginState !is LoginState.LoggedIn) {
            _state.value = _state.value.copy(
                submitError = "Please sign in to Codeforces before submitting."
            )
            return
        }
        if (code.isBlank()) {
            _state.value = _state.value.copy(submitError = "Code is empty")
            return
        }
        val detail = _state.value.detail ?: return
        val languageLabel = _state.value.languages.firstOrNull { it.id == languageId }?.label.orEmpty()

        viewModelScope.launch {
            val handle = currentHandle()
            if (handle == null) {
                _state.value = _state.value.copy(submitError = "Could not determine your handle.")
                return@launch
            }
            tracker.begin(handle, detail.contestId, detail.index, detail.name, languageLabel)
            _state.value = _state.value.copy(
                isSubmitting = true,
                submitError = null,
                submitRequest = SubmitRequest(languageId, code)
            )
        }
    }

    /** The WebView filled the form and fired the native form POST. From here
     *  the public API is the source of truth — start polling immediately so
     *  a slow/undetected redirect can't be mistaken for a failed submit. */
    fun onSubmitDispatched() {
        _state.value = _state.value.copy(submitRequest = null)
        tracker.onDispatched(null)
    }

    /** The WebView landed on the "my submissions" page after a successful POST. */
    fun onSubmitSucceeded(submissionId: Long?) {
        if (!_state.value.isSubmitting) return
        _state.value = _state.value.copy(isSubmitting = false, submitRequest = null, submitError = null)
        tracker.onDispatched(submissionId)
    }

    /** The WebView reported that the submission didn't go through (form
     *  validation error, missing editor, etc. — the POST never happened). */
    fun onSubmitFailed(message: String) {
        if (!_state.value.isSubmitting) return
        tracker.fail()
        _state.value = _state.value.copy(
            isSubmitting = false,
            submitRequest = null,
            submitError = message
        )
    }

    /** The POST was sent but Codeforces didn't confirm in time (watchdog).
     *  The submission may well have landed — keep watching via the tracker
     *  instead of declaring failure. */
    fun onSubmitAmbiguous() {
        if (!_state.value.isSubmitting) return
        _state.value = _state.value.copy(
            isSubmitting = false,
            submitRequest = null,
            submitError = null
        )
    }

    /** Hide the finished/timed-out verdict card. */
    fun dismissTrack() {
        tracker.dismiss()
    }

    // ── Verdicts via the public API (works without cookies / Cloudflare) ─────

    fun loadSubmissions() {
        val detail = _state.value.detail ?: return
        if (_state.value.isSubmissionsLoading) return
        refreshSubmissions()
    }

    fun refreshSubmissions() {
        val detail = _state.value.detail ?: return
        _state.value = _state.value.copy(isSubmissionsLoading = true)
        viewModelScope.launch {
            try {
                val handle = currentHandle() ?: run {
                    _state.value = _state.value.copy(isSubmissionsLoading = false)
                    return@launch
                }
                val resp = api.getUserStatus(handle, 1, 100)
                val subs = resp.takeIf { it.status == "OK" }?.result
                    .orEmpty()
                    .filter { it.contestId?.toString() == detail.contestId && it.problem.index == detail.index }
                    .sortedByDescending { it.creationTimeSeconds }
                    .take(10)
                    .map { it.toView() }
                _state.value = _state.value.copy(isSubmissionsLoading = false, submissions = subs)
            } catch (_: Exception) {
                _state.value = _state.value.copy(isSubmissionsLoading = false)
            }
        }
    }

    private suspend fun currentHandle(): String? {
        val fromState = (_state.value.loginState as? LoginState.LoggedIn)?.handle?.takeIf { it.isNotBlank() }
        return fromState ?: prefs.handle.first()
    }

    /** The editorial needs two additional web requests, so fetch it only when its tab is opened. */
    fun loadEditorial() {
        val currentDetail = _state.value.detail ?: return
        if (currentDetail.editorialHtml != null || _state.value.isEditorialLoading || _state.value.editorialLoadAttempted) return

        _state.value = _state.value.copy(isEditorialLoading = true, editorialLoadAttempted = true)
        viewModelScope.launch {
            val editorial = withContext(Dispatchers.IO) {
                scraper.fetchEditorial(currentDetail.contestId)
            }
            _state.value = _state.value.copy(
                detail = _state.value.detail?.copy(editorialHtml = editorial),
                isEditorialLoading = false
            )
        }
    }

    /** Fetch the problem page HTML. Pre-escapes < > inside $$$ math blocks
     *  so Jsoup doesn't mangle them as HTML tags. */
    private fun fetchDoc(contestId: String, index: String): org.jsoup.nodes.Document {
        val urls = listOf(
            "https://codeforces.com/contest/$contestId/problem/$index?locale=en",
            "https://codeforces.com/problemset/problem/$contestId/$index?locale=en",
            "https://codeforces.com/gym/$contestId/problem/$index?locale=en"
        )
        for (url in urls) {
            scraper.fetchUrl(url)?.let { doc ->
                if (doc.selectFirst(".problem-statement") != null) return doc
            }
        }
        throw Exception("Failed to fetch problem page")
    }

    private fun scrape(
        contestId: String,
        index: String,
        name: String,
        rating: Int?,
        tags: List<String>
    ): ProblemDetail {
        val doc = fetchDoc(contestId, index)

        val header = doc.selectFirst(".problem-statement > .header")
        val timeLimit = header?.selectFirst(".time-limit")?.ownText()?.trim() ?: "?"
        val memLimit  = header?.selectFirst(".memory-limit")?.ownText()?.trim() ?: "?"

        val statementDiv = doc.selectFirst(".problem-statement")!!

        // Fix relative image URLs
        statementDiv.select("img[src]").forEach { img ->
            val src = img.attr("src")
            if (src.startsWith("/")) img.attr("src", "https://codeforces.com$src")
        }
        statementDiv.select("[style]").forEach { el ->
            val style = el.attr("style")
            if (style.contains("url(")) {
                el.attr("style", style
                    .replace("url('/", "url('https://codeforces.com/")
                    .replace("url(/", "url(https://codeforces.com/"))
            }
        }

        // Extract samples
        val sampleSection = statementDiv.selectFirst(".sample-tests")
        val inputs  = mutableListOf<String>()
        val outputs = mutableListOf<String>()
        sampleSection?.select(".input")?.forEach  { inp -> inp.selectFirst("pre")?.let { inputs.add(extractPreContent(it)) } }
        sampleSection?.select(".output")?.forEach { out -> out.selectFirst("pre")?.let { outputs.add(extractPreContent(it)) } }
        val samples = inputs.zip(outputs).map { (i, o) -> SampleTest(i, o) }

        val statementHtml = statementDiv.outerHtml()

        return ProblemDetail(
            name = name, contestId = contestId, index = index,
            rating = rating, tags = tags,
            timeLimit = timeLimit, memoryLimit = memLimit,
            statementHtml = statementHtml, samples = samples
        )
    }

    private fun extractPreContent(pre: org.jsoup.nodes.Element): String {
        val divLines = pre.select("div")
        if (divLines.isNotEmpty()) {
            return divLines.joinToString("\n") { it.text().trim() }
        }
        return pre.wholeText().replace("\u00a0", " ").trim()
    }
}
