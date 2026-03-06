package com.example.test_code

import app.cash.turbine.test
import com.example.test_code.data.repository.MovieRepository
import com.example.test_code.domain.model.Movie
import com.example.test_code.presentation.list.MovieListUiState
import com.example.test_code.presentation.list.MovieListViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class MovieListViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    private val repository = mockk<MovieRepository>()
    private lateinit var viewModel: MovieListViewModel

    @Test
    fun `영화 목록 로딩 성공 시 Success 상태`() = runTest {
        // given
        val movies = listOf(
            Movie(1, "어벤져스", "마블 영화", "/path", 8.5),
            Movie(2, "인터스텔라", "우주 영화", "/path", 9.0)
        )
        coEvery { repository.getPopularMovies() } returns Result.success(movies)

        // when
        viewModel = MovieListViewModel(repository)

        // then
        viewModel.uiState.test {
            // 첫 번째는 Loading (initialValue)
            skipItems(1)
            // 두 번째가 실제 Success
            val state = awaitItem()
            assertTrue(state is MovieListUiState.Success)
            assertEquals(2, (state as MovieListUiState.Success).movies.size)
        }
    }

    @Test
    fun `영화 목록 로딩 실패 시 Error 상태`() = runTest {
        // given
        coEvery { repository.getPopularMovies() } returns Result.failure(Exception("네트워크 오류"))

        // when
        viewModel = MovieListViewModel(repository)

        // then
        viewModel.uiState.test {
            // 첫 번째는 Loading (initialValue)
            skipItems(1)
            // 두 번째가 실제 Error
            val state = awaitItem()
            assertTrue(state is MovieListUiState.Error)
            assertEquals("네트워크 오류", (state as MovieListUiState.Error).message)
        }
    }

    @Test
    fun `검색어 입력 시 필터링된 결과 반환`() = runTest {
        // given
        val movies = listOf(
            Movie(1, "어벤져스", "마블 영화", "/path", 8.5),
            Movie(2, "인터스텔라", "우주 영화", "/path", 9.0)
        )
        coEvery { repository.getPopularMovies() } returns Result.success(movies)
        viewModel = MovieListViewModel(repository)

        viewModel.uiState.test {
            // 1. Loading 스킵
            skipItems(1)
            // 2. Success (전체 영화 2개)
            val initialState = awaitItem()
            assertTrue(initialState is MovieListUiState.Success)
            assertEquals(2, (initialState as MovieListUiState.Success).movies.size)

            // when - 검색어 입력
            viewModel.onQueryChange("어벤")
            advanceTimeBy(400) // debounce 대기

            // then - 필터링된 결과
            val filteredState = awaitItem()
            assertTrue(filteredState is MovieListUiState.Success)
            assertEquals(1, (filteredState as MovieListUiState.Success).movies.size)
            assertEquals("어벤져스", filteredState.movies[0].title)
        }
    }
}