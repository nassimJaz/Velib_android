package fr.epf.sni2.velib_android.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GbfsResponse<T>(
    @SerialName("lastUpdatedOther") val lastUpdated: Long = 0,
    val ttl: Int = 0,
    val data: T,
)

@Serializable
data class StationsPayload<T>(
    val stations: List<T>,
)
