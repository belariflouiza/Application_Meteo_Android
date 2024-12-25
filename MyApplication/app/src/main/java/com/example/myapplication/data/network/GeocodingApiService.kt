package com.example.myapplication.data.network

import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApiService {
    @GET("v1/search")
    suspend fun getCoordinates(
        @Query("name") cityName: String
    ): GeocodingResponse
}

data class GeocodingResponse(
    val results: List<CityCoordinates>
)

data class CityCoordinates(
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val country: String
)