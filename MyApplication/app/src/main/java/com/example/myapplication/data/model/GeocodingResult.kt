package com.example.myapplication.data.model

data class GeocodingResult(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String,
    val country_code: String
) 