package com.example.myapplication.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.screens.HomeScreen
import com.example.myapplication.ui.screens.WeatherDetailScreen
import com.example.myapplication.ui.viewmodel.WeatherViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Detail : Screen("detail/{cityId}") {
        fun createRoute(cityId: String) = "detail/$cityId"
    }
}

@Composable
fun WeatherNavigation(viewModel: WeatherViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        
        composable(
            route = Screen.Detail.route
        ) { backStackEntry ->
            val cityId = backStackEntry.arguments?.getString("cityId") ?: return@composable
            WeatherDetailScreen(
                navController = navController,
                viewModel = viewModel,
                cityId = cityId
            )
        }
    }
} 