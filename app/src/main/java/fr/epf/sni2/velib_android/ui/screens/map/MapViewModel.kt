package fr.epf.sni2.velib_android.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.epf.sni2.velib_android.data.repository.StationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: StationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Loading)
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadStations()
    }

    fun loadStations() {
        viewModelScope.launch {
            _uiState.value = MapUiState.Loading
            repository.getStations()
                .onSuccess { _uiState.value = MapUiState.Success(it) }
                .onFailure { _uiState.value = MapUiState.Error(it.message ?: "Erreur inconnue") }
        }
    }

    /** Recharge les dispos sans masquer la carte déjà affichée. */
    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.getStations()
                .onSuccess { _uiState.value = MapUiState.Success(it) }
                .onFailure {
                    if (_uiState.value !is MapUiState.Success) {
                        _uiState.value = MapUiState.Error(it.message ?: "Erreur inconnue")
                    }
                }
            _isRefreshing.value = false
        }
    }
}
