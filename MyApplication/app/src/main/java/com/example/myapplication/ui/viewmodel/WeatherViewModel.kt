package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.FavoriteCity
import com.example.myapplication.data.model.WeatherEntity
import com.example.myapplication.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {

    // État pour stocker les données météo
    private val _weather = MutableStateFlow<WeatherEntity?>(null)
    val weather: StateFlow<WeatherEntity?> = _weather

    // État pour stocker les villes favorites
    private val _favoriteCities = MutableStateFlow<List<FavoriteCity>>(emptyList())
    val favoriteCities: StateFlow<List<FavoriteCity>> = _favoriteCities

    // Fonction pour récupérer la météo d'une ville
    fun getWeather(cityName: String) {
        viewModelScope.launch {
            _weather.value = repository.getWeather(cityName)
        }
    }

    // Fonction pour ajouter une ville aux favoris
    fun addFavoriteCity(city: FavoriteCity) {
        viewModelScope.launch {
            repository.addFavoriteCity(city)
            // Mettre à jour la liste des favoris après l'ajout
            _favoriteCities.value = repository.getFavoriteCities()
        }
    }

    // Fonction pour supprimer une ville des favoris
    fun removeFavoriteCity(cityName: String) {
        viewModelScope.launch {
            repository.removeFavoriteCity(cityName)
            // Mettre à jour la liste des favoris après la suppression
            _favoriteCities.value = repository.getFavoriteCities()
        }
    }

    // Fonction pour charger la liste des villes favorites
    fun loadFavoriteCities() {
        viewModelScope.launch {
            // Charger la liste des favoris depuis le repository
            _favoriteCities.value = repository.getFavoriteCities()
        }
    }
}
