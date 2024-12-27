package com.example.myapplication

import android.app.Application
import com.example.myapplication.data.database.AppDatabase
import com.example.myapplication.data.repository.WeatherRepository
import com.example.myapplication.data.network.ApiClient

class WeatherApplication : Application() {
    lateinit var weatherRepository: WeatherRepository

    override fun onCreate() {
        super.onCreate()
        initializeRepository()
    }

    private fun initializeRepository() {
        val database = AppDatabase.getDatabase(this)
        weatherRepository = WeatherRepository(
            weatherApiService = ApiClient.weatherService,
            geocodingApiService = ApiClient.geocodingService,
            favoriteCityDao = database.favoriteCityDao(),
            weatherDao = database.weatherDao()
        )
    }
} 