package com.example.myapplication

import android.app.Application
import com.example.myapplication.data.database.AppDatabase
import com.example.myapplication.data.network.ApiClient
import com.example.myapplication.data.repository.WeatherRepository

class WeatherApplication : Application() {
    private val database by lazy { AppDatabase.getDatabase(this) }
    
    val weatherRepository by lazy {
        WeatherRepository(
            weatherApiService = ApiClient.weatherApiService,
            geocodingApiService = ApiClient.geocodingApiService,
            favoriteCityDao = database.favoriteCityDao(),
            weatherDao = database.weatherDao(),
            context = this
        )
    }
} 