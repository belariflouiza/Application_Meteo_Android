package com.example.myapplication.data.model

data class GeocodingResponse(
    val results: List<GeocodingResult>
)

data class GeocodingResult(
    val name: String,
    val latitude: Double,
    val longitude: Double
)