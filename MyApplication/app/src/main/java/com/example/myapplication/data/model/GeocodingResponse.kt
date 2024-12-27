package com.example.myapplication.data.model

data class GeocodingResponse(
    val results: List<GeocodingResultItem>,
    val generationtime_ms: Double
)
