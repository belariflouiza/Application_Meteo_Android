package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.screens.HomeScreen
import com.example.myapplication.ui.screens.WeatherDetailScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.viewmodel.WeatherViewModel
import com.example.myapplication.ui.viewmodel.WeatherViewModelFactory
import com.example.myapplication.data.repository.WeatherRepository
import com.example.myapplication.data.database.AppDatabase
import com.example.myapplication.data.network.ApiClient

class MainActivity : ComponentActivity() {
    private lateinit var weatherRepository: WeatherRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        weatherRepository = WeatherRepository(
            weatherApiService = ApiClient.weatherService,
            geocodingApiService = ApiClient.geocodingService,
            favoriteCityDao = AppDatabase.getDatabase(applicationContext).favoriteCityDao(),
            weatherDao = AppDatabase.getDatabase(applicationContext).weatherDao()
        )

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
                            HomeScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }

                        composable(
                            route = "detail/{cityId}",
                            arguments = listOf(
                                navArgument("cityId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val viewModel: WeatherViewModel = viewModel(
                                factory = WeatherViewModelFactory(weatherRepository)
                            )
                            WeatherDetailScreen(
                                cityId = backStackEntry.arguments?.getString("cityId") ?: "",
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}