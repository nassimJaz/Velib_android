package fr.epf.sni2.velib_android.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteStationDao {

    @Query("SELECT * FROM favorite_stations ORDER BY name")
    fun observeAll(): Flow<List<FavoriteStationEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_stations WHERE id = :id)")
    fun observeIsFavorite(id: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_stations WHERE id = :id)")
    suspend fun exists(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(station: FavoriteStationEntity)

    @Query("DELETE FROM favorite_stations WHERE id = :id")
    suspend fun delete(id: String)
}
