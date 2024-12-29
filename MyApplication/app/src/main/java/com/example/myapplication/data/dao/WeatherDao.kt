

package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.WeatherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather_data WHERE cityId = :cityId")
    suspend fun getWeatherForCity(cityId: String): WeatherEntity?

    @Query("SELECT * FROM weather_data")
    suspend fun getAllWeather(): List<WeatherEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherEntity)

    @Query("DELETE FROM weather_data WHERE cityId = :cityId")
    suspend fun deleteWeatherForCity(cityId: String)
}
