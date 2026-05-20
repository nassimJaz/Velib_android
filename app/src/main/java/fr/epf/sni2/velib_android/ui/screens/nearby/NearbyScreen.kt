package fr.epf.sni2.velib_android.ui.screens.nearby

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.epf.sni2.velib_android.util.hasLocationPermission
import fr.epf.sni2.velib_android.util.locationPermissions
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyScreen(
    onStationClick: (String) -> Unit,
    viewModel: NearbyViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(hasLocationPermission(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        hasPermission = result.values.any { it }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) viewModel.load()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("À proximité") }) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (!hasPermission) {
                PermissionRequest(
                    modifier = Modifier.fillMaxSize(),
                    onRequest = { permissionLauncher.launch(locationPermissions) },
                )
            } else {
                NearbyContent(viewModel = viewModel, onStationClick = onStationClick)
            }
        }
    }
}

@Composable
private fun NearbyContent(
    viewModel: NearbyViewModel,
    onStationClick: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val radius by viewModel.radiusMeters.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is NearbyUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }

        is NearbyUiState.Error -> Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = state.message,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = viewModel::load) { Text("Réessayer") }
        }

        is NearbyUiState.Success -> {
            val visible = state.stations.filter { it.distanceMeters <= radius }
            Column(Modifier.fillMaxSize()) {
                RadiusSlider(
                    radiusMeters = radius,
                    count = visible.size,
                    onRadiusChange = viewModel::setRadius,
                )
                if (visible.isEmpty()) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text(
                            text = "Aucune station dans ce rayon",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(visible, key = { it.station.id }) { item ->
                            NearbyCard(
                                item = item,
                                onClick = { onStationClick(item.station.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RadiusSlider(
    radiusMeters: Float,
    count: Int,
    onRadiusChange: (Float) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Rayon : ${formatDistance(radiusMeters)}",
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = "$count station${if (count > 1) "s" else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Slider(
            value = radiusMeters,
            onValueChange = onRadiusChange,
            valueRange = NearbyViewModel.MIN_RADIUS_METERS..NearbyViewModel.MAX_RADIUS_METERS,
            steps = 17,
        )
    }
}

@Composable
private fun NearbyCard(item: StationDistance, onClick: () -> Unit) {
    val station = item.station
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = station.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                IconText(Icons.Default.NearMe, formatDistance(item.distanceMeters))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconText(Icons.AutoMirrored.Filled.DirectionsBike, "${station.totalBikes} vélos")
                IconText(Icons.Default.LocalParking, "${station.docksAvailable} places")
            }
        }
    }
}

@Composable
private fun PermissionRequest(modifier: Modifier, onRequest: () -> Unit) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        Text("Localisation nécessaire", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Autorise l'accès à ta position pour découvrir les stations Vélib' autour de toi.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRequest) { Text("Autoriser la localisation") }
    }
}

@Composable
private fun IconText(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun formatDistance(meters: Float): String {
    val m = meters.roundToInt()
    return if (m < 1000) {
        "$m m"
    } else {
        String.format(Locale.FRANCE, "%.1f km", m / 1000.0)
    }
}
