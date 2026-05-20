package fr.epf.sni2.velib_android.ui.screens.detail

import fr.epf.sni2.velib_android.domain.model.Station

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val station: Station) : DetailUiState
    data class Error(val message: String) : DetailUiState
}
