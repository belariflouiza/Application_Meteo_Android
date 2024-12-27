package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.screens.HomeScreen
import com.example.myapplication.ui.screens.WeatherDetailScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.viewmodel.WeatherViewModel
import com.example.myapplication.ui.viewmodel.WeatherViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val weatherRepository = (application as WeatherApplication).weatherRepository

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            val viewModel: WeatherViewModel = viewModel(
                                factory = WeatherViewModelFactory(weatherRepository)
                            )
                            HomeScreen(navController = navController, viewModel = viewModel)
                        }
                        composable(
                            "detail/{cityId}",
                            arguments = listOf(navArgument("cityId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val cityId = backStackEntry.arguments?.getString("cityId") ?: return@composable
                            val viewModel: WeatherViewModel = viewModel(
                                factory = WeatherViewModelFactory(weatherRepository)
                            )
                            WeatherDetailScreen(
                                navController = navController,
                                viewModel = viewModel,
                                cityId = cityId
                            )
                        }
                    }
                }
            }
        }
    }
}