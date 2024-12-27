package com.example.myapplication.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.FloatEntry
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.example.myapplication.data.model.HourlyWeather
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.myapplication.ui.viewmodel.WeatherViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherDetailScreen(
    navController: NavController,
    viewModel: WeatherViewModel,
    cityId: String
) {
    val weatherData by viewModel.hourlyWeatherData.collectAsState()
    val cityName by viewModel.selectedCityName.collectAsState()

    LaunchedEffect(cityId) {
        viewModel.loadHourlyWeather(cityId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(cityName ?: "Détails météo") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Prévisions horaires",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            weatherData?.let { hourlyData: List<HourlyWeather> ->
                val entries = hourlyData.mapIndexed { index, data ->
                    FloatEntry(
                        x = index.toFloat(),
                        y = data.temperature.toFloat()
                    )
                }

                Chart(
                    chart = lineChart(),
                    model = entryModelOf(entries),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    startAxis = startAxis(),
                    bottomAxis = bottomAxis()
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn {
                    items(hourlyData) { weather ->
                        HourlyWeatherItem(weather)
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun HourlyWeatherItem(weather: HourlyWeather) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = weather.time.format(DateTimeFormatter.ofPattern("HH:mm")),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${weather.temperature}°C",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = weather.weatherDescription,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}