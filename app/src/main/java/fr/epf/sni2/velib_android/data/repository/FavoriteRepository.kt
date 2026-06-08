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

    /**
     * Met à jour les données (vélos, places, horodatage) des stations déjà en favori
     * à partir d'une liste fraîche. Renvoie le nombre de favoris rafraîchis.
     */
    suspend fun refreshFavorites(freshStations: List<Station>): Int {
        val favoriteIds = dao.getFavoriteIds().toSet()
        if (favoriteIds.isEmpty()) return 0
        val toUpdate = freshStations.filter { it.id in favoriteIds }
        toUpdate.forEach { dao.insert(it.toEntity()) }
        return toUpdate.size
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
