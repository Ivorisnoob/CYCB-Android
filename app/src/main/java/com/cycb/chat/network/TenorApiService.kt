package com.cycb.chat.network

import com.cycb.chat.data.model.TenorSearchResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface TenorApiService {
    @GET("featured")
    suspend fun getTrendingGifs(
        @Query("key") apiKey: String,
        @Query("limit") limit: Int = 20
    ): TenorSearchResponse

    @GET("search")
    suspend fun searchGifs(
        @Query("q") query: String,
        @Query("key") apiKey: String,
        @Query("limit") limit: Int = 20
    ): TenorSearchResponse

    companion object {
        private const val BASE_URL = "https://tenor.googleapis.com/v2/"
        const val API_KEY = "AIzaSyAyimkuYQYF_FXVALexPuGQctUWRURdCYQ"

        fun create(): TenorApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(TenorApiService::class.java)
        }
    }
}
