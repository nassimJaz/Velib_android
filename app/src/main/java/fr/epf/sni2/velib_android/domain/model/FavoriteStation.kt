package fr.epf.sni2.velib_android.domain.model

/**
 * Station favorite telle qu'enregistrée en base.
 * [savedAt] = date du snapshot, affichée en mode hors-ligne.
 */
data class FavoriteStation(
    val station: Station,
    val savedAt: Long,
)
