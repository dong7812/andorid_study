package com.example.test_code.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test_code.data.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MovieDetailUiState>(MovieDetailUiState.Loading)
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    fun loadMovieDetail(movieId: Int) {
        viewModelScope.launch {
            _uiState.value = MovieDetailUiState.Loading
            repository.getMovieDetail(movieId)
                .onSuccess { movie ->
                    _uiState.value = MovieDetailUiState.Success(movie)
                }
                .onFailure { e ->
                    _uiState.value = MovieDetailUiState.Error(e.message ?: "알 수 없음")
                }
        }
    }
}