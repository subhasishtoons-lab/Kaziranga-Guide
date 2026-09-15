package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MapLocation
import com.example.data.model.MapLocationType
import com.example.ui.theme.*

@Composable
fun InteractiveParkMap(
    locations: List<MapLocation>,
    selectedLocation: MapLocation?,
    onSelectLocation: (MapLocation?) -> Unit,
    activeFilter: MapLocationType?,
    onFilterChange: (MapLocationType?) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredLocations = remember(locations, activeFilter) {
        if (activeFilter == null) locations else locations.filter { it.type == activeFilter }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(12.dp)
    ) {
        // Map Title & Layer legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Kaziranga Landscape Map",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tap any pin to view details & safari tracks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "4 Zones • NH 715",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Category Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = activeFilter == null,
                onClick = { onFilterChange(null) },
                label = { Text("All Pins", style = MaterialTheme.typography.labelSmall) },
                modifier = Modifier.testTag("filter_all_pins")
            )
            FilterChip(
                selected = activeFilter == MapLocationType.RHINO_HOTSPOT,
                onClick = { onFilterChange(if (activeFilter == MapLocationType.RHINO_HOTSPOT) null else MapLocationType.RHINO_HOTSPOT) },
                label = { Text("Rhino Spots", style = MaterialTheme.typography.labelSmall) },
                leadingIcon = {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(RhinoGold))
                },
                modifier = Modifier.testTag("filter_rhino_spots")
            )
            FilterChip(
                selected = activeFilter == MapLocationType.WATCH_TOWER,
                onClick = { onFilterChange(if (activeFilter == MapLocationType.WATCH_TOWER) null else MapLocationType.WATCH_TOWER) },
                label = { Text("Towers", style = MaterialTheme.typography.labelSmall) },
                leadingIcon = {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(TigerOrange))
                },
                modifier = Modifier.testTag("filter_towers")
            )
            FilterChip(
                selected = activeFilter == MapLocationType.ENTRY_GATE,
                onClick = { onFilterChange(if (activeFilter == MapLocationType.ENTRY_GATE) null else MapLocationType.ENTRY_GATE) },
                label = { Text("Gates", style = MaterialTheme.typography.labelSmall) },
                leadingIcon = {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ForestGreenPrimary))
                },
                modifier = Modifier.testTag("filter_gates")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Interactive Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFE8F2E6)) // Soft meadow background
                .border(1.dp, Color(0xFFC7DEC3), RoundedCornerShape(16.dp))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(filteredLocations) {
                        detectTapGestures { offset ->
                            // Find nearest pin within 32dp tap radius
                            val width = size.width
                            val height = size.height
                            val clicked = filteredLocations.firstOrNull { loc ->
                                val pinX = loc.normX * width
                                val pinY = loc.normY * height
                                val distSq = (pinX - offset.x) * (pinX - offset.x) + (pinY - offset.y) * (pinY - offset.y)
                                distSq <= 36f * 36f * density
                            }
                            onSelectLocation(clicked)
                        }
                    }
                    .testTag("interactive_map_canvas")
            ) {
                val w = size.width
                val h = size.height

                // 1. Draw Northern Brahmaputra River
                val riverPath = Path().apply {
                    moveTo(0f, h * 0.16f)
                    cubicTo(w * 0.25f, h * 0.12f, w * 0.5f, h * 0.20f, w * 0.75f, h * 0.14f)
                    lineTo(w, h * 0.10f)
                    lineTo(w, 0f)
                    lineTo(0f, 0f)
                    close()
                }
                drawPath(riverPath, color = Color(0xFFAED9E0))

                // River Flow ripples
                val ripplePath = Path().apply {
                    moveTo(0f, h * 0.08f)
                    cubicTo(w * 0.3f, h * 0.06f, w * 0.6f, h * 0.12f, w, h * 0.05f)
                }
                drawPath(
                    path = ripplePath,
                    color = Color(0xFF86BBD8),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f), 0f)
                    )
                )

                // 2. Zone Boundaries (Burapahar, Bagori, Kohora, Agoratoli)
                val zoneDividerStyle = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
                // Burapahar | Bagori
                drawLine(
                    color = Color(0xFF6B8E23).copy(alpha = 0.6f),
                    start = Offset(w * 0.20f, h * 0.15f),
                    end = Offset(w * 0.20f, h * 0.88f),
                    strokeWidth = zoneDividerStyle.width,
                    pathEffect = zoneDividerStyle.pathEffect
                )
                // Bagori | Kohora
                drawLine(
                    color = Color(0xFF6B8E23).copy(alpha = 0.6f),
                    start = Offset(w * 0.40f, h * 0.17f),
                    end = Offset(w * 0.40f, h * 0.88f),
                    strokeWidth = zoneDividerStyle.width,
                    pathEffect = zoneDividerStyle.pathEffect
                )
                // Kohora | Agoratoli
                drawLine(
                    color = Color(0xFF6B8E23).copy(alpha = 0.6f),
                    start = Offset(w * 0.66f, h * 0.16f),
                    end = Offset(w * 0.66f, h * 0.88f),
                    strokeWidth = zoneDividerStyle.width,
                    pathEffect = zoneDividerStyle.pathEffect
                )

                // 3. Wetlands & Beels (Donga Beel & Sohola Beel)
                drawOval(
                    color = Color(0xFFB4D2BA),
                    topLeft = Offset(w * 0.18f, h * 0.42f),
                    size = androidx.compose.ui.geometry.Size(w * 0.14f, h * 0.15f)
                )
                drawOval(
                    color = Color(0xFFB4D2BA),
                    topLeft = Offset(w * 0.76f, h * 0.38f),
                    size = androidx.compose.ui.geometry.Size(w * 0.16f, h * 0.16f)
                )

                // 4. Safari Tracks (Dotted trails)
                val safariTrack1 = Path().apply {
                    moveTo(w * 0.48f, h * 0.72f) // Mihimukh Gate
                    lineTo(w * 0.46f, h * 0.58f)
                    lineTo(w * 0.44f, h * 0.52f) // Donga Tower
                    lineTo(w * 0.53f, h * 0.42f) // Kathpara
                    lineTo(w * 0.50f, h * 0.30f)
                }
                drawPath(
                    path = safariTrack1,
                    color = Color(0xFF8D6E63),
                    style = Stroke(
                        width = 2.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )
                )

                val safariTrack2 = Path().apply {
                    moveTo(w * 0.28f, h * 0.76f) // Bagori Gate
                    lineTo(w * 0.25f, h * 0.60f)
                    lineTo(w * 0.22f, h * 0.48f) // Donga Beel
                }
                drawPath(
                    path = safariTrack2,
                    color = Color(0xFF8D6E63),
                    style = Stroke(
                        width = 2.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )
                )

                // 5. Southern Highway 715 (Asphalt road strip)
                drawLine(
                    color = Color(0xFF37474F),
                    start = Offset(0f, h * 0.88f),
                    end = Offset(w, h * 0.88f),
                    strokeWidth = 6.dp.toPx()
                )
                // Road center stripe
                drawLine(
                    color = Color(0xFFFFD54F),
                    start = Offset(0f, h * 0.88f),
                    end = Offset(w, h * 0.88f),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                )

                // 6. Draw Location Pins
                filteredLocations.forEach { loc ->
                    val cx = loc.normX * w
                    val cy = loc.normY * h
                    val isSelected = selectedLocation?.id == loc.id

                    val pinColor = when (loc.type) {
                        MapLocationType.ENTRY_GATE -> Color(0xFF1E4D2B)
                        MapLocationType.WATCH_TOWER -> Color(0xFFE76F51)
                        MapLocationType.BEEL_WATERBODY -> Color(0xFF2A9D8F)
                        MapLocationType.RHINO_HOTSPOT -> Color(0xFFE9C46A)
                        MapLocationType.SAFARI_TRACK -> Color(0xFF6D4C41)
                        MapLocationType.FOREST_BEAT -> Color(0xFF3F51B5)
                    }

                    // Selection halo
                    if (isSelected) {
                        drawCircle(
                            color = pinColor.copy(alpha = 0.35f),
                            radius = 20.dp.toPx(),
                            center = Offset(cx, cy)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 12.dp.toPx(),
                            center = Offset(cx, cy)
                        )
                    }

                    // Pin core
                    drawCircle(
                        color = pinColor,
                        radius = if (isSelected) 8.dp.toPx() else 6.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = if (isSelected) 3.5.dp.toPx() else 2.5.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                }
            }

            // Overlay Zone Labels on Canvas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ZoneWatermark("BURAPAHAR")
                ZoneWatermark("BAGORI (WEST)")
                ZoneWatermark("KOHORA (CENTRAL)")
                ZoneWatermark("AGORATOLI (EAST)")
            }

            // Highway tag at bottom
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF263238).copy(alpha = 0.85f)
            ) {
                Text(
                    text = "NH 715 (Kaziranga Corridor)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFFD54F),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // North Arrow & Scale Indicator
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "North",
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "N (Brahmaputra)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                }
            }
        }

        // Selected Location Card
        selectedLocation?.let { loc ->
            Spacer(modifier = Modifier.height(10.dp))
            LocationDetailCard(
                location = loc,
                onDismiss = { onSelectLocation(null) }
            )
        }
    }
}

@Composable
private fun ZoneWatermark(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 8.sp,
        color = Color(0xFF2D5A27).copy(alpha = 0.65f),
        letterSpacing = 0.5.sp
    )
}

@Composable
fun LocationDetailCard(
    location: MapLocation,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val icon = when (location.type) {
                        MapLocationType.ENTRY_GATE -> Icons.Default.MeetingRoom
                        MapLocationType.WATCH_TOWER -> Icons.Default.Visibility
                        MapLocationType.BEEL_WATERBODY -> Icons.Default.Water
                        MapLocationType.RHINO_HOTSPOT -> Icons.Default.Star
                        MapLocationType.SAFARI_TRACK -> Icons.Default.DirectionsCar
                        MapLocationType.FOREST_BEAT -> Icons.Default.Shield
                    }
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = location.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = location.type.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp).testTag("close_map_location_card")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = location.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "Highlight: ${location.keyAttraction}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "${location.distanceKmFromKohora} km from Kohora",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
