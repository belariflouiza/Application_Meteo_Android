package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather")
data class WeatherEntity(
    @PrimaryKey
    val cityId: String,
    val cityName: String = "",
    val temperature: Double,
    val condition: String,
    val minTemp: Double,
    val maxTemp: Double,
    val windSpeed: Double,
    val lastUpdated: Long = System.currentTimeMillis()
) 