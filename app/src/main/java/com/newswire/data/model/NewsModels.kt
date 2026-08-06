package com.newswire.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NewsResponse(
    val status: String,
    @SerialName("totalResults") val totalResults: Int = 0,
    val articles: List<Article> = emptyList(),
    val code: String? = null,
    val message: String? = null,
)

@Serializable
data class Article(
    val source: Source = Source(),
    val author: String? = null,
    val title: String = "",
    val description: String? = null,
    val url: String = "",
    @SerialName("urlToImage") val urlToImage: String? = null,
    @SerialName("publishedAt") val publishedAt: String = "",
    val content: String? = null,
)

@Serializable
data class Source(
    val id: String? = null,
    val name: String = "",
)
