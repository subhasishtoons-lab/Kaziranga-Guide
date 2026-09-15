package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.*
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.KazirangaTheme
import com.example.ui.theme.RhinoGold
import com.example.ui.viewmodel.AppNavDestination
import com.example.ui.viewmodel.KazirangaViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: KazirangaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KazirangaTheme {
                KazirangaMainApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KazirangaMainApp(
    viewModel: KazirangaViewModel,
    modifier: Modifier = Modifier
) {
    val currentDestination by viewModel.currentDestination.collectAsStateWithLifecycle()
    val safariBookings by viewModel.safariBookings.collectAsStateWithLifecycle()
    val hotelBookings by viewModel.accommodationBookings.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("main_scaffold"),
        topBar = {
            KazirangaTopAppBar(
                currentDestination = currentDestination,
                activeBookingsCount = safariBookings.size + hotelBookings.size,
                onOpenBookings = {
                    viewModel.setBookingSubTab(2)
                    viewModel.navigateTo(AppNavDestination.BOOKING)
                }
            )
        },
        bottomBar = {
            KazirangaBottomNavigation(
                currentDestination = currentDestination,
                onNavigate = { dest -> viewModel.navigateTo(dest) },
                bookingCount = safariBookings.size + hotelBookings.size
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentDestination) {
                AppNavDestination.HOME -> HomeScreen(viewModel = viewModel)
                AppNavDestination.FLORA_FAUNA -> FloraFaunaScreen(viewModel = viewModel)
                AppNavDestination.MAP -> MapScreen(viewModel = viewModel)
                AppNavDestination.TRAVEL_GUIDE -> TravelGuideScreen(viewModel = viewModel)
                AppNavDestination.BOOKING -> BookingScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KazirangaTopAppBar(
    currentDestination: AppNavDestination,
    activeBookingsCount: Int,
    onOpenBookings: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = ForestGreenPrimary,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.NaturePeople,
                            contentDescription = null,
                            tint = RhinoGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = "Kaziranga",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = currentDestination.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        actions = {
            // My Passes shortcut badge in TopBar
            IconButton(
                onClick = onOpenBookings,
                modifier = Modifier.testTag("topbar_passes_button")
            ) {
                BadgedBox(
                    badge = {
                        if (activeBookingsCount > 0) {
                            Badge(
                                containerColor = ForestGreenPrimary,
                                contentColor = Color.White
                            ) {
                                Text("$activeBookingsCount")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ConfirmationNumber,
                        contentDescription = "My Passes & Bookings",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun KazirangaBottomNavigation(
    currentDestination: AppNavDestination,
    onNavigate: (AppNavDestination) -> Unit,
    bookingCount: Int
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = Modifier.testTag("bottom_nav_bar")
    ) {
        val items = listOf(
            Triple(AppNavDestination.HOME, Icons.Default.Explore, "Overview"),
            Triple(AppNavDestination.FLORA_FAUNA, Icons.Default.Pets, "Species"),
            Triple(AppNavDestination.MAP, Icons.Default.Map, "Map"),
            Triple(AppNavDestination.TRAVEL_GUIDE, Icons.Default.MenuBook, "Guides"),
            Triple(AppNavDestination.BOOKING, Icons.Default.DirectionsCar, "Safaris")
        )

        items.forEach { (destination, icon, label) ->
            val isSelected = currentDestination == destination
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(destination) },
                icon = {
                    if (destination == AppNavDestination.BOOKING && bookingCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = ForestGreenPrimary,
                                    contentColor = Color.White
                                ) {
                                    Text("$bookingCount")
                                }
                            }
                        ) {
                            Icon(imageVector = icon, contentDescription = label)
                        }
                    } else {
                        Icon(imageVector = icon, contentDescription = label)
                    }
                },
                label = {
                    Text(
                        text = label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ForestGreenPrimary,
                    selectedTextColor = ForestGreenPrimary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.testTag("nav_tab_${destination.name}")
            )
        }
    }
}

// Backwards-compatible Greeting composable for tests
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
