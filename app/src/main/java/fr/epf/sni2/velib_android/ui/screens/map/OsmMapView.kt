package fr.epf.sni2.velib_android.ui.screens.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import fr.epf.sni2.velib_android.domain.model.Station
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

private const val PARIS_LAT = 48.8566
private const val PARIS_LON = 2.3522
private const val DEFAULT_ZOOM = 13.0

@Composable
fun OsmMapView(
    stations: List<Station>,
    modifier: Modifier = Modifier,
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

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { view ->
            view.overlays.removeAll { it is Marker }
            stations.forEach { station ->
                val marker = Marker(view).apply {
                    position = GeoPoint(station.lat, station.lon)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = station.name
                    snippet = "${station.totalBikes} vélos · ${station.docksAvailable} places"
                    setOnMarkerClickListener { m, _ ->
                        m.showInfoWindow()
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
