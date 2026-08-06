package com.newswire.data.repository

import com.newswire.data.model.Article
import com.newswire.data.remote.NewsApiService
import javax.inject.Inject
import javax.inject.Singleton

class NewsApiException(message: String) : Exception(message)

@Singleton
class NewsRepository @Inject constructor(
    private val api: NewsApiService,
) {

    suspend fun getHeadlines(category: String?): List<Article> {
        val response = api.getTopHeadlines(category = category)
        if (response.status != "ok") {
            throw NewsApiException(
                response.message ?: "News API error (${response.code ?: "unknown"})"
            )
        }
        return response.articles.filter {
            it.title.isNotBlank() && !it.title.equals("[Removed]", ignoreCase = true)
        }
    }
}
