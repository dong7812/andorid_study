package com.example.test_code

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NavGraph(
    navController: NavController = rememberNavController()
){
    val tabs = BottomNavTab.values()
    val navBackStckEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStckEntry?.destination?.route
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // 더블 백 프레스 상태
    var backPressedOnce by remember { mutableStateOf(false) }

    // HOME 화면일 때만 더블 백 프레스 활성화
    BackHandler(enabled = currentRoute == "movie_list") {
        if (backPressedOnce) {
            // 2번째 클릭: 앱 종료
            (context as? ComponentActivity)?.finish()
        } else {
            // 1번째 클릭: 토스트 표시
            backPressedOnce = true
            Toast.makeText(
                context,
                "한 번 더 누르면 종료됩니다",
                Toast.LENGTH_SHORT
            ).show()

            // 2초 후 상태 초기화
            scope.launch {
                delay(2000)
                backPressedOnce = false
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
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