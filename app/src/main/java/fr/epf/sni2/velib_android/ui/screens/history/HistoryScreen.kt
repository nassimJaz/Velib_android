package fr.epf.sni2.velib_android.ui.screens.history

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.epf.sni2.velib_android.domain.model.Station
import fr.epf.sni2.velib_android.domain.model.Trip
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val trips by viewModel.trips.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val stations by viewModel.stations.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mes trajets") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter un trajet")
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (trips.isEmpty()) {
                EmptyHistory(Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item { StatsCard(stats) }
                    items(trips, key = { it.id }) { trip ->
                        TripRow(trip = trip, onDelete = { viewModel.deleteTrip(trip.id) })
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddTripDialog(
            stations = stations,
            onDismiss = { showDialog = false },
            onConfirm = { departure, arrival, dateMillis ->
                viewModel.addTrip(departure, arrival, dateMillis)
                showDialog = false
            },
        )
    }
}

@Composable
private fun StatsCard(stats: TripStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                StatItem(value = stats.tripCount.toString(), label = "trajets")
                StatItem(value = String.format(Locale.FRANCE, "%.1f", stats.totalKm), label = "km cumulés")
            }
            stats.mostUsedStation?.let { station ->
                Text(
                    text = "Station la plus utilisée : $station",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium)
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun TripRow(trip: Trip, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = trip.departureName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "vers",
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .size(16.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = trip.arrivalName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${formatDistance(trip.distanceMeters)} • ${formatDate(trip.dateEpochMillis)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Supprimer",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun EmptyHistory(modifier: Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Route,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        Text("Aucun trajet enregistré", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Touche le bouton + pour noter un trajet et suivre tes kilomètres.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTripDialog(
    stations: List<Station>,
    onDismiss: () -> Unit,
    onConfirm: (departure: Station, arrival: Station, dateMillis: Long) -> Unit,
) {
    var departure by remember { mutableStateOf<Station?>(null) }
    var arrival by remember { mutableStateOf<Station?>(null) }
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouveau trajet") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (stations.isEmpty()) {
                    Text(
                        text = "Stations en cours de chargement… vérifie ta connexion.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                StationDropdown(
                    label = "Départ",
                    stations = stations,
                    selected = departure,
                    onSelect = { departure = it },
                )
                StationDropdown(
                    label = "Arrivée",
                    stations = stations,
                    selected = arrival,
                    onSelect = { arrival = it },
                )
                TextButton(onClick = { showDatePicker = true }) {
                    Text("Date : ${formatDate(dateMillis)}")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val dep = departure
                    val arr = arrival
                    if (dep != null && arr != null) onConfirm(dep, arr, dateMillis)
                },
                enabled = departure != null && arrival != null,
            ) { Text("Enregistrer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        },
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dateMillis = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Annuler") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StationDropdown(
    label: String,
    stations: List<Station>,
    selected: Station?,
    onSelect: (Station) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, stations) {
        if (query.isBlank()) emptyList()
        else stations.filter { it.name.contains(query, ignoreCase = true) }.take(10)
    }

    ExposedDropdownMenuBox(
        expanded = expanded && filtered.isNotEmpty(),
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                expanded = true
            },
            label = { Text(label) },
            singleLine = true,
            isError = query.isNotBlank() && selected?.name != query,
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded && filtered.isNotEmpty(),
            onDismissRequest = { expanded = false },
        ) {
            filtered.forEach { station ->
                DropdownMenuItem(
                    text = { Text(station.name) },
                    onClick = {
                        onSelect(station)
                        query = station.name
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

private val dateFormatter: DateTimeFormatter = DateTimeFormatter
    .ofPattern("dd/MM/yyyy", Locale.FRANCE)
    .withZone(ZoneId.systemDefault())

private fun formatDate(epochMillis: Long): String =
    dateFormatter.format(Instant.ofEpochMilli(epochMillis))

private fun formatDistance(meters: Double): String =
    if (meters < 1000) "${meters.toInt()} m"
    else String.format(Locale.FRANCE, "%.1f km", meters / 1000.0)
