package com.example.myapplication.data.repository

import android.content.Context
import android.util.Log
import com.example.myapplication.data.network.GeocodingApiService
import com.example.myapplication.data.network.WeatherApiService
import com.example.myapplication.data.dao.FavoriteCityDao
import com.example.myapplication.data.dao.WeatherDao
import com.example.myapplication.data.model.*
import com.example.myapplication.utils.NetworkUtils
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository(
    private val weatherApiService: WeatherApiService,
    private val geocodingApiService: GeocodingApiService,
    private val favoriteCityDao: FavoriteCityDao,
    private val weatherDao: WeatherDao,
    private val context: Context
) {
    companion object {
        private const val CACHE_DURATION = 30 * 60 * 1000 // 30 minutes en millisecondes
        private const val TAG = "WeatherRepository"
    }

    suspend fun searchCities(query: String): List<GeocodingResultItem> {
        return withContext(Dispatchers.IO) {
            try {
                val response = geocodingApiService.searchCity(query)
                response.results.map { result ->
                    GeocodingResultItem(
                        id = "${result.latitude}_${result.longitude}",
                        name = result.name,
                        latitude = result.latitude,
                        longitude = result.longitude,
                        country = result.country,
                        admin1 = result.admin1 ?: ""
                    )
                }
            } catch (e: Exception) {
                throw Exception("Erreur lors de la recherche des villes: ${e.message}")
            }
        }
    }

    suspend fun getWeather(latitude: Double, longitude: Double): WeatherResponse {
        return withContext(Dispatchers.IO) {
            try {
                weatherApiService.getWeather(
                    latitude = latitude,
                    longitude = longitude
                )
            } catch (e: Exception) {
                throw Exception("Erreur lors de la récupération de la météo: ${e.message}")
            }
        }
    }

    suspend fun getFavoriteCities(): List<FavoriteCity> {
        return withContext(Dispatchers.IO) {
            favoriteCityDao.getAllFavoriteCities()
        }
    }

    suspend fun addFavoriteCity(favoriteCity: FavoriteCity) {
        withContext(Dispatchers.IO) {
            favoriteCityDao.insertFavoriteCity(favoriteCity)
        }
    }

    suspend fun removeFavoriteCity(cityId: String) {
        withContext(Dispatchers.IO) {
            favoriteCityDao.deleteFavoriteCityById(cityId)
        }
    }

    suspend fun getWeatherForCity(cityId: String, latitude: Double, longitude: Double): Result<WeatherEntity> {
        return try {
            val weather = weatherApiService.getWeather(
                latitude = latitude,
                longitude = longitude
            )
            
            val weatherEntity = WeatherEntity(
                cityId = cityId,
                temperature = weather.current_weather.temperature,
                windSpeed = weather.current_weather.windspeed,
                condition = getWeatherCondition(weather.current_weather.weathercode),
                minTemp = weather.daily.temperature_2m_min.firstOrNull() ?: 0.0,
                maxTemp = weather.daily.temperature_2m_max.firstOrNull() ?: 0.0,
                hourlyTemperatures = weather.hourly.temperature_2m,
                hourlyTimes = weather.hourly.time.map { formatHourFromDateTime(it) }
            )

            // Sauvegarder en cache
            weatherDao.insertWeather(weatherEntity)
            
            Result.success(weatherEntity)
        } catch (e: Exception) {
            Result.failure(Exception("Erreur lors de la récupération de la météo: ${e.message}"))
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

    private fun formatHourFromDateTime(dateTime: String): String {
        return try {
            dateTime.split("T")[1].substring(0, 5)
        } catch (e: Exception) {
            "00:00"
        }
    }
}

