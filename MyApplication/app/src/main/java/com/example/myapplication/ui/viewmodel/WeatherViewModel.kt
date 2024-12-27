package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.FavoriteCity
import com.example.myapplication.data.model.GeocodingResultItem
import com.example.myapplication.data.model.WeatherEntity
import com.example.myapplication.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val repository: WeatherRepository
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

    init {
        loadFavorites()
    }

    fun setShowSearchResults(show: Boolean) {
        _showSearchResults.value = show
    }

    fun searchCities(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.searchCity(query)
                    .onSuccess { _searchResults.value = it }
                    .onFailure { _error.value = it.message }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getWeatherForCity(city: GeocodingResultItem) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getWeatherForCity(city.id, city.latitude, city.longitude)
                    .onSuccess { weather ->
                        _weatherData.value = _weatherData.value + (city.id to weather)
                        addFavoriteCity(city)
                    }
                    .onFailure { _error.value = it.message }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getCurrentLocationWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getCurrentLocationWeather(latitude, longitude)
                    .onSuccess { weather ->
                        _weatherData.value = _weatherData.value + (weather.cityId to weather)
                    }
                    .onFailure { _error.value = it.message }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addFavoriteCity(city: GeocodingResultItem) {
        viewModelScope.launch {
            try {
                repository.addFavoriteCity(city)
                _successMessage.value = "Ville ajoutée aux favoris"
                _showSearchResults.value = false  // Ferme les résultats de recherche
                loadFavorites()  // Recharge la liste des favoris
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun removeFavoriteCity(cityId: String) {
        viewModelScope.launch {
            try {
                repository.removeFavoriteCity(cityId)
                _weatherData.value = _weatherData.value - cityId
                loadFavorites()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            try {
                val favorites = repository.getFavoriteCities()
                _favorites.value = favorites
                favorites.forEach { city ->
                    refreshWeatherData(city)
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    private fun refreshWeatherData(city: FavoriteCity) {
        viewModelScope.launch {
            repository.getWeatherForCity(city.cityId, city.latitude, city.longitude)
                .onSuccess { weather ->
                    _weatherData.value = _weatherData.value + (city.cityId to weather)
                }
                .onFailure { _error.value = it.message }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }
}
