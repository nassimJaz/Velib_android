package fr.epf.sni2.velib_android.data.repository

import fr.epf.sni2.velib_android.data.local.TripDao
import fr.epf.sni2.velib_android.data.local.TripEntity
import fr.epf.sni2.velib_android.domain.model.Trip
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripRepository @Inject constructor(
    private val dao: TripDao,
) {

    fun observeTrips(): Flow<List<Trip>> =
        dao.observeAll().map { entities -> entities.map { it.toTrip() } }

    suspend fun addTrip(
        departureName: String,
        arrivalName: String,
        distanceMeters: Double,
        dateEpochMillis: Long,
    ) {
        dao.insert(
            TripEntity(
                departureName = departureName,
                arrivalName = arrivalName,
                distanceMeters = distanceMeters,
                dateEpochMillis = dateEpochMillis,
            ),
        )
    }

    suspend fun deleteTrip(id: Long) = dao.delete(id)
}

private fun TripEntity.toTrip(): Trip = Trip(
    id = id,
    departureName = departureName,
    arrivalName = arrivalName,
    distanceMeters = distanceMeters,
    dateEpochMillis = dateEpochMillis,
)
