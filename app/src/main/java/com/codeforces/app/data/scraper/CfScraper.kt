package com.codeforces.app.data.scraper

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.concurrent.ConcurrentHashMap

/**
 * Scrapes Codeforces problem/editorial HTML pages.
 *
 * Editorials load each problem's tutorial dynamically: the blog page ships a
 * `.problemTutorial` placeholder and a JS POST to `/data/problemTutorial`
 * (with `problemCode`) returns the real HTML. That endpoint requires the same
 * session cookies that the blog GET set, plus the page's `X-Csrf-Token`, so
 * this class keeps an in-memory cookie jar and reuses it for both requests.
 */
class CfScraper(okHttpClient: OkHttpClient) {

    private val client: OkHttpClient = okHttpClient.newBuilder()
        .cookieJar(InMemoryCookieJar())
        .build()

    private val ua = "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 " +
        "(KHTML, like Gecko) Chrome/125.0.6422.165 Mobile Safari/537.36"

    private val mathBlocks = Regex("""\$\$\$(.*?)\$\$\$""", RegexOption.DOT_MATCHES_ALL)

    /** Fetch a URL and parse it with Jsoup, pre-escaping < > inside $$$ math blocks. */
    fun fetchUrl(url: String): Document? {
        return try {
            val req = Request.Builder()
                .url(url)
                .header("User-Agent", ua)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Referer", "https://codeforces.com/")
                .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val raw = resp.body?.string() ?: return null
                Jsoup.parse(escapeMath(raw), url)
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Find the editorial blog entry from a contest page, fetch it and resolve all
     * dynamically-loaded problem tutorials. Returns the blog content HTML, or null
     * if no editorial link can be found.
     */
    fun fetchEditorial(contestId: String): String? {
        val blogUrl = findEditorialBlogUrl(contestId) ?: return null
        return fetchBlogEditorial(blogUrl)
    }

    /** Find the contest's editorial/blog entry URL (https://codeforces.com/blog/entry/...). */
    private fun findEditorialBlogUrl(contestId: String): String? {
        val contestDoc = fetchUrl("https://codeforces.com/contest/$contestId?locale=en") ?: return null
        for (link in contestDoc.select("a[href]")) {
            val text = link.text().lowercase()
            val href = link.attr("abs:href")
            if ((text.contains("tutorial") || text.contains("editorial")) && href.contains("/blog/entry/")) {
                return if (href.contains("?")) "$href&locale=en" else "$href?locale=en"
            }
        }
        return null
    }

    /** Fetch the blog page and replace every `.problemTutorial` placeholder with its real tutorial HTML. */
    private fun fetchBlogEditorial(blogUrl: String): String? {
        val blogDoc = fetchUrl(blogUrl) ?: return null
        val csrf = blogDoc.selectFirst("meta[name='X-Csrf-Token']")?.attr("content")

        blogDoc.select(".problemTutorial").forEach { placeholder ->
            val code = placeholder.attr("problemcode")
            if (code.isNotEmpty()) {
                val html = fetchTutorial(blogUrl, csrf, code)
                if (html != null) {
                    placeholder.html(escapeMath(html))
                } else {
                    placeholder.remove()
                }
            }
        }

        val content = blogDoc.selectFirst(".ttypography")
            ?: blogDoc.selectFirst("#pageContent .content")
            ?: return null

        content.select("img[src]").forEach { img ->
            val src = img.attr("src")
            if (src.startsWith("/")) img.attr("src", "https://codeforces.com$src")
        }

        return content.outerHtml()
    }

    /** POST to /data/problemTutorial and return the tutorial HTML for the given problem code. */
    private fun fetchTutorial(blogUrl: String, csrf: String?, problemCode: String): String? {
        if (csrf == null) return null
        return try {
            val req = Request.Builder()
                .url("https://codeforces.com/data/problemTutorial")
                .post("problemCode=$problemCode".toRequestBody(FORM))
                .header("User-Agent", ua)
                .header("X-Csrf-Token", csrf)
                .header("Referer", blogUrl)
                .header("X-Requested-With", "XMLHttpRequest")
                .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val body = resp.body?.string() ?: return null
                val json = JSONObject(body)
                if (json.optString("success") == "true") json.optString("html") else null
            }
        } catch (_: Exception) {
            null
        }
    }

    /** Escape < > inside $$$ math blocks so Jsoup doesn't treat them as HTML tags. */
    private fun escapeMath(raw: String): String {
        return mathBlocks.replace(raw) { m ->
            val inner = m.groupValues[1].replace("<", "&lt;").replace(">", "&gt;")
            "\$\$\$$inner\$\$\$"
        }
    }

    private companion object {
        val FORM: okhttp3.MediaType = "application/x-www-form-urlencoded; charset=utf-8".toMediaType()
    }
}

/** Minimal in-memory cookie jar so the blog GET and tutorial POST share a session. */
private class InMemoryCookieJar : CookieJar {

    private val cache = ConcurrentHashMap<String, MutableList<Cookie>>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val jar = cache.getOrPut(url.host) { mutableListOf() }
        cookies.forEach { cookie ->
            jar.removeAll { it.name == cookie.name }
            jar.add(cookie)
        }
        jar.removeAll { it.expiresAt < System.currentTimeMillis() }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val jar = cache[url.host] ?: return emptyList()
        jar.removeAll { it.expiresAt < System.currentTimeMillis() }
        return jar.filter { it.matches(url) }
    }
}
