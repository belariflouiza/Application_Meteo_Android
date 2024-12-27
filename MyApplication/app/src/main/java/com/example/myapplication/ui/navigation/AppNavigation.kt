package com.example.myapplication.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.ui.screens.HomeScreen
import com.example.myapplication.ui.screens.WeatherDetailScreen
import com.example.myapplication.ui.viewmodel.WeatherViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: WeatherViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(navController = navController, viewModel = viewModel)
        }
        composable(
            route = "detail/{cityId}",
            arguments = listOf(
                navArgument("cityId") { type = NavType.StringType }
            )
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
