package com.example.myapplication.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.FavoriteCity
import com.example.myapplication.data.model.WeatherEntity
import com.example.myapplication.data.model.GeocodingResultItem
import com.example.myapplication.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.util.Log
import kotlinx.coroutines.flow.StateFlow
import com.example.myapplication.utils.LocationUtils

class WeatherViewModel(
    private val repository: WeatherRepository,
    private val context: Context
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

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    private val _currentLocation = MutableStateFlow<GeocodingResultItem?>(null)
    val currentLocation = _currentLocation.asStateFlow()

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    init {
        loadFavorites()
    }

    fun searchCities(query: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _searchResults.value = repository.searchCities(query)
            } catch (e: Exception) {
                _error.value = "Erreur lors de la recherche: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getWeatherForCity(cityId: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val weather = repository.getWeather(latitude, longitude)
                _weatherData.value = _weatherData.value + (cityId to WeatherEntity(
                    cityId = cityId,
                    temperature = weather.current_weather.temperature,
                    windSpeed = weather.current_weather.windspeed,
                    condition = getWeatherCondition(weather.current_weather.weathercode),
                    minTemp = weather.daily.temperature_2m_min.firstOrNull() ?: 0.0,
                    maxTemp = weather.daily.temperature_2m_max.firstOrNull() ?: 0.0,
                    hourlyTemperatures = weather.hourly.temperature_2m,
                    hourlyTimes = weather.hourly.time.map { formatHourFromDateTime(it) }
                ))
            } catch (e: Exception) {
                _error.value = "Erreur lors de la récupération de la météo: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getWeatherForCurrentLocation() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val location = LocationUtils.getCurrentLocation(context)
                if (location != null) {
                    val currentLocation = GeocodingResultItem(
                        id = "current_location",
                        name = "Ma position actuelle",
                        latitude = location.latitude,
                        longitude = location.longitude,
                        country = "",
                        admin1 = ""
                    )
                    
                    repository.getWeatherForCity(currentLocation.id, location.latitude, location.longitude)
                        .onSuccess { weather ->
                            val currentWeatherData = _weatherData.value.toMutableMap()
                            currentWeatherData[currentLocation.id] = weather
                            _weatherData.value = currentWeatherData
                            _currentLocation.value = currentLocation
                            _selectedCity.value = currentLocation
                            _successMessage.value = "Position actuelle mise à jour"
                        }
                        .onFailure { e ->
                            _error.value = "Erreur lors de la récupération de la météo: ${e.message}"
                        }
                } else {
                    _error.value = "Impossible d'obtenir votre position. Vérifiez que le GPS est activé."
                }
            } catch (e: Exception) {
                _error.value = "Erreur lors de la récupération de la météo: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    private fun formatHourFromDateTime(dateTime: String): String {
        return try {
            dateTime.split("T")[1].substring(0, 5)
        } catch (e: Exception) {
            "00:00"
        }
    }

    private fun getWeatherCondition(code: Int): String {
        return when (code) {
            0 -> "Ciel dégagé"
            1, 2, 3 -> "Partiellement nuageux"
            45, 48 -> "Brouillard"
            51, 53, 55, 56, 57, 61, 63, 65, 66, 67 -> "Pluie"
            71, 73, 75, 77 -> "Neige"
            80, 81, 82 -> "Averses"
            85, 86 -> "Averses de neige"
            95 -> "Orage"
            96, 99 -> "Orage avec grêle"
            else -> "Indéterminé"
        }
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            try {
                _favorites.value = repository.getFavoriteCities()
            } catch (e: Exception) {
                _error.value = "Erreur lors du chargement des favoris: ${e.message}"
            }
        }
    }

    fun toggleFavorite(city: GeocodingResultItem) {
        viewModelScope.launch {
            try {
                val isFavorite = favorites.value.any { it.cityId == city.id }
                if (isFavorite) {
                    repository.removeFavoriteCity(city.id)
                    _successMessage.value = "Ville retirée des favoris"
                } else {
                    repository.addFavoriteCity(
                        FavoriteCity(
                            cityId = city.id,
                            name = city.name,
                            latitude = city.latitude,
                            longitude = city.longitude
                        )
                    )
                    _successMessage.value = "Ville ajoutée aux favoris"
                }
                loadFavorites() // Recharger la liste des favoris
            } catch (e: Exception) {
                _error.value = "Erreur lors de la modification des favoris: ${e.message}"
            }
        }
    }

    fun selectCity(city: GeocodingResultItem) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.getWeatherForCity(city.id, city.latitude, city.longitude)
                    .onSuccess { weather ->
                        val updatedWeatherData = _weatherData.value.toMutableMap()
                        updatedWeatherData[city.id] = weather
                        _weatherData.value = updatedWeatherData
                        _selectedCity.value = city
                    }
                    .onFailure { e ->
                        _error.value = "Erreur lors de la récupération de la météo: ${e.message}"
                    }
            } catch (e: Exception) {
                _error.value = "Erreur lors de la sélection de la ville: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
