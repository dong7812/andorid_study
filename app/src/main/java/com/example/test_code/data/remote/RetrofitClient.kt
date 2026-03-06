package com.example.test_code.data.remote

import com.example.test_code.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://api.themoviedb.org/3/"

    // 1 HttpLoggingInterceptor - HTTP 요청/응답 로깅 (디버그 빌드에만)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY  // 전체 로그 (개발 중)
        } else {
            HttpLoggingInterceptor.Level.NONE  // 로그 없음 (릴리즈)
        }
    }

    // 2 에러 핸들링 Interceptor - HTTP 에러 코드를 Exception으로 변환
    private val errorInterceptor = Interceptor { chain ->
        val request = chain.request()
        val response = chain.proceed(request)

        // HTTP 에러 처리 (400, 500번대)
        if (!response.isSuccessful) {
            when (response.code) {
                400 -> throw IllegalArgumentException("잘못된 요청입니다")
                401 -> throw SecurityException("인증이 필요합니다")
                403 -> throw SecurityException("접근 권한이 없습니다")
                404 -> throw NoSuchElementException("리소스를 찾을 수 없습니다")
                500 -> throw IllegalStateException("서버 오류가 발생했습니다")
                503 -> throw IllegalStateException("서버가 일시적으로 사용 불가능합니다")
                else -> throw IllegalStateException("알 수 없는 오류: ${response.code}")
            }
        }

        response
    }

    // 3 OkHttpClient - Timeout, Interceptor, 재시도 설정
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)  // 연결 타임아웃 10초
        .readTimeout(15, TimeUnit.SECONDS)     // 읽기 타임아웃 15초
        .writeTimeout(15, TimeUnit.SECONDS)    // 쓰기 타임아웃 15초
        .retryOnConnectionFailure(true)        // 연결 실패 시 자동 재시도
        .addInterceptor(loggingInterceptor)    // 로깅 (먼저 추가)
        .addInterceptor(errorInterceptor)      // 에러 처리 (나중에 추가)
        .build()

    // 4 Retrofit 인스턴스
    val movieApi: MovieApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)  // ✅ OkHttpClient 설정 적용
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MovieApi::class.java)
    }
}