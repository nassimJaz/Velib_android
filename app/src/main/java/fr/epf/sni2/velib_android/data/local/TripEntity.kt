package fr.epf.sni2.velib_android.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val departureName: String,
    val arrivalName: String,
    val distanceMeters: Double,
    val dateEpochMillis: Long,
)
