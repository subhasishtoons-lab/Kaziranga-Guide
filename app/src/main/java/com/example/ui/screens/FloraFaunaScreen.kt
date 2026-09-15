package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.components.FloraSpeciesCard
import com.example.ui.components.WildlifeSpeciesCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.KazirangaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloraFaunaScreen(
    viewModel: KazirangaViewModel,
    modifier: Modifier = Modifier
) {
    val selectedCategory by viewModel.selectedWildlifeCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.speciesSearchQuery.collectAsStateWithLifecycle()
    val selectedSpecies by viewModel.selectedSpecies.collectAsStateWithLifecycle()
    val selectedFlora by viewModel.selectedFlora.collectAsStateWithLifecycle()

    var showFloraTab by remember { mutableStateOf(false) }

    // Filter wildlife
    val filteredWildlife = remember(viewModel.allWildlife, selectedCategory, searchQuery, showFloraTab) {
        if (showFloraTab) emptyList()
        else viewModel.allWildlife.filter { species ->
            val matchCategory = when (selectedCategory) {
                WildlifeCategory.ALL -> true
                WildlifeCategory.BIG_FIVE -> species.isBigFive
                else -> species.category == selectedCategory
            }
            val matchSearch = searchQuery.isBlank() ||
                species.commonName.contains(searchQuery, ignoreCase = true) ||
                species.scientificName.contains(searchQuery, ignoreCase = true) ||
                species.localAssameseName.contains(searchQuery, ignoreCase = true)

            matchCategory && matchSearch
        }
    }

    // Filter flora
    val filteredFlora = remember(viewModel.allFlora, searchQuery, showFloraTab) {
        if (!showFloraTab) emptyList()
        else viewModel.allFlora.filter { flora ->
            searchQuery.isBlank() ||
                flora.commonName.contains(searchQuery, ignoreCase = true) ||
                flora.scientificName.contains(searchQuery, ignoreCase = true) ||
                flora.localName.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // Search Field
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSpeciesSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("species_search_input"),
                placeholder = { Text("Search by name, scientific name or Assamese...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSpeciesSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        }

        // Wildlife vs Flora Primary Toggle
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = !showFloraTab,
                    onClick = { showFloraTab = false },
                    label = { Text("Fauna / Animals (${viewModel.allWildlife.size})", fontWeight = FontWeight.SemiBold) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Pets, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    modifier = Modifier.weight(1f).testTag("tab_fauna")
                )
                FilterChip(
                    selected = showFloraTab,
                    onClick = { showFloraTab = true },
                    label = { Text("Flora / Forests (${viewModel.allFlora.size})", fontWeight = FontWeight.SemiBold) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Forest, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    modifier = Modifier.weight(1f).testTag("tab_flora")
                )
            }
        }

        // Subcategory Filter Pills (Only for Wildlife)
        if (!showFloraTab) {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(WildlifeCategory.values()) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { viewModel.setWildlifeCategory(category) },
                            label = { Text(category.displayName, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("wildlife_cat_${category.name}")
                        )
                    }
                }
            }
        }

        // Results Count Header
        item {
            val count = if (showFloraTab) filteredFlora.size else filteredWildlife.size
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showFloraTab) "Forest & Vegetation Catalog ($count)" else "Documented Wildlife ($count)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Wildlife List
        if (!showFloraTab) {
            if (filteredWildlife.isEmpty()) {
                item {
                    EmptyStateCard(message = "No species matched your filter. Try adjusting your query.")
                }
            } else {
                items(filteredWildlife, key = { it.id }) { species ->
                    WildlifeSpeciesCard(
                        species = species,
                        onClick = { viewModel.selectSpecies(species) }
                    )
                }
            }
        }

        // Flora List
        if (showFloraTab) {
            if (filteredFlora.isEmpty()) {
                item {
                    EmptyStateCard(message = "No botanical species matched your search.")
                }
            } else {
                items(filteredFlora, key = { it.id }) { flora ->
                    FloraSpeciesCard(
                        flora = flora,
                        onClick = { viewModel.selectFlora(flora) }
                    )
                }
            }
        }
    }

    // Detail Dialogs
    selectedSpecies?.let { species ->
        SpeciesDetailModal(
            species = species,
            onDismiss = { viewModel.selectSpecies(null) }
        )
    }

    selectedFlora?.let { flora ->
        FloraDetailModal(
            flora = flora,
            onDismiss = { viewModel.selectFlora(null) }
        )
    }
}

@Composable
fun EmptyStateCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SpeciesDetailModal(
    species: WildlifeSpecies,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = species.commonName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Text(
                    text = species.scientificName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Local Assamese: ${species.localAssameseName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ForestGreenPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    // IUCN & Population Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = RhinoGold.copy(alpha = 0.2f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("STATUS", style = MaterialTheme.typography.labelSmall, color = AmberSecondary)
                                Text(species.iucnStatus, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("POPULATION", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Text(species.populationEstimate, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Overview & Habitat",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = species.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Habitat: ${species.habitat}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                item {
                    Text(
                        text = "Wildlife Spotting Guide",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ForestGreenContainer.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "• Best Zones: ${species.bestZones.joinToString(", ")}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "• Peak Hours: ${species.peakSpottingTime}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "• Field Advice: ${species.spottingTips}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AmberSecondaryContainer.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = AmberSecondary)
                            Text(
                                text = species.fascinatingFact,
                                style = MaterialTheme.typography.bodySmall,
                                color = AmberOnSecondaryContainer
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Got It")
            }
        }
    )
}

@Composable
fun FloraDetailModal(
    flora: FloraSpecies,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = flora.commonName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = flora.scientificName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Local: ${flora.localName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ForestGreenPrimary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = flora.description, style = MaterialTheme.typography.bodySmall)

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ForestGreenContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Ecosystem Role", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text(flora.roleInEcosystem, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Text(
                    text = "Where to Observe: ${flora.bestObservedAt}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
