package fr.epf.sni2.velib_android.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.epf.sni2.velib_android.data.repository.FavoriteRepository
import fr.epf.sni2.velib_android.data.repository.StationRepository
import fr.epf.sni2.velib_android.domain.model.FavoriteStation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val stationRepository: StationRepository,
) : ViewModel() {

    val favorites: StateFlow<List<FavoriteStation>> = favoriteRepository.observeFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    /** Met à jour les données des favoris depuis le réseau ; la liste se met à jour via le Flow. */
    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            stationRepository.getStations()
                .onSuccess { favoriteRepository.refreshFavorites(it) }
            _isRefreshing.value = false
        }
    }
}
