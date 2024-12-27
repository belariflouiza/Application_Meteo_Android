package com.example.myapplication.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object ApiClient {
    private const val WEATHER_BASE_URL = "https://api.open-meteo.com/v1/"
    private const val GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/v1/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val weatherRetrofit = Retrofit.Builder()
        .baseUrl(WEATHER_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val geocodingRetrofit = Retrofit.Builder()
        .baseUrl(GEOCODING_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val weatherService: WeatherApiService = weatherRetrofit.create(WeatherApiService::class.java)
    val geocodingService: GeocodingApiService = geocodingRetrofit.create(GeocodingApiService::class.java)
}