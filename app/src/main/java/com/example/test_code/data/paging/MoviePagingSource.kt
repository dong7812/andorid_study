package com.example.test_code.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.test_code.data.repository.MovieRepository
import com.example.test_code.domain.model.Movie

/**
 * Paging 3 - PagingSource
 * - 페이지별로 데이터를 로드
 * - 자동으로 다음 페이지 요청
 * - 에러 처리 자동화
 */
class MoviePagingSource(
    private val repository: MovieRepository
) : PagingSource<Int, Movie>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        // 현재 페이지 (null이면 첫 페이지 = 1)
        val page = params.key ?: 1

        return try {
            // Repository에서 영화 목록 가져오기
            repository.getPopularMovies(page = page)
                .fold(
                    onSuccess = { movies ->
                        LoadResult.Page(
                            data = movies,
                            prevKey = if (page == 1) null else page - 1,
                            nextKey = if (movies.isEmpty()) null else page + 1
                        )
                    },
                    onFailure = { exception ->
                        LoadResult.Error(exception)
                    }
                )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    /**
     * getRefreshKey
     * - 새로고침 시 어느 페이지부터 다시 로드할지 결정
     * - 사용자가 보던 위치 근처의 페이지를 반환
     */
    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
