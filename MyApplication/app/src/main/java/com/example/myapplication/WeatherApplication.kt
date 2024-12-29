package com.example.myapplication

import android.app.Application
import androidx.room.Room
import com.example.myapplication.data.database.AppDatabase
import com.example.myapplication.data.network.GeocodingApiService
import com.example.myapplication.data.network.WeatherApiService
import com.example.myapplication.data.repository.WeatherRepository
import com.example.myapplication.ui.viewmodel.WeatherViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherApplication : Application() {
    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "weather_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    private val weatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }

    private val geocodingApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://geocoding-api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeocodingApiService::class.java)
    }

    val repository by lazy {
        WeatherRepository(
            weatherApiService = weatherApiService,
            geocodingApiService = geocodingApiService,
            favoriteCityDao = database.favoriteCityDao(),
            weatherDao = database.weatherDao(),
            context = applicationContext
        )
    }

    val weatherViewModel by lazy {
        WeatherViewModel(
            repository = repository,
            context = applicationContext
        )
    }
} 