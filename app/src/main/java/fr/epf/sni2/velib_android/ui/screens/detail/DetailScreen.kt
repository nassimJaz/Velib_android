package fr.epf.sni2.velib_android.ui.screens.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.epf.sni2.velib_android.domain.model.Station
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val title = (uiState as? DetailUiState.Success)?.station?.name ?: "Station"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (val state = uiState) {
                is DetailUiState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )

                is DetailUiState.Error -> Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Erreur : ${state.message}", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = viewModel::loadStation) { Text("Réessayer") }
                }

                is DetailUiState.Success -> StationDetailContent(state.station)
            }
        }
    }
}

@Composable
private fun StationDetailContent(station: Station) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StatusBadge(operational = station.isOperational)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.DirectionsBike,
                value = station.totalBikes.toString(),
                label = "vélos disponibles",
                container = MaterialTheme.colorScheme.primaryContainer,
                content = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.LocalParking,
                value = station.docksAvailable.toString(),
                label = "places libres",
                container = MaterialTheme.colorScheme.secondaryContainer,
                content = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                DetailRow(Icons.Default.DirectionsBike, "Vélos mécaniques", station.mechanicalBikes.toString())
                DetailRow(Icons.Default.ElectricBike, "Vélos électriques", station.electricBikes.toString())
                DetailRow(Icons.Default.Inventory2, "Capacité totale", "${station.capacity} bornes")
                if (station.stationCode != null) {
                    DetailRow(Icons.Default.Tag, "Code station", station.stationCode)
                }
                DetailRow(
                    Icons.Default.Place,
                    "Position",
                    String.format(Locale.US, "%.5f, %.5f", station.lat, station.lon),
                )
                DetailRow(Icons.Default.Schedule, "Dernière mise à jour", formatLastReported(station.lastReported))
            }
        }
    }
}

@Composable
private fun StatusBadge(operational: Boolean) {
    val container = if (operational) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }
    val onContainer = if (operational) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onError
    }
    Surface(color = container, shape = MaterialTheme.shapes.small) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = if (operational) Icons.Default.CheckCircle else Icons.Default.Block,
                contentDescription = null,
                tint = onContainer,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = if (operational) "Station en service" else "Station hors service",
                color = onContainer,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    container: Color,
    content: Color,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = container, contentColor = content),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium)
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private fun formatLastReported(epochSeconds: Long): String {
    val elapsed = System.currentTimeMillis() / 1000 - epochSeconds
    return when {
        elapsed < 60 -> "il y a moins d'une minute"
        elapsed < 3600 -> "il y a ${elapsed / 60} min"
        elapsed < 86_400 -> "il y a ${elapsed / 3600} h"
        else -> "il y a ${elapsed / 86_400} j"
    }
}
