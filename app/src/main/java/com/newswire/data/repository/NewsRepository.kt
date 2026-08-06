package com.newswire.data.repository

import com.newswire.data.model.Article
import com.newswire.data.remote.GoogleNewsRssParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
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
    private val ogImageReverseRegex = Regex(
        """content=["']([^"']+)["']\s+property=["']og:image["']""",
        RegexOption.IGNORE_CASE,
    )
    private val twitterImageRegex = Regex(
        """name=["']twitter:image(?::src)?["']\s+content=["']([^"']+)["']""",
        RegexOption.IGNORE_CASE,
    )
    private val twitterImageReverseRegex = Regex(
        """content=["']([^"']+)["']\s+name=["']twitter:image(?::src)?["']""",
        RegexOption.IGNORE_CASE,
    )
    private val signatureRegex = Regex("""data-n-a-sg="([^"]+)"""")
    private val timestampRegex = Regex("""data-n-a-ts="([^"]+)"""")

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
            val realUrl = resolveArticleUrl(link) ?: return@withContext null
            scrapeOgImage(realUrl)
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun resolveArticleUrl(link: String): String? {
        val articleId = link.substringAfterLast('/').substringBefore('?')
        val html = getAsText(link, WRAPPER_MAX_BYTES) ?: return null
        val signature = signatureRegex.find(html)?.groupValues?.get(1) ?: return null
        val timestamp = timestampRegex.find(html)?.groupValues?.get(1) ?: return null
        val fReq = buildFReq(articleId, timestamp, signature)
        val body = postBatch(fReq) ?: return null
        return parseGarturlResponse(body)
    }

    private fun buildFReq(articleId: String, timestamp: String, signature: String): String {
        val inner = "[\"garturlreq\"," +
            "[[\"X\",\"X\",[\"X\",\"X\"],null,null,1,1,\"US:en\",null,1,null,null,null,null,null,0,1]," +
            "\"X\",\"X\",1,[1,1,1],1,1,null,0,0,null,0]," +
            "\"$articleId\",$timestamp,\"$signature\"]"
        val escaped = inner.replace("\"", "\\\"")
        return "[[[\"Fbv4je\",\"$escaped\",null,\"generic\"]]]"
    }

    private suspend fun postBatch(fReq: String): String? {
        val form = FormBody.Builder().add("f.req", fReq).build()
        val request = Request.Builder()
            .url(BATCH_EXECUTE_URL)
            .header("Referer", "https://news.google.com/")
            .header("User-Agent", DATA_TOOLS_UA)
            .post(form)
            .build()
        val response = client.newCall(request).execute()
        val result: String? = if (response.isSuccessful) response.body?.string() else null
        response.close()
        return result
    }

    private fun parseGarturlResponse(body: String): String? {
        var text = body
        if (text.startsWith(")]}'")) {
            text = text.substringAfter('\n').trimStart()
            val nl = text.indexOf('\n')
            if (nl >= 0 && text.substring(0, nl).trim().toLongOrNull() != null) {
                text = text.substring(nl + 1)
            }
        }
        val envelopes = try {
            JSONArray(text)
        } catch (_: Exception) {
            return null
        }
        for (i in 0 until envelopes.length()) {
            val env = envelopes.optJSONArray(i) ?: continue
            if (env.length() < 3) continue
            if (env.getString(0) == "wrb.fr" && env.getString(1) == "Fbv4je") {
                val payload = try {
                    JSONArray(env.getString(2))
                } catch (_: Exception) {
                    continue
                }
                if (payload.length() > 0 && payload.getString(0) == "garturlres") {
                    return payload.getString(1)
                }
            }
        }
        return null
    }

    private suspend fun scrapeOgImage(realUrl: String): String? {
        val html = getAsText(realUrl, OG_MAX_BYTES) ?: return null
        ogImageRegex.find(html)?.groupValues?.get(1)?.let { return it }
        ogImageReverseRegex.find(html)?.groupValues?.get(1)?.let { return it }
        twitterImageRegex.find(html)?.groupValues?.get(1)?.let { return it }
        return twitterImageReverseRegex.find(html)?.groupValues?.get(1)
    }

    private suspend fun getAsText(url: String, limit: Int): String? {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val source = response.body?.source() ?: return null
            source.request(limit.toLong())
            return source.buffer.clone().readUtf8()
        }
    }

    private companion object {
        const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/124.0 Safari/537.36"
        const val DATA_TOOLS_UA = "Mozilla/5.0 (compatible; socialcontext-datatools)"
        const val BATCH_EXECUTE_URL = "https://news.google.com/_/DotsSplashUi/data/batchexecute"
        const val WRAPPER_MAX_BYTES = 768 * 1024
        const val OG_MAX_BYTES = 400 * 1024
    }
}
