package fr.epf.sni2.velib_android.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Snapshot d'une station mise en favori, stocké en base pour rester consultable hors-ligne.
 * [savedAt] = moment où le favori a été enregistré (pour le badge "données du...").
 */
@Entity(tableName = "favorite_stations")
data class FavoriteStationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val stationCode: String?,
    val lat: Double,
    val lon: Double,
    val capacity: Int,
    val mechanicalBikes: Int,
    val electricBikes: Int,
    val docksAvailable: Int,
    val isInstalled: Boolean,
    val isRenting: Boolean,
    val isReturning: Boolean,
    val lastReported: Long,
    val savedAt: Long,
)
