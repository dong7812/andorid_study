package com.example.test_code

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.test_code.presentation.BottomNavTab
import com.example.test_code.presentation.WebViewScreen
import com.example.test_code.presentation.detail.MovieDetailScreen
import com.example.test_code.presentation.list.MovieListScreen

@Composable
fun NavGraph(
    navController: NavController = rememberNavController()
){
    val tabs = BottomNavTab.values()
    val navBackStckEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStckEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { },  // 빈 아이콘
                       label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController as NavHostController,
            startDestination = "movie_list",
            modifier = Modifier.padding(paddingValues)
        ){
            composable("movie_list"){
                MovieListScreen(
                    onMovieClick = { movieId ->
                        navController.navigate(
                            "movie_detail/$movieId"
                        )
                    }
                )
            }

            composable("web_view"){
                WebViewScreen()
            }

            composable(
                route = "movie_detail/{movie_id}",
                arguments = listOf(navArgument("movie_id") { type = NavType.IntType })
            ) { backStackEntry ->
                val movieId = backStackEntry.arguments?.getInt("movie_id") ?: return@composable
                MovieDetailScreen(
                    movieId = movieId,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}