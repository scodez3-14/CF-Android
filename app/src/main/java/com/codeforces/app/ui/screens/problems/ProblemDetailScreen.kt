package com.codeforces.app.ui.screens.problems

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// ─── Dark CSS for WebView ─────────────────────────────────────────────────────

private const val DARK_CSS = """
<style>
  :root {
    --bg: #151515;
    --surface: #1E1E1E;
    --border: #333333;
    --text: #E0E0E0;
    --muted: #9E9E9E;
    --mono: 'Roboto Mono', 'Courier New', monospace;
  }

  * { box-sizing: border-box; margin: 0; padding: 0; }

  html, body {
    background: var(--bg) !important;
    color: var(--text) !important;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
    font-size: 14.5px;
    line-height: 1.55;
    padding: 0 14px 16px;
    overflow-x: hidden;
    word-break: break-word;
  }

  /* ── Collapse Codeforces nested div sections ── */
  .problem-statement > div {
    margin: 0;
    padding: 0;
  }

  /* ── Problem header ── */
  .problem-statement > .header {
    text-align: center;
    margin: 10px 0 14px;
  }
  .problem-statement > .header .title {
    font-size: 17px;
    font-weight: 600;
    color: #FFFFFF;
    margin-bottom: 4px;
  }
  .problem-statement > .header .time-limit,
  .problem-statement > .header .memory-limit {
    display: block;
    font-size: 12.5px;
    color: var(--muted);
    margin: 1px 0;
  }
  .problem-statement > .header .input-file,
  .problem-statement > .header .output-file {
    display: none;
  }

  /* ── Section titles ── */
  .section-title {
    font-size: 14.5px;
    font-weight: 700;
    color: #FFFFFF;
    margin: 14px 0 4px;
  }

  /* ── Body paragraphs ── */
  .problem-statement p, p {
    margin: 4px 0;
    color: var(--text);
  }

  .section-title + p {
    margin-top: 4px;
  }

  /* ── Math (MathJax v2) ── */
  .MathJax, .MathJax_Display, .MathJax span {
    color: var(--text) !important;
  }
  .MathJax_Display {
    overflow-x: auto;
    margin: 8px 0 !important;
  }

  /* ── Images / Figures ── */
  img {
    max-width: 100%;
    border-radius: 6px;
    display: block;
    margin: 8px auto;
  }
  center {
    margin: 6px 0;
  }

  /* ── Tables ── */
  table {
    width: 100%;
    border-collapse: collapse;
    margin: 8px 0;
    font-size: 13px;
  }
  th, td {
    border: 1px solid var(--border);
    padding: 6px 10px;
    text-align: left;
    color: var(--text);
  }
  th { background: var(--surface); font-weight: 600; }

  /* ── Sample tests ── */
  .sample-tests {
    margin-top: 6px;
  }
  .sample-tests .section-title {
    margin-top: 14px;
    margin-bottom: 6px;
  }
  .sample-tests .sample-test {
    border: 1px solid #2B2B2B;
    border-radius: 6px;
    overflow: hidden;
    margin-bottom: 8px;
  }
  .sample-tests .input,
  .sample-tests .output {
    display: block;
  }
  .sample-tests .output {
    border-top: 1px solid #2B2B2B;
  }
  .sample-tests .input > .title,
  .sample-tests .output > .title {
    font-size: 12.5px;
    font-weight: 600;
    color: #A0A0A0;
    background: #1E1E1E;
    padding: 5px 12px;
    font-family: var(--mono);
  }
  .sample-tests pre {
    background: #121212;
    margin: 0;
    padding: 0;
    font-family: var(--mono);
    font-size: 13px;
    line-height: 1.45;
    overflow-x: auto;
    white-space: pre;
    color: #CCCCCC;
  }
  .sample-tests pre > div, .sample-tests pre > span {
    display: block;
    padding: 3px 12px;
    min-height: 18px;
  }
  .sample-tests pre > div:nth-child(even) {
    background: rgba(255, 255, 255, 0.03);
  }

  /* ── Inline code / <tt> ── */
  tt, code {
    font-family: var(--mono);
    background: rgba(255,255,255,0.08);
    border-radius: 4px;
    padding: 1px 4px;
    font-size: 0.9em;
    color: #E0E0E0;
  }

  /* ── Lists ── */
  ul, ol {
    padding-left: 20px;
    margin: 4px 0;
  }
  li { margin: 3px 0; }

  /* ── Note box ── */
  .note {
    margin-top: 8px;
  }
  .note .section-title {
    margin-top: 8px;
  }

  /* ── Links ── */
  a { color: #4FC3F7; }

  /* ── Spoilers (editorial) ── */
  .spoiler-content { display: block !important; }

  /* ── Scrollbar styling ── */
  ::-webkit-scrollbar { width: 5px; height: 4px; }
  ::-webkit-scrollbar-track { background: var(--bg); }
  ::-webkit-scrollbar-thumb { background: var(--border); border-radius: 4px; }
</style>
"""

// Codeforces's own MathJax v2 config — uses $$$ for inline math natively
private const val MATHJAX_SCRIPT = """
<script type="text/x-mathjax-config">
  MathJax.Hub.Config({
    tex2jax: {
      inlineMath: [['${'$'}${'$'}${'$'}','${'$'}${'$'}${'$'}']],
      displayMath: [['${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}','${'$'}${'$'}${'$'}${'$'}${'$'}${'$'}']],
      processEscapes: true
    },
    "HTML-CSS": { linebreaks: { automatic: true }, scale: 95 },
    SVG: { linebreaks: { automatic: true } },
    showProcessingMessages: false,
    messageStyle: "none"
  });
</script>
<script type="text/javascript" async
  src="https://codeforces.com/mathjax.codeforces.org/MathJax.js?config=TeX-AMS_HTML-full">
</script>
"""

fun buildProblemHtml(statementHtml: String): String {
    return """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=3.0">
  $DARK_CSS
  $MATHJAX_SCRIPT
</head>
<body>
  $statementHtml
</body>
</html>
""".trimIndent()
}

fun buildEditorialHtml(editorialHtml: String): String {
    return """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=3.0">
  $DARK_CSS
  $MATHJAX_SCRIPT
</head>
<body>
  $editorialHtml
</body>
</html>
""".trimIndent()
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProblemDetailScreen(
    contestId: String,
    index: String,
    name: String,
    onBack: () -> Unit,
    viewModel: ProblemDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Description", "Submit", "Submission", "Solution")

    LaunchedEffect(contestId, index) {
        viewModel.load(contestId, index, name)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Problem",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally).padding(end = 48.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Bookmark */ }) {
                        Icon(Icons.Rounded.BookmarkBorder, contentDescription = "Bookmark", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF151515))
            )
        },
        containerColor = Color(0xFF151515)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Tabs ──
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color(0xFF151515),
                contentColor = Color.White,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Color.White,
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
                                    color = if (selectedTabIndex == i) Color.White else Color(0xFF888888),
                                    fontWeight = if (selectedTabIndex == i) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                                if (title == "Submit") {
                                    Spacer(Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFE53935))
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

            HorizontalDivider(color = Color(0xFF2B2B2B), thickness = 1.dp)

            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                // ── Loading ──
                if (state.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp)
                    }
                }

                // ── Error ──
                if (state.error != null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Rounded.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(48.dp)
                            )
                            Text("Failed to load", color = Color.White, fontWeight = FontWeight.Bold)
                            Text(state.error ?: "", color = Color(0xFF888888), fontSize = 13.sp)
                            Button(
                                onClick = { viewModel.load(contestId, index, name) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }

                // ── Content ──
                val detail = state.detail
                if (detail != null && !state.isLoading && state.error == null) {
                    when (selectedTabIndex) {
                        0 -> {
                            // Description tab
                            CfWebView(
                                html = buildProblemHtml(detail.statementHtml),
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        3 -> {
                            // Solution / Editorial tab
                            val editorial = detail.editorialHtml
                            if (editorial != null) {
                                CfWebView(
                                    html = buildEditorialHtml(editorial),
                                    modifier = Modifier.fillMaxSize()
                                )
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
                                            "No editorial available",
                                            color = Color(0xFF888888),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            "Editorial for this contest\nhas not been published yet.",
                                            color = Color(0xFF555555),
                                            fontSize = 13.sp,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                        else -> {
                            // Submit / Submission tabs - placeholder
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    "Coming soon",
                                    color = Color(0xFF555555),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun CfWebView(html: String, modifier: Modifier = Modifier) {
    val webViewRef = remember { mutableStateOf<WebView?>(null) }
    val currentHtml by rememberUpdatedState(html)

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                settings.allowFileAccess = false
                setBackgroundColor(android.graphics.Color.parseColor("#151515"))

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest
                    ): Boolean {
                        val url = request.url.toString()
                        if (url.startsWith("http://") || url.startsWith("https://")) {
                            view.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            return true
                        }
                        return false
                    }
                }

                webViewRef.value = this
                loadDataWithBaseURL(
                    "https://codeforces.com",
                    currentHtml,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        },
        update = { webView ->
            // Only reload if the html actually changed
            if (webViewRef.value == webView) {
                webView.loadDataWithBaseURL(
                    "https://codeforces.com",
                    currentHtml,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        },
        modifier = modifier
    )
}
