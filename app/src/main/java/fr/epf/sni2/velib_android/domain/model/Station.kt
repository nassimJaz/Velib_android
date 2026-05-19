package fr.epf.sni2.velib_android.domain.model

data class Station(
    val id: String,
    val name: String,
    val stationCode: String?,
    val lat: Double,
    val lon: Double,
    val capacity: Int,
    val mechanicalBikes: Int,
    val electricBikes: Int,
    val docksAvailable: Int,
    val isInstalled: Boolean,
    val isRenting: Boolean,
    val isReturning: Boolean,
    val lastReported: Long,
) {
    val totalBikes: Int get() = mechanicalBikes + electricBikes
    val isOperational: Boolean get() = isInstalled && isRenting && isReturning
}
