package com.example.myapplication.data.model


data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val hourly: HourlyData,
    val current_weather: CurrentWeather?
)

data class HourlyData(
    val temperature_2m: List<Double>,
    val wind_speed_10m: List<Double>,
    val weather_code: List<Int>,
    val time: List<String>
)

data class CurrentWeather(
    val temperature: Double,
    val windspeed: Double,
    val weathercode: Int,
    val time: String
)

data class WeatherInfo(
    val description: String,
    val imageRes: Int
)