@file:Repository("https://repo1.maven.org/maven2/")
@file:DependsOn("org.jsoup:jsoup:1.17.2")

import org.jsoup.Jsoup

val html = "<div>$$$i < n$$$ and $$$a > b$$$</div>"
val doc = Jsoup.parse(html)
println(doc.body().outerHtml())
