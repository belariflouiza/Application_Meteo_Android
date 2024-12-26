package com.example.myapplication.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.data.model.WeatherEntity

@Dao
interface WeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherEntity)

    @Query("SELECT * FROM weather WHERE cityName = :cityName")
    suspend fun getWeather(cityName: String): WeatherEntity?

    @Query("DELETE FROM weather WHERE cityName = :cityName")
    suspend fun deleteWeather(cityName: String)
}