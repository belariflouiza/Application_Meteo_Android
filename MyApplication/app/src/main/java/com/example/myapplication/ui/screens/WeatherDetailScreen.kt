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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherDetailScreen(
    navController: NavController,
    viewModel: WeatherViewModel,
    cityId: String
) {
    val weatherData by viewModel.weatherData.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedCity?.name ?: "Détails météo") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Retour")
                    }
                },
                actions = {
                    selectedCity?.let { city ->
                        val isFavorite = favorites.any { it.cityId == city.id }
                        IconButton(onClick = { viewModel.toggleFavorite(city) }) {
                            Icon(
                                if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                if (isFavorite) "Retirer des favoris" else "Ajouter aux favoris"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        selectedCity?.let { city ->
            val cityWeather = weatherData[city.id]
            if (cityWeather != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Température actuelle et condition
                    CurrentWeatherCard(cityWeather)

                    // Prévisions horaires
                    HourlyForecastCard(cityWeather)

                    // Prévisions sur 7 jours
                    DailyForecastCard(cityWeather)
                }
            }
        }
    }
}

@Composable
fun CurrentWeatherCard(weather: WeatherEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (weather.condition.lowercase()) {
                "ciel dégagé" -> Color(0xFF87CEEB) // Bleu ciel
                "partiellement nuageux" -> Color(0xFFB0C4DE) // Bleu gris
                "brouillard" -> Color(0xFFDCDCDC) // Gris
                "pluie", "averses" -> Color(0xFF4682B4) // Bleu acier
                "neige", "averses de neige" -> Color(0xFFF0F8FF) // Blanc bleuté
                "orage" -> Color(0xFF483D8B) // Bleu foncé
                else -> MaterialTheme.colorScheme.primaryContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${weather.temperature.toInt()}°",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White
            )
            Text(
                text = weather.condition,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color.White
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherInfo(
                    icon = Icons.Default.KeyboardArrowDown,
                    value = "${weather.minTemp.toInt()}°",
                    label = "Min",
                    tint = Color.White
                )
                WeatherInfo(
                    icon = Icons.Default.KeyboardArrowUp,
                    value = "${weather.maxTemp.toInt()}°",
                    label = "Max",
                    tint = Color.White
                )
                WeatherInfo(
                    icon = Icons.Default.Info,
                    value = "${weather.windSpeed.toInt()} km/h",
                    label = "Vent",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun HourlyForecastCard(weather: WeatherEntity) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val scrollState = rememberScrollState()
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Prévisions horaires",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            val temperatures = weather.hourlyTemperatures.take(24)
            val times = weather.hourlyTimes.take(24)
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(start = 32.dp, end = 16.dp, bottom = 32.dp)
            ) {
                // Axe Y (températures)
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .align(Alignment.CenterStart)
                        .padding(end = 12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    val maxTemp = temperatures.maxOrNull()?.toInt() ?: 0
                    val minTemp = temperatures.minOrNull()?.toInt() ?: 0
                    val step = ((maxTemp - minTemp) / 4f).roundToInt().coerceAtLeast(1)
                    
                    for (temp in maxTemp downTo minTemp step step) {
                        Text(
                            text = "$temp°",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Zone scrollable pour le graphique
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(scrollState)
                ) {
                    Canvas(
                        modifier = Modifier
                            .width(1000.dp)
                            .fillMaxHeight()
                    ) {
                        val width = size.width
                        val height = size.height
                        val maxTemp = temperatures.maxOrNull()?.toFloat() ?: 0f
                        val minTemp = temperatures.minOrNull()?.toFloat() ?: 0f
                        val range = (maxTemp - minTemp + 4f)
                        val spaceBetweenPoints = width / (temperatures.size - 1)

                        // Lignes de grille horizontales avec effet dégradé
                        val numLines = 5
                        for (i in 0..numLines) {
                            val y = height * i / numLines
                            drawLine(
                                color = Color.LightGray.copy(alpha = 0.2f),
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 1f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                            )
                        }

                        // Points et lignes de température
                        val points = temperatures.mapIndexed { index, temp ->
                            Offset(
                                x = index * spaceBetweenPoints,
                                y = height - ((temp.toFloat() - minTemp) * height / range)
                            )
                        }

                        // Dessiner l'aire sous la courbe
                        val path = Path().apply {
                            moveTo(points.first().x, height)
                            points.forEach { point ->
                                lineTo(point.x, point.y)
                            }
                            lineTo(points.last().x, height)
                            close()
                        }

                        drawPath(
                            path = path,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.2f),
                                    primaryColor.copy(alpha = 0.05f)
                                )
                            )
                        )

                        // Dessiner les lignes entre les points
                        for (i in 0 until points.size - 1) {
                            drawLine(
                                color = primaryColor,
                                start = points[i],
                                end = points[i + 1],
                                strokeWidth = 2f
                            )
                        }

                        // Dessiner les points et températures
                        points.forEachIndexed { index, point ->
                            // Point extérieur (cercle blanc)
                            drawCircle(
                                color = Color.White,
                                radius = 6f,
                                center = point
                            )
                            // Point intérieur
                            drawCircle(
                                color = primaryColor,
                                radius = 4f,
                                center = point
                            )

                            // Température
                            drawContext.canvas.nativeCanvas.apply {
                                drawText(
                                    "${temperatures[index].toInt()}°",
                                    point.x,
                                    point.y - 15,
                                    textPaint
                                )
                            }
                        }
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
                    times.forEach { time ->
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
fun DailyForecastCard(weather: WeatherEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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