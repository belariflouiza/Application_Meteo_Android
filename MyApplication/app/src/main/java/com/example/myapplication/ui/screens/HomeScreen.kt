package com.example.myapplication.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.data.model.GeocodingResultItem
import com.example.myapplication.data.model.WeatherEntity
import com.example.myapplication.ui.viewmodel.WeatherViewModel
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.Context
import android.location.LocationManager
import android.os.Looper
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.android.gms.location.*
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Info

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: WeatherViewModel
) {
    val searchResults by viewModel.searchResults.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val weatherData by viewModel.weatherData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var showSearchResults by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Météo") },
                actions = {
                    IconButton(onClick = {
                        getCurrentLocation(context, fusedLocationClient) { location ->
                            location?.let {
                                viewModel.getCurrentLocationWeather(it.latitude, it.longitude)
                            }
                        }
                    }) {
                        Icon(Icons.Default.LocationOn, "Ma position")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Barre de recherche
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    if (it.length >= 2) {
                        showSearchResults = true
                        viewModel.searchCities(it)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Rechercher une ville...") },
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Rechercher"
                    )
                },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { 
                            searchQuery = ""
                            showSearchResults = false
                        }) {
                            Icon(Icons.Default.Clear, "Effacer")
                        }
                    }
                } else null,
                singleLine = true,
                shape = RoundedCornerShape(24.dp)
            )

            if (showSearchResults && searchQuery.isNotEmpty()) {
                // Résultats de recherche
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(searchResults) { result ->
                        SearchResultItem(
                            result = result,
                            onItemClick = {
                                viewModel.getWeatherForCity(result)
                                showSearchResults = false
                                searchQuery = ""
                            }
                        )
                    }
                }
            } else {
                // Liste des favoris
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(favorites) { favorite ->
                        WeatherCard(
                            weather = weatherData[favorite.cityId],
                            isFavorite = true,
                            onFavoriteClick = { viewModel.removeFavoriteCity(favorite.cityId) },
                            onCardClick = { navController.navigate("detail/${favorite.cityId}") }
                        )
                    }
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        error?.let { errorMessage ->
            AlertDialog(
                onDismissRequest = { viewModel.clearError() },
                title = { Text("Erreur") },
                text = { Text(errorMessage) },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
private fun SearchResultItem(
    result: GeocodingResultItem,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onItemClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = result.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${result.country} (${result.country_code})",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun WeatherCard(
    weather: WeatherEntity?,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onCardClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = weather?.cityName ?: "Chargement...",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Retirer des favoris" else "Ajouter aux favoris"
                    )
                }
            }

            weather?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${it.temperature}°C",
                    style = MaterialTheme.typography.displayMedium
                )
                Text(
                    text = it.condition,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    WeatherInfo(
                        icon = Icons.Default.KeyboardArrowDown,
                        label = "Min",
                        value = "${it.minTemp}°C"
                    )
                    WeatherInfo(
                        icon = Icons.Default.KeyboardArrowUp,
                        label = "Max",
                        value = "${it.maxTemp}°C"
                    )
                    WeatherInfo(
                        icon = Icons.Default.Info,
                        label = "Vent",
                        value = "${it.windSpeed} km/h"
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherInfo(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = label)
        Text(text = label, style = MaterialTheme.typography.bodySmall)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun getCurrentLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationResult: (Location?) -> Unit
) {
    // Vérifier les permissions
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
        onLocationResult(null)
        return
    }

    // Vérifier si le GPS est activé
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        onLocationResult(null)
        return
    }

    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
        .setWaitForAccurateLocation(false)
        .setMinUpdateIntervalMillis(5000)
        .build()

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                onLocationResult(location)
                fusedLocationClient.removeLocationUpdates(this)
            }
        }
    }

    try {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    } catch (e: SecurityException) {
        onLocationResult(null)
    }
}

