package fr.epf.sni2.velib_android.ui.screens.nearby

import fr.epf.sni2.velib_android.domain.model.Station

/** Une station accompagnée de sa distance (en mètres) à la position de l'utilisateur. */
data class StationDistance(
    val station: Station,
    val distanceMeters: Float,
)

sealed interface NearbyUiState {
    data object Loading : NearbyUiState
    data class Success(val stations: List<StationDistance>) : NearbyUiState
    data class Error(val message: String) : NearbyUiState
}
