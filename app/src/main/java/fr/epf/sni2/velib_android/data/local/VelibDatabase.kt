package fr.epf.sni2.velib_android.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FavoriteStationEntity::class, TripEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class VelibDatabase : RoomDatabase() {
    abstract fun favoriteStationDao(): FavoriteStationDao
    abstract fun tripDao(): TripDao
}
