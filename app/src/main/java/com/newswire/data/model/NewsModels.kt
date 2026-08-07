package com.newswire.data.model

data class Article(
    val title: String = "",
    val summary: String = "",
    val link: String = "",
    val source: String = "",
    val sourceUrl: String = "",
    val publishedAt: String = "",
)

data class FunFact(
    val id: String = "",
    val text: String = "",
    val source: String = "",
    val permalink: String = "",
)

sealed class FeedItem {
    abstract val key: String

    data class News(val article: Article) : FeedItem() {
        override val key: String = article.link
    }

    data class Fact(val fact: FunFact) : FeedItem() {
        override val key: String = "fact:${fact.id}"
    }
}
