package com.example.test_code.data.repository

import com.example.test_code.data.remote.RetrofitClient
import com.example.test_code.data.remote.toDomain
import com.example.test_code.domain.model.Movie
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieRepository @Inject constructor() {
    private val api = RetrofitClient.movieApi

    suspend fun getPopularMovies(page: Int = 1): Result<List<Movie>> {
        return try {
            val response = api.getPopularMovies(page = page)
            // mapNotNull: toDomain()이 null 반환하는 항목은 제외
            val movies = response.results?.mapNotNull { it.toDomain() } ?: emptyList()
            Result.success(movies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchMovies(query: String, page: Int = 1): Result<List<Movie>> {
        return try {
            val response = api.searchMovies(query = query, page = page)
            val movies = response.results?.mapNotNull { it.toDomain() } ?: emptyList()
            Result.success(movies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMovieDetail(movieId: Int): Result<Movie> {
        return try {
            val response = api.getMovieDetail(movieId)
            val movie = response.toDomain()

            if (movie != null) {
                Result.success(movie)
            } else {
                Result.failure(Exception("유효하지 않은 영화 데이터"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}