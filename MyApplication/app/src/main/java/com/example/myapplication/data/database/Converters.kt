package com.example.myapplication.data.database

import androidx.room.TypeConverter

@androidx.room.TypeConverters
class Converters {
    @TypeConverter
    fun fromString(value: String): List<String> {
        return value.split(",")
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        return list.joinToString(",")
    }

    @TypeConverter
    fun fromDoubleString(value: String): List<Double> {
        return value.split(",").map { it.toDouble() }
    }

    @TypeConverter
    fun fromDoubleList(list: List<Double>): String {
        return list.joinToString(",")
    }
} 