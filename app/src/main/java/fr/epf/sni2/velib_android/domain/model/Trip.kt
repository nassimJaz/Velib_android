package fr.epf.sni2.velib_android.domain.model

data class Trip(
    val id: Long,
    val departureName: String,
    val arrivalName: String,
    val distanceMeters: Double,
    val dateEpochMillis: Long,
)
