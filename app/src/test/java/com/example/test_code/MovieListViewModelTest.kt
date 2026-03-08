package com.example.test_code

import androidx.paging.PagingSource
import com.example.test_code.data.paging.MoviePagingSource
import com.example.test_code.data.paging.SearchPagingSource
import com.example.test_code.data.repository.MovieRepository
import com.example.test_code.domain.model.Movie
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

/**
 * Paging 3 ViewModel 테스트
 *
 * ⚠️ PagingData는 직접 테스트하기 어려우므로
 * PagingSource를 테스트합니다
 */
class MovieListViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    private val repository = mockk<MovieRepository>()

    @Test
    fun `MoviePagingSource - 첫 페이지 로딩 성공`() = runTest {
        // given
        val movies = listOf(
            Movie(1, "어벤져스", "마블 영화", "https://image.url/1", 8.5),
            Movie(2, "인터스텔라", "우주 영화", "https://image.url/2", 9.0)
        )
        coEvery { repository.getPopularMovies(page = 1) } returns Result.success(movies)

        val pagingSource = MoviePagingSource(repository)

        // when
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,  // 첫 페이지
                loadSize = 20,
                placeholdersEnabled = false
            )
        )

        // then
        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertEquals(2, page.data.size)
        assertEquals("어벤져스", page.data[0].title)
        assertEquals(null, page.prevKey)
        assertEquals(2, page.nextKey)
    }

    @Test
    fun `MoviePagingSource - 로딩 실패 시 Error 반환`() = runTest {
        // given
        val exception = Exception("네트워크 오류")
        coEvery { repository.getPopularMovies(page = 1) } returns Result.failure(exception)

        val pagingSource = MoviePagingSource(repository)

        // when
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 20,
                placeholdersEnabled = false
            )
        )

        // then
        assertTrue(result is PagingSource.LoadResult.Error)
        val error = result as PagingSource.LoadResult.Error
        assertEquals("네트워크 오류", error.throwable.message)
    }

    @Test
    fun `SearchPagingSource - 검색 결과 로딩 성공`() = runTest {
        // given
        val movies = listOf(
            Movie(1, "어벤져스", "마블 영화", "https://image.url/1", 8.5)
        )
        coEvery { repository.searchMovies(query = "어벤", page = 1) } returns Result.success(movies)

        val pagingSource = SearchPagingSource(repository, "어벤")

        // when
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 20,
                placeholdersEnabled = false
            )
        )

        // then
        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertEquals(1, page.data.size)
        assertEquals("어벤져스", page.data[0].title)
    }

    @Test
    fun `SearchPagingSource - 빈 검색어일 때 빈 결과 반환`() = runTest {
        // given
        val pagingSource = SearchPagingSource(repository, "")

        // when
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 20,
                placeholdersEnabled = false
            )
        )

        // then
        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertEquals(0, page.data.size)
        assertEquals(null, page.nextKey)
    }

    @Test
    fun `MoviePagingSource - 2페이지 로딩 시 prevKey와 nextKey 확인`() = runTest {
        // given
        val movies = listOf(
            Movie(21, "영화21", "설명", "url", 7.0),
            Movie(22, "영화22", "설명", "url", 7.5)
        )
        coEvery { repository.getPopularMovies(page = 2) } returns Result.success(movies)

        val pagingSource = MoviePagingSource(repository)

        // when
        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = 2,  // 2페이지
                loadSize = 20,
                placeholdersEnabled = false
            )
        )

        // then
        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertEquals(1, page.prevKey)  // 이전 페이지 = 1
        assertEquals(3, page.nextKey)  // 다음 페이지 = 3
    }
}