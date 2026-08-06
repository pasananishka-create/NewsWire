package com.newswire.data.repository

import com.newswire.data.model.Article
import com.newswire.data.remote.GoogleNewsRssParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

class NewsFetchException(message: String) : Exception(message)

@Singleton
class NewsRepository @Inject constructor(
    private val client: OkHttpClient,
) {

    private val parser = GoogleNewsRssParser()

    suspend fun getHeadlines(query: String): List<Article> = withContext(Dispatchers.IO) {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val url = "https://news.google.com/rss/search?q=$encoded&hl=en-LK&gl=LK&ceid=LK:en"
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

    private companion object {
        const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/124.0 Safari/537.36"
    }
}
