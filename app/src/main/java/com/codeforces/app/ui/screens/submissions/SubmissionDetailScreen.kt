package com.codeforces.app.ui.screens.submissions

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.draw.alpha
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.codeforces.app.ui.components.SkeletonBox
import com.codeforces.app.ui.components.SignInRequired
import com.codeforces.app.ui.components.rememberShimmerBrush
import com.codeforces.app.ui.components.verdictColor
import com.codeforces.app.ui.components.verdictLabel
import com.codeforces.app.ui.navigation.Screen
import com.codeforces.app.ui.theme.CfBackground
import com.codeforces.app.ui.theme.CfCardSurface
import com.codeforces.app.ui.theme.CfSurface
import com.codeforces.app.ui.theme.CfTextDisabled
import com.codeforces.app.ui.theme.CfTextPrimary
import com.codeforces.app.ui.theme.CfTextSecondary
import com.codeforces.app.ui.theme.CodeforcesAccent

/** In-app submission view: verdict, stats, source code with copy/share. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmissionDetailScreen(
    contestId: String,
    submissionId: Long,
    handle: String,
    navController: NavController,
    onBack: () -> Unit,
    onLogin: () -> Unit = {},
    viewModel: SubmissionDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val meta = state.meta

    LaunchedEffect(contestId, submissionId) {
        viewModel.load(contestId, submissionId, handle)
    }

    // After returning from the web login (session renewed), retry the fetch.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (state.needsCheck) viewModel.retrySource()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Submission", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CfSurface)
            )
        },
        containerColor = CfBackground
    ) { padding ->
        if (isLoggedIn == false) {
            SignInRequired(
                message = "Source code is tied to your Codeforces account. Sign in to view it.",
                onLogin = onLogin,
                modifier = Modifier.padding(padding)
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Verdict header ──
            val color = verdictColor(meta?.verdict)
            Card(
                colors = CardDefaults.cardColors(containerColor = CfCardSurface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        verdictLabel(meta?.verdict),
                        color = color,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        buildString {
                            append("#$submissionId")
                            meta?.programmingLanguage?.takeIf { it.isNotBlank() }?.let { append("  ·  $it") }
                            meta?.let { append("  ·  ${relativeTime(it.creationTimeSeconds)}") }
                        },
                        color = CfTextSecondary,
                        fontSize = 12.sp
                    )
                    if (meta != null) {
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatChip("Tests", "${meta.passedTestCount}")
                            StatChip("Time", "${meta.timeConsumedMillis} ms")
                            StatChip("Memory", formatMemory(meta.memoryConsumedBytes))
                        }
                    }
                }
            }

            // ── Problem ──
            if (meta != null) {
                Card(
                    onClick = {
                        meta.problem.contestId?.let { cid ->
                            navController.navigate(
                                Screen.ProblemDetail.createRoute(
                                    cid.toString(),
                                    meta.problem.index,
                                    meta.problem.name
                                )
                            )
                        }
                    },
                    colors = CardDefaults.cardColors(containerColor = CfCardSurface),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Code,
                            contentDescription = null,
                            tint = CodeforcesAccent,
                            modifier = Modifier.size(22.dp)
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                meta.problem.name,
                                color = CfTextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                buildString {
                                    meta.problem.contestId?.let { append("${it}${meta.problem.index}") }
                                    meta.problem.rating?.let { append("  ·  $it") }
                                },
                                color = CfTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Text("Open", color = CodeforcesAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // ── Source code ──
            Card(
                colors = CardDefaults.cardColors(containerColor = CfCardSurface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Source code",
                            color = CfTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        if (state.source != null) {
                            IconButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(state.source.orEmpty()))
                                    Toast.makeText(context, "Code copied", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.ContentCopy,
                                    contentDescription = "Copy code",
                                    tint = CfTextSecondary,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    val send = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            state.source.orEmpty()
                                        )
                                    }
                                    context.startActivity(Intent.createChooser(send, "Share source code"))
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Share,
                                    contentDescription = "Share code",
                                    tint = CfTextSecondary,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    when {
                        // Still loading — either the direct fetch or the hidden
                        // WebView fallback is in flight.
                        state.sourceLoading || (state.source == null && !state.webViewGiveUp) -> {
                            val brush = rememberShimmerBrush()
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                SkeletonBox(brush, Modifier.fillMaxWidth().height(14.dp), cornerRadius = 4.dp)
                                SkeletonBox(brush, Modifier.fillMaxWidth(0.85f).height(14.dp), cornerRadius = 4.dp)
                                SkeletonBox(brush, Modifier.fillMaxWidth(0.92f).height(14.dp), cornerRadius = 4.dp)
                                SkeletonBox(brush, Modifier.fillMaxWidth(0.6f).height(14.dp), cornerRadius = 4.dp)
                            }
                        }
                        state.source != null -> {
                            SelectionContainer {
                                Text(
                                    state.source.orEmpty(),
                                    color = CfTextPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                )
                            }
                        }
                        else -> {
                            Text(
                                "Source code isn't available — Codeforces only shows it while you're signed in on the web session.",
                                color = CfTextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                            TextButton(
                                onClick = {
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://codeforces.com/contest/$contestId/submission/$submissionId")
                                        )
                                    )
                                }
                            ) {
                                Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Open on Codeforces")
                            }
                        }
                    }
                }
            }

            // ── Actions ──
            OutlinedButton(
                onClick = {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://codeforces.com/contest/$contestId/submission/$submissionId")
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text("Open on Codeforces", color = CfTextPrimary)
            }

            // Cloudflare challenge card — renew via the proven web login flow
            // (it refreshes the session cookies submit also depends on).
            if (state.needsCheck && state.source == null && !state.webViewGiveUp) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CodeforcesAccent.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Rounded.WarningAmber,
                                contentDescription = null,
                                tint = CodeforcesAccent,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                "Web session needs renewal",
                                color = CfTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Codeforces wants re-verification. Sign in through the browser once — it renews everything, including code submission. This screen reloads automatically when you return.",
                            color = CfTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { navController.navigate(Screen.WebLogin.route) },
                                colors = ButtonDefaults.buttonColors(containerColor = CodeforcesAccent)
                            ) {
                                Text("Renew session")
                            }
                            OutlinedButton(onClick = { viewModel.onRenewalDismissed() }) {
                                Text("Not now", color = CfTextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }

    // Cloudflare-safe source fetch: hidden WebView with the real browser
    // fingerprint + system cookie session. Polls patiently — silent
    // challenges auto-solve.
    if (isLoggedIn != false && state.source == null && !state.sourceLoading && !state.webViewGiveUp) {
        SourceWebView(
            contestId = contestId,
            submissionId = submissionId,
            userAgent = state.loginUa ?: WebSettings.getDefaultUserAgent(context),
            onSource = viewModel::onWebViewSource
        )
    }
}

// ── Source WebView (Cloudflare-safe) ──────────────────────────────────────────

private fun sourceUrl(contestId: String, submissionId: Long) =
    "https://codeforces.com/contest/$contestId/submission/$submissionId?locale=en"

private const val SOURCE_CHECK_JS =
    "(function(){var e=document.querySelector('#program-source-text');return e?e.textContent:'';})()"

/** evaluateJavascript returns a JSON-encoded string; decode it properly so
 *  newlines/quotes in the source survive. */
private fun decodeJsString(r: String?): String? = try {
    if (r.isNullOrBlank() || r == "null") null
    else org.json.JSONArray(r).optString(0).trim().ifBlank { null }
} catch (_: Exception) {
    null
}

/** Polls until the source element appears; after a quiet period reports null
 *  (challenge detected) but keeps watching — the challenge often self-solves,
 *  and the expanded manual check resolves it for good. */
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun SourceWebView(
    contestId: String,
    submissionId: Long,
    userAgent: String?,
    onSource: (String?) -> Unit
) {
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.userAgentString = userAgent
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                CookieManager.getInstance().setAcceptCookie(true)

                webViewClient = object : WebViewClient() {
                    private var redirectedOnce = false

                    override fun onPageFinished(view: WebView, url: String?) {
                        super.onPageFinished(view, url)
                        // If we got bounced to the login page, the user isn't
                        // logged in on the WebView side either — surface the
                        // renewal card immediately rather than waiting 45 s.
                        if (url != null && url.contains("codeforces.com") &&
                            (url.contains("/enter") || url.contains("/login"))
                        ) {
                            onSource(null)
                            return
                        }
                        // If we got bounced off the target page (Cloudflare
                        // redirect etc.), go back exactly once — and give any
                        // in-flight challenge plenty of time to clear itself.
                        if (!redirectedOnce && url != null && url.contains("codeforces.com") &&
                            !url.contains("/submission/")
                        ) {
                            redirectedOnce = true
                            view.postDelayed({ view.loadUrl(sourceUrl(contestId, submissionId)) }, 10_000)
                        }
                    }
                }

                loadUrl(sourceUrl(contestId, submissionId))

                // Patient poller: reads the DOM every 2.5s via JS — no page
                // reloads. Silent Cloudflare checks auto-solve undisturbed,
                // exactly like the hidden submit WebView.
                val handler = Handler(Looper.getMainLooper())
                var ticks = 0
                var nullStreak = 0
                var reportedChallenge = false
                val poller = object : Runnable {
                    override fun run() {
                        ticks++
                        evaluateJavascript(SOURCE_CHECK_JS) { r ->
                            val code = decodeJsString(r)
                            if (!code.isNullOrBlank()) {
                                onSource(code)
                            } else {
                                nullStreak++
                                // ~45s of nothing = likely an interactive
                                // challenge; surface the renewal card.
                                // Polling continues in case it still clears.
                                if (nullStreak >= 18 && !reportedChallenge) {
                                    reportedChallenge = true
                                    onSource(null)
                                }
                            }
                        }
                        if (ticks < 120) handler.postDelayed(this, 2500)
                    }
                }
                handler.postDelayed(poller, 2000)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .alpha(0f)
    )
}

@Composable
private fun StatChip(label: String, value: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CfBackground.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(value, color = CfTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Text(label, color = CfTextDisabled, fontSize = 10.sp)
    }
}

private fun formatMemory(bytes: Long): String = when {
    bytes >= 1024 * 1024 -> "%.1f MB".format(bytes / 1048576.0)
    bytes > 0 -> "${bytes / 1024} KB"
    else -> "—"
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
