package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.myapplication.data.database.Converters

@Entity(tableName = "weather_data")
@TypeConverters(Converters::class)
data class WeatherEntity(
    @PrimaryKey
    val cityId: String,
    val temperature: Double,
    val windSpeed: Double,
    val condition: String,
    val minTemp: Double,
    val maxTemp: Double,
    val hourlyTemperatures: List<Double>,
    val hourlyTimes: List<String>
) 