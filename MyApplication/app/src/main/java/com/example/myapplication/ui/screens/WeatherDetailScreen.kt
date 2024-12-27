package com.example.myapplication.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.data.model.WeatherEntity
import com.example.myapplication.ui.viewmodel.WeatherViewModel
import kotlin.math.roundToInt
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherDetailScreen(
    navController: NavController,
    viewModel: WeatherViewModel,
    cityId: String
) {
    val weatherData by viewModel.weatherData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    LaunchedEffect(cityId) {
        viewModel.loadWeatherDetails(cityId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails météo") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Text(
                        text = error ?: "Une erreur est survenue",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                else -> {
                    weatherData[cityId]?.let { weather ->
                        val configuration = LocalConfiguration.current
                        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

                        if (isLandscape) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                ) {
                                    CurrentWeatherCard(weather)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                    ) {
                                        HourlyForecastCard(weather)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 8.dp)
                                ) {
                                    DailyForecastCard(weather)
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                CurrentWeatherCard(weather)
                                Spacer(modifier = Modifier.height(16.dp))
                                HourlyForecastCard(
                                    weather = weather,
                                    modifier = Modifier.height(300.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                DailyForecastCard(
                                    weather = weather,
                                    modifier = Modifier.height(400.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CurrentWeatherCard(
    weather: WeatherEntity,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${weather.temperature.toInt()}°C",
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = weather.condition,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Min", style = MaterialTheme.typography.bodyMedium)
                    Text("${weather.minTemp.toInt()}°C", style = MaterialTheme.typography.titleMedium)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Max", style = MaterialTheme.typography.bodyMedium)
                    Text("${weather.maxTemp.toInt()}°C", style = MaterialTheme.typography.titleMedium)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Vent", style = MaterialTheme.typography.bodyMedium)
                    Text("${weather.windSpeed.toInt()} km/h", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun HourlyForecastCard(
    weather: WeatherEntity,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val scrollState = rememberScrollState()
    val primaryColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val textPaint = remember {
        android.graphics.Paint().apply {
            color = textColor
            textSize = 28f
            textAlign = android.graphics.Paint.Align.CENTER
            isFakeBoldText = true
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isLandscape) {
                    Modifier.height(300.dp)
                } else {
                    Modifier.height(250.dp)
                }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Prévisions horaires",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .horizontalScroll(scrollState)
            ) {
                Canvas(
                    modifier = Modifier
                        .width(1000.dp)
                        .fillMaxHeight()
                ) {
                    val temperatures = weather.hourlyTemperatures.take(24)
                    val maxTemp = temperatures.maxOrNull() ?: 0.0
                    val minTemp = temperatures.minOrNull() ?: 0.0
                    val range = (maxTemp - minTemp).coerceAtLeast(1.0)
                    val width = size.width
                    val height = size.height
                    val points = temperatures.mapIndexed { index, temp ->
                        val x = width * index / 23
                        val y = height - (height * (temp - minTemp) / range).toFloat()
                        Offset(x, y)
                    }

                    // Dessiner les segments de ligne entre chaque point
                    for (i in 0 until points.size - 1) {
                        drawLine(
                            color = primaryColor,
                            start = points[i],
                            end = points[i + 1],
                            strokeWidth = 2.dp.toPx()
                        )
                    }

                    // Dessiner les points et les températures
                    points.forEachIndexed { index, point ->
                        drawCircle(
                            color = primaryColor,
                            radius = 4.dp.toPx(),
                            center = point
                        )

                        // Température
                        drawContext.canvas.nativeCanvas.drawText(
                            "${temperatures[index].toInt()}°",
                            point.x,
                            point.y - 15,
                            textPaint
                        )
                    }
                }
            }
            
            // Heures (axe X)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
            ) {
                Row(
                    modifier = Modifier
                        .width(1000.dp)
                        .padding(start = 0.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weather.hourlyTimes.take(24).forEach { time ->
                        Text(
                            text = time,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherInfo(
    icon: ImageVector,
    value: String,
    label: String,
    tint: Color = Color.Unspecified
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = tint
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = tint
        )
    }
}

@Composable
fun DailyForecastCard(
    weather: WeatherEntity,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val scrollState = rememberScrollState()
    val primaryColor = MaterialTheme.colorScheme.primary
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isLandscape) {
                    Modifier.fillMaxHeight()
                } else {
                    Modifier
                }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text(
                text = "Prévisions sur 7 jours",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            // Ici nous utiliserons les données daily de l'API
            // Pour l'instant, affichons des données simulées
            val days = listOf("Aujourd'hui", "Demain", "J+2", "J+3", "J+4", "J+5", "J+6")
            days.forEachIndexed { index, day ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = day)
                    Row {
                        Text(
                            text = "${(weather.minTemp - index).toInt()}°",
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(text = " / ")
                        Text(
                            text = "${(weather.maxTemp + index).toInt()}°",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}