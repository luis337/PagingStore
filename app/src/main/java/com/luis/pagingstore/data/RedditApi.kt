package com.luis.pagingstore.data

import android.util.Log
import androidx.room.Entity
import com.luis.pagingstore.vo.ListingResponse
import com.luis.pagingstore.vo.RedditPost
import kotlinx.coroutines.flow.Flow
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RedditApi {
    @GET("/r/{subreddit}/hot.json")
    fun getTop(
        @Path("subreddit") subreddit: String,
        @Query("limit") limit: Int): Call<ListingResponse>

    @GET("/r/{subreddit}/hot.json")
    fun getTopAfter(
        @Path("subreddit") subreddit: String,
        @Query("after") after: String,
        @Query("limit") limit: Int): Call<ListingResponse>

    @GET("/r/{subreddit}/hot.json")
    fun getTopBefore(
        @Path("subreddit") subreddit: String,
        @Query("before") before: String,
        @Query("limit") limit: Int): Call<ListingResponse>

    companion object {
        private const val BASE_URL = "https://www.reddit.com/"
        fun create(): RedditApi = create(HttpUrl.parse(BASE_URL)!!)
        fun create(httpUrl: HttpUrl): RedditApi {
            val logger = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
                Log.d("API", it)
            })
            logger.level = HttpLoggingInterceptor.Level.BASIC

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()
            return Retrofit.Builder()
                .baseUrl(httpUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RedditApi::class.java)
        }
    }
}