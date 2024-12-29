package com.example.myapplication.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.FavoriteCity
import com.example.myapplication.data.model.WeatherEntity
import com.example.myapplication.data.model.GeocodingResultItem
import com.example.myapplication.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import android.util.Log

class WeatherViewModel(
    private val context: Context,
    private val repository: WeatherRepository
) : ViewModel() {

    private val _selectedCity = MutableStateFlow<GeocodingResultItem?>(null)
    val selectedCity: StateFlow<GeocodingResultItem?> = _selectedCity

    private val _weatherData = MutableStateFlow<Map<String, WeatherEntity>>(emptyMap())
    val weatherData: StateFlow<Map<String, WeatherEntity>> = _weatherData

    private val _searchResults = MutableStateFlow<List<GeocodingResultItem>>(emptyList())
    val searchResults: StateFlow<List<GeocodingResultItem>> = _searchResults

    private val _favorites = MutableStateFlow<List<FavoriteCity>>(emptyList())
    val favorites: StateFlow<List<FavoriteCity>> = _favorites

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _currentLocation = MutableStateFlow<GeocodingResultItem?>(null)
    val currentLocation: StateFlow<GeocodingResultItem?> = _currentLocation

    init {
        loadFavorites()
    }

    fun searchCities(query: String) {
        if (query.length < 2) return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _searchResults.value = repository.searchCities(query)
            } catch (e: Exception) {
                _error.value = "Erreur lors de la recherche: ${e.message}"
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getWeatherForCurrentLocation() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {
                    _error.value = "Permission de localisation nécessaire"
                    return@launch
                }

                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        viewModelScope.launch {
                            try {
                                val currentLocation = GeocodingResultItem(
                                    id = "current_location",
                                    name = "Ma position",
                                    latitude = location.latitude,
                                    longitude = location.longitude,
                                    country = "",
                                    admin1 = ""
                                )

                                _currentLocation.value = currentLocation
                                loadWeatherForCity(currentLocation.id, location.latitude, location.longitude)

                                try {
                                    val cities = repository.searchCities(
                                        "${location.latitude},${location.longitude}"
                                    )
                                    if (cities.isNotEmpty()) {
                                        val updatedLocation = currentLocation.copy(
                                            name = cities.first().name,
                                            country = cities.first().country,
                                            admin1 = cities.first().admin1
                                        )
                                        _currentLocation.value = updatedLocation
                                    }
                                } catch (e: Exception) {
                                    Log.e("WeatherViewModel", "Erreur lors de la récupération du nom de la ville", e)
                                }
                            } catch (e: Exception) {
                                _error.value = "Erreur lors de la récupération de la météo: ${e.message}"
                            }
                        }
                    } else {
                        _error.value = "Impossible d'obtenir la position actuelle"
                    }
                }.addOnFailureListener { e ->
                    _error.value = "Erreur de localisation: ${e.message}"
                }
            } catch (e: Exception) {
                _error.value = "Erreur lors de la localisation: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            try {
                _favorites.value = repository.getFavoriteCities()
                _favorites.value.forEach { favorite ->
                    if (!_weatherData.value.containsKey(favorite.cityId)) {
                        loadWeatherForCity(favorite.cityId, favorite.latitude, favorite.longitude)
                    }
                }
            } catch (e: Exception) {
                _error.value = "Erreur lors du chargement des favoris: ${e.message}"
            }
        }
    }

    fun loadWeatherForCity(cityId: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                repository.getWeatherForCity(cityId, latitude, longitude).fold(
                    onSuccess = { weather ->
                        _weatherData.value = _weatherData.value + (cityId to weather)
                        _error.value = null
                    },
                    onFailure = { e ->
                        _error.value = "Erreur lors du chargement de la météo: ${e.message}"
                    }
                )
            } catch (e: Exception) {
                _error.value = "Erreur lors du chargement de la météo: ${e.message}"
            }
        }
    }

    fun toggleFavorite(city: GeocodingResultItem) {
        viewModelScope.launch {
            try {
                val isFavorite = _favorites.value.any { it.cityId == city.id }
                if (isFavorite) {
                    repository.removeFavoriteCity(city.id)
                } else {
                    repository.addFavoriteCity(
                        FavoriteCity(
                            cityId = city.id,
                            name = city.name,
                            latitude = city.latitude,
                            longitude = city.longitude
                        )
                    )
                }
                loadFavorites()
            } catch (e: Exception) {
                _error.value = "Erreur lors de la modification des favoris: ${e.message}"
            }
        }
    }

    fun selectCity(city: GeocodingResultItem) {
        _selectedCity.value = city
        loadWeatherForCity(city.id, city.latitude, city.longitude)
    }

    fun clearError() {
        _error.value = null
    }

    fun loadWeatherDetails(cityId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Chercher d'abord dans les favoris
                val favoriteCity = _favorites.value.find { it.cityId == cityId }
                if (favoriteCity != null) {
                    loadWeatherForCity(cityId, favoriteCity.latitude, favoriteCity.longitude)
                    return@launch
                }

                // Si ce n'est pas un favori, vérifier si c'est la position actuelle
                val currentLoc = _currentLocation.value
                if (currentLoc?.id == cityId) {
                    loadWeatherForCity(cityId, currentLoc.latitude, currentLoc.longitude)
                    return@launch
                }

                // Si on ne trouve pas la ville, on peut chercher dans les résultats de recherche
                val searchResult = _searchResults.value.find { it.id == cityId }
                if (searchResult != null) {
                    loadWeatherForCity(cityId, searchResult.latitude, searchResult.longitude)
                    return@launch
                }

                // Si on ne trouve pas la ville du tout
                _error.value = "Ville non trouvée"
            } catch (e: Exception) {
                _error.value = "Erreur lors du chargement des détails: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}