package com.example.myapplication.data.model

data class GeocodingResultItem(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String,
    val country_code: String,
    val admin1: String? = null,
    val admin2: String? = null,
    val admin3: String? = null
)