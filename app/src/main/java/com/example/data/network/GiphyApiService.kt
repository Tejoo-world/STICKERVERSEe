package com.example.data.network

import com.example.data.model.GiphyAutocompleteResponse
import com.example.data.model.GiphyResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GiphyApiService {
    @GET("v1/stickers/trending")
    suspend fun getTrendingStickers(
        @Query("api_key") apiKey: String,
        @Query("limit") limit: Int = 30,
        @Query("offset") offset: Int = 0,
        @Query("rating") rating: String = "g"
    ): GiphyResponse

    @GET("v1/stickers/search")
    suspend fun searchStickers(
        @Query("api_key") apiKey: String,
        @Query("q") query: String,
        @Query("limit") limit: Int = 30,
        @Query("offset") offset: Int = 0,
        @Query("rating") rating: String = "g",
        @Query("lang") lang: String = "en"
    ): GiphyResponse

    @GET("v1/stickers/search/tags")
    suspend fun getAutocompleteTags(
        @Query("api_key") apiKey: String,
        @Query("q") query: String,
        @Query("limit") limit: Int = 10
    ): GiphyAutocompleteResponse
}
