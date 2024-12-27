package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.WeatherEntity

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather WHERE cityId = :cityId")
    suspend fun getWeatherForCity(cityId: String): WeatherEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherEntity)

    @Query("DELETE FROM weather WHERE cityId = :cityId")
    suspend fun deleteWeather(cityId: String)
}