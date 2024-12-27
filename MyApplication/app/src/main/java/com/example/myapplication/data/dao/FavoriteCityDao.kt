package com.example.myapplication.data.dao
import androidx.room.*
import com.example.myapplication.data.model.FavoriteCity


@Dao
interface FavoriteCityDao {
    @Query("SELECT * FROM favorite_cities")
    suspend fun getAllFavoriteCities(): List<FavoriteCity>

    @Query("SELECT * FROM favorite_cities WHERE cityId = :cityId")
    suspend fun getFavoriteCity(cityId: String): FavoriteCity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteCity(city: FavoriteCity)

    @Query("DELETE FROM favorite_cities WHERE cityId = :cityId")
    suspend fun deleteFavoriteCity(cityId: String)
}