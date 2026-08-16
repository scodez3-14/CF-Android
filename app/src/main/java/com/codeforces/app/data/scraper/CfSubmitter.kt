package com.codeforces.app.data.scraper

import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import android.util.Log
import org.jsoup.Jsoup

/**
 * Codeforces web-session client: authenticates with a real handle/password and
 * submits solutions through the website's forms using the resulting session
 * cookies (kept in a shared [PersistentCookieJar] so the login survives restarts).
 */
class CfSubmitter(
    okHttpClient: OkHttpClient,
    private val cookieJar: CookieJar,
    initialUserAgent: String
) {
    private val client: OkHttpClient = okHttpClient.newBuilder()
        .cookieJar(cookieJar)
        .build()

    /**
     * Must exactly match the User-Agent the login WebView used, because
     * Codeforces' `cf_clearance` cookie is bound to that UA.
     */
    @Volatile
    var userAgent: String = initialUserAgent
        private set

    private val initialUserAgent: String = initialUserAgent

    fun setUserAgent(ua: String) {
        if (ua.isNotBlank()) userAgent = ua
    }

    /** Drop back to the device's default WebView UA. */
    fun resetUserAgent() {
        userAgent = initialUserAgent
    }

    data class SubmitPage(
        val csrfToken: String,
        val tta: Long,
        val languages: List<Pair<String, String>> // (id, label)
    )

    data class SubmitResult(val success: Boolean, val message: String)

    data class LoginResult(val success: Boolean, val message: String)

    private val loginErrorPatterns = listOf(
        "invalid handle or email",
        "incorrect password",
        "too many requests",
        "captcha",
        "must be logged",
        "access is denied",
        "login failed"
    )

    private val errorPatterns = listOf(
        "Login failed",
        "Incorrect password",
        "exactly the same code",
        "too many submissions",
        "Problem index is not correct",
        "Source code should satisfy the rules",
        "You have not solved",
        "Unknown error",
        "must be logged",
        "access is denied"
    )

    /** GET the submit page and parse the form tokens + language list. */
    fun fetchSubmitPage(contestId: String): SubmitPage? {
        return try {
            val req = Request.Builder()
                .url("https://codeforces.com/contest/$contestId/submit?locale=en")
                .header("User-Agent", userAgent)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Referer", "https://codeforces.com/contest/$contestId")
                .build()
            client.newCall(req).execute().use { resp ->
                val code = resp.code
                if (!resp.isSuccessful) {
                    Log.d("CFLOGIN", "fetchSubmitPage http=$code")
                    return null
                }
                val html = resp.body?.string() ?: return null
                val doc = Jsoup.parse(html)
                val csrf = doc.selectFirst("meta[name='X-Csrf-Token']")?.attr("content")
                    ?: doc.selectFirst("input[name='csrf_token']")?.attr("value")
                val tta = extractTta(html)
                Log.d("CFLOGIN", "fetchSubmitPage http=$code csrf=${csrf != null} tta=$tta langs=${doc.select("#programTypeId option").size}")
                if (csrf.isNullOrBlank()) return null
                val languages = doc.select("#programTypeId option").mapNotNull { option ->
                    val value = option.attr("value")
                    if (value.isNotBlank()) value to option.text() else null
                }
                SubmitPage(csrf, tta, languages)
            }
        } catch (e: Exception) {
            Log.d("CFLOGIN", "fetchSubmitPage exception: ${e.message}")
            null
        }
    }

    /** Codeforces computes `_tta` as a simple `a*b+c` expression on the page. */
    private fun extractTta(html: String): Long {
        // _tta is the LAST `a*b+c` expression in the page's JS.
        val matches = Regex("""[0-9]+\*[0-9]+[+-][0-9]+""").findAll(html)
        val match = matches.lastOrNull() ?: return 0L
        val expr = match.value
        return try {
            val a = expr.substringBefore('*').trim().toLong()
            val rest = expr.substringAfter('*').trim()
            val op = if ('+' in rest) '+' else '-'
            val b = rest.substringBefore(op).trim().toLong()
            val c = rest.substringAfter(op).trim().toLong()
            if (op == '+') a * b + c else a * b - c
        } catch (_: Exception) {
            0L
        }
    }

    /** GET the login page and return its (csrf token, _tta). */
    private fun fetchEnterPage(): Pair<String, Long>? {
        return try {
            val req = Request.Builder()
                .url("https://codeforces.com/enter?locale=en")
                .header("User-Agent", userAgent)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val html = resp.body?.string() ?: return null
                val doc = Jsoup.parse(html)
                val csrf = doc.selectFirst("meta[name='X-Csrf-Token']")?.attr("content")
                    ?: doc.selectFirst("input[name='csrf_token']")?.attr("value")
                if (csrf.isNullOrBlank()) return null
                csrf to extractTta(html)
            }
        } catch (_: Exception) {
            null
        }
    }

    /** Sign in with a real Codeforces handle/email and password. */
    fun login(handleOrEmail: String, password: String): LoginResult {
        return try {
            val page = fetchEnterPage()
                ?: return LoginResult(
                    false,
                    "Could not load login page (Codeforces blocked the request). " +
                        "Try signing in with the browser instead."
                )
            val body = FormBody.Builder()
                .add("action", "enter")
                .add("handleOrEmail", handleOrEmail)
                .add("password", password)
                .add("remember", "on")
                .add("csrf_token", page.first)
                .add("_tta", page.second.toString())
                .add("ftaa", "")
                .add("bfaa", "")
                .build()
            val req = Request.Builder()
                .url("https://codeforces.com/enter")
                .post(body)
                .header("User-Agent", userAgent)
                .header("Referer", "https://codeforces.com/enter")
                .header("Origin", "https://codeforces.com")
                .header("Accept-Language", "en-US,en;q=0.9")
                .build()
            client.newCall(req).execute().use { resp ->
                val html = resp.body?.string() ?: return LoginResult(false, "Empty response")
                if (isLoggedIn()) {
                    LoginResult(true, "Signed in")
                } else {
                    val lowered = html.lowercase()
                    val error = loginErrorPatterns.firstOrNull { lowered.contains(it) }
                    LoginResult(false, error ?: "Login failed")
                }
            }
        } catch (e: Exception) {
            LoginResult(false, e.message ?: "Network error")
        }
    }

    /** True if the shared cookie jar currently holds a valid CF session. */
    fun isLoggedIn(): Boolean {
        return try {
            val req = Request.Builder()
                .url("https://codeforces.com/")
                .header("User-Agent", userAgent)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    Log.d("CFLOGIN", "isLoggedIn http=${resp.code}")
                    return false
                }
                val doc = Jsoup.parse(resp.body?.string() ?: return false)
                doc.selectFirst("a[href='/logout']") != null
            }
        } catch (e: Exception) {
            Log.d("CFLOGIN", "isLoggedIn exception: ${e.message}")
            false
        }
    }

    /** Kill the server-side session and drop the stored cookies. */
    fun logout() {
        try {
            client.newCall(
                Request.Builder()
                    .url("https://codeforces.com/logout")
                    .header("User-Agent", userAgent)
                    .header("Referer", "https://codeforces.com/")
                    .build()
            ).execute().close()
        } catch (_: Exception) {
            // Best effort; still clear local cookies below.
        }
        (cookieJar as? PersistentCookieJar)?.clear()
    }

    /**
     * Bridge cookies from an Android WebView login (which can pass Codeforces'
     * Cloudflare challenge) into this session's persistent cookie jar.
     */
    fun importCookies(cookieHeader: String) {
        (cookieJar as? PersistentCookieJar)?.importCookieHeader(cookieHeader)
    }

    /**
     * Source code of a submission page. Viewing source requires the signed-in
     * session this class holds, so it lives here rather than in [CfScraper].
     */
    fun fetchSubmissionSource(contestId: String, submissionId: Long): String? {
        val urls = listOf(
            "https://codeforces.com/contest/$contestId/submission/$submissionId?locale=en",
            "https://codeforces.com/gym/$contestId/submission/$submissionId?locale=en"
        )
        for (url in urls) {
            try {
                val req = Request.Builder()
                    .url(url)
                    .header("User-Agent", userAgent)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .build()
                client.newCall(req).execute().use { resp ->
                    Log.d("CFLOGIN", "submissionSource http=${resp.code} gym=${url.contains("/gym/")} uaOK=${userAgent == initialUserAgent}")
                    if (!resp.isSuccessful) return@use
                    val html = resp.body?.string() ?: return@use
                    val doc = Jsoup.parse(html)
                    val code = doc.selectFirst("#program-source-text")?.wholeText()?.trim()
                    Log.d(
                        "CFLOGIN",
                        "submissionSource element=${code != null} len=${code?.length ?: 0} " +
                            "loggedIn=${doc.selectFirst("a[href='/logout']") != null} pageBytes=${html.length}"
                    )
                    if (!code.isNullOrEmpty()) return code
                }
            } catch (_: Exception) {
            }
        }
        return null
    }

    /** The logged-in handle, parsed from the homepage header, or null. */
    fun currentHandle(): String? {
        return try {
            val req = Request.Builder()
                .url("https://codeforces.com/")
                .header("User-Agent", userAgent)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val doc = Jsoup.parse(resp.body?.string() ?: return null)
                val link = doc.selectFirst("#header a[href*='/profile/']")
                    ?: doc.selectFirst("a[href*='/profile/']") ?: return null
                Regex("""/profile/([^/?]+)""").find(link.attr("abs:href"))?.groupValues?.get(1)
            }
        } catch (_: Exception) {
            null
        }
    }

    fun submit(
        contestId: String,
        problemIndex: String,
        programTypeId: String,
        source: String,
        csrfToken: String,
        tta: Long
    ): SubmitResult {
        return try {
            val body = FormBody.Builder()
                .add("action", "submitSolutionFormSubmitted")
                .add("source", source)
                .add("sourceFile", "")
                .add("tabSize", "4")
                .add("contestId", contestId)
                .add("submittedProblemCode", problemIndex)
                .add("programTypeId", programTypeId)
                .add("csrf_token", csrfToken)
                .add("_tta", tta.toString())
                .build()
            val req = Request.Builder()
                .url("https://codeforces.com/contest/$contestId/submit")
                .post(body)
                .header("User-Agent", userAgent)
                .header("Referer", "https://codeforces.com/contest/$contestId/submit")
                .header("Origin", "https://codeforces.com")
                .header("Accept-Language", "en-US,en;q=0.9")
                .build()
            client.newCall(req).execute().use { resp ->
                val html = resp.body?.string() ?: return SubmitResult(false, "Empty response")
                val lowered = html.lowercase()
                val error = errorPatterns.firstOrNull { lowered.contains(it.lowercase()) }
                if (error != null) {
                    SubmitResult(false, error)
                } else {
                    SubmitResult(true, "Submitted")
                }
            }
        } catch (e: Exception) {
            SubmitResult(false, e.message ?: "Network error")
        }
    }
}
