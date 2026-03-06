package com.example.test_code.presentation.list

import com.example.test_code.domain.model.Movie

sealed class MovieListUiState {
    object Loading : MovieListUiState()
    data class Success(val movies: List<Movie>) : MovieListUiState()
    data class Error(val message: String) : MovieListUiState()
}