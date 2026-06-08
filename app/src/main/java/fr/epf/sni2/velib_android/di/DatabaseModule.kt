package fr.epf.sni2.velib_android.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.epf.sni2.velib_android.data.local.FavoriteStationDao
import fr.epf.sni2.velib_android.data.local.TripDao
import fr.epf.sni2.velib_android.data.local.VelibDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VelibDatabase =
        Room.databaseBuilder(context, VelibDatabase::class.java, "velib.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideFavoriteStationDao(database: VelibDatabase): FavoriteStationDao =
        database.favoriteStationDao()

    @Provides
    fun provideTripDao(database: VelibDatabase): TripDao =
        database.tripDao()
}
