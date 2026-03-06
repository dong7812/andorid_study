package com.example.test_code.domain.model

/**
 * 영화 Domain Model
 * - 앱 비즈니스 로직에 최적화된 구조
 * - Non-null 보장 (UI에서 안전하게 사용)
 * - 의미있는 네이밍
 */
data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val posterImageUrl: String,  // 전체 이미지 URL (바로 사용 가능)
    val rating: Double           // 평점 (0.0 ~ 10.0)
)