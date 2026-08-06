package com.newswire.data.remote

import com.newswire.data.model.Article
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class GoogleNewsRssParser {

    private val rfc1123 = DateTimeFormatter.RFC_1123_DATE_TIME
    private val tagRegex = Regex("</?[a-zA-Z][^>]*>")
    private val spaceRegex = Regex("\\s+")

    fun parse(xml: String): List<Article> {
        val parser = XmlPullParserFactory.newInstance().newPullParser()
        parser.setInput(xml.reader())

        var event = parser.eventType
        var inItem = false
        var inTitle = false
        var inLink = false
        var inPubDate = false
        var inDescription = false
        var inSource = false

        var title = ""
        var link = ""
        var pubDate = ""
        var description = ""
        var source = ""
        var sourceUrl = ""

        val articles = mutableListOf<Article>()

        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "item" -> {
                        inItem = true
                        title = ""
                        link = ""
                        pubDate = ""
                        description = ""
                        source = ""
                        sourceUrl = ""
                    }
                    "title" -> inTitle = inItem
                    "link" -> inLink = inItem
                    "pubDate" -> inPubDate = inItem
                    "description" -> inDescription = inItem
                    "source" -> {
                        if (inItem) {
                            inSource = true
                            sourceUrl = parser.getAttributeValue(null, "url") ?: ""
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    val text = parser.text ?: ""
                    when {
                        inTitle -> title += text
                        inLink -> link += text
                        inPubDate -> pubDate += text
                        inDescription -> description += text
                        inSource -> source += text
                    }
                }
                XmlPullParser.END_TAG -> when (parser.name) {
                    "title" -> inTitle = false
                    "link" -> inLink = false
                    "pubDate" -> inPubDate = false
                    "description" -> inDescription = false
                    "source" -> inSource = false
                    "item" -> {
                        if (inItem) {
                            articles += buildArticle(
                                title = title,
                                link = link,
                                pubDate = pubDate,
                                source = source,
                                sourceUrl = sourceUrl,
                                description = description,
                            )
                            inItem = false
                        }
                    }
                }
            }
            event = parser.next()
        }
        return articles
    }

    private fun buildArticle(
        title: String,
        link: String,
        pubDate: String,
        source: String,
        sourceUrl: String,
        description: String,
    ): Article {
        val sourceName = source.trim()
        val headline = extractHeadline(title, sourceName)
        return Article(
            title = headline,
            summary = stripHtml(description),
            link = link.trim(),
            source = sourceName.ifBlank { extractSourceFromTitle(title) },
            sourceUrl = sourceUrl.trim(),
            publishedAt = parseDate(pubDate),
        )
    }

    private fun extractHeadline(title: String, source: String): String {
        if (title.isBlank()) return ""
        return when {
            source.isNotBlank() && title.endsWith(" - $source") ->
                title.removeSuffix(" - $source").trim()
            else -> {
                val idx = title.lastIndexOf(" - ")
                if (idx > 0) title.substring(0, idx).trim() else title.trim()
            }
        }
    }

    private fun extractSourceFromTitle(title: String): String {
        val idx = title.lastIndexOf(" - ")
        return if (idx > 0) title.substring(idx + 3).trim() else ""
    }

    private fun stripHtml(html: String): String {
        if (html.isBlank()) return ""
        var s = tagRegex.replace(html, " ")
        s = s.replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
        return spaceRegex.replace(s, " ").trim()
    }

    private fun parseDate(raw: String): String {
        if (raw.isBlank()) return ""
        return try {
            ZonedDateTime.parse(raw.trim(), rfc1123).toInstant().toString()
        } catch (_: Exception) {
            ""
        }
    }
}
