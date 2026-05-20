package fr.epf.sni2.velib_android.data.repository

import fr.epf.sni2.velib_android.data.local.FavoriteStationDao
import fr.epf.sni2.velib_android.data.local.FavoriteStationEntity
import fr.epf.sni2.velib_android.domain.model.FavoriteStation
import fr.epf.sni2.velib_android.domain.model.Station
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(
    private val dao: FavoriteStationDao,
) {

    fun isFavorite(id: String): Flow<Boolean> = dao.observeIsFavorite(id)

    fun observeFavorites(): Flow<List<FavoriteStation>> =
        dao.observeAll().map { entities -> entities.map { it.toFavoriteStation() } }

    /** Ajoute la station aux favoris si absente, la retire sinon. */
    suspend fun toggleFavorite(station: Station) {
        if (dao.exists(station.id)) {
            dao.delete(station.id)
        } else {
            dao.insert(station.toEntity())
        }
    }
}

private fun FavoriteStationEntity.toFavoriteStation(): FavoriteStation = FavoriteStation(
    station = Station(
        id = id,
        name = name,
        stationCode = stationCode,
        lat = lat,
        lon = lon,
        capacity = capacity,
        mechanicalBikes = mechanicalBikes,
        electricBikes = electricBikes,
        docksAvailable = docksAvailable,
        isInstalled = isInstalled,
        isRenting = isRenting,
        isReturning = isReturning,
        lastReported = lastReported,
    ),
    savedAt = savedAt,
)

private fun Station.toEntity(): FavoriteStationEntity = FavoriteStationEntity(
    id = id,
    name = name,
    stationCode = stationCode,
    lat = lat,
    lon = lon,
    capacity = capacity,
    mechanicalBikes = mechanicalBikes,
    electricBikes = electricBikes,
    docksAvailable = docksAvailable,
    isInstalled = isInstalled,
    isRenting = isRenting,
    isReturning = isReturning,
    lastReported = lastReported,
    savedAt = System.currentTimeMillis(),
)
