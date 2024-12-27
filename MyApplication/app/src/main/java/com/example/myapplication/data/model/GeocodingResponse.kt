package com.example.myapplication.data.model

data class GeocodingResponse(
    val results: List<GeocodingResultItem>,
    val generationtime_ms: Double
)

data class GeocodingResultItem(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String,
    val country_code: String
)