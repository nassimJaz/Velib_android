package fr.epf.sni2.velib_android.ui.navigation

object Routes {
    const val MAP = "map"
    const val DETAIL = "detail/{stationId}"
    const val FAVORITES = "favorites"
    const val NEARBY = "nearby"
    const val HISTORY = "history"

    const val ARG_STATION_ID = "stationId"

    fun detail(stationId: String): String = "detail/$stationId"
}
