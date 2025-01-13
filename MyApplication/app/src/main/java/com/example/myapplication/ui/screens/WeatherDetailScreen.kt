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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.data.model.WeatherEntity
import com.example.myapplication.ui.viewmodel.WeatherViewModel
import kotlin.math.roundToInt
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.res.painterResource
import com.example.myapplication.data.model.getWeatherIcon
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.sp

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
    val favorites by viewModel.favorites.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()

    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1A237E),  // Bleu très foncé
            Color(0xFF3949AB),  // Bleu indigo
            Color(0xFF42A5F5),  // Bleu clair
            Color(0xFF90CAF9)   // Bleu très clair
        )
    )

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
                },
                actions = {
                    val isFavorite = favorites.any { it.cityId == cityId }
                    IconButton(
                        onClick = {
                            selectedCity?.let { city ->
                                viewModel.toggleFavorite(city)
                            }
                        }
                    ) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Retirer des favoris" else "Ajouter aux favoris",
                            tint = if (isFavorite) Color(0xFFE91E63) else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBackground)
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
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = weather.getWeatherIcon()),
                contentDescription = weather.condition,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "${weather.temperature.toInt()}°C",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
            )
            Text(
                text = weather.condition,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif
                ),
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

    // État pour le tooltip
    var selectedPoint by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    var selectedTemp by remember { mutableStateOf<Double?>(null) }
    var selectedTime by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = modifier.fillMaxWidth()
            .then(if (isLandscape) Modifier.height(300.dp) else Modifier.height(250.dp))
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
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val temperatures = weather.hourlyTemperatures.take(24)
                                val maxTemp = temperatures.maxOrNull() ?: 0.0
                                val minTemp = temperatures.minOrNull() ?: 0.0
                                val range = (maxTemp - minTemp).coerceAtLeast(1.0)
                                val width = size.width
                                val height = size.height

                                val points = temperatures.mapIndexed { index, temp ->
                                    val x = (width * index / 23).toFloat()
                                    val y = (height - (height * (temp - minTemp) / range)).toFloat()
                                    Triple(x, y, temp)
                                }

                                val closest = points.minByOrNull { point ->
                                    val dx = point.first - offset.x
                                    val dy = point.second - offset.y
                                    dx * dx + dy * dy
                                }

                                if (closest != null) {
                                    selectedPoint = Pair(closest.first, closest.second)
                                    selectedTemp = closest.third
                                    selectedTime = weather.hourlyTimes[points.indexOf(closest)]

                                }
                            }
                        }
                ) {
                    val temperatures = weather.hourlyTemperatures.take(24)
                    val maxTemp = temperatures.maxOrNull() ?: 0.0
                    val minTemp = temperatures.minOrNull() ?: 0.0
                    val range = (maxTemp - minTemp).coerceAtLeast(1.0)
                    val width = size.width
                    val height = size.height

                    // Dessiner les lignes de grille horizontales
                    (0..4).forEach { i ->
                        val y = height * i / 4
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.3f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                        )
                        // Afficher les températures sur l'axe Y
                        val temp = minTemp + (range * (4 - i) / 4)
                        drawContext.canvas.nativeCanvas.drawText(
                            "${temp.roundToInt()}°C",
                            10f,
                            y + 20f,
                            android.graphics.Paint().apply {
                                color = Color.Gray.toArgb()
                                textSize = 12.sp.toPx()
                                textAlign = android.graphics.Paint.Align.LEFT
                            }
                        )
                    }

                    // Dessiner la courbe
                    val points = temperatures.mapIndexed { index, temp ->
                        val x = width * index / 23
                        val y = height - (height * (temp - minTemp) / range).toFloat()
                        Offset(x, y)
                    }

                    // Dessiner la ligne du graphe
                    val path = Path().apply {
                        points.forEachIndexed { index, point ->
                            if (index == 0) moveTo(point.x, point.y)
                            else lineTo(point.x, point.y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = primaryColor,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 2.dp.toPx()
                        )
                    )

                    // Dessiner les points
                    points.forEachIndexed { index, point ->
                        drawCircle(
                            color = primaryColor,
                            radius = 4.dp.toPx(),
                            center = point
                        )
                    }

                    // Afficher le tooltip si un point est sélectionné
                    selectedPoint?.let { (x, y) ->
                        selectedTemp?.let { temp ->
                            selectedTime?.let { time ->
                                // Dessiner le fond du tooltip
                                drawRoundRect(
                                    color = Color.White,
                                    topLeft = Offset(x - 50f, y - 60f),
                                    size = androidx.compose.ui.geometry.Size(100f, 40f),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
                                )
                                // Dessiner le texte du tooltip
                                drawContext.canvas.nativeCanvas.drawText(
                                    "$time: ${temp.roundToInt()}°C",
                                    x,
                                    y - 30f,
                                    android.graphics.Paint().apply {
                                        color = Color.Black.toArgb()
                                        textSize = 14.sp.toPx()
                                        textAlign = android.graphics.Paint.Align.CENTER
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Axe X (heures)
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
            .wrapContentHeight() // Changé de fillMaxHeight à wrapContentHeight
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .wrapContentHeight() // Ajouté pour s'adapter au contenu
        ) {
            Text(
                text = "Prévisions sur 7 jours",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val days = listOf("Aujourd'hui", "Demain", "J+2", "J+3", "J+4", "J+5", "J+6")
            days.forEachIndexed { index, day ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp), // Augmenté le padding vertical
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically // Ajouté pour aligner verticalement
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = weather.getWeatherIcon()),
                            contentDescription = weather.condition,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = day,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${(weather.minTemp - index).toInt()}°",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(text = "/")
                        Text(
                            text = "${(weather.maxTemp + index).toInt()}°",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Ajouter un séparateur sauf pour le dernier élément
                if (index < days.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}
