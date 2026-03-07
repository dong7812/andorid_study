package com.example.test_code.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieApi {
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String = "bc7ac48fde050e4b2ad93eb50e13f7f3",
        @Query("language") language: String = "ko-KR",
        @Query("page") page: Int = 1
    ): MovieListResponse

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String = "bc7ac48fde050e4b2ad93eb50e13f7f3",
        @Query("language") language: String = "ko-KR",
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): MovieListResponse

    @GET("movie/{movie_id}")
    suspend fun getMovieDetail(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String = "bc7ac48fde050e4b2ad93eb50e13f7f3",
        @Query("language") language: String = "ko-KR"
    ): MovieResponse
}