package com.example.myapplication.data.network


import com.example.myapplication.data.model.GeocodingResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface Geocoding {
    @GET("v1/search")
    suspend fun getCoordinates(
        @Query("name") cityName: String
    ): GeocodingResponse
}