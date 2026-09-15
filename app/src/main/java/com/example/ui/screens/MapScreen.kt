package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MapLocation
import com.example.data.model.ParkZone
import com.example.ui.components.InteractiveParkMap
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppNavDestination
import com.example.ui.viewmodel.KazirangaViewModel

@Composable
fun MapScreen(
    viewModel: KazirangaViewModel,
    modifier: Modifier = Modifier
) {
    val selectedLocation by viewModel.selectedMapLocation.collectAsStateWithLifecycle()
    val activeFilter by viewModel.mapFilterType.collectAsStateWithLifecycle()
    val selectedZone by viewModel.selectedZone.collectAsStateWithLifecycle()

    var activeZoneTab by remember { mutableStateOf<String?>("all") }

    val filteredZones = remember(activeZoneTab, viewModel.parkZones) {
        if (activeZoneTab == "all" || activeZoneTab == null) viewModel.parkZones
        else viewModel.parkZones.filter { it.id == activeZoneTab }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // Interactive Canvas Map
        item {
            InteractiveParkMap(
                locations = viewModel.mapLocations,
                selectedLocation = selectedLocation,
                onSelectLocation = { viewModel.selectMapLocation(it) },
                activeFilter = activeFilter,
                onFilterChange = { viewModel.setMapFilterType(it) }
            )
        }

        // Zone Selector Tabs
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Safari Range Explorer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Filter map ranges & inspect trail regulations",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = activeZoneTab == "all",
                            onClick = { activeZoneTab = "all" },
                            label = { Text("All 4 Ranges", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                    items(viewModel.parkZones) { zone ->
                        FilterChip(
                            selected = activeZoneTab == zone.id,
                            onClick = { activeZoneTab = zone.id },
                            label = { Text(zone.name.split(" ").first(), style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }

        // Display Zone Detail Cards
        items(filteredZones, key = { it.id }) { zone ->
            ZoneMapDetailCard(
                zone = zone,
                onBookZoneSafari = {
                    viewModel.updateSafariForm { copy(zoneName = zone.name) }
                    viewModel.setBookingSubTab(0)
                    viewModel.navigateTo(AppNavDestination.BOOKING)
                }
            )
        }

        // Key Points of Interest List
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Park Checkpoints & Viewpoints (${viewModel.mapLocations.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                viewModel.mapLocations.forEach { loc ->
                    PoiRowItem(
                        location = loc,
                        isSelected = selectedLocation?.id == loc.id,
                        onClick = { viewModel.selectMapLocation(loc) }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }

    // Zone Detail Dialog
    selectedZone?.let { zone ->
        ZoneDetailDialog(
            zone = zone,
            onDismiss = { viewModel.selectZone(null) },
            onBookSafari = {
                viewModel.selectZone(null)
                viewModel.updateSafariForm { copy(zoneName = zone.name) }
                viewModel.setBookingSubTab(0)
                viewModel.navigateTo(AppNavDestination.BOOKING)
            }
        )
    }
}

@Composable
fun ZoneMapDetailCard(
    zone: ParkZone,
    onBookZoneSafari: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = zone.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Gate: ${zone.gateLocation}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "${zone.safariRouteLengthKm} km Track",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = zone.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Key Sightings
            Text(
                text = "Key Wildlife: ${zone.keySightings.joinToString(" • ")}",
                style = MaterialTheme.typography.labelSmall,
                color = ForestGreenPrimary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (zone.elephantSafariAllowed) {
                        BadgePill("Elephant Ride")
                    }
                    if (zone.jeepSafariAllowed) {
                        BadgePill("Jeep Safari")
                    }
                }

                Button(
                    onClick = onBookZoneSafari,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Select For Safari", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun BadgePill(text: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun PoiRowItem(
    location: MapLocation,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .border(
                1.dp,
                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = location.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${location.type.label} • ${location.keyAttraction}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "${location.distanceKmFromKohora} km",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun ZoneDetailDialog(
    zone: ParkZone,
    onDismiss: () -> Unit,
    onBookSafari: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = zone.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Gate: ${zone.gateLocation}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = zone.description, style = MaterialTheme.typography.bodySmall)

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Landscape & Terrain", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text(zone.landscape, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Best Suited For", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text(zone.bestFor, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Coordinates", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text(zone.coordinates, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onBookSafari) {
                Text("Book Safari In This Range")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
