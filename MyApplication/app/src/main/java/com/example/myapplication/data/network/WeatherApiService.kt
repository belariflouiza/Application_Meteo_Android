package com.example.myapplication.data.network

import com.example.myapplication.data.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("v1/forecast")
    suspend fun getWeatherForecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current_weather") currentWeather: Boolean = true, // Inclure current_weather
        @Query("hourly") hourly: String = "temperature_2m,weather_code,relative_humidity_2m,apparent_temperature,rain,wind_speed_10m",
        @Query("models") model: String = "meteofrance_seamless"
    ): WeatherResponse
}