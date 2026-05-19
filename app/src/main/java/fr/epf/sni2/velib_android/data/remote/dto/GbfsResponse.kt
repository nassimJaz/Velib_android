package fr.epf.sni2.velib_android.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GbfsResponse<T>(
    @SerialName("last_updated") val lastUpdated: Long,
    val ttl: Int,
    val data: T,
)

@Serializable
data class StationsPayload<T>(
    val stations: List<T>,
)
