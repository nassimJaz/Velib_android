package fr.epf.sni2.velib_android.ui.screens.map

import fr.epf.sni2.velib_android.domain.model.Station

sealed interface MapUiState {
    data object Loading : MapUiState
    data class Success(val stations: List<Station>) : MapUiState
    data class Error(val message: String) : MapUiState
}
