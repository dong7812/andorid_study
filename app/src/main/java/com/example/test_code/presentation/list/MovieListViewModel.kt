package com.example.test_code.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test_code.data.repository.MovieRepository
import com.example.test_code.domain.model.Movie
import com.example.test_code.presentation.list.MovieListUiState.Success
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.filter

@HiltViewModel
class MovieListViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    private val _query = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    // combine — query랑 movies 둘 중 하나라도 바뀌면 실행
    val uiState: StateFlow<MovieListUiState> = combine(
        _movies,
        _query.debounce(300), // 300ms 동안 입력 없으면 실행
        _isLoading,
        _error
    ) { movies, query, isLoading, error ->
        when {
            isLoading -> MovieListUiState.Loading
            error != null -> MovieListUiState.Error(error)
            else -> Success(
                movies.filter {
                    it.title.contains(query, ignoreCase = true)
                }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MovieListUiState.Loading
    )

    init {
        loadMovies()
    }

    fun onQueryChange(query: String) {
        _query.value = query
    }

    private fun loadMovies() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getPopularMovies()
                .onSuccess { movies ->
                    _movies.value = movies
                    _isLoading.value = false
                }
                .onFailure { e ->
                    _error.value = e.message ?: "오류"
                    _isLoading.value = false
                }
        }
    }
}