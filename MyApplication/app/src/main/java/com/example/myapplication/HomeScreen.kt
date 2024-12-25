package com.example.myapplication.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.data.model.WeatherResponse
import com.example.myapplication.data.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun HomeScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var weatherResult by remember { mutableStateOf<WeatherResponse?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            // Récupérer les coordonnées de la ville
                            val geocodingResponse = ApiClient.geocodingApiService.getCoordinates(searchQuery)
                            if (geocodingResponse.results.isEmpty()) {
                                errorMessage = "Ville non trouvée."
                                weatherResult = null
                                return@launch
                            }

                            val coordinates = geocodingResponse.results.first()
                            // Récupérer les données météo
                            val weather = ApiClient.weatherApiService.getWeatherForecast(
                                lat = coordinates.latitude,
                                lon = coordinates.longitude
                            )

                            if (weather.current_weather == null) {
                                errorMessage = "Aucune donnée météo disponible pour cette ville."
                                weatherResult = null
                            } else {
                                weatherResult = weather
                                errorMessage = null
                            }
                        } catch (e: Exception) {
                            errorMessage = "Erreur: ${e.message}"
                            weatherResult = null
                        }
                    }
                }) {
                    Icon(Icons.Default.Search, contentDescription = "Rechercher")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Affichage des résultats météo
        weatherResult?.let { weather ->
            if (weather.current_weather != null) {
                // Convertir le weather_code en description et icône
                val (description, iconRes) = getWeatherDescriptionAndIcon(weather.current_weather.weathercode)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Afficher l'icône météo
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = description,
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Afficher la température
                    Text(text = "Température: ${weather.current_weather.temperature}°C", style = MaterialTheme.typography.titleLarge)
                    // Afficher la condition météo
                    Text(text = "Condition: $description", style = MaterialTheme.typography.bodyLarge)
                    // Afficher la vitesse du vent
                    Text(text = "Vent: ${weather.current_weather.windspeed} km/h", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                Text(text = "Aucune donnée météo disponible pour cette ville.")
            }
        }

        // Afficher les erreurs
        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
    }
}

// Fonction pour convertir weather_code en description et icône
fun getWeatherDescriptionAndIcon(weatherCode: Int?): Pair<String, Int> {
    return when (weatherCode) {
        0 -> Pair("Ensoleillé", R.drawable.pluvieux)
        1, 2, 3 -> Pair("Nuageux", R.drawable.pluvieux)
        45, 48 -> Pair("Brouillard", R.drawable.pluvieux)
        51, 53, 55 -> Pair("Pluie légère", R.drawable.pluvieux)
        56, 57 -> Pair("Pluie verglaçante", R.drawable.pluvieux)
        61, 63, 65 -> Pair("Pluie", R.drawable.pluvieux)
        66, 67 -> Pair("Pluie verglaçante", R.drawable.pluvieux)
        71, 73, 75 -> Pair("Neige", R.drawable.pluvieux)
        77 -> Pair("Grêle", R.drawable.pluvieux)
        80, 81, 82 -> Pair("Averses", R.drawable.pluvieux)
        85, 86 -> Pair("Neige", R.drawable.pluvieux)
        95, 96, 99 -> Pair("Orage", R.drawable.pluvieux)
        else -> Pair("Inconnu", R.drawable.pluvieux)
    }
}