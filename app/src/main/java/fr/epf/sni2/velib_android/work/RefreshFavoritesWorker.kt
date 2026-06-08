package fr.epf.sni2.velib_android.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import fr.epf.sni2.velib_android.data.repository.FavoriteRepository
import fr.epf.sni2.velib_android.data.repository.StationRepository

/**
 * Tâche de fond périodique : récupère les dispos à jour et rafraîchit les
 * données des stations favorites stockées en local, pour qu'elles restent
 * fraîches même consultées hors connexion.
 */
@HiltWorker
class RefreshFavoritesWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val stationRepository: StationRepository,
    private val favoriteRepository: FavoriteRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val stations = stationRepository.getStations().getOrElse { return Result.retry() }
        favoriteRepository.refreshFavorites(stations)
        return Result.success()
    }

    companion object {
        const val UNIQUE_NAME = "refresh_favorites"
    }
}
