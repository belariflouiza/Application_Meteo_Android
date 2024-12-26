package com.example.myapplication.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myapplication.data.dao.FavoriteCityDao
import com.example.myapplication.data.dao.WeatherDao
import com.example.myapplication.data.model.FavoriteCity
import com.example.myapplication.data.model.WeatherEntity

@Database(entities = [WeatherEntity::class, FavoriteCity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao
    abstract fun favoriteCityDao(): FavoriteCityDao
}