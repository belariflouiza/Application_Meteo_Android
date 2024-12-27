package com.example.myapplication.data.network

import com.example.myapplication.data.model.GeocodingResponse
import com.example.myapplication.data.model.GeocodingResultItem

import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApiService {
    @GET("v1/search")
    suspend fun searchCity(
        @Query("name") query: String,
        @Query("count") count: Int = 10,
        @Query("language") language: String = "fr"
    ): GeocodingResponse
}

data class GeocodingResponse(
    val results: List<GeocodingResultItem>
)