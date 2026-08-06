package com.newswire.data.remote

import com.newswire.BuildConfig
import com.newswire.data.model.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {

    @GET("v2/top-headlines")
    suspend fun getTopHeadlines(
        @Query("category") category: String? = null,
        @Query("pageSize") pageSize: Int = 20,
        @Query("apiKey") apiKey: String = BuildConfig.NEWSAPI_KEY,
    ): NewsResponse
}
