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
import com.example.myapplication.data.model.FavoriteCity
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
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: WeatherViewModel
) {
    val searchResults by viewModel.searchResults.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val weatherData by viewModel.weatherData.collectAsState()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var searchQuery by remember { mutableStateOf("") }
    var showSearchResults by remember { mutableStateOf(false) }
    
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "Villes favorites",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge
                )
                Divider()
                LazyColumn {
                    // Position actuelle (si disponible)
                    currentLocation?.let { location ->
                        item {
                            ListItem(
                                headlineContent = { Text(location.name) },
                                supportingContent = {
                                    weatherData[location.id]?.let { weather ->
                                        Column {
                                            Text("${weather.temperature.toInt()}°C, ${weather.condition}")
                                            Text(
                                                "Min: ${weather.minTemp.toInt()}°C, Max: ${weather.maxTemp.toInt()}°C",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                },
                                leadingContent = {
                                    Icon(Icons.Default.LocationOn, "Position actuelle")
                                },
                                trailingContent = {
                                    Row {
                                        IconButton(onClick = { viewModel.toggleFavorite(location) }) {
                                            Icon(
                                                if (favorites.any { it.cityId == location.id }) 
                                                    Icons.Default.Favorite 
                                                else 
                                                    Icons.Default.FavoriteBorder,
                                                "Favori"
                                            )
                                        }
                                        IconButton(onClick = {
                                            scope.launch {
                                                drawerState.close()
                                                navController.navigate("detail/${location.id}")
                                            }
                                        }) {
                                            Icon(Icons.Default.ArrowForward, "Voir détails")
                                        }
                                    }
                                }
                            )
                            Divider()
                        }
                    }
                    
                    // Villes favorites
                    items(favorites) { favorite ->
                        ListItem(
                            modifier = Modifier.clickable {
                                viewModel.selectCity(GeocodingResultItem(
                                    id = favorite.cityId,
                                    name = favorite.name,
                                    latitude = favorite.latitude,
                                    longitude = favorite.longitude,
                                    country = "",
                                    admin1 = ""
                                ))
                                scope.launch {
                                    drawerState.close()
                                    navController.navigate("detail/${favorite.cityId}")
                                }
                            },
                            headlineContent = { Text(favorite.name) },
                            supportingContent = {
                                weatherData[favorite.cityId]?.let { weather ->
                                    Column {
                                        Text("${weather.temperature.toInt()}°C, ${weather.condition}")
                                        Text(
                                            "Min: ${weather.minTemp.toInt()}°C, Max: ${weather.maxTemp.toInt()}°C",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            },
                            trailingContent = {
                                Row {
                                    IconButton(onClick = {
                                        viewModel.toggleFavorite(GeocodingResultItem(
                                            id = favorite.cityId,
                                            name = favorite.name,
                                            latitude = favorite.latitude,
                                            longitude = favorite.longitude,
                                            country = "",
                                            admin1 = ""
                                        ))
                                    }) {
                                        Icon(Icons.Default.Delete, "Supprimer des favoris")
                                    }
                                }
                            }
                        )
                        Divider()
                    }
                }
            }
        },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Météo") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open()
                                else drawerState.close()
                            }
                        }) {
                            Icon(Icons.Default.Menu, "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.getWeatherForCurrentLocation() }) {
                            Icon(Icons.Default.LocationOn, "Position actuelle")
                        }
                    }
                )
            }
        ) { padding ->
            if (isLandscape) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Zone de recherche et résultats (gauche)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { query ->
                                searchQuery = query
                                showSearchResults = query.length >= 2
                                if (query.length >= 2) {
                                    viewModel.searchCities(query)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            placeholder = { Text("Rechercher une ville...") },
                            leadingIcon = { Icon(Icons.Default.Search, "Rechercher") },
                            singleLine = true
                        )

                        if (showSearchResults) {
                            LazyColumn {
                                items(searchResults) { city ->
                                    ListItem(
                                        headlineContent = { Text(city.name) },
                                        supportingContent = { Text("${city.admin1}, ${city.country}") },
                                        trailingContent = {
                                            Row {
                                                IconButton(onClick = { viewModel.toggleFavorite(city) }) {
                                                    Icon(
                                                        if (favorites.any { it.cityId == city.id })
                                                            Icons.Default.Favorite
                                                        else
                                                            Icons.Default.FavoriteBorder,
                                                        "Favori"
                                                    )
                                                }
                                                IconButton(onClick = {
                                                    viewModel.selectCity(city)
                                                    navController.navigate("detail/${city.id}")
                                                }) {
                                                    Icon(Icons.Default.ArrowForward, "Voir détails")
                                                }
                                            }
                                        },
                                        modifier = Modifier.clickable {
                                            viewModel.selectCity(city)
                                            navController.navigate("detail/${city.id}")
                                        }
                                    )
                                    Divider()
                                }
                            }
                        }
                    }

                    // Position actuelle et favoris (droite)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                    ) {
                        currentLocation?.let { location ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                                    .clickable {
                                        viewModel.selectCity(location)
                                        navController.navigate("detail/${location.id}")
                                    }
                            ) {
                                ListItem(
                                    headlineContent = { Text(location.name) },
                                    supportingContent = {
                                        weatherData[location.id]?.let { weather ->
                                            Column {
                                                Text("${weather.temperature.toInt()}°C, ${weather.condition}")
                                                Text(
                                                    "Min: ${weather.minTemp.toInt()}°C, Max: ${weather.maxTemp.toInt()}°C",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    },
                                    leadingContent = {
                                        Icon(Icons.Default.LocationOn, "Position actuelle")
                                    },
                                    trailingContent = {
                                        IconButton(onClick = { viewModel.toggleFavorite(location) }) {
                                            Icon(
                                                if (favorites.any { it.cityId == location.id })
                                                    Icons.Default.Favorite
                                                else
                                                    Icons.Default.FavoriteBorder,
                                                "Favori"
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                // Layout portrait
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { query ->
                            searchQuery = query
                            showSearchResults = query.length >= 2
                            if (query.length >= 2) {
                                viewModel.searchCities(query)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        placeholder = { Text("Rechercher une ville...") },
                        leadingIcon = { Icon(Icons.Default.Search, "Rechercher") },
                        singleLine = true
                    )

                    if (showSearchResults) {
                        LazyColumn {
                            items(searchResults) { city ->
                                ListItem(
                                    headlineContent = { Text(city.name) },
                                    supportingContent = { Text("${city.admin1}, ${city.country}") },
                                    trailingContent = {
                                        Row {
                                            IconButton(onClick = { viewModel.toggleFavorite(city) }) {
                                                Icon(
                                                    if (favorites.any { it.cityId == city.id })
                                                        Icons.Default.Favorite
                                                    else
                                                        Icons.Default.FavoriteBorder,
                                                    "Favori"
                                                )
                                            }
                                            IconButton(onClick = {
                                                viewModel.selectCity(city)
                                                navController.navigate("detail/${city.id}")
                                            }) {
                                                Icon(Icons.Default.ArrowForward, "Voir détails")
                                            }
                                        }
                                    },
                                    modifier = Modifier.clickable {
                                        viewModel.selectCity(city)
                                        navController.navigate("detail/${city.id}")
                                    }
                                )
                                Divider()
                            }
                        }
                    } else {
                        currentLocation?.let { location ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .clickable {
                                        viewModel.selectCity(location)
                                        navController.navigate("detail/${location.id}")
                                    }
                            ) {
                                ListItem(
                                    headlineContent = { Text(location.name) },
                                    supportingContent = {
                                        weatherData[location.id]?.let { weather ->
                                            Column {
                                                Text("${weather.temperature.toInt()}°C, ${weather.condition}")
                                                Text(
                                                    "Min: ${weather.minTemp.toInt()}°C, Max: ${weather.maxTemp.toInt()}°C",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    },
                                    leadingContent = {
                                        Icon(Icons.Default.LocationOn, "Position actuelle")
                                    },
                                    trailingContent = {
                                        IconButton(onClick = { viewModel.toggleFavorite(location) }) {
                                            Icon(
                                                if (favorites.any { it.cityId == location.id })
                                                    Icons.Default.Favorite
                                                else
                                                    Icons.Default.FavoriteBorder,
                                                "Favori"
                                            )
                                        }
                                    }
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
private fun SearchResultItem(
    result: GeocodingResultItem,
    isFavorite: Boolean,
    onItemClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    viewModel: WeatherViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onItemClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.name,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        viewModel.toggleFavorite(result)
                    }
                ) {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Retirer des favoris" else "Ajouter aux favoris"
                    )
                }
                IconButton(onClick = onItemClick) {
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = "Voir les détails"
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherPreviewCard(
    result: GeocodingResultItem,
    weather: WeatherEntity?,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onDetailsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
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
                    text = result.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Row {
                    IconButton(
                        onClick = {
                            onFavoriteClick()
                            // La navigation vers les favoris se fait automatiquement via le ViewModel
                        }
                    ) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Retirer des favoris" else "Ajouter aux favoris"
                        )
                    }
                    IconButton(onClick = onDetailsClick) {
                        Icon(Icons.Default.ArrowForward, "Voir les détails")
                    }
                }
            }

            weather?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${it.temperature}°C",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = it.condition,
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
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
private fun FavoriteWeatherCard(
    city: FavoriteCity,
    weather: WeatherEntity?,
    onRemoveClick: () -> Unit,
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
                    text = city.name,
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onRemoveClick) {
                    Icon(Icons.Default.Delete, "Supprimer des favoris")
                }
            }

            weather?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${it.temperature}°C",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = it.condition,
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
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
