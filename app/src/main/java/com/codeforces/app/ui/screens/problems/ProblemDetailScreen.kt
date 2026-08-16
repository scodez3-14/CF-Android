package com.codeforces.app.ui.screens.problems

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codeforces.app.data.tracker.SubmissionView
import com.codeforces.app.data.tracker.TrackStage
import com.codeforces.app.data.tracker.TrackedSubmission
import com.codeforces.app.ui.components.AcceptedReveal
import com.codeforces.app.ui.components.CfWebView
import com.codeforces.app.ui.components.ShimmerList
import com.codeforces.app.ui.components.buildEditorialHtml
import com.codeforces.app.ui.components.buildProblemHtml
import com.codeforces.app.ui.components.hasFailingTestNumber
import com.codeforces.app.ui.components.verdictColor
import com.codeforces.app.ui.components.verdictLabel
import com.codeforces.app.ui.theme.*
import org.json.JSONArray
import org.json.JSONObject
import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProblemDetailScreen(
    contestId: String,
    index: String,
    name: String,
    onLogin: () -> Unit,
    onBack: () -> Unit,
    onOpenSubmission: ((contestId: String, submissionId: Long, handle: String) -> Unit)? = null,
    viewModel: ProblemDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle(initialValue = emptySet())
    val track by viewModel.track.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Description", "Submit", "Submission", "Solution")

    // Full-screen Accepted celebration: triggered once, when the tracked
    // submission for THIS problem finishes with an OK verdict.
    var reveal by remember { mutableStateOf<TrackedSubmission?>(null) }
    LaunchedEffect(Unit) {
        viewModel.finishedEvents.collect { finished ->
            val detail = state.detail
            if (finished.finalVerdict == "OK" &&
                detail?.contestId == finished.contestId &&
                detail?.index == finished.problemIndex
            ) {
                reveal = finished
            }
        }
    }

    val isBookmarked = state.detail?.let { "${it.contestId}_${it.index}" in bookmarks } == true

    LaunchedEffect(contestId, index) {
        viewModel.load(contestId, index, name)
    }

    // Re-verify the login session whenever this screen is resumed (e.g. after
    // returning from the Login screen) so the Submit tab reflects reality.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshLoginState(autoLogin = false)
    }

    LaunchedEffect(selectedTabIndex, state.detail?.contestId) {
        when (selectedTabIndex) {
            1 -> viewModel.loadLanguages()
            2 -> viewModel.loadSubmissions()
            3 -> viewModel.loadEditorial()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Problem",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = CfTextPrimary,
                        modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally).padding(end = 48.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = CfTextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleBookmark() }) {
                        Icon(
                            if (isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) CodeforcesAccent else CfTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CfSurface)
            )
        },
        containerColor = CfBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Tabs ──
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = CfSurface,
                contentColor = CfTextPrimary,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = CodeforcesAccent,
                        height = 2.dp
                    )
                },
                divider = {}
            ) {
                tabs.forEachIndexed { i, title ->
                    Tab(
                        selected = selectedTabIndex == i,
                        onClick = { selectedTabIndex = i },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    title,
                                    color = if (selectedTabIndex == i) CfTextPrimary else CfTextSecondary,
                                    fontWeight = if (selectedTabIndex == i) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                                if (title == "Submit") {
                                    Spacer(Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(CodeforcesAccent)
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            "New",
                                            color = Color.White,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }

            HorizontalDivider(color = CfDivider, thickness = 1.dp)

            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                val contentKey = when {
                    state.isLoading -> 0
                    state.error != null -> 1
                    state.detail != null -> 2
                    else -> 3
                }
                AnimatedContent(
                    targetState = contentKey,
                    modifier = Modifier.fillMaxSize(),
                    transitionSpec = {
                        (fadeIn(tween(250, easing = FastOutSlowInEasing)) +
                                slideInVertically(tween(250, easing = FastOutSlowInEasing)) { it / 20 })
                                .togetherWith(fadeOut(tween(150)))
                    },
                    label = "problemDetailContent"
                ) { key ->
                    when (key) {
                        // ── Loading ──
                        0 -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = CodeforcesAccent, strokeWidth = 3.dp)
                            }
                        }
                        // ── Error ──
                        1 -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.ErrorOutline,
                                        contentDescription = null,
                                        tint = CodeforcesAccent,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text("Failed to load", color = CfTextPrimary, fontWeight = FontWeight.Bold)
                                    Text(state.error ?: "", color = CfTextSecondary, fontSize = 13.sp)
                                    Button(
                                        onClick = { viewModel.load(contestId, index, name) },
                                        colors = ButtonDefaults.buttonColors(containerColor = CodeforcesAccent)
                                    ) {
                                        Text("Retry")
                                    }
                                }
                            }
                        }
                        // ── Content ──
                        2 -> {
                            val detail = state.detail
                            if (detail != null) {
                                when (selectedTabIndex) {
                                    0 -> {
                                        // Description tab
                                        CfWebView(
                                            html = buildProblemHtml(detail.statementHtml),
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    1 -> {
                                        // Submit tab
                                        SubmitTab(state = state, track = track, viewModel = viewModel, onLogin = onLogin)
                                    }
                                    2 -> {
                                        // Submission tab
                                        SubmissionsTab(state = state, viewModel = viewModel, onOpenSubmission = onOpenSubmission)
                                    }
                                    else -> {
                                        // Solution / Editorial tab
                                        val editorial = detail.editorialHtml
                                        if (editorial != null) {
                                            CfWebView(
                                                html = buildEditorialHtml(editorial),
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else if (state.isEditorialLoading) {
                                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                CircularProgressIndicator(color = CodeforcesAccent, strokeWidth = 3.dp)
                                            }
                                        } else {
                                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(
                                                        "📝",
                                                        fontSize = 40.sp
                                                    )
                                                    Text(
                                                        "No solution found",
                                                        color = CfTextSecondary,
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Text(
                                                        "A solution for this problem\nis not available in the contest tutorial.",
                                                        color = CfTextDisabled,
                                                        fontSize = 13.sp,
                                                        textAlign = TextAlign.Center,
                                                        lineHeight = 18.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // ── Idle ──
                        else -> {
                            Box(Modifier.fillMaxSize())
                        }
                    }
                }
            }

            // Hidden browser used to pass Cloudflare and submit code. Lives at
            // screen level so it stays warm across tab switches.
            val loaded = state.detail
            if (loaded != null && !state.isLoading && state.error == null) {
                HiddenSubmitWebView(
                    contestId = loaded.contestId,
                    index = loaded.index,
                    userAgent = state.submitUserAgent,
                    submitRequest = state.submitRequest,
                    reloadTrigger = state.languagesReloadTrigger,
                    onLanguagesLoaded = viewModel::onLanguagesLoaded,
                    onSubmitSucceeded = viewModel::onSubmitSucceeded,
                    onSubmitFailed = viewModel::onSubmitFailed,
                    onSubmitAmbiguous = viewModel::onSubmitAmbiguous,
                    onSubmitDispatched = viewModel::onSubmitDispatched
                )
            }
        }
        }

        // ── Accepted celebration overlay ──
        reveal?.let { finished ->
            val view = finished.view ?: SubmissionView(
                id = finished.submissionId ?: 0L,
                verdict = finished.finalVerdict,
                passedTestCount = 0,
                timeMillis = 0,
                memoryBytes = 0,
                language = finished.language,
                creationTimeSeconds = System.currentTimeMillis() / 1000
            )
            AcceptedReveal(
                problemLabel = "${finished.contestId}${finished.problemIndex}",
                contestId = finished.contestId,
                submissionId = finished.submissionId,
                passedTests = view.passedTestCount,
                timeMillis = view.timeMillis,
                memoryBytes = view.memoryBytes,
                language = view.language.ifBlank { finished.language },
                onSubmitAgain = { reveal = null },
                onDismiss = { reveal = null }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubmitTab(
    state: ProblemDetailUiState,
    track: TrackedSubmission?,
    viewModel: ProblemDetailViewModel,
    onLogin: () -> Unit
) {
    when (state.loginState) {
        is LoginState.LoggedIn, LoginState.Checking -> SubmitForm(state = state, track = track, viewModel = viewModel)
        LoginState.LoggedOut -> LoginRequired(onLogin = onLogin)
    }
}

@Composable
private fun LoginRequired(onLogin: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(32.dp))
        Icon(
            imageVector = Icons.Rounded.Lock,
            contentDescription = null,
            tint = CfTextDisabled,
            modifier = Modifier.size(56.dp)
        )
        Text(
            "Sign in to submit",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = CfTextPrimary
        )
        Text(
            "Submitting a solution requires a real Codeforces account.\nSign in once and your session is kept for future submissions.",
            color = CfTextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Button(
            onClick = onLogin,
            colors = ButtonDefaults.buttonColors(containerColor = CodeforcesAccent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Rounded.Login, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Sign in with handle & password")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubmitForm(
    state: ProblemDetailUiState,
    track: TrackedSubmission?,
    viewModel: ProblemDetailViewModel
) {
    var selectedLanguage by remember { mutableStateOf<String?>(null) }
    var code by remember { mutableStateOf("") }
    var menuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.languages) {
        if (selectedLanguage == null && state.languages.isNotEmpty()) {
            selectedLanguage = state.languages.first().id
        }
    }

    val selectedLabel = state.languages.firstOrNull { it.id == selectedLanguage }?.label
    val isLoggedIn = state.loginState is LoginState.LoggedIn
    val canSubmit = isLoggedIn && !state.isSubmitting && selectedLanguage != null && code.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!isLoggedIn) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    color = CodeforcesAccent,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    "Checking sign-in…",
                    color = CfTextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        ExposedDropdownMenuBox(
            expanded = menuExpanded,
            onExpandedChange = { menuExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedLabel ?: "Loading languages…",
                onValueChange = {},
                readOnly = true,
                label = { Text("Language") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CodeforcesAccent,
                    focusedContainerColor = CfCardSurface,
                    unfocusedContainerColor = CfCardSurface
                )
            )
            ExposedDropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                state.languages.forEach { lang ->
                    DropdownMenuItem(
                        text = { Text(lang.label, maxLines = 1) },
                        onClick = {
                            selectedLanguage = lang.id
                            menuExpanded = false
                        }
                    )
                }
            }
        }

        if (state.languages.isEmpty() && !state.isSubmitting) {
            TextButton(
                onClick = { viewModel.loadLanguages() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Languages didn't load — retry")
            }
        }

        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("Source code") },
            modifier = Modifier.fillMaxWidth().height(260.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CodeforcesAccent,
                focusedContainerColor = CfCardSurface,
                unfocusedContainerColor = CfCardSurface
            )
        )

        Button(
            onClick = {
                selectedLanguage?.let { viewModel.submitCode(it, code) }
            },
            enabled = canSubmit,
            colors = ButtonDefaults.buttonColors(containerColor = CodeforcesAccent),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Submitting…")
            } else {
                Text("Submit Code")
            }
        }

        // ── Live verdict card: app-wide tracking, updates in real time. ──
        track?.let {
            LiveJudgingCard(track = it, onDismiss = { viewModel.dismissTrack() })
        }

        state.submitError?.let {
            Text(it, color = VerdictWA, style = MaterialTheme.typography.bodyMedium)
        }
        if (state.submitError == null && track == null && !state.isSubmitting) {
            Text(
                "Signed in as ${(state.loginState as? LoginState.LoggedIn)?.handle ?: "Codeforces user"}. Your session is used to submit on your behalf.",
                color = CfTextDisabled,
                fontSize = 12.sp
            )
        }
    }
}

// ── Live Judging Card (Codeforces-style) ────────────────────────────────────

@Composable
private fun LiveJudgingCard(track: TrackedSubmission, onDismiss: () -> Unit) {
    val t = track.view
    val running = track.isRunning

    // Pulsing glow animation for the card border while the judge runs
    val infiniteTransition = rememberInfiniteTransition(label = "judgingPulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glowAlpha"
    )
    val dotScale by infiniteTransition.animateFloat(
        initialValue = 0.7f, targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "dotScale"
    )

    val stageColor = when (track.stage) {
        TrackStage.SUBMITTING, TrackStage.IN_QUEUE -> CodeforcesAccent
        TrackStage.TESTING -> Color(0xFFFFC107)   // amber
        TrackStage.FINAL -> verdictColor(track.finalVerdict)
        TrackStage.TIMED_OUT -> CfTextSecondary
    }

    val statusText = when (track.stage) {
        TrackStage.SUBMITTING -> "Sending solution…"
        TrackStage.IN_QUEUE -> "In queue for a judge…"
        TrackStage.TESTING -> "Running on test ${(t?.passedTestCount ?: 0) + 1}…"
        TrackStage.FINAL -> verdictLabel(track.finalVerdict)
        TrackStage.TIMED_OUT -> "Still judging…"
    }

    val cardShape = RoundedCornerShape(14.dp)
    Card(
        colors = CardDefaults.cardColors(containerColor = CfCardSurface),
        shape = cardShape,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (running) {
                    Modifier.border(
                        width = 1.5.dp,
                        brush = Brush.horizontalGradient(
                            listOf(
                                stageColor.copy(alpha = glowAlpha * 0.8f),
                                stageColor.copy(alpha = glowAlpha * 0.3f),
                                stageColor.copy(alpha = glowAlpha * 0.8f)
                            )
                        ),
                        shape = cardShape
                    )
                } else {
                    Modifier.border(1.dp, stageColor.copy(alpha = 0.35f), shape = cardShape)
                }
            )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header row with pulsing dot / verdict icon + status
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (running) {
                    Box(
                        modifier = Modifier
                            .size(10.dp * dotScale)
                            .clip(RoundedCornerShape(50))
                            .background(stageColor.copy(alpha = glowAlpha))
                    )
                } else {
                    Icon(
                        imageVector = when (track.stage) {
                            TrackStage.FINAL -> when (track.finalVerdict) {
                                "OK" -> Icons.Rounded.CheckCircle
                                "WRONG_ANSWER", "REJECTED" -> Icons.Rounded.Cancel
                                "TIME_LIMIT_EXCEEDED", "IDLENESS_LIMIT_EXCEEDED" -> Icons.Rounded.Timer
                                "MEMORY_LIMIT_EXCEEDED" -> Icons.Rounded.Memory
                                "COMPILATION_ERROR" -> Icons.Rounded.BugReport
                                else -> Icons.Rounded.ErrorOutline
                            }
                            else -> Icons.Rounded.HourglassTop
                        },
                        contentDescription = null,
                        tint = stageColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                AnimatedContent(
                    targetState = statusText,
                    transitionSpec = {
                        (fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 3 })
                            .togetherWith(fadeOut(tween(150)))
                    },
                    label = "judgingStatus",
                    modifier = Modifier.weight(1f)
                ) { text ->
                    Text(text, color = stageColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                if (running) {
                    CircularProgressIndicator(
                        color = stageColor,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Stage stepper: Sent → Queue → Tests → Done
            JudgingStepper(stage = track.stage, finalVerdict = track.finalVerdict, accent = stageColor)

            // Live progress while the judge runs tests
            if (track.stage == TrackStage.TESTING && t != null) {
                val progressAnim by animateFloatAsState(
                    targetValue = if (t.passedTestCount > 0) {
                        // We don't know total tests; show an indeterminate fill up to ~80%
                        (t.passedTestCount * 0.04f).coerceAtMost(0.85f)
                    } else 0.05f,
                    animationSpec = tween(600),
                    label = "progressAnim"
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LinearProgressIndicator(
                        progress = { progressAnim },
                        modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(50)),
                        color = stageColor,
                        trackColor = stageColor.copy(alpha = 0.15f)
                    )
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("${t.passedTestCount} tests passed", color = CfTextSecondary, fontSize = 11.sp)
                        if (t.timeMillis > 0) {
                            Text("${t.timeMillis} ms", color = CfTextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }

            if (track.stage == TrackStage.FINAL) {
                FinalVerdictBlock(track = track, tint = stageColor, onDismiss = onDismiss)
            }
            if (track.stage == TrackStage.TIMED_OUT) {
                TimedOutBlock(track = track, onDismiss = onDismiss)
            }
        }
    }
}

@Composable
private fun JudgingStepper(stage: TrackStage, finalVerdict: String?, accent: Color) {
    val currentStep = when (stage) {
        TrackStage.SUBMITTING -> 0
        TrackStage.IN_QUEUE -> 1
        TrackStage.TESTING -> 2
        TrackStage.FINAL -> 3
        TrackStage.TIMED_OUT -> 2
    }
    val doneColor = if (stage == TrackStage.FINAL) verdictColor(finalVerdict) else accent

    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        StepNode(Icons.Rounded.CloudUpload, "Sent", 0, currentStep, doneColor)
        StepSegment(done = currentStep >= 1, color = doneColor)
        StepNode(Icons.Rounded.Schedule, "Queue", 1, currentStep, doneColor)
        StepSegment(done = currentStep >= 2, color = doneColor)
        StepNode(Icons.Rounded.Science, "Tests", 2, currentStep, doneColor)
        StepSegment(done = currentStep >= 3, color = doneColor)
        StepNode(Icons.Rounded.TaskAlt, "Done", 3, currentStep, doneColor)
    }
}

@Composable
private fun StepNode(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    step: Int,
    currentStep: Int,
    doneColor: Color
) {
    val reached = step < currentStep
    val isCurrent = step == currentStep
    val tint = if (reached || isCurrent) doneColor else CfTextDisabled

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(50))
                .background(if (reached || isCurrent) tint.copy(alpha = 0.16f) else CfCardSurface)
                .border(
                    width = 1.dp,
                    color = if (reached || isCurrent) tint else CfDivider,
                    shape = RoundedCornerShape(50)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(15.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            fontSize = 9.sp,
            color = if (reached || isCurrent) CfTextSecondary else CfTextDisabled,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.StepSegment(
    done: Boolean,
    color: Color
) {
    val alpha by animateFloatAsState(
        targetValue = if (done) 0.9f else 0.12f,
        animationSpec = tween(400),
        label = "stepSegment"
    )
    Box(
        modifier = Modifier
            .weight(1f)
            .padding(top = 14.dp)
            .padding(horizontal = 4.dp)
            .height(2.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(color.copy(alpha = alpha))
    )
}

/** Inline verdict summary shown once judging finishes (the Accepted case gets
 *  the full-screen overlay as well). */
@Composable
private fun FinalVerdictBlock(track: TrackedSubmission, tint: Color, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val t = track.view
    val verdict = track.finalVerdict

    HorizontalDivider(color = CfDivider, thickness = 0.5.dp)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(tint.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(verdictLabel(verdict), color = tint, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        if (verdict == "OK" && t != null && t.passedTestCount > 0) {
            Text("· ${t.passedTestCount} tests", color = CfTextSecondary, fontSize = 12.sp)
        } else if (hasFailingTestNumber(verdict) && t != null) {
            Text("on test ${t.passedTestCount + 1}", color = CfTextSecondary, fontSize = 12.sp)
        }
    }

    // Stats row (time + memory)
    if (t != null && (t.timeMillis > 0 || t.memoryBytes > 0)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (t.timeMillis > 0) {
                StatChip(label = "Time", value = "${t.timeMillis} ms", tint = Color(0xFF64B5F6))
            }
            if (t.memoryBytes > 0) {
                StatChip(label = "Memory", value = formatMemory(t.memoryBytes), tint = Color(0xFFA5D6A7))
            }
        }
    }

    Row(
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.fillMaxWidth()
    ) {
        TextButton(
            onClick = {
                val id = t?.id ?: track.submissionId
                val url = if (id != null) {
                    "https://codeforces.com/contest/${track.contestId}/submission/$id"
                } else {
                    "https://codeforces.com/submissions/${track.handle}"
                }
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        ) {
            Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text("View on Codeforces", fontSize = 12.sp)
        }
        TextButton(onClick = onDismiss) {
            Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text("Clear", fontSize = 12.sp)
        }
    }
}

/** Polling budget exhausted without a final verdict — don't fail silently. */
@Composable
private fun TimedOutBlock(track: TrackedSubmission, onDismiss: () -> Unit) {
    val context = LocalContext.current
    HorizontalDivider(color = CfDivider, thickness = 0.5.dp)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Rounded.WarningAmber, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(18.dp))
        Text("Taking longer than usual", color = CfTextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
    Text(
        "The judge is still busy with this submission. Your verdict will show up on your submissions page shortly.",
        color = CfTextDisabled,
        fontSize = 11.sp,
        lineHeight = 15.sp
    )
    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
        TextButton(
            onClick = {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://codeforces.com/submissions/${track.handle}"))
                )
            }
        ) {
            Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text("Open submissions", fontSize = 12.sp)
        }
        TextButton(onClick = onDismiss) {
            Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text("Clear", fontSize = 12.sp)
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, tint: Color) {
    Column {
        Text(label, color = CfTextDisabled, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        Text(value, color = tint, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ── Submission history (verdicts from the public API) ─────────────────────────

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun SubmissionsTab(
    state: ProblemDetailUiState,
    viewModel: ProblemDetailViewModel,
    onOpenSubmission: ((contestId: String, submissionId: Long, handle: String) -> Unit)? = null
) {
    val context = LocalContext.current

    if (state.isSubmissionsLoading && state.submissions.isEmpty()) {
        ShimmerList(
            modifier = Modifier.fillMaxSize(),
            itemCount = 6,
            contentPadding = PaddingValues(12.dp)
        )
        return
    }

    if (state.submissions.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.weight(1f))
            Icon(
                Icons.Rounded.History,
                contentDescription = null,
                tint = CfTextDisabled,
                modifier = Modifier.size(48.dp)
            )
            Text(
                "No submissions yet",
                color = CfTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                "Submit a solution from the Submit tab to see it here.",
                color = CfTextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.weight(1f))
            OutlinedButton(onClick = { viewModel.refreshSubmissions() }) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Refresh")
            }
            Spacer(Modifier.height(12.dp))
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(state.submissions, key = { it.id }) { sub ->
            val color = verdictColor(sub.verdict)
            Card(
                onClick = {
                    val problemContestId = state.detail?.contestId
                    if (problemContestId != null) {
                        val submissionHandle = (state.loginState as? LoginState.LoggedIn)?.handle ?: ""
                        if (onOpenSubmission != null) {
                            onOpenSubmission(problemContestId, sub.id, submissionHandle)
                        } else {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://codeforces.com/contest/$problemContestId/submission/${sub.id}")
                                )
                            )
                        }
                    }
                },
                colors = CardDefaults.cardColors(containerColor = CfCardSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().animateItemPlacement()
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(color.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                verdictLabel(sub.verdict),
                                color = color,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Text("#${sub.id}", color = CfTextSecondary, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(sub.language, color = CfTextPrimary, fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (sub.passedTestCount > 0) {
                            InfoText("tests ${sub.passedTestCount}")
                        }
                        InfoText("${sub.timeMillis} ms")
                        InfoText(formatMemory(sub.memoryBytes))
                        InfoText(relativeTime(sub.creationTimeSeconds))
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoText(text: String) {
    Text(text, color = CfTextSecondary, fontSize = 12.sp)
}

private fun formatMemory(bytes: Long): String = when {
    bytes >= 1024 * 1024 -> "%.1f MB".format(bytes / 1048576.0)
    bytes > 0 -> "${bytes / 1024} KB"
    else -> "0 KB"
}

private fun relativeTime(seconds: Long): String {
    val diff = (System.currentTimeMillis() / 1000) - seconds
    return when {
        diff < 60 -> "just now"
        diff < 3600 -> "${diff / 60}m ago"
        diff < 86400 -> "${diff / 3600}h ago"
        else -> "${diff / 86400}d ago"
    }
}

// ── Hidden submit WebView ────────────────────────────────────────────────────

private const val TAG = "CFSUBMIT"

private fun submitUrl(contestId: String) =
    "https://codeforces.com/contest/$contestId/submit?locale=en"

private fun gymSubmitUrl(contestId: String) =
    "https://codeforces.com/gym/$contestId/submit?locale=en"

/** Installed once per loaded submit page. Defines the helpers the app calls:
 *  language extraction, form filling + native POST, submission-id and error reads. */
private val INSTALL_HELPERS_JS = """
(function(){
  window.__CF_EXTRACT_LANGS=function(){
    function readOptions(){
      var sel=document.querySelector('select[name="programTypeId"], #programTypeId');
      if(!sel||!sel.options||!sel.options.length)return [];
      var out=[];
      for(var i=0;i<sel.options.length;i++){
        var o=sel.options[i];
        var label=(o.textContent||o.text||'').replace(/\s+/g,' ').trim();
        if(o.value&&label)out.push({id:String(o.value),label:label});
      }
      return out;
    }
    function send(arr){ if(arr.length){ CodeforcesApp.reportLanguages(JSON.stringify(arr)); return true; } return false; }
    try{
      if(send(readOptions()))return;
      fetch('/data/languages'+(window.__CF_CONTEST?'?contestId='+window.__CF_CONTEST:''),{credentials:'same-origin'}).then(function(r){return r.text();}).then(function(t){
        try{
          var body=t.replace(/^[\s\S]*?try\s*\{/,'').replace(/\}\s*catch\s*\([\s\S]*$/,'');
          var arr=null;
          try{arr=JSON.parse(body);}catch(e){arr=null;}
          if(arr&&arr.length&&arr[0].name!==undefined){
            var mapped=arr.map(function(x){return {id:String(x.id),label:String(x.name||'')};}).filter(function(x){return x.id&&x.label;});
            if(send(mapped))return;
          }
          try{(new Function(body))();}catch(e){}
          send(readOptions());
        }catch(e){}
      }).catch(function(){});
    }catch(e){}
  };
  window.__CF_SUBMIT=function(code,langId,index){
    try{
      var form=document.getElementById('submit-form')||document.querySelector('form.submit-form')||document.querySelector('form.submitForm')||document.querySelector('form[action*="csrf_token"]')||document.querySelector('form[action*="/submit"]')||document.querySelector('form');
      if(!form){CodeforcesApp.reportSubmit('error:No submit form found on the page.');return;}
      var sel=document.querySelector('select[name="programTypeId"], #programTypeId');
      if(sel){
        var found=false;
        for(var i=0;i<sel.options.length;i++){if(sel.options[i].value===String(langId)){sel.selectedIndex=i;found=true;break;}}
        if(!found){CodeforcesApp.reportSubmit('error:Language is not available on Codeforces. Pick another one.');return;}
        sel.dispatchEvent(new Event('change',{bubbles:true}));
      }
      var prob=document.querySelector('select[name="submittedProblemIndex"],#submittedProblemCode,select[name="submittedProblemCode"],input[name="submittedProblemCode"]');
      if(prob){prob.value=index;prob.dispatchEvent(new Event('change',{bubbles:true}));}
      var ta=form.querySelector('textarea[name="source"]')||document.getElementById('sourceCodeTextarea');
      if(!ta){CodeforcesApp.reportSubmit('error:No source editor found on the page.');return;}
      ta.value=code;
      ta.dispatchEvent(new Event('input',{bubbles:true}));
      var action=form.getAttribute('action')||'';
      if(action.indexOf('csrf_token')===-1){
        var csrf=form.querySelector('input[name="csrf_token"]')||form.querySelector('input[name="_csrf"]');
        if(csrf&&csrf.value){
          var sep=action.indexOf('?')===-1?'?':'&';
          form.setAttribute('action',action+sep+'csrf_token='+encodeURIComponent(csrf.value));
        }
      }
      form.submit();
    }catch(e){CodeforcesApp.reportSubmit('error:'+String((e&&e.message)||e));}
  };
})()
""".trimIndent()

private const val EXTRACT_LANGS_JS = "window.__CF_EXTRACT_LANGS&&window.__CF_EXTRACT_LANGS();"

private val EXTRACT_SUBMISSION_ID_JS = """
(function(){
  try{
    var a=document.querySelectorAll('a[href*="/submission/"]');
    for(var i=0;i<a.length;i++){
      var m=(a[i].getAttribute('href')||'').match(/\/submission\/(\d+)/);
      if(m)return m[1];
    }
    var r=document.querySelector('[data-submission-id]');
    if(r)return r.getAttribute('data-submission-id');
  }catch(e){}
  return '';
})()
""".trimIndent()

private val EXTRACT_ERROR_JS = """
(function(){
  try{
    var els=document.querySelectorAll('.error, #error, .form-error');
    var seen={};
    for(var i=0;i<els.length;i++){
      var t=(els[i].textContent||'').replace(/\s+/g,' ').trim();
      if(t&&!seen[t]){seen[t]=1;return t;}
    }
    var p=document.querySelector('.error pre, #error pre');
    if(p&&p.textContent.trim())return p.textContent.replace(/\s+/g,' ').trim();
  }catch(e){}
  return '';
})()
""".trimIndent()

/** @JavascriptInterface callbacks from the page run on a background thread,
 *  so everything is re-posted onto the main looper. */
private class SubmitJsBridge(
    private val handler: Handler,
    private val onReady: () -> Unit,
    private val onLanguages: (List<SubmitLanguage>) -> Unit,
    private val onFailed: (String) -> Unit
) {
    @JavascriptInterface
    fun reportLanguages(json: String) {
        val list = parseLanguagesJson(json)
        if (list.isNotEmpty()) {
            handler.post {
                onReady()
                onLanguages(list)
            }
        }
    }

    @JavascriptInterface
    fun reportSubmit(result: String) {
        if (result.startsWith("error:")) {
            val msg = result.removePrefix("error:")
            handler.post { onFailed(msg) }
        }
    }
}

private fun parseLanguagesJson(json: String): List<SubmitLanguage> {
    return try {
        val arr = JSONArray(json)
        (0 until arr.length()).mapNotNull { i ->
            val o = arr.getJSONObject(i)
            val id = o.optString("id")
            val label = o.optString("label")
            if (id.isNotBlank() && label.isNotBlank()) SubmitLanguage(id, label) else null
        }
    } catch (_: Exception) {
        emptyList()
    }
}

/** JSON-encode a value so it can be dropped into a JS string literal safely. */
private fun jsString(value: String): String {
    val json = JSONObject().put("v", value).toString()
    return json.substringAfter("\"v\":").dropLast(1)
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun HiddenSubmitWebView(
    contestId: String,
    index: String,
    userAgent: String?,
    submitRequest: SubmitRequest?,
    reloadTrigger: Int,
    onLanguagesLoaded: (List<SubmitLanguage>) -> Unit,
    onSubmitSucceeded: (Long?) -> Unit,
    onSubmitFailed: (String) -> Unit,
    onSubmitAmbiguous: () -> Unit,
    onSubmitDispatched: () -> Unit
) {
    val context = LocalContext.current
    val webViewRef = remember { mutableStateOf<WebView?>(null) }
    val pageReady = remember { mutableStateOf(false) }
    val pendingSubmit = remember { mutableStateOf<SubmitRequest?>(null) }
    val submitDispatched = remember { mutableStateOf(false) }
    val gymMode = remember { mutableStateOf(false) }
    val appliedUa = remember { mutableStateOf<String?>(null) }
    val lastReloadTrigger = remember { mutableIntStateOf(-1) }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    fun currentSubmitUrl(): String =
        if (gymMode.value) gymSubmitUrl(contestId) else submitUrl(contestId)

    fun maybeDispatch() {
        val req = pendingSubmit.value ?: return
        val view = webViewRef.value ?: return
        val url = view.url ?: ""
        if (!url.contains("/submit") && !url.contains("/problemset/problem")) {
            pageReady.value = false
            view.post { view.loadUrl(currentSubmitUrl()) }
            return
        }
        if (!pageReady.value) return
        val script = "window.__CF_SUBMIT&&window.__CF_SUBMIT(" +
            jsString(req.code) + "," +
            jsString(req.languageId) + "," +
            jsString(index) + ");"
        Log.d(TAG, "dispatching submit")
        view.evaluateJavascript(script, null)
        pendingSubmit.value = null
        submitDispatched.value = true
        onSubmitDispatched()
        mainHandler.postDelayed({
            if (submitDispatched.value) {
                // POST was sent but Codeforces didn't confirm in time. The
                // submission may still have landed — don't call it a failure.
                submitDispatched.value = false
                onSubmitAmbiguous()
            }
        }, 25000)
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                // The cf_clearance cookie is bound to the UA of the WebView that
                // logged in, so this one must present the same UA.
                settings.userAgentString = userAgent ?: WebSettings.getDefaultUserAgent(ctx)
                setBackgroundColor(android.graphics.Color.TRANSPARENT)

                addJavascriptInterface(
                    SubmitJsBridge(
                        handler = mainHandler,
                        onReady = {
                            pageReady.value = true
                            maybeDispatch()
                        },
                        onLanguages = onLanguagesLoaded,
                        onFailed = { msg ->
                            submitDispatched.value = false
                            pendingSubmit.value = null
                            onSubmitFailed(msg)
                        }
                    ),
                    "CodeforcesApp"
                )

                webViewClient = object : WebViewClient() {
                    override fun onReceivedHttpError(
                        view: WebView,
                        request: android.webkit.WebResourceRequest,
                        errorResponse: android.webkit.WebResourceResponse
                    ) {
                        super.onReceivedHttpError(view, request, errorResponse)
                        // Gym problems aren't under /contest; retry under /gym.
                        if (!gymMode.value && request.isForMainFrame && errorResponse.statusCode == 404) {
                            gymMode.value = true
                            view.post { view.loadUrl(gymSubmitUrl(contestId)) }
                        }
                    }

                    override fun onPageFinished(view: WebView, url: String?) {
                        super.onPageFinished(view, url)
                        val u = url ?: return
                        Log.d(TAG, "onPageFinished $u")
                        if (u.contains("/my") || u.contains("/submissions") || u.contains("/status")) {
                            if (submitDispatched.value) {
                                submitDispatched.value = false
                                view.postDelayed({
                                    view.evaluateJavascript(EXTRACT_SUBMISSION_ID_JS) { r ->
                                        val id = r?.trim()?.removeSurrounding("\"")?.toLongOrNull()
                                        mainHandler.post { onSubmitSucceeded(id) }
                                    }
                                }, 1500)
                            }
                            return
                        }
                        if (u.contains("/submit") || u.contains("/problemset")) {
                            view.evaluateJavascript("window.__CF_CONTEST=" + jsString(contestId) + ";", null)
                            view.evaluateJavascript(INSTALL_HELPERS_JS, null)
                            if (submitDispatched.value) {
                                // Came back to the form. A real validation error
                                // has visible text; a blank re-render is ambiguous
                                // (the POST may still have landed) — let the API
                                // tracker decide instead of declaring failure.
                                submitDispatched.value = false
                                view.postDelayed({
                                    view.evaluateJavascript(EXTRACT_ERROR_JS) { r ->
                                        val err = r?.trim()?.removeSurrounding("\"")?.ifBlank { null }
                                        mainHandler.post {
                                            if (err != null) onSubmitFailed(err)
                                            else onSubmitAmbiguous()
                                        }
                                    }
                                }, 800)
                            } else {
                                view.postDelayed({ view.evaluateJavascript(EXTRACT_LANGS_JS, null) }, 600)
                            }
                        }
                    }
                }

                webViewRef.value = this
                loadUrl(submitUrl(contestId))
            }
        },
        update = { view ->
            // The saved login UA (which cf_clearance is bound to) may arrive
            // after the first load; apply it once and reload so the cookie matches.
            val targetUa = userAgent ?: WebSettings.getDefaultUserAgent(view.context)
            if (appliedUa.value != targetUa) {
                appliedUa.value = targetUa
                if (view.settings.userAgentString != targetUa) {
                    view.settings.userAgentString = targetUa
                    pageReady.value = false
                    view.reload()
                }
            }
            if (reloadTrigger != lastReloadTrigger.intValue) {
                lastReloadTrigger.intValue = reloadTrigger
                view.evaluateJavascript(EXTRACT_LANGS_JS, null)
            }
            val request = submitRequest
            if (request != null && pendingSubmit.value == null) {
                pendingSubmit.value = request
                maybeDispatch()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .alpha(0f)
    )
}
