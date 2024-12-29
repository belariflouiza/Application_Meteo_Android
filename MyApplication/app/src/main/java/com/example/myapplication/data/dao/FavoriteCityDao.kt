
package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.FavoriteCity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteCityDao {
    @Query("SELECT * FROM favorite_cities")
    suspend fun getAllFavoriteCities(): List<FavoriteCity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteCity(favoriteCity: FavoriteCity)

    @Query("DELETE FROM favorite_cities WHERE cityId = :cityId")
    suspend fun deleteFavoriteCityById(cityId: String)

    @Query("SELECT COUNT(*) > 0 FROM favorite_cities WHERE cityId = :cityId")
    suspend fun isCityFavorite(cityId: String): Boolean
}
