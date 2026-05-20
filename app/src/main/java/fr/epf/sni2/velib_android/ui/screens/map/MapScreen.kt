package fr.epf.sni2.velib_android.ui.screens.map

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.epf.sni2.velib_android.util.hasLocationPermission
import fr.epf.sni2.velib_android.util.locationPermissions

@Composable
fun MapScreen(
    onStationClick: (String) -> Unit,
    viewModel: MapViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var hasPermission by remember { mutableStateOf(hasLocationPermission(context)) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        hasPermission = result.values.any { it }
    }
    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(locationPermissions)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is MapUiState.Loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            is MapUiState.Success -> {
                OsmMapView(
                    stations = state.stations,
                    modifier = Modifier.fillMaxSize(),
                    showUserLocation = hasPermission,
                    onStationClick = { onStationClick(it.id) },
                )
                StationSearchBar(
                    stations = state.stations,
                    onResultClick = { onStationClick(it.id) },
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            }

            is MapUiState.Error -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Erreur : ${state.message}", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))
                Button(onClick = viewModel::loadStations) { Text("Réessayer") }
            }
        }
    }
}
