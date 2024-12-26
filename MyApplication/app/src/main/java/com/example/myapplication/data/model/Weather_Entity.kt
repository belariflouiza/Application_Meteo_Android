package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather")
data class WeatherEntity(
    @PrimaryKey val cityName: String,
    val temperature: Double,
    val weatherCondition: String,
    val minTemperature: Double,
    val maxTemperature: Double,
    val windSpeed: Double
)