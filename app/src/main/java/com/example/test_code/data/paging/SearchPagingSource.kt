package com.example.test_code.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.test_code.data.repository.MovieRepository
import com.example.test_code.domain.model.Movie

/**
 * 검색용 PagingSource
 * - 서버 사이드 검색 (TMDB /search/movie API 사용)
 * - 전체 DB에서 검색어에 맞는 영화만 반환
 */
class SearchPagingSource(
    private val repository: MovieRepository,
    private val query: String
) : PagingSource<Int, Movie>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        // 검색어가 비어있으면 빈 결과 반환
        if (query.isBlank()) {
            return LoadResult.Page(
                data = emptyList(),
                prevKey = null,
                nextKey = null
            )
        }

        val page = params.key ?: 1

        return try {
            // Repository에서 검색 결과 가져오기
            repository.searchMovies(query = query, page = page)
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

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
