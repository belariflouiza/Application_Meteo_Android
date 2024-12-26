package com.example.myapplication.data.repository

import com.example.myapplication.data.dao.FavoriteCityDao
import com.example.myapplication.data.dao.WeatherDao
import com.example.myapplication.data.model.FavoriteCity
import com.example.myapplication.data.model.WeatherEntity
import com.example.myapplication.data.model.WeatherResponse
import com.example.myapplication.data.network.ApiClient
import kotlinx.coroutines.flow.Flow

class WeatherRepository(
    private val weatherDao: WeatherDao,
    private val favoriteCityDao: FavoriteCityDao
) {
    suspend fun getWeather(cityName: String): WeatherEntity? {
        val cachedWeather = weatherDao.getWeather(cityName)
        if (cachedWeather != null) return cachedWeather

        val coordinates = ApiClient.geocodingApiService.getCoordinates(cityName).results.first()
        val weather = ApiClient.weatherApiService.getWeatherForecast(coordinates.latitude, coordinates.longitude)

        val weatherEntity = WeatherEntity(
            cityName = cityName,
            temperature = weather.current_weather?.temperature ?: 0.0,
            weatherCondition = getWeatherCondition(weather.current_weather?.weathercode),
            minTemperature = weather.hourly.temperature_2m.filterNotNull().minOrNull() ?: 0.0,
            maxTemperature = weather.hourly.temperature_2m.filterNotNull().maxOrNull() ?: 0.0,
            windSpeed = weather.current_weather?.windspeed ?: 0.0
        )

        weatherDao.insertWeather(weatherEntity)
        return weatherEntity
    }

    suspend fun addFavoriteCity(city: FavoriteCity) {
        favoriteCityDao.insertFavoriteCity(city)
    }

    suspend fun removeFavoriteCity(cityName: String) {
        favoriteCityDao.deleteFavoriteCity(cityName)
    }

    suspend fun getFavoriteCities(): List<FavoriteCity> {
        return favoriteCityDao.getFavoriteCities()
    }

    private fun getWeatherCondition(weatherCode: Int?): String {
        return when (weatherCode) {
            0 -> "Ensoleillé"
            1, 2, 3 -> "Nuageux"
            45, 48 -> "Brouillard"
            51, 53, 55 -> "Pluie légère"
            56, 57 -> "Pluie verglaçante"
            61, 63, 65 -> "Pluie"
            80, 81, 82 -> "Averses"
            else -> "Inconnu"
        }
    }
}
