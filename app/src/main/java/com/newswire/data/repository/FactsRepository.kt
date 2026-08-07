package com.newswire.data.repository

import com.newswire.data.model.FunFact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FactsRepository @Inject constructor(
    private val client: OkHttpClient,
) {

    suspend fun fetchRandomFact(): FunFact? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://uselessfacts.jsph.pl/api/v2/facts/random?language=en")
                .header("User-Agent", USER_AGENT)
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val json = response.body?.string() ?: return@use null
                val obj = JSONObject(json)
                FunFact(
                    id = obj.optString("id"),
                    text = obj.optString("text"),
                    source = obj.optString("source"),
                    permalink = obj.optString("permalink"),
                )
            }
        } catch (_: Exception) {
            null
        }
    }

    private companion object {
        const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/124.0 Safari/537.36"
    }
}
