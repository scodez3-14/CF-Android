package com.codeforces.app.ui.screens.problems

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
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

data class ProblemDetailUiState(
    val detail: ProblemDetail? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProblemDetailViewModel @Inject constructor(
    private val okHttpClient: OkHttpClient
) : ViewModel() {

    private val _state = MutableStateFlow(ProblemDetailUiState())
    val state: StateFlow<ProblemDetailUiState> = _state.asStateFlow()

    private val ua = "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.6422.165 Mobile Safari/537.36"

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

    /** Fetch the problem page HTML. Pre-escapes < > inside $$$ math blocks
     *  so Jsoup doesn't mangle them as HTML tags. */
    private fun fetchDoc(contestId: String, index: String): org.jsoup.nodes.Document {
        val urls = listOf(
            "https://codeforces.com/contest/$contestId/problem/$index?locale=en",
            "https://codeforces.com/problemset/problem/$contestId/$index?locale=en",
            "https://codeforces.com/gym/$contestId/problem/$index?locale=en"
        )
        var lastEx: Exception? = null
        for (url in urls) {
            try {
                val req = Request.Builder()
                    .url(url)
                    .header("User-Agent", ua)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Referer", "https://codeforces.com/")
                    .build()
                okHttpClient.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) throw Exception("HTTP ${resp.code}")
                    val raw = resp.body?.string() ?: throw Exception("Empty body")
                    // Pre-escape < > inside $$$ blocks before Jsoup parsing
                    val safe = raw.replace(Regex("""\$\$\$(.*?)\$\$\$""", RegexOption.DOT_MATCHES_ALL)) { m ->
                        val inner = m.groupValues[1].replace("<", "&lt;").replace(">", "&gt;")
                        "\$\$\$$inner\$\$\$"
                    }
                    val doc = Jsoup.parse(safe, url)
                    if (doc.selectFirst(".problem-statement") != null) return doc
                }
            } catch (e: Exception) { lastEx = e }
        }
        throw lastEx ?: Exception("Failed to fetch problem page")
    }

    /** Fetch a URL and return the parsed document. */
    private fun fetchUrl(url: String): org.jsoup.nodes.Document? {
        return try {
            val req = Request.Builder()
                .url(url)
                .header("User-Agent", ua)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Referer", "https://codeforces.com/")
                .build()
            okHttpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val raw = resp.body?.string() ?: return null
                // Pre-escape math blocks for editorial too
                val safe = raw.replace(Regex("""\$\$\$(.*?)\$\$\$""", RegexOption.DOT_MATCHES_ALL)) { m ->
                    val inner = m.groupValues[1].replace("<", "&lt;").replace(">", "&gt;")
                    "\$\$\$$inner\$\$\$"
                }
                Jsoup.parse(safe, url)
            }
        } catch (_: Exception) { null }
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

        // Try to scrape editorial
        val editorialHtml = scrapeEditorial(contestId)

        return ProblemDetail(
            name = name, contestId = contestId, index = index,
            rating = rating, tags = tags,
            timeLimit = timeLimit, memoryLimit = memLimit,
            statementHtml = statementHtml, samples = samples,
            editorialHtml = editorialHtml
        )
    }

    /** Scrape editorial: find the tutorial blog link from the contest page, then extract its content. */
    private fun scrapeEditorial(contestId: String): String? {
        // Try the contest page to find the editorial/tutorial link
        val contestDoc = fetchUrl("https://codeforces.com/contest/$contestId?locale=en") ?: return null

        // Look for links that say "Tutorial" or "Editorial"
        val links = contestDoc.select("a[href]")
        var blogUrl: String? = null
        for (link in links) {
            val text = link.text().lowercase()
            val href = link.attr("abs:href")
            if ((text.contains("tutorial") || text.contains("editorial")) && href.contains("/blog/entry/")) {
                blogUrl = if (href.contains("?")) "$href&locale=en" else "$href?locale=en"
                break
            }
        }
        if (blogUrl == null) return null

        // Fetch the blog page
        val blogDoc = fetchUrl(blogUrl) ?: return null

        // Extract the blog content
        val content = blogDoc.selectFirst(".ttypography")
            ?: blogDoc.selectFirst("#pageContent .content")
            ?: return null

        // Fix relative image URLs in editorial
        content.select("img[src]").forEach { img ->
            val src = img.attr("src")
            if (src.startsWith("/")) img.attr("src", "https://codeforces.com$src")
        }

        return content.outerHtml()
    }

    private fun extractPreContent(pre: org.jsoup.nodes.Element): String {
        val divLines = pre.select("div")
        if (divLines.isNotEmpty()) {
            return divLines.joinToString("\n") { it.text().trim() }
        }
        return pre.wholeText().replace("\u00a0", " ").trim()
    }
}