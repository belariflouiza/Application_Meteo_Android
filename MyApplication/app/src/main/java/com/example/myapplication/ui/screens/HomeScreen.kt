package com.example.myapplication.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.R
import com.example.myapplication.data.model.FavoriteCity
import com.example.myapplication.data.model.WeatherEntity
import com.example.myapplication.ui.viewmodel.WeatherViewModel

@Composable
fun HomeScreen(viewModel: WeatherViewModel = viewModel()) {
    var searchQuery by remember { mutableStateOf("") }
    val weather by viewModel.weather.collectAsState()
    val favoriteCities by viewModel.favoriteCities.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadFavoriteCities()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Barre de recherche
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Rechercher une ville") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = {
                    viewModel.getWeather(searchQuery)
                }) {
                    Icon(Icons.Default.Search, contentDescription = "Rechercher")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Liste des villes favorites
        LazyColumn {
            items(favoriteCities) { city ->
                FavoriteCityItem(city = city, onCitySelected = { viewModel.getWeather(it) })
            }
        }

        // Affichage des résultats météo
        weather?.let {
            WeatherDisplay(weather = it)
        }
    }
}

@Composable
fun FavoriteCityItem(city: FavoriteCity, onCitySelected: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        onClick = { onCitySelected(city.cityName) }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = city.cityName, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
fun WeatherDisplay(weather: WeatherEntity) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Température: ${weather.temperature}°C", style = MaterialTheme.typography.titleLarge)
        Text(text = "Condition: ${weather.weatherCondition}", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Min: ${weather.minTemperature}°C, Max: ${weather.maxTemperature}°C", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Vent: ${weather.windSpeed} km/h", style = MaterialTheme.typography.bodyLarge)
    }
}

