package com.example.myapplication.data.model

data class GeocodingResultItem(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    val admin1: String? = null
)