package com.codeforces.app.data.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

data class ReleaseInfo(
    val tagName: String,       // e.g. "v1.2.0"
    val name: String,          // release title
    val body: String,          // changelog / release notes
    val htmlUrl: String,       // browser link to the release page
    val publishedAt: String
)

@Singleton
class UpdateChecker @Inject constructor() {

    /**
     * Replace with your real GitHub owner/repo, e.g. "santu/codeforces-app".
     * The checker compares [currentVersionName] (from BuildConfig.VERSION_NAME)
     * against the tag returned by GitHub and considers the release "new" when
     * they differ (ignoring a leading "v").
     */
    companion object {
        // ── ⚠ Change this to your actual GitHub repo ──────────────────────────
        private const val OWNER = "scodez3-14"         // your GitHub username
        private const val REPO  = "CF-Android"   // your GitHub repository name
        // ─────────────────────────────────────────────────────────────────────
        private const val API_URL =
            "https://api.github.com/repos/$OWNER/$REPO/releases/latest"
    }

    /**
     * Returns [ReleaseInfo] when a newer version is available, null otherwise.
     * Never throws; returns null on any network/parse failure.
     */
    suspend fun checkForUpdate(currentVersionName: String): ReleaseInfo? =
        withContext(Dispatchers.IO) {
            try {
                val conn = URL(API_URL).openConnection()
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json")
                conn.connectTimeout = 8_000
                conn.readTimeout   = 8_000
                val text = conn.getInputStream().bufferedReader().readText()
                val json = JSONObject(text)

                val tag         = json.optString("tag_name", "")
                val name        = json.optString("name", tag)
                val body        = json.optString("body", "")
                val htmlUrl     = json.optString("html_url", "")
                val publishedAt = json.optString("published_at", "")

                if (tag.isBlank()) return@withContext null

                // Strip leading "v" from both sides before comparing
                val remote  = tag.trimStart('v')
                val current = currentVersionName.trimStart('v')

                if (remote == current) null
                else ReleaseInfo(tag, name, body, htmlUrl, publishedAt)
            } catch (_: Exception) {
                null
            }
        }
}
