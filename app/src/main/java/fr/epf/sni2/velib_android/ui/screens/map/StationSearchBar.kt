package fr.epf.sni2.velib_android.ui.screens.map

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.epf.sni2.velib_android.domain.model.Station
import java.text.Normalizer

private const val MAX_RESULTS = 40
private val diacriticsRegex = Regex("\\p{Mn}+")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationSearchBar(
    stations: List<Station>,
    onResultClick: (Station) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf(false) }

    // Noms normalisés (sans accents, en minuscules) calculés une seule fois
    val indexed = remember(stations) {
        stations.map { it to it.name.normalizeForSearch() }
    }
    val results = remember(query, indexed) {
        val needle = query.trim().normalizeForSearch()
        if (needle.isEmpty()) {
            emptyList()
        } else {
            indexed.asSequence()
                .filter { it.second.contains(needle) }
                .map { it.first }
                .sortedBy { it.name }
                .take(MAX_RESULTS)
                .toList()
        }
    }

    SearchBar(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = it },
        colors = SearchBarDefaults.colors(
            // Barre translucide "verre dépoli" qui laisse transparaître la carte
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.80f),
        ),
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = { query = it },
                onSearch = { expanded = false },
                expanded = expanded,
                onExpandedChange = { expanded = it },
                placeholder = { Text("Rechercher une station") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Effacer")
                        }
                    }
                },
            )
        },
    ) {
        when {
            query.isBlank() -> SearchHint("Tape le nom d'une station")
            results.isEmpty() -> SearchHint("Aucune station trouvée")
            else -> LazyColumn {
                items(results, key = { it.id }) { station ->
                    ListItem(
                        headlineContent = { Text(station.name) },
                        supportingContent = {
                            Text("${station.totalBikes} vélos · ${station.docksAvailable} places")
                        },
                        leadingContent = {
                            Icon(Icons.Default.LocationOn, contentDescription = null)
                        },
                        modifier = Modifier.clickable {
                            expanded = false
                            query = ""
                            onResultClick(station)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchHint(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(16.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** Met en minuscules et retire les accents pour une recherche tolérante. */
private fun String.normalizeForSearch(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(diacriticsRegex, "")
        .lowercase()
