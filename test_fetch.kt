import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import java.io.File

fun main() {
    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
        
    val url = "https://codeforces.com/contest/1/problem/A?locale=en"
    val ua = "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.6422.165 Mobile Safari/537.36"
    
    val request = Request.Builder()
        .url(url)
        .header("User-Agent", ua)
        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
        .header("Accept-Language", "en-US,en;q=0.9")
        .header("Connection", "keep-alive")
        .header("Referer", "https://codeforces.com/")
        .build()

    okHttpClient.newCall(request).execute().use { response ->
        val rawHtml = response.body?.string() ?: ""
        println("HTML Length: ${rawHtml.length}")
        println("Contains </head>? ${rawHtml.contains("</head>", ignoreCase = true)}")
        
        File("test_output.html").writeText(rawHtml)
    }
}
