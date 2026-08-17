package com.codeforces.app.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

// ─── CSS for WebView (theme-aware, matching the app theme) ──────────────────

private fun cfCss(isDark: Boolean): String {
    // Light values are chosen for ≥4.5:1 contrast on white. CF formula
    // images are black-on-transparent, so they only invert in dark mode.
    val themeVars = if (isDark) """
    --bg: #151515;
    --surface: #1E1E1E;
    --border: #333333;
    --text: #E0E0E0;
    --muted: #9E9E9E;
    --title: #FFFFFF;
    --code-bg: #101010;
    --code-text: #E0E0E0;
    --inline-code-bg: rgba(255, 255, 255, 0.08);
    --zebra: rgba(255, 255, 255, 0.03);
    --link: #4FC3F7;
    --formula-filter: var(--formula-filter);
""" else """
    --bg: #FFFFFF;
    --surface: #F2F4F3;
    --border: #E0E0E0;
    --text: #1F2422;
    --muted: #5F6C69;
    --title: #14181A;
    --code-bg: #F6F8F7;
    --code-text: #24302C;
    --inline-code-bg: rgba(0, 0, 0, 0.06);
    --zebra: rgba(0, 0, 0, 0.03);
    --link: #0277BD;
    --formula-filter: none;
"""
    return """
<style>
  :root {
    $themeVars
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
    word-break: normal;
  }

  /* ── Math: force a readable color on every math form Codeforces serves ──
     (.tex-span = pre-rendered text math, .MathJax = typeset TeX, <math> = MathML).
     MathJax output and MathML can carry their own black inline colors, which are
     invisible on this dark background, so override them all. */
  .tex-span, .tex-span *,
  .MathJax, .MathJax *,
  .MathJax_Display, .MathJax_Display *,
  .MathJax_Preview,
  math, math *,
  .upper-index, .lower-index {
    color: var(--text) !important;
    background-color: transparent !important;
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
    color: var(--title);
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
    color: var(--title);
    margin: 14px 0 4px;
  }

  /* ── Body paragraphs ── */
  .problem-statement p, p {
    margin: 4px 0;
    color: var(--text);
  }
  p, li, td { overflow-wrap: anywhere; }

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

  /* Codeforces pre-rendered formula images (class="tex-formula") are black
     glyphs on a transparent background — invisible on dark. Invert them so
     the math reads as light text. They are pure grayscale, so invert is safe. */
  img.tex-formula {
    filter: var(--formula-filter);
    display: inline !important;
    vertical-align: middle;
  }

  /* ── Tables ── */
  table {
    display: block;
    max-width: 100%;
    overflow-x: auto;
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
    border: 1px solid var(--border);
    border-radius: 6px;
    overflow: hidden;
    margin-bottom: 8px;
  }
  .sample-tests .input,
  .sample-tests .output {
    display: block;
  }
  .sample-tests .output {
    border-top: 1px solid var(--border);
  }
  .sample-tests .input > .title,
  .sample-tests .output > .title {
    font-size: 12.5px;
    font-weight: 600;
    color: var(--muted);
    background: var(--surface);
    padding: 5px 12px;
    font-family: var(--mono);
  }
  .sample-tests pre {
    background: var(--code-bg);
    margin: 0;
    padding: 0;
    font-family: var(--mono);
    font-size: 13px;
    line-height: 1.45;
    overflow-x: auto;
    white-space: pre;
    color: var(--code-text);
  }

  /* ── Tutorial source code ── */
  pre {
    display: block;
    max-width: 100%;
    overflow-x: auto !important;
    overflow-y: hidden;
    white-space: pre !important;
    word-break: normal !important;
    overflow-wrap: normal !important;
    -webkit-overflow-scrolling: touch;
    background: var(--code-bg);
    border: 1px solid var(--border);
    border-radius: 8px;
    margin: 10px 0;
    padding: 12px;
    color: var(--code-text);
    font-family: var(--mono);
    font-size: 12.5px;
    line-height: 1.5;
  }
  pre code {
    display: block;
    width: max-content;
    min-width: 100%;
    padding: 0;
    background: transparent;
    border-radius: 0;
    white-space: pre !important;
  }
  .sample-tests pre > div, .sample-tests pre > span {
    display: block;
    padding: 3px 12px;
    min-height: 18px;
  }
  .sample-tests pre > div:nth-child(even) {
    background: var(--zebra);
  }

  /* ── Inline code / <tt> ── */
  tt, code {
    font-family: var(--mono);
    background: var(--inline-code-bg);
    border-radius: 4px;
    padding: 1px 4px;
    font-size: 0.9em;
    color: var(--code-text);
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
  a { color: var(--link); }

  /* ── Spoilers (editorial) ── */
  .spoiler { margin: 8px 0; }
  .spoiler-title {
    display: block;
    font-weight: 700;
    color: var(--title);
    margin: 12px 0 6px;
    font-size: 15px;
  }
  .spoiler-content { display: block !important; }
  .problemTutorial { display: block; }
  .problemTutorial > h3 {
    font-size: 15px;
    font-weight: 700;
    color: var(--title);
    margin: 10px 0 6px;
  }

  /* ── Scrollbar styling ── */
  ::-webkit-scrollbar { width: 5px; height: 4px; }
  ::-webkit-scrollbar-track { background: var(--bg); }
  ::-webkit-scrollbar-thumb { background: var(--border); border-radius: 4px; }
</style>
"""
}

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
  ${cfCss(com.codeforces.app.ui.theme.CfThemeState.isDark)}
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
  ${cfCss(com.codeforces.app.ui.theme.CfThemeState.isDark)}
  $MATHJAX_SCRIPT
</head>
<body>
  $editorialHtml
</body>
</html>
""".trimIndent()
}

@SuppressLint("SetJavaScriptEnabled")
@androidx.compose.runtime.Composable
fun CfWebView(html: String, modifier: Modifier = Modifier) {
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
                isHorizontalScrollBarEnabled = true
                setBackgroundColor(android.graphics.Color.parseColor(if (com.codeforces.app.ui.theme.CfThemeState.isDark) "#151515" else "#FFFFFF"))

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
                tag = currentHtml
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
            // AndroidView calls update after construction too; avoid rendering the
            // same (potentially large) statement a second time.
            if (webView.tag != currentHtml) {
                webView.tag = currentHtml
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
