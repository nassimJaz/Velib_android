package fr.epf.sni2.velib_android.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StationInformationDto(
    @SerialName("station_id") val stationId: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    val capacity: Int,
    @SerialName("stationCode") val stationCode: String? = null,
)
