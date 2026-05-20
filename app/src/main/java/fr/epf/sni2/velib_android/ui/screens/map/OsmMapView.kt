package fr.epf.sni2.velib_android.ui.screens.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
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
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

private const val PARIS_LAT = 48.8566
private const val PARIS_LON = 2.3522
private const val DEFAULT_ZOOM = 13.0

// En dessous de ce zoom, les stations sont de petits points ; au-dessus, des pins complets
private const val DETAIL_ZOOM = 15.0

// Couleurs des marqueurs selon l'état de la station
private val MARKER_AVAILABLE = Color.rgb(0, 178, 116)    // vert : vélos disponibles
private val MARKER_RETURN_ONLY = Color.rgb(245, 124, 0)  // orange : dépôt seulement
private val MARKER_UNAVAILABLE = Color.rgb(211, 47, 47)  // rouge : station indisponible

private enum class StationCategory { AVAILABLE, RETURN_ONLY, UNAVAILABLE }

private val categoryColors = mapOf(
    StationCategory.AVAILABLE to MARKER_AVAILABLE,
    StationCategory.RETURN_ONLY to MARKER_RETURN_ONLY,
    StationCategory.UNAVAILABLE to MARKER_UNAVAILABLE,
)

// Pins vectoriels avec contour blanc, un par état
private val pinDrawableRes = mapOf(
    StationCategory.AVAILABLE to R.drawable.ic_marker_available,
    StationCategory.RETURN_ONLY to R.drawable.ic_marker_return_only,
    StationCategory.UNAVAILABLE to R.drawable.ic_marker_unavailable,
)

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

    // Icônes préparées une fois : un pin et un point pour chaque état
    val pinIcons = remember { pinDrawableRes.mapValues { ContextCompat.getDrawable(context, it.value)!! } }
    val dotIcons = remember { categoryColors.mapValues { coloredDot(context, it.value) } }

    // Marqueurs gardés en mémoire pour pouvoir rebasculer leur icône au zoom
    val markers = remember { mutableListOf<Pair<Marker, StationCategory>>() }

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

    // Bascule point <-> pin quand le zoom franchit le seuil
    DisposableEffect(mapView) {
        var detailed = mapView.zoomLevelDouble >= DETAIL_ZOOM
        val listener = object : MapListener {
            override fun onZoom(event: ZoomEvent): Boolean {
                val nowDetailed = event.zoomLevel >= DETAIL_ZOOM
                if (nowDetailed != detailed) {
                    detailed = nowDetailed
                    markers.forEach { (marker, category) ->
                        applyIcon(marker, category, detailed, pinIcons, dotIcons)
                    }
                    mapView.invalidate()
                }
                return false
            }

            override fun onScroll(event: ScrollEvent): Boolean = false
        }
        mapView.addMapListener(listener)
        onDispose { mapView.removeMapListener(listener) }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { view ->
            view.overlays.removeAll { it is Marker }
            markers.clear()
            val detailed = view.zoomLevelDouble >= DETAIL_ZOOM
            stations.forEach { station ->
                val category = categoryOf(station)
                val marker = Marker(view).apply {
                    position = GeoPoint(station.lat, station.lon)
                    setOnMarkerClickListener { _, _ ->
                        onStationClick(station)
                        true
                    }
                }
                applyIcon(marker, category, detailed, pinIcons, dotIcons)
                view.overlays.add(marker)
                markers.add(marker to category)
            }
            view.invalidate()
        },
    )
}

private fun categoryOf(station: Station): StationCategory = when {
    !station.isOperational -> StationCategory.UNAVAILABLE
    station.totalBikes == 0 -> StationCategory.RETURN_ONLY
    else -> StationCategory.AVAILABLE
}

private fun applyIcon(
    marker: Marker,
    category: StationCategory,
    detailed: Boolean,
    pinIcons: Map<StationCategory, Drawable>,
    dotIcons: Map<StationCategory, Drawable>,
) {
    if (detailed) {
        marker.icon = pinIcons[category]
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
    } else {
        marker.icon = dotIcons[category]
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
    }
}

/** Petit point coloré cerclé de blanc, utilisé quand la carte est dézoomée. */
private fun coloredDot(context: Context, color: Int): Drawable {
    val size = (13 * context.resources.displayMetrics.density).toInt()
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val center = size / 2f
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    paint.color = Color.WHITE
    canvas.drawCircle(center, center, center, paint)
    paint.color = color
    canvas.drawCircle(center, center, center * 0.72f, paint)

    return BitmapDrawable(context.resources, bitmap)
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
