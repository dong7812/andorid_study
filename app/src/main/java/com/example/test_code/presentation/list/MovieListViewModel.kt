package com.example.test_code.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.test_code.data.paging.MoviePagingSource
import com.example.test_code.data.paging.SearchPagingSource
import com.example.test_code.data.repository.MovieRepository
import com.example.test_code.domain.model.Movie
import com.example.test_code.presentation.detail.MovieDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class MovieListViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    // 검색어 상태
    private val _searchQuery = MutableStateFlow("")

    /**
     * Paging 3 + 서버 사이드 검색
     * - 검색어가 비어있으면: 인기 영화 목록 (MoviePagingSource)
     * - 검색어가 있으면: 검색 결과 (SearchPagingSource)
     * - debounce로 300ms 동안 입력 없으면 검색 실행
     * - flatMapLatest로 검색어 바뀔 때마다 새로운 PagingSource 생성
     */
//    @OptIn(ExperimentalCoroutinesApi::class)
    val moviePagingFlow: Flow<PagingData<Movie>> = _searchQuery
        .debounce(300)  // 300ms 디바운스
        .flatMapLatest { query ->
            Pager(
                config = PagingConfig(
                    pageSize = 20,
                    enablePlaceholders = false,
                    initialLoadSize = 20
                ),
                pagingSourceFactory = {
                    if (query.isBlank()) {
                        // 검색어 없음 → 인기 영화 목록
                        MoviePagingSource(repository)
                    } else {
                        // 검색어 있음 → 서버 사이드 검색
                        SearchPagingSource(repository, query)
                    }
                }
            ).flow
        }
        .cachedIn(viewModelScope)

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}