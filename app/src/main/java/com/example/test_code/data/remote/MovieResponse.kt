package com.example.test_code.data.remote

import com.example.test_code.domain.model.Movie
import com.google.gson.annotations.SerializedName

/**
 * API 응답 DTO (Data Transfer Object)
 * - API 명세에 의존하는 구조
 * - Nullable 허용 (API가 필드를 안 줄 수도 있음)
 */
data class MovieResponse(
    @SerializedName("id") val id: Int?,
    @SerializedName("title") val title: String?,
    @SerializedName("overview") val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("vote_average") val voteAverage: Double?
)

data class MovieListResponse(
    @SerializedName("page") val page: Int?,
    @SerializedName("results") val results: List<MovieResponse>?,
    @SerializedName("total_pages") val totalPages: Int?,
    @SerializedName("total_results") val totalResults: Int?
)

/**
 * DTO → Domain Model 변환
 * - Null 체크 및 기본값 설정
 * - 데이터 가공 (이미지 URL 조합 등)
 * - 유효하지 않은 데이터는 null 반환
 */
fun MovieResponse.toDomain(): Movie? {
    // 필수값 검증 (id, title 없으면 유효하지 않은 데이터)
    if (id == null || title.isNullOrBlank()) {
        return null
    }

    return Movie(
        id = id,
        title = title,
        overview = overview?.takeIf { it.isNotBlank() } ?: "정보가 없습니다",
        posterImageUrl = posterPath?.let { path ->
            "https://image.tmdb.org/t/p/w500$path"
        } ?: "",
        rating = (voteAverage ?: 0.0).coerceIn(0.0, 10.0)  // 0~10 범위 보장
    )
}