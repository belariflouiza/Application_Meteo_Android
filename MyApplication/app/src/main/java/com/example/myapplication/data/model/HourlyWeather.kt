package com.example.myapplication.data.model

import java.time.LocalDateTime

data class HourlyWeather(
    val time: LocalDateTime,
    val temperature: Double,
    val weatherCode: Int,
    val weatherDescription: String
) 