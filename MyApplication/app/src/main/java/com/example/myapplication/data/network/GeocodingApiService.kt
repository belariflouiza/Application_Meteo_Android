package com.example.myapplication.data.network

import com.example.myapplication.data.model.GeocodingResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApiService {
    @GET("search")
    suspend fun searchCity(
        @Query("name") query: String,
        @Query("count") count: Int = 5,
        @Query("language") language: String = "fr",
        @Query("format") format: String = "json"
    ): GeocodingResponse
}