package com.codeforces.app.data.scraper

import android.content.Context
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap

/**
 * Cookie jar that survives process death, so the Codeforces login session
 * (JSESSIONID) is kept between app launches. Cookies are persisted to
 * SharedPreferences as a flat "name|value|domain|path|expiresAt|..." string.
 */
class PersistentCookieJar(context: Context) : CookieJar {

    private val prefs = context.getSharedPreferences("cf_session_cookies", Context.MODE_PRIVATE)
    private val cache = ConcurrentHashMap<String, Cookie>()

    init {
        load()
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookies.forEach { cookie ->
            cache["${cookie.name}@${cookie.domain}"] = cookie
        }
        prune()
        persist()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        prune()
        val now = System.currentTimeMillis()
        return cache.values
            .filter { it.expiresAt == Long.MAX_VALUE || it.expiresAt > now }
            .filter { it.matches(url) }
    }

    fun clear() {
        cache.clear()
        persist()
    }

    /**
     * Import cookies from an Android WebView session. [header] is the string
     * returned by `CookieManager.getCookie(url)`, i.e. `name=value; name=value`.
     * This bridges a browser-based Codeforces login into the OkHttp session so
     * [CfSubmitter] can submit with it.
     */
    fun importCookieHeader(header: String) {
        header.split(";").forEach { piece ->
            val idx = piece.indexOf('=')
            if (idx <= 0) return@forEach
            val name = piece.substring(0, idx).trim()
            val value = piece.substring(idx + 1).trim()
            if (name.isEmpty()) return@forEach
            try {
                cache["$name@codeforces.com"] = Cookie.Builder()
                    .name(name)
                    .value(value)
                    .domain("codeforces.com")
                    .path("/")
                    .expiresAt(Long.MAX_VALUE)
                    .build()
            } catch (_: Exception) {
                // Ignore malformed cookies (values with illegal chars, etc.)
            }
        }
        persist()
    }

    private fun prune() {
        val now = System.currentTimeMillis()
        cache.entries.removeAll { it.value.expiresAt != Long.MAX_VALUE && it.value.expiresAt <= now }
    }

    // ── Persistence ──────────────────────────────────────────────────────────

    private fun persist() {
        prefs.edit()
            .putStringSet("cookies", cache.values.map { it.encode() }.toSet())
            .apply()
    }

    private fun load() {
        prefs.getStringSet("cookies", emptySet())
            ?.forEach { line -> decode(line)?.let { cache["${it.name}@${it.domain}"] = it } }
    }

    private fun Cookie.encode(): String = listOf(
        name, value, domain, path, expiresAt.toString(),
        secure.toString(), httpOnly.toString(), hostOnly.toString()
    ).joinToString("|") { it.replace("|", "%7C") }

    private fun decode(line: String): Cookie? {
        return try {
            val p = line.split("|").map { it.replace("%7C", "|") }
            if (p.size != 8) return null
            val builder = Cookie.Builder()
                .name(p[0])
                .value(p[1])
                .path(p[3])
                .expiresAt(p[4].toLong())
            if (p[7].toBoolean()) builder.hostOnlyDomain(p[2]) else builder.domain(p[2])
            if (p[5].toBoolean()) builder.secure()
            if (p[6].toBoolean()) builder.httpOnly()
            builder.build()
        } catch (_: Exception) {
            null
        }
    }
}
