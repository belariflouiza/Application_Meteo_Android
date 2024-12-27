package com.example.myapplication.data.model

data class DailyForecast(
    val date: String,
    val minTemp: Double,
    val maxTemp: Double
)

data class HourlyForecast(
    val time: String,
    val temperature: Double,
    val weatherCode: Int
) 