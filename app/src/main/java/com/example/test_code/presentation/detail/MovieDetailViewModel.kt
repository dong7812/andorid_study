package com.example.test_code.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.test_code.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MovieDetailViewModel (
    private val movieId: Int
): ViewModel(){
    private val repository = MovieRepository()

    private val _uiState = MutableStateFlow<MovieDetailUiState>(MovieDetailUiState.Loading)
    val uiState : StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    init{
        loadMovieDetail()
    }

    private fun loadMovieDetail(){
        viewModelScope.launch {
            _uiState.value = MovieDetailUiState.Loading
            repository.getMovieDetail(movieId)
                .onSuccess { movie ->
                _uiState.value = MovieDetailUiState.Success(movie)
                }
                .onFailure{ e->
                    _uiState.value = MovieDetailUiState.Error(e.message ?: "알수 없음")
                }
        }
    }
}