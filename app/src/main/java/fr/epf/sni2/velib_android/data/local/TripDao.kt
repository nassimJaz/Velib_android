package fr.epf.sni2.velib_android.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    @Query("SELECT * FROM trips ORDER BY dateEpochMillis DESC")
    fun observeAll(): Flow<List<TripEntity>>

    @Insert
    suspend fun insert(trip: TripEntity)

    @Query("DELETE FROM trips WHERE id = :id")
    suspend fun delete(id: Long)
}
