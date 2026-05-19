package fr.epf.sni2.velib_android.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StationStatusDto(
    @SerialName("station_id") val stationId: String,
    @SerialName("num_bikes_available") val numBikesAvailable: Int,
    @SerialName("num_bikes_available_types") val numBikesAvailableTypes: List<Map<String, Int>> = emptyList(),
    @SerialName("num_docks_available") val numDocksAvailable: Int,
    @SerialName("is_installed") val isInstalled: Int,
    @SerialName("is_renting") val isRenting: Int,
    @SerialName("is_returning") val isReturning: Int,
    @SerialName("last_reported") val lastReported: Long,
) {
    val mechanicalBikes: Int
        get() = numBikesAvailableTypes.firstOrNull { it.containsKey("mechanical") }?.get("mechanical") ?: 0

    val electricBikes: Int
        get() = numBikesAvailableTypes.firstOrNull { it.containsKey("ebike") }?.get("ebike") ?: 0
}
