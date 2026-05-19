package fr.epf.sni2.velib_android.data.repository

import fr.epf.sni2.velib_android.data.remote.VelibApi
import fr.epf.sni2.velib_android.data.remote.dto.StationInformationDto
import fr.epf.sni2.velib_android.data.remote.dto.StationStatusDto
import fr.epf.sni2.velib_android.domain.model.Station
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.async
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StationRepository @Inject constructor(
    private val api: VelibApi,
) {

    suspend fun getStations(): Result<List<Station>> = runCatching {
        coroutineScope {
            val infoDeferred = async { api.getStationInformation().data.stations }
            val statusDeferred = async { api.getStationStatus().data.stations }
            val info = infoDeferred.await()
            val status = statusDeferred.await().associateBy { it.stationId }
            info.mapNotNull { dto ->
                val s = status[dto.stationId] ?: return@mapNotNull null
                merge(dto, s)
            }
        }
    }

    private fun merge(info: StationInformationDto, status: StationStatusDto): Station = Station(
        id = info.stationId,
        name = info.name,
        stationCode = info.stationCode,
        lat = info.lat,
        lon = info.lon,
        capacity = info.capacity,
        mechanicalBikes = status.mechanicalBikes,
        electricBikes = status.electricBikes,
        docksAvailable = status.numDocksAvailable,
        isInstalled = status.isInstalled == 1,
        isRenting = status.isRenting == 1,
        isReturning = status.isReturning == 1,
        lastReported = status.lastReported,
    )
}
