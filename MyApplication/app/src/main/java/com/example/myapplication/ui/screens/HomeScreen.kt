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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: WeatherViewModel
) {
    val context = LocalContext.current
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showFavoritesDialog by remember { mutableStateOf(false) }
    val errorMessage by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var searchQuery by remember { mutableStateOf("") }
    
    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    ) { isGranted ->
        if (isGranted) {
            viewModel.getWeatherForCurrentLocation()
        } else {
            showPermissionDialog = true
        }
    }

    LaunchedEffect(errorMessage, successMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
            selectedCity?.let { city ->
                navController.navigate("detail/${city.id}")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Météo") },
                navigationIcon = {
                    IconButton(onClick = { showFavoritesDialog = true }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu favoris")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            when (locationPermissionState.status) {
                                is PermissionStatus.Granted -> {
                                    viewModel.getWeatherForCurrentLocation()
                                }
                                is PermissionStatus.Denied -> {
                                    if ((locationPermissionState.status as PermissionStatus.Denied).shouldShowRationale) {
                                        showPermissionDialog = true
                                    } else {
                                        locationPermissionState.launchPermissionRequest()
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Ma position"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Barre de recherche
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { 
                        searchQuery = it
                        viewModel.searchCities(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Rechercher une ville...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Rechercher") },
                    singleLine = true
                )

                // Résultats de recherche
                LazyColumn {
                    items(searchResults) { result ->
                        SearchResultItem(
                            result = result,
                            isFavorite = favorites.any { it.cityId == result.id },
                            onItemClick = {
                                viewModel.setSelectedCity(result)
                                navController.navigate("detail/${result.id}")
                            },
                            onFavoriteClick = { viewModel.addFavoriteCity(result) }
                        )
                    }
                }
            }

            // Indicateur de chargement
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.Center)
                )
            }

            // Dialog de permission
            if (showPermissionDialog) {
                AlertDialog(
                    onDismissRequest = { showPermissionDialog = false },
                    title = { Text("Permission de localisation") },
                    text = { 
                        Text("L'application a besoin de la permission de localisation pour afficher la météo de votre position actuelle.")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showPermissionDialog = false
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            }
                        ) {
                            Text("Paramètres")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPermissionDialog = false }) {
                            Text("Annuler")
                        }
                    }
                )
            }

            // Dialog des favoris
            if (showFavoritesDialog) {
                AlertDialog(
                    onDismissRequest = { showFavoritesDialog = false },
                    title = { Text("Villes favorites") },
                    text = {
                        if (favorites.isEmpty()) {
                            Text("Aucune ville favorite")
                        } else {
                            LazyColumn {
                                items(favorites) { favorite ->
                                    ListItem(
                                        headlineContent = { Text(favorite.name) },
                                        trailingContent = {
                                            Row {
                                                IconButton(
                                                    onClick = {
                                                        viewModel.setSelectedCity(GeocodingResultItem(
                                                            id = favorite.cityId,
                                                            name = favorite.name,
                                                            latitude = favorite.latitude,
                                                            longitude = favorite.longitude,
                                                            country = "",
                                                            admin1 = "",
                                                            country_code = ""
                                                        ))
                                                        navController.navigate("detail/${favorite.cityId}")
                                                        showFavoritesDialog = false
                                                    }
                                                ) {
                                                    Icon(
                                                        Icons.Default.ArrowForward,
                                                        contentDescription = "Voir détails"
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { 
                                                        viewModel.removeFavoriteCity(favorite.cityId)
                                                    }
                                                ) {
                                                    Icon(
                                                        Icons.Default.Delete,
                                                        contentDescription = "Supprimer des favoris"
                                                    )
                                                }
                                            }
                                        },
                                        modifier = Modifier.clickable {
                                            viewModel.setSelectedCity(GeocodingResultItem(
                                                id = favorite.cityId,
                                                name = favorite.name,
                                                latitude = favorite.latitude,
                                                longitude = favorite.longitude,
                                                country = "",
                                                admin1 = "",
                                                country_code = ""
                                            ))
                                            navController.navigate("detail/${favorite.cityId}")
                                            showFavoritesDialog = false
                                        }
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showFavoritesDialog = false }) {
                            Text("Fermer")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    result: GeocodingResultItem,
    isFavorite: Boolean,
    onItemClick: () -> Unit,
    onFavoriteClick: () -> Unit
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
                Text(
                    text = "${result.admin1 ?: ""}, ${result.country}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onFavoriteClick) {
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
