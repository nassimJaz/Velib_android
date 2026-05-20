package fr.epf.sni2.velib_android.ui.screens.nearby

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.epf.sni2.velib_android.data.location.LocationProvider
import fr.epf.sni2.velib_android.data.repository.StationRepository
import fr.epf.sni2.velib_android.domain.model.Station
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NearbyViewModel @Inject constructor(
    private val stationRepository: StationRepository,
    private val locationProvider: LocationProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow<NearbyUiState>(NearbyUiState.Loading)
    val uiState: StateFlow<NearbyUiState> = _uiState.asStateFlow()

    private val _radiusMeters = MutableStateFlow(DEFAULT_RADIUS_METERS)
    val radiusMeters: StateFlow<Float> = _radiusMeters.asStateFlow()

    fun setRadius(meters: Float) {
        _radiusMeters.value = meters
    }

    /** À appeler une fois la permission de localisation accordée. */
    fun load() {
        viewModelScope.launch {
            _uiState.value = NearbyUiState.Loading
            val location = runCatching { locationProvider.getCurrentLocation() }.getOrNull()
            if (location == null) {
                _uiState.value = NearbyUiState.Error("Impossible de récupérer ta position GPS.")
                return@launch
            }
            stationRepository.getStations()
                .onSuccess { stations ->
                    _uiState.value = NearbyUiState.Success(stations.sortedByDistance(location))
                }
                .onFailure {
                    _uiState.value = NearbyUiState.Error(it.message ?: "Erreur inconnue")
                }
        }
    }

    private fun List<Station>.sortedByDistance(from: Location): List<StationDistance> =
        map { station ->
            val results = FloatArray(1)
            Location.distanceBetween(
                from.latitude, from.longitude,
                station.lat, station.lon,
                results,
            )
            StationDistance(station, results[0])
        }.sortedBy { it.distanceMeters }

    companion object {
        const val MIN_RADIUS_METERS = 200f
        const val MAX_RADIUS_METERS = 2_000f
        const val DEFAULT_RADIUS_METERS = 500f
    }
}
