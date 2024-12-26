package com.example.myapplication.data.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.data.model.FavoriteCity

@Dao
interface FavoriteCityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteCity(city: FavoriteCity)

    @Query("SELECT * FROM favorite_cities")
    suspend fun getFavoriteCities(): List<FavoriteCity>

    @Query("DELETE FROM favorite_cities WHERE cityName = :cityName")
    suspend fun deleteFavoriteCity(cityName: String)
}