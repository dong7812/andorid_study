package com.example.test_code.presentation

import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavTab(
    val route: String,
    val label: String
) {
    MOVIE_LIST("movie_list", "영화"),
    WEB_VIEW("web_view", "웹")
}