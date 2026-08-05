package com.codeforces.app.ui.screens.login

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codeforces.app.ui.theme.*
import kotlinx.coroutines.delay

private const val CF_ENTER_URL = "https://codeforces.com/enter?locale=en"
private const val CF_COOKIE_URL = "https://codeforces.com"

private val PROFILE_URL_REGEX = Regex("""/profile/([^/?]+)""")

private const val LOGIN_DETECT_JS =
    "(function(){" +
        "try{" +
        "var out={logged:false,handle:''};" +
        "if(document.querySelector('a[href=\"/logout\"]')){" +
        "out.logged=true;" +
        "var p=document.querySelector('#header a[href*=\"/profile/\"]')||document.querySelector('a[href*=\"/profile/\"]');" +
        "if(p){var m=(p.getAttribute('href')||'').match(/\\/profile\\/([^/\\?]+)/);" +
        "if(m)out.handle=m[1];}" +
        "}" +
        "out.url=location.href;" +
        "return JSON.stringify(out);" +
        "}catch(e){return '{\"logged\":false,\"handle\":\"\"}';}" +
        "})()"

private const val DESKTOP_UA = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 " +
    "(KHTML, like Gecko) Chrome/125.0.6422.165 Safari/537.36"

private fun isChallengePage(url: String?): Boolean {
    val u = url?.lowercase() ?: return false
    return "challenge" in u || "cf-chl" in u || "cdn-cgi" in u ||
        "challenges.cloudflare.com" in u
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebLoginScreen(
    onLoggedIn: () -> Unit,
    onBack: () -> Unit,
    viewModel: WebLoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var desktopMode by remember { mutableStateOf(false) }
    var reloadTrigger by remember { mutableIntStateOf(0) }
    var onChallenge by remember { mutableStateOf(false) }
    var challengeCount by remember { mutableIntStateOf(0) }
    var enterLoads by remember { mutableIntStateOf(0) }

    // The UA the WebView is currently presenting. Must stay in sync with
    // CfSubmitter so the cf_clearance cookie matches.
    val mobileUa = remember { WebSettings.getDefaultUserAgent(context) }
    val activeUa = if (desktopMode) DESKTOP_UA else mobileUa
    val currentUa by rememberUpdatedState(activeUa)

    // Poll the WebView cookies so a login that doesn't trigger a clean
    // onPageFinished is still detected.
    LaunchedEffect(Unit) {
        while (!state.isLoggedIn) {
            val cookies = CookieManager.getInstance().getCookie(CF_COOKIE_URL)
            viewModel.checkSession(cookies, currentUa)
            delay(2000)
        }
    }

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) {
            delay(1200)
            onLoggedIn()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Codeforces Login", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { desktopMode = !desktopMode }) {
                        Icon(
                            imageVector = if (desktopMode) Icons.Rounded.Computer else Icons.Rounded.Smartphone,
                            contentDescription = if (desktopMode) "Switch to mobile mode" else "Switch to desktop mode"
                        )
                    }
                    IconButton(onClick = { reloadTrigger++ }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Reload")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CfSurface)
            )
        },
        containerColor = CfBackground
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            Column(Modifier.fillMaxSize()) {
                Text(
                    "Sign in with your Codeforces handle in the browser below. " +
                        "The session is kept so the app can submit code on your behalf.",
                    color = CfTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)
                )

                if (onChallenge) {
                    Surface(
                        color = CodeforcesAccent.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                Icons.Rounded.WarningAmber,
                                contentDescription = null,
                                tint = CodeforcesAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Codeforces is showing a security check. Tap the " +
                                    "\"Verify you are human\" box and complete it manually. " +
                                    "It may ask a few times — just keep completing it.",
                                color = CfTextPrimary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                if (challengeCount >= 2 && !state.isLoggedIn) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CfCardSurface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text(
                                "Cloudflare keeps asking you to verify.",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = CfTextPrimary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                if (desktopMode) {
                                    "Desktop mode didn't help. Try reloading, or switch back to mobile mode."
                                } else {
                                    "Embedded browsers get challenged repeatedly. " +
                                        "Switch to desktop mode — it usually stops the loop."
                                },
                                color = CfTextSecondary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                            Spacer(Modifier.height(10.dp))
                            Row {
                                Button(
                                    onClick = { desktopMode = !desktopMode },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CodeforcesAccent)
                                ) {
                                    Icon(
                                        if (desktopMode) Icons.Rounded.Smartphone else Icons.Rounded.Computer,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(if (desktopMode) "Use mobile mode" else "Use desktop mode")
                                }
                                Spacer(Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = { reloadTrigger++ },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Reload")
                                }
                            }
                        }
                    }
                }

                LoginWebView(
                    desktopMode = desktopMode,
                    reloadTrigger = reloadTrigger,
                    onSessionUpdate = { header -> viewModel.checkSession(header, activeUa) },
                    onLoginDetected = { handle ->
                        viewModel.confirmBrowserLogin(
                            CookieManager.getInstance().getCookie(CF_COOKIE_URL),
                            handle,
                            activeUa
                        )
                    },
                    onPageFinished = { url ->
                        if (isChallengePage(url)) {
                            onChallenge = true
                            challengeCount++
                        } else if (url.contains("enter") || url.contains("login")) {
                            // Turnstile is embedded inside /enter, so the URL never
                            // changes while Cloudflare re-verifies. Repeated reloads
                            // of the same login page = verification loop.
                            enterLoads++
                            onChallenge = enterLoads >= 2
                            if (enterLoads >= 3) challengeCount = maxOf(challengeCount, 2)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
            }

            if (state.isLoggedIn) {
                Surface(
                    color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.75f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Rounded.VerifiedUser,
                            contentDescription = null,
                            tint = VerdictOK,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Signed in as ${state.handle ?: "Codeforces user"}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Returning to the app…", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun LoginWebView(
    desktopMode: Boolean,
    reloadTrigger: Int,
    onSessionUpdate: (String?) -> Unit,
    onLoginDetected: (String?) -> Unit,
    onPageFinished: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var lastReload by remember { mutableIntStateOf(0) }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.databaseEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                // Use the real WebView UA. Spoofing a Chrome UA makes the
                // fingerprint mismatch (userAgentData reports "WebView"), which
                // is what causes Cloudflare's endless re-verification.
                settings.userAgentString = WebSettings.getDefaultUserAgent(ctx)
                setBackgroundColor(AndroidColor.parseColor("#121212"))

                CookieManager.getInstance().setAcceptCookie(true)
                CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest
                    ): Boolean {
                        return false
                    }

                    override fun onPageFinished(view: WebView, url: String?) {
                        super.onPageFinished(view, url)
                        val pageUrl = url ?: ""
                        onPageFinished(pageUrl)
                        val cookies = CookieManager.getInstance().getCookie(CF_COOKIE_URL)
                        Log.d("CFLOGIN", "onPageFinished url=$pageUrl cookies=${cookies?.length}")
                        onSessionUpdate(cookies)
                        // Codeforces redirects a logged-in user away from /enter to
                        // their profile page — the handle is right in the URL.
                        val profileMatch = PROFILE_URL_REGEX.find(pageUrl)
                        if (profileMatch != null) {
                            onLoginDetected(profileMatch.groupValues[1])
                            return
                        }
                        detectLoginInPage(view, cookies)
                    }

                    private fun detectLoginInPage(view: WebView, cookies: String?) {
                        // Evaluate after the page has settled: running it while a
                        // redirect is in flight returns null.
                        view.postDelayed({
                            if (view.url == null) return@postDelayed
                            view.evaluateJavascript(LOGIN_DETECT_JS) { result ->
                                Log.d("CFLOGIN", "evalResult=$result")
                                try {
                                    val obj = org.json.JSONObject(result)
                                    if (obj.optBoolean("logged")) {
                                        onLoginDetected(obj.optString("handle").ifBlank { null })
                                    }
                                } catch (_: Exception) {
                                }
                            }
                        }, 500L)
                    }
                }

                loadUrl(CF_ENTER_URL)
            }
        },
        update = { view ->
            // Switch browser fingerprint when the user toggles mode. A real
            // desktop Chrome fingerprint is far less likely to loop Cloudflare.
            val target = if (desktopMode) DESKTOP_UA else WebSettings.getDefaultUserAgent(view.context)
            if (view.settings.userAgentString != target) {
                view.settings.userAgentString = target
                view.reload()
            }
            if (reloadTrigger != lastReload) {
                lastReload = reloadTrigger
                view.reload()
            }
        },
        modifier = modifier
    )
}
