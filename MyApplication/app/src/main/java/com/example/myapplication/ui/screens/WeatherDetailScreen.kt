package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.ui.viewmodel.WeatherViewModel
import com.example.myapplication.data.model.GeocodingResultItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherDetailScreen(
    navController: NavController,
    viewModel: WeatherViewModel,
    cityId: String
) {
    val weatherData by viewModel.weatherData.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()
    val weather = weatherData[cityId]
    val isFavorite = favorites.any { it.cityId == cityId }

    LaunchedEffect(cityId) {
        if (selectedCity == null) {
            val favorite = favorites.find { it.cityId == cityId }
            favorite?.let {
                val geocodingResult = GeocodingResultItem(
                    id = it.cityId,
                    name = it.name,
                    latitude = it.latitude,
                    longitude = it.longitude,
                    country = "",
                    admin1 = "",
                    country_code = ""
                )
                viewModel.setSelectedCity(geocodingResult)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedCity?.name ?: weather?.cityName ?: "Détails météo") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite(cityId) }) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Retirer des favoris" else "Ajouter aux favoris"
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (weather != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${weather.temperature}°C",
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = weather.condition,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Min", style = MaterialTheme.typography.bodyMedium)
                        Text("${weather.minTemp}°C", style = MaterialTheme.typography.titleMedium)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Max", style = MaterialTheme.typography.bodyMedium)
                        Text("${weather.maxTemp}°C", style = MaterialTheme.typography.titleMedium)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Vent", style = MaterialTheme.typography.bodyMedium)
                        Text("${weather.windSpeed} km/h", style = MaterialTheme.typography.titleMedium)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Prévisions horaires",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(weather.hourlyTemperatures.zip(weather.hourlyTimes)) { (temp, time) ->
                        HourlyWeatherCard(time = time, temperature = temp)
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Données météo non disponibles")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HourlyWeatherCard(
    time: String,
    temperature: Double
) {
    Card(
        modifier = Modifier.width(80.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = time,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${temperature}°C",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}