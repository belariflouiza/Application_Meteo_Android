package com.example.myapplication.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.data.model.FavoriteCity
import com.example.myapplication.data.model.GeocodingResultItem
import com.example.myapplication.data.model.WeatherEntity
import com.example.myapplication.ui.viewmodel.WeatherViewModel
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import android.content.Context
import android.location.LocationManager
import android.os.Looper
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.android.gms.location.*
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Info

import androidx.compose.ui.text.style.TextAlign
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: WeatherViewModel
) {
    val searchResults by viewModel.searchResults.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val weatherData by viewModel.weatherData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1976D2),  // Bleu foncé
            Color(0xFF64B5F6),  // Bleu clair
            Color(0xFF90CAF9)   // Bleu très clair
        )
    )

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1976D2),  // Bleu foncé
                            Color(0xFF64B5F6),  // Bleu clair
                            Color(0xFF90CAF9)   // Bleu très clair
                        )
                    )
                ),
                drawerContainerColor = Color.Transparent
            ) {
                Text(
                    "Villes favorites",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Divider(color = Color.White.copy(alpha = 0.2f))
                LazyColumn {
                    items(favorites) { favorite ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable {
                                    scope.launch {
                                        drawerState.close()
                                        navController.navigate("detail/${favorite.cityId}")
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.9f)
                            )
                        ) {
                            ListItem(
                                headlineContent = { 
                                    Text(favorite.name) 
                                },
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
                                            Icon(
                                                if (favorites.any { it.cityId == favorite.cityId })
                                                    Icons.Default.Favorite
                                                else
                                                    Icons.Default.FavoriteBorder,
                                                contentDescription = "Gérer les favoris"
                                            )
                                        }
                                        IconButton(onClick = {
                                            scope.launch {
                                                drawerState.close()
                                                navController.navigate("detail/${favorite.cityId}")
                                            }
                                        }) {
                                            Icon(Icons.Default.ArrowForward, "Voir détails")
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Météo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                "Météo",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
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
                        IconButton(
                            onClick = { viewModel.getWeatherForCurrentLocation() }
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = "Ma position"
                            )
                        }
                    }
                )
            }
        ) { padding ->
            if (isLandscape) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(gradientBackground)
                        .padding(padding)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text(
                                    text = "Météo en temps réel",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White
                                )
                                Text(
                                    text = "Restez informé des conditions météorologiques",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                        currentLocation?.let { location ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .clickable {
                                        viewModel.selectCity(location)
                                        navController.navigate("detail/${location.id}")
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.9f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.LocationOn,
                                                contentDescription = "Position actuelle",
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                            Text(
                                                text = location.name,
                                                style = MaterialTheme.typography.titleLarge
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.toggleFavorite(location) }
                                        ) {
                                            Icon(
                                                if (favorites.any { it.cityId == location.id })
                                                    Icons.Default.Favorite
                                                else
                                                    Icons.Default.FavoriteBorder,
                                                "Ajouter/Retirer des favoris"
                                            )
                                        }
                                    }
                                    weatherData[location.id]?.let { weather ->
                                        Text(
                                            text = "${weather.temperature.toInt()}°C",
                                            style = MaterialTheme.typography.displayLarge
                                        )
                                        Text(
                                            text = weather.condition,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Min: ${weather.minTemp.toInt()}°C")
                                            Text("Max: ${weather.maxTemp.toInt()}°C")
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { query ->
                                searchQuery = query
                                if (isNetworkAvailable(context)) {
                                    viewModel.searchCities(query)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.White,
                                disabledContainerColor = Color.White.copy(alpha = 0.6f)
                            ),
                            placeholder = { 
                                Text(
                                    if (!isNetworkAvailable(context)) 
                                        "Recherche impossible - Hors ligne" 
                                    else 
                                        "Rechercher une ville..."
                                ) 
                            },
                            leadingIcon = { 
                                Icon(Icons.Default.Search, "Rechercher") 
                            },
                            singleLine = true,
                            enabled = isNetworkAvailable(context)
                        )
                        
                        if (searchQuery.isEmpty()) {
                            if (favorites.isNotEmpty()) {
                                Text(
                                    text = "Vos favoris",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                                )
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp)
                                        .padding(horizontal = 16.dp)
                                ) {
                                    items(favorites) { favorite ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = Color.White.copy(alpha = 0.9f)
                                            )
                                        ) {
                                            ListItem(
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
                                                            val geocodingItem = GeocodingResultItem(
                                                                id = favorite.cityId,
                                                                name = favorite.name,
                                                                latitude = favorite.latitude,
                                                                longitude = favorite.longitude,
                                                                country = "",
                                                                admin1 = ""
                                                            )
                                                            viewModel.toggleFavorite(geocodingItem)
                                                        }) {
                                                            Icon(
                                                                if (favorites.any { it.cityId == favorite.cityId })
                                                                    Icons.Default.Favorite
                                                                else
                                                                    Icons.Default.FavoriteBorder,
                                                                contentDescription = "Gérer les favoris"
                                                            )
                                                        }
                                                        IconButton(onClick = {
                                                            navController.navigate("detail/${favorite.cityId}")
                                                        }) {
                                                            Icon(Icons.Default.ArrowForward, "Voir détails")
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.clickable {
                                                    navController.navigate("detail/${favorite.cityId}")
                                                }
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                        }
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                items(searchResults) { city ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.White.copy(alpha = 0.9f)
                                        )
                                    ) {
                                        ListItem(
                                            headlineContent = { Text(city.name) },
                                            supportingContent = { Text("${city.admin1}, ${city.country}") },
                                            trailingContent = {
                                                Row {
                                                    IconButton(
                                                        onClick = { viewModel.toggleFavorite(city) }
                                                    ) {
                                                        Icon(
                                                            if (favorites.any { it.cityId == city.id })
                                                                Icons.Default.Favorite
                                                            else
                                                                Icons.Default.FavoriteBorder,
                                                            "Ajouter/Retirer des favoris"
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            viewModel.selectCity(city)
                                                            navController.navigate("detail/${city.id}")
                                                        }
                                                    ) {
                                                        Icon(Icons.Default.ArrowForward, "Voir détails")
                                                    }
                                                }
                                            },
                                            modifier = Modifier.clickable {
                                                viewModel.selectCity(city)
                                                navController.navigate("detail/${city.id}")
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                            }
                        }

                        if (!isNetworkAvailable(context)) {
                            NetworkErrorMessage(
                                message = "Vérifiez votre connexion internet et réessayez",
                                onRetry = {
                                    if (searchQuery.isNotEmpty()) {
                                        viewModel.searchCities(searchQuery)
                                    } else {
                                        viewModel.getWeatherForCurrentLocation()
                                    }
                                }
                            )
                        } else if (error != null) {
                            NetworkErrorMessage(
                                message = error ?: "Une erreur est survenue, veuillez réessayer",
                                onRetry = {
                                    if (searchQuery.isNotEmpty()) {
                                        viewModel.searchCities(searchQuery)
                                    } else {
                                        viewModel.getWeatherForCurrentLocation()
                                    }
                                }
                            )
                        }

                        if (isLoading) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(gradientBackground)
                        .padding(padding)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "Météo en temps réel",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White
                            )
                            Text(
                                text = "Restez informé des conditions météorologiques",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    currentLocation?.let { location ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .clickable {
                                    viewModel.selectCity(location)
                                    navController.navigate("detail/${location.id}")
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.9f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.LocationOn,
                                            contentDescription = "Position actuelle",
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                        Text(
                                            text = location.name,
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.toggleFavorite(location) }
                                    ) {
                                        Icon(
                                            if (favorites.any { it.cityId == location.id })
                                                Icons.Default.Favorite
                                            else
                                                Icons.Default.FavoriteBorder,
                                            "Ajouter/Retirer des favoris"
                                        )
                                    }
                                }
                                weatherData[location.id]?.let { weather ->
                                    Text(
                                        text = "${weather.temperature.toInt()}°C",
                                        style = MaterialTheme.typography.displayLarge
                                    )
                                    Text(
                                        text = weather.condition,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Min: ${weather.minTemp.toInt()}°C")
                                        Text("Max: ${weather.maxTemp.toInt()}°C")
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { query ->
                            searchQuery = query
                            if (isNetworkAvailable(context)) {
                                viewModel.searchCities(query)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.White,
                            disabledContainerColor = Color.White.copy(alpha = 0.6f)
                        ),
                        placeholder = { 
                            Text(
                                if (!isNetworkAvailable(context)) 
                                    "Recherche impossible - Hors ligne" 
                                else 
                                    "Rechercher une ville..."
                            ) 
                        },
                        leadingIcon = { 
                            Icon(Icons.Default.Search, "Rechercher") 
                        },
                        singleLine = true,
                        enabled = isNetworkAvailable(context)
                    )

                    if (searchQuery.isEmpty()) {
                        if (favorites.isNotEmpty()) {
                            Text(
                                text = "Vos favoris",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                            )
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .padding(horizontal = 16.dp)
                            ) {
                                items(favorites) { favorite ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.White.copy(alpha = 0.9f)
                                        )
                                    ) {
                                        ListItem(
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
                                                        val geocodingItem = GeocodingResultItem(
                                                            id = favorite.cityId,
                                                            name = favorite.name,
                                                            latitude = favorite.latitude,
                                                            longitude = favorite.longitude,
                                                            country = "",
                                                            admin1 = ""
                                                        )
                                                        viewModel.toggleFavorite(geocodingItem)
                                                    }) {
                                                        Icon(
                                                            if (favorites.any { it.cityId == favorite.cityId })
                                                                Icons.Default.Favorite
                                                            else
                                                                Icons.Default.FavoriteBorder,
                                                            contentDescription = "Gérer les favoris"
                                                        )
                                                    }
                                                    IconButton(onClick = {
                                                        navController.navigate("detail/${favorite.cityId}")
                                                    }) {
                                                        Icon(Icons.Default.ArrowForward, "Voir détails")
                                                    }
                                                }
                                            },
                                            modifier = Modifier.clickable {
                                                navController.navigate("detail/${favorite.cityId}")
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            items(searchResults) { city ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White.copy(alpha = 0.9f)
                                    )
                                ) {
                                    ListItem(
                                        headlineContent = { Text(city.name) },
                                        supportingContent = { Text("${city.admin1}, ${city.country}") },
                                        trailingContent = {
                                            Row {
                                                IconButton(
                                                    onClick = { viewModel.toggleFavorite(city) }
                                                ) {
                                                    Icon(
                                                        if (favorites.any { it.cityId == city.id })
                                                            Icons.Default.Favorite
                                                        else
                                                            Icons.Default.FavoriteBorder,
                                                        "Ajouter/Retirer des favoris"
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        viewModel.selectCity(city)
                                                        navController.navigate("detail/${city.id}")
                                                    }
                                                ) {
                                                    Icon(Icons.Default.ArrowForward, "Voir détails")
                                                }
                                            }
                                        },
                                        modifier = Modifier.clickable {
                                            viewModel.selectCity(city)
                                            navController.navigate("detail/${city.id}")
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                        }
                    }

                    if (!isNetworkAvailable(context)) {
                        NetworkErrorMessage(
                            message = "Vérifiez votre connexion internet et réessayez",
                            onRetry = {
                                if (searchQuery.isNotEmpty()) {
                                    viewModel.searchCities(searchQuery)
                                } else {
                                    viewModel.getWeatherForCurrentLocation()
                                }
                            }
                        )
                    } else if (error != null) {
                        NetworkErrorMessage(
                            message = error ?: "Une erreur est survenue, veuillez réessayer",
                            onRetry = {
                                if (searchQuery.isNotEmpty()) {
                                    viewModel.searchCities(searchQuery)
                                } else {
                                    viewModel.getWeatherForCurrentLocation()
                                }
                            }
                        )
                    }

                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(16.dp)
                            )
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

@Composable
fun NetworkErrorMessage(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Pas de connexion internet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Réessayer")
            }
        }
    }
}

// Fonction pour vérifier la connexion (non-Composable)
fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork
    val capabilities = connectivityManager.getNetworkCapabilities(network)
    return capabilities != null && (
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    )
}