package fr.epf.sni2.velib_android.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.epf.sni2.velib_android.data.repository.StationRepository
import fr.epf.sni2.velib_android.ui.navigation.Routes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: StationRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val stationId: String = checkNotNull(savedStateHandle[Routes.ARG_STATION_ID])

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadStation()
    }

    fun loadStation() {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            repository.getStation(stationId)
                .onSuccess { _uiState.value = DetailUiState.Success(it) }
                .onFailure { _uiState.value = DetailUiState.Error(it.message ?: "Erreur inconnue") }
        }
    }
}
