package fr.epf.sni2.velib_android.ui.screens.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import fr.epf.sni2.velib_android.R
import fr.epf.sni2.velib_android.domain.model.Station
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

private const val PARIS_LAT = 48.8566
private const val PARIS_LON = 2.3522
private const val DEFAULT_ZOOM = 13.0

// Couleurs des marqueurs selon l'état de la station
private val MARKER_AVAILABLE = Color.rgb(0, 178, 116)    // vert : vélos disponibles
private val MARKER_RETURN_ONLY = Color.rgb(245, 124, 0)  // orange : dépôt seulement
private val MARKER_UNAVAILABLE = Color.rgb(211, 47, 47)  // rouge : station indisponible

@Composable
fun OsmMapView(
    stations: List<Station>,
    modifier: Modifier = Modifier,
    showUserLocation: Boolean = false,
    onStationClick: (Station) -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(DEFAULT_ZOOM)
            controller.setCenter(GeoPoint(PARIS_LAT, PARIS_LON))
        }
    }

    // Icônes de marqueur préparées une fois pour chaque état
    val availableIcon = remember { tintedPin(context, MARKER_AVAILABLE) }
    val returnOnlyIcon = remember { tintedPin(context, MARKER_RETURN_ONLY) }
    val unavailableIcon = remember { tintedPin(context, MARKER_UNAVAILABLE) }

    // Overlay "point bleu" pour la position de l'utilisateur
    val locationOverlay = remember {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
            val dot = blueLocationDot(context)
            setPersonIcon(dot)
            setDirectionIcon(dot)
            setPersonAnchor(0.5f, 0.5f)
            setDirectionAnchor(0.5f, 0.5f)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    DisposableEffect(showUserLocation) {
        if (showUserLocation) {
            locationOverlay.enableMyLocation()
            if (locationOverlay !in mapView.overlays) {
                mapView.overlays.add(locationOverlay)
            }
        } else {
            locationOverlay.disableMyLocation()
            mapView.overlays.remove(locationOverlay)
        }
        mapView.invalidate()
        onDispose { locationOverlay.disableMyLocation() }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { view ->
            view.overlays.removeAll { it is Marker }
            stations.forEach { station ->
                val marker = Marker(view).apply {
                    position = GeoPoint(station.lat, station.lon)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    icon = when {
                        !station.isOperational -> unavailableIcon
                        station.totalBikes == 0 -> returnOnlyIcon
                        else -> availableIcon
                    }
                    setOnMarkerClickListener { _, _ ->
                        onStationClick(station)
                        true
                    }
                }
                view.overlays.add(marker)
            }
            view.invalidate()
        },
    )
}

/** Charge le pin de station et le teinte avec la couleur d'état. */
private fun tintedPin(context: Context, color: Int): Drawable {
    val drawable = ContextCompat.getDrawable(context, R.drawable.ic_station_marker)!!.mutate()
    drawable.setTint(color)
    return drawable
}

/** Dessine un point bleu type Google Maps : halo translucide, anneau blanc, point bleu. */
private fun blueLocationDot(context: Context): Bitmap {
    val size = (24 * context.resources.displayMetrics.density).toInt()
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val center = size / 2f
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    paint.color = Color.argb(48, 66, 133, 244)
    canvas.drawCircle(center, center, center, paint)
    paint.color = Color.WHITE
    canvas.drawCircle(center, center, size * 0.30f, paint)
    paint.color = Color.rgb(66, 133, 244)
    canvas.drawCircle(center, center, size * 0.22f, paint)

    return bitmap
}
