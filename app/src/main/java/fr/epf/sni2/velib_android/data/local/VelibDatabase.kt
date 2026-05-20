package fr.epf.sni2.velib_android.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FavoriteStationEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class VelibDatabase : RoomDatabase() {
    abstract fun favoriteStationDao(): FavoriteStationDao
}
