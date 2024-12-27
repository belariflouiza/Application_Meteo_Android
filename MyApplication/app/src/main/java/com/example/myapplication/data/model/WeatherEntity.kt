package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather")
data class WeatherEntity(
    @PrimaryKey
    val cityId: String,
    val cityName: String,
    val temperature: Double,
    val minTemp: Double,
    val maxTemp: Double,
    val condition: String,
    val windSpeed: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val hourlyTemperatures: List<Double> = emptyList(),
    val hourlyTimes: List<String> = emptyList()
) 