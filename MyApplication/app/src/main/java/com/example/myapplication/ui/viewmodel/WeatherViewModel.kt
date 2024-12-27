package com.example.myapplication.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.FavoriteCity
import com.example.myapplication.data.model.GeocodingResultItem
import com.example.myapplication.data.model.WeatherEntity
import com.example.myapplication.data.repository.WeatherRepository
import com.example.myapplication.utils.LocationUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val repository: WeatherRepository,
    private val context: Context
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<GeocodingResultItem>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _favorites = MutableStateFlow<List<FavoriteCity>>(emptyList())
    val favorites = _favorites.asStateFlow()

    private val _weatherData = MutableStateFlow<Map<String, WeatherEntity>>(emptyMap())
    val weatherData = _weatherData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage = _successMessage.asStateFlow()

    private val _showSearchResults = MutableStateFlow(false)
    val showSearchResults = _showSearchResults.asStateFlow()

    private val _selectedCity = MutableStateFlow<GeocodingResultItem?>(null)
    val selectedCity = _selectedCity.asStateFlow()

    init {
        loadFavorites()
    }

    fun searchCities(query: String) {
        if (query.length < 2) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                val results = repository.searchCities(query)
                _searchResults.value = results
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Error searching cities", e)
                _error.value = "Erreur lors de la recherche : ${e.message}"
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getWeatherForCity(city: GeocodingResultItem) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _selectedCity.value = city
                repository.getWeatherForCity(city.id, city.latitude, city.longitude)
                    .onSuccess { weather ->
                        val currentWeatherData = _weatherData.value.toMutableMap()
                        currentWeatherData[city.id] = weather
                        _weatherData.value = currentWeatherData
                    }
                    .onFailure { e ->
                        _error.value = e.message ?: "Erreur lors de la récupération des données météo"
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur inattendue"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addFavoriteCity(city: GeocodingResultItem) {
        viewModelScope.launch {
            try {
                repository.addFavoriteCity(city)
                // S'assurer que nous avons les données météo
                getWeatherForCity(city)
                loadFavorites()
                _successMessage.value = "${city.name} ajoutée aux favoris"
            } catch (e: Exception) {
                _error.value = "Erreur lors de l'ajout aux favoris: ${e.message}"
            }
        }
    }

    fun removeFavoriteCity(cityId: String) {
        viewModelScope.launch {
            try {
                repository.removeFavoriteCity(cityId)
                // Mettre à jour la liste des favoris
                loadFavorites()
                _successMessage.value = "Ville retirée des favoris"
            } catch (e: Exception) {
                _error.value = "Erreur lors de la suppression des favoris: ${e.message}"
            }
        }
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            try {
                repository.getFavoriteCities().collect { cities ->
                    _favorites.value = cities
                    // Charger la météo pour chaque ville favorite
                    cities.forEach { city ->
                        val result = repository.getWeatherForCity(city.cityId, city.latitude, city.longitude)
                        result.onSuccess { weather ->
                            val newMap = _weatherData.value.toMutableMap()
                            newMap[city.cityId] = weather
                            _weatherData.value = newMap
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = "Erreur lors du chargement des favoris: ${e.message}"
            }
        }
    }

    fun setShowSearchResults(show: Boolean) {
        _showSearchResults.value = show
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    fun getWeatherForCurrentLocation() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("WeatherViewModel", "Getting current location...")
                val location = LocationUtils.getCurrentLocation(context)
                if (location != null) {
                    Log.d("WeatherViewModel", "Location received: ${location.latitude}, ${location.longitude}")
                    val currentLocation = GeocodingResultItem(
                        id = "current_location",
                        name = "Ma position actuelle",
                        latitude = location.latitude,
                        longitude = location.longitude,
                        country = "",
                        admin1 = "",
                        country_code = ""
                    )
                    setSelectedCity(currentLocation)
                    _successMessage.value = "Météo mise à jour pour votre position"
                } else {
                    Log.e("WeatherViewModel", "Location is null")
                    _error.value = "Impossible d'obtenir votre position. Vérifiez que le GPS est activé."
                }
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Error getting weather for current location", e)
                _error.value = "Erreur lors de la récupération de la météo: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite(cityId: String) {
        viewModelScope.launch {
            val isFavorite = _favorites.value.any { it.cityId == cityId }
            if (isFavorite) {
                repository.removeFavoriteCity(cityId)
                _successMessage.value = "Ville retirée des favoris"
            } else {
                val city = _selectedCity.value
                if (city != null) {
                    repository.addFavoriteCity(city)
                    _successMessage.value = "Ville ajoutée aux favoris"
                }
            }
        }
    }

    fun setSelectedCity(city: GeocodingResultItem) {
        _selectedCity.value = city
        getWeatherForCity(city)
    }
}
