package fr.epf.sni2.velib_android.ui.screens.history

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.epf.sni2.velib_android.data.repository.StationRepository
import fr.epf.sni2.velib_android.data.repository.TripRepository
import fr.epf.sni2.velib_android.domain.model.Station
import fr.epf.sni2.velib_android.domain.model.Trip
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Statistiques agrégées sur l'ensemble des trajets enregistrés. */
data class TripStats(
    val tripCount: Int,
    val totalKm: Double,
    val mostUsedStation: String?,
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val stationRepository: StationRepository,
) : ViewModel() {

    val trips: StateFlow<List<Trip>> = tripRepository.observeTrips()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val stats: StateFlow<TripStats> = tripRepository.observeTrips()
        .map { it.toStats() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TripStats(0, 0.0, null))

    private val _stations = MutableStateFlow<List<Station>>(emptyList())
    val stations: StateFlow<List<Station>> = _stations.asStateFlow()

    init {
        loadStations()
    }

    fun loadStations() {
        viewModelScope.launch {
            stationRepository.getStations().onSuccess { _stations.value = it.sortedBy { s -> s.name } }
        }
    }

    fun addTrip(departure: Station, arrival: Station, dateEpochMillis: Long) {
        viewModelScope.launch {
            val result = FloatArray(1)
            Location.distanceBetween(
                departure.lat, departure.lon,
                arrival.lat, arrival.lon,
                result,
            )
            tripRepository.addTrip(
                departureName = departure.name,
                arrivalName = arrival.name,
                distanceMeters = result[0].toDouble(),
                dateEpochMillis = dateEpochMillis,
            )
        }
    }

    fun deleteTrip(id: Long) {
        viewModelScope.launch { tripRepository.deleteTrip(id) }
    }

    private fun List<Trip>.toStats(): TripStats {
        val totalMeters = sumOf { it.distanceMeters }
        val mostUsed = flatMap { listOf(it.departureName, it.arrivalName) }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
        return TripStats(
            tripCount = size,
            totalKm = totalMeters / 1000.0,
            mostUsedStation = mostUsed,
        )
    }
}
