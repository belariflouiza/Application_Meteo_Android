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
import com.example.myapplication.ui.screens.HomeScreen
import com.example.myapplication.ui.screens.WeatherDetailScreen
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel by lazy {
        (application as WeatherApplication).weatherViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
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
                            arguments = listOf(navArgument("cityId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val cityId = backStackEntry.arguments?.getString("cityId")
                            if (cityId != null) {
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
}