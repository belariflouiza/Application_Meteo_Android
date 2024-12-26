package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.WeatherEntity

@Composable
fun WeatherDetailScreen(weather: WeatherEntity) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Température: ${weather.temperature}°C", style = MaterialTheme.typography.titleLarge)
        Text(text = "Condition: ${weather.weatherCondition}", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Min: ${weather.minTemperature}°C, Max: ${weather.maxTemperature}°C", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Vent: ${weather.windSpeed} km/h", style = MaterialTheme.typography.bodyLarge)
    }
}