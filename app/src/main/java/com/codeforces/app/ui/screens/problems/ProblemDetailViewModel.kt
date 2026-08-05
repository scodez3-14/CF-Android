package com.codeforces.app.ui.screens.problems

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeforces.app.data.api.CodeforcesApiService
import com.codeforces.app.data.api.SubmissionDto
import com.codeforces.app.data.repository.UserPreferencesRepository
import com.codeforces.app.data.scraper.CfScraper
import com.codeforces.app.data.scraper.CfSubmitter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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

data class SubmissionView(
    val id: Long,
    val verdict: String?,
    val passedTestCount: Int,
    val timeMillis: Int,
    val memoryBytes: Long,
    val language: String,
    val creationTimeSeconds: Long
)

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
    val isSubmitting: Boolean = false,
    val submitResult: String? = null,
    val submitVerdict: String? = null,
    val submitError: String? = null,
    val submitRequest: SubmitRequest? = null,
    val trackedSubmission: SubmissionView? = null,
    // Submission tab
    val submissions: List<SubmissionView> = emptyList(),
    val isSubmissionsLoading: Boolean = false
)

@HiltViewModel
class ProblemDetailViewModel @Inject constructor(
    okHttpClient: OkHttpClient,
    private val prefs: UserPreferencesRepository,
    private val submitter: CfSubmitter,
    private val api: CodeforcesApiService
) : ViewModel() {

    private val _state = MutableStateFlow(ProblemDetailUiState())
    val state: StateFlow<ProblemDetailUiState> = _state.asStateFlow()

    private val scraper = CfScraper(okHttpClient)

    val bookmarks: Flow<Set<String>> = prefs.bookmarks

    init {
        viewModelScope.launch {
            _state.value = _state.value.copy(submitUserAgent = prefs.loginUserAgent())
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
     *  (which passes Cloudflare), driven by [submitRequest]. */
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
        _state.value = _state.value.copy(
            isSubmitting = true,
            submitError = null,
            submitResult = null,
            submitVerdict = null,
            trackedSubmission = null,
            submitRequest = SubmitRequest(languageId, code)
        )
    }

    /** The WebView filled the form and fired the native form POST. */
    fun onSubmitDispatched() {
        _state.value = _state.value.copy(submitRequest = null)
    }

    /** The WebView landed on the "my submissions" page after a successful POST. */
    fun onSubmitSucceeded(submissionId: Long?) {
        if (!_state.value.isSubmitting) return
        _state.value = _state.value.copy(isSubmitting = false, submitRequest = null, submitError = null)
        viewModelScope.launch {
            trackSubmission(submissionId)
        }
    }

    /** The WebView reported that the submission didn't go through. */
    fun onSubmitFailed(message: String) {
        if (!_state.value.isSubmitting) return
        _state.value = _state.value.copy(
            isSubmitting = false,
            submitRequest = null,
            submitError = message
        )
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

    private suspend fun trackSubmission(submissionId: Long?) {
        val detail = _state.value.detail ?: return
        val handle = currentHandle() ?: run {
            onSubmitFailed("Could not determine your handle.")
            return
        }
        val now = System.currentTimeMillis() / 1000
        repeat(60) {
            delay(3000)
            try {
                val resp = api.getUserStatus(handle, 1, 30)
                val subs = resp.takeIf { it.status == "OK" }?.result.orEmpty()
                val match = when {
                    submissionId != null -> {
                        val byId = subs.firstOrNull { it.id == submissionId }
                        // If the id points at an old submission (page hadn't updated),
                        // fall back to the most recent matching one.
                        if (byId != null && now - byId.creationTimeSeconds < 180) byId
                        else newestRecent(subs, detail, now)
                    }
                    else -> newestRecent(subs, detail, now)
                }
                if (match != null) {
                    _state.value = _state.value.copy(trackedSubmission = match.toView())
                    val verdict = match.verdict
                    if (verdict != null && verdict != "TESTING" && verdict != "IN_QUEUE") {
                        _state.value = _state.value.copy(
                            submitResult = verdictLabel(verdict),
                            submitVerdict = verdict,
                            isSubmitting = false
                        )
                        refreshSubmissions()
                        return
                    }
                }
            } catch (_: Exception) {
                // Transient network error; keep polling.
            }
        }
        _state.value = _state.value.copy(isSubmitting = false)
    }

    private fun newestRecent(
        subs: List<SubmissionDto>,
        detail: ProblemDetail,
        now: Long
    ): SubmissionDto? {
        return subs
            .filter {
                it.contestId?.toString() == detail.contestId &&
                    it.problem.index == detail.index &&
                    now - it.creationTimeSeconds < 600
            }
            .maxByOrNull { it.creationTimeSeconds }
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

/** Display label for a Codeforces API verdict. */
fun verdictLabel(verdict: String?): String = when (verdict) {
    null, "", "IN_QUEUE" -> "In queue"
    "TESTING" -> "Testing…"
    "OK" -> "Accepted"
    "WRONG_ANSWER" -> "Wrong answer"
    "TIME_LIMIT_EXCEEDED" -> "Time limit exceeded"
    "MEMORY_LIMIT_EXCEEDED" -> "Memory limit exceeded"
    "RUNTIME_ERROR" -> "Runtime error"
    "COMPILATION_ERROR" -> "Compilation error"
    "IDLENESS_LIMIT_EXCEEDED" -> "Idleness limit exceeded"
    "PRESENTATION_ERROR" -> "Presentation error"
    "SECURITY_VIOLATED" -> "Security violated"
    "CHALLENGED" -> "Hacked"
    "SKIPPED" -> "Skipped"
    "PARTIAL" -> "Partial"
    "CRASHED" -> "Crashed"
    "REJECTED" -> "Rejected"
    else -> verdict ?: "In queue"
}

private fun SubmissionDto.toView(): SubmissionView = SubmissionView(
    id = id,
    verdict = verdict,
    passedTestCount = passedTestCount,
    timeMillis = timeConsumedMillis,
    memoryBytes = memoryConsumedBytes,
    language = programmingLanguage,
    creationTimeSeconds = creationTimeSeconds
)
