package com.example.myapplication.data.repository

import android.content.Context
import android.util.Log
import com.example.myapplication.data.network.GeocodingApiService
import com.example.myapplication.data.network.WeatherApiService
import com.example.myapplication.data.dao.FavoriteCityDao
import com.example.myapplication.data.dao.WeatherDao
import com.example.myapplication.data.model.*
import com.example.myapplication.utils.NetworkUtils

class WeatherRepository(
    private val weatherApiService: WeatherApiService,
    private val geocodingApiService: GeocodingApiService,
    private val favoriteCityDao: FavoriteCityDao,
    private val weatherDao: WeatherDao,
    private val context: Context
) {
    companion object {
        private const val CACHE_DURATION = 30 * 60 * 1000 // 30 minutes en millisecondes
    }

    suspend fun searchCities(query: String): List<GeocodingResultItem> {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            throw Exception("Pas de connexion internet")
        }

        return try {
            val response = geocodingApiService.searchCity(query)
            response.results
        } catch (e: Exception) {
            throw Exception("Erreur lors de la recherche: ${e.message}")
        }
    }

    fun getFavoriteCities() = favoriteCityDao.getAllFavoriteCitiesFlow()

    suspend fun addFavoriteCity(city: GeocodingResultItem) {
        val favoriteCity = FavoriteCity(
            cityId = city.id,
            name = city.name,
            latitude = city.latitude,
            longitude = city.longitude
        )
        favoriteCityDao.insertFavoriteCity(favoriteCity)
    }

    suspend fun removeFavoriteCity(cityId: String) {
        favoriteCityDao.deleteFavoriteCity(cityId)
    }

    suspend fun getWeatherForCity(cityId: String, latitude: Double, longitude: Double): Result<WeatherEntity> = runCatching {
        Log.d("WeatherRepository", "Fetching weather for city $cityId at $latitude, $longitude")
        
        if (!NetworkUtils.isNetworkAvailable(context)) {
            Log.d("WeatherRepository", "No network connection")
            val cachedWeather = weatherDao.getWeatherForCity(cityId)
            if (cachedWeather != null) {
                Log.d("WeatherRepository", "Returning cached weather: $cachedWeather")
                return@runCatching cachedWeather
            }
            throw Exception("Pas de connexion internet et aucune donnée en cache")
        }

        try {
            Log.d("WeatherRepository", "Making API call to Open-Meteo")
            val response = weatherApiService.getWeather(
                latitude = latitude,
                longitude = longitude
            )
            Log.d("WeatherRepository", "API response received: $response")
            
            val cityName = favoriteCityDao.getFavoriteCity(cityId)?.name ?: run {
                Log.d("WeatherRepository", "City name not found in favorites, using default")
                "Ville inconnue"
            }
            
            Log.d("WeatherRepository", "Current weather: ${response.current_weather}")
            Log.d("WeatherRepository", "Daily weather: ${response.daily}")
            Log.d("WeatherRepository", "Hourly weather: ${response.hourly}")
            
            val weather = WeatherEntity(
                cityId = cityId,
                cityName = cityName,
                temperature = response.current_weather.temperature,
                minTemp = response.daily.temperature_2m_min[0],
                maxTemp = response.daily.temperature_2m_max[0],
                condition = getWeatherDescription(response.current_weather.weathercode),
                windSpeed = response.current_weather.windspeed,
                timestamp = System.currentTimeMillis(),
                hourlyTemperatures = response.hourly.temperature_2m.take(24),
                hourlyTimes = response.hourly.time.take(24).map { time ->
                    time.substringAfterLast('T').substringBefore(':') + "h"
                }
            )
            Log.d("WeatherRepository", "Created WeatherEntity: $weather")
            
            weatherDao.insertWeather(weather)
            Log.d("WeatherRepository", "Weather saved to database")
            weather
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Error fetching weather", e)
            Log.e("WeatherRepository", "Error details: ${e.message}")
            e.printStackTrace()
            val cachedWeather = weatherDao.getWeatherForCity(cityId)
            if (cachedWeather != null) {
                Log.d("WeatherRepository", "Returning cached weather after error: $cachedWeather")
                return@runCatching cachedWeather
            }
            throw Exception("Erreur lors de la récupération des données météo: ${e.message}")
        }
    }

    private fun isCacheExpired(timestamp: Long): Boolean {
        val currentTime = System.currentTimeMillis()
        val cacheAge = currentTime - timestamp
        return cacheAge > CACHE_DURATION
    }

    private fun getWeatherDescription(code: Int): String {
        return when (code) {
            0 -> "Ciel dégagé"
            1, 2, 3 -> "Partiellement nuageux"
            45, 48 -> "Brouillard"
            51, 53, 55 -> "Bruine"
            61, 63, 65 -> "Pluie"
            71, 73, 75 -> "Neige"
            77 -> "Grains de neige"
            80, 81, 82 -> "Averses"
            85, 86 -> "Averses de neige"
            95 -> "Orage"
            96, 99 -> "Orage avec grêle"
            else -> "Conditions inconnues"
        }
    }
}

