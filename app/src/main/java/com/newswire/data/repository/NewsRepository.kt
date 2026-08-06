package com.newswire.data.repository

import com.newswire.data.model.Article
import com.newswire.data.remote.GoogleNewsRssParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

class NewsFetchException(message: String) : Exception(message)

@Singleton
class NewsRepository @Inject constructor(
    private val client: OkHttpClient,
) {

    private val parser = GoogleNewsRssParser()
    private val ogImageRegex = Regex(
        """property=["']og:image["']\s+content=["']([^"']+)["']""",
        RegexOption.IGNORE_CASE,
    )
    private val twitterImageRegex = Regex(
        """name=["']twitter:image(?::src)?["']\s+content=["']([^"']+)["']""",
        RegexOption.IGNORE_CASE,
    )

    suspend fun getHeadlines(query: String): List<Article> = withContext(Dispatchers.IO) {
        val url = if (query.isBlank()) {
            "https://news.google.com/rss?hl=en-LK&gl=LK&ceid=LK:en"
        } else {
            val encoded = URLEncoder.encode(query, "UTF-8")
            "https://news.google.com/rss/search?q=$encoded&hl=en-LK&gl=LK&ceid=LK:en"
        }
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw NewsFetchException("Feed error (${response.code})")
            }
            val body = response.body?.string() ?: throw NewsFetchException("Empty feed")
            parser.parse(body)
        }
    }

    suspend fun fetchImageUrl(link: String): String? = withContext(Dispatchers.IO) {
        if (link.isBlank()) return@withContext null
        try {
            val request = Request.Builder()
                .url(link)
                .header("User-Agent", USER_AGENT)
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val html = readUpTo(response, MAX_PAGE_BYTES) ?: return@use null
                val og = ogImageRegex.find(html)?.groupValues?.get(1)
                og?.let { return@use it }
                twitterImageRegex.find(html)?.groupValues?.get(1)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun readUpTo(response: okhttp3.Response, limit: Int): String? {
        val source = response.body?.source() ?: return null
        source.request(limit.toLong())
        return source.buffer.clone().readUtf8()
    }

    private companion object {
        const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/124.0 Safari/537.36"
        const val MAX_PAGE_BYTES = 256 * 1024
    }
}
