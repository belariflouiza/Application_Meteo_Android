package com.example.myapplication.data.repository

import com.example.myapplication.data.dao.FavoriteCityDao
import com.example.myapplication.data.dao.WeatherDao
import com.example.myapplication.data.model.*
import com.example.myapplication.data.network.GeocodingApiService
import com.example.myapplication.data.network.WeatherApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository(
    private val weatherApiService: WeatherApiService,
    private val geocodingApiService: GeocodingApiService,
    private val favoriteCityDao: FavoriteCityDao,
    private val weatherDao: WeatherDao
) {
    suspend fun searchCity(query: String): Result<List<GeocodingResultItem>> = withContext(Dispatchers.IO) {
        try {
            val response = geocodingApiService.searchCity(query)
            Result.success(response.results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFavoriteCities(): List<FavoriteCity> = withContext(Dispatchers.IO) {
        favoriteCityDao.getAllFavoriteCities()
    }

    suspend fun addFavoriteCity(city: GeocodingResultItem) = withContext(Dispatchers.IO) {
        val favoriteCity = FavoriteCity(
            cityId = city.id,
            name = "${city.name}, ${city.country}",
            latitude = city.latitude,
            longitude = city.longitude
        )
        favoriteCityDao.insertFavoriteCity(favoriteCity)
    }

    suspend fun removeFavoriteCity(cityId: String) = withContext(Dispatchers.IO) {
        favoriteCityDao.deleteFavoriteCity(cityId)
        weatherDao.deleteWeather(cityId)
    }

    suspend fun getCurrentLocationWeather(latitude: Double, longitude: Double): Result<WeatherEntity> = withContext(Dispatchers.IO) {
        try {
            val weatherResponse = weatherApiService.getWeather(latitude, longitude)
            val currentWeather = weatherResponse.current_weather
                ?: return@withContext Result.failure(Exception("Weather data not available"))

            val weatherEntity = WeatherEntity(
                cityId = "current_location",
                cityName = "Position actuelle",
                temperature = currentWeather.temperature,
                condition = getWeatherDescription(currentWeather.weathercode),
                minTemp = weatherResponse.hourly.temperature_2m.minOrNull() ?: currentWeather.temperature,
                maxTemp = weatherResponse.hourly.temperature_2m.maxOrNull() ?: currentWeather.temperature,
                windSpeed = currentWeather.windspeed
            )

            weatherDao.insertWeather(weatherEntity)
            Result.success(weatherEntity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWeatherForCity(cityId: String, latitude: Double, longitude: Double): Result<WeatherEntity> = withContext(Dispatchers.IO) {
        try {
            // Essayer d'abord de récupérer depuis le cache
            weatherDao.getWeatherForCity(cityId)?.let {
                if (shouldUpdateCache(it.lastUpdated)) {
                    return@withContext fetchAndCacheWeather(cityId, latitude, longitude)
                }
                return@withContext Result.success(it)
            }

            // Si pas dans le cache, faire l'appel API
            return@withContext fetchAndCacheWeather(cityId, latitude, longitude)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchAndCacheWeather(cityId: String, latitude: Double, longitude: Double): Result<WeatherEntity> {
        return try {
            val response = weatherApiService.getWeather(latitude, longitude)
            val weather = WeatherEntity(
                cityId = cityId,
                temperature = response.current_weather.temperature,
                condition = getWeatherDescription(response.current_weather.weathercode),
                minTemp = response.daily.temperature_2m_min[0],
                maxTemp = response.daily.temperature_2m_max[0],
                windSpeed = response.current_weather.windspeed,
                lastUpdated = System.currentTimeMillis()
            )
            weatherDao.insertWeather(weather)
            Result.success(weather)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun shouldUpdateCache(lastUpdated: Long): Boolean {
        val currentTime = System.currentTimeMillis()
        val oneHourInMillis = 60 * 60 * 1000
        return (currentTime - lastUpdated) > oneHourInMillis
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

