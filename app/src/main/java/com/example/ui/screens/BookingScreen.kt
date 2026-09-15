package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Accommodation
import com.example.data.model.AccommodationCategory
import com.example.data.model.RoomTypeOption
import com.example.ui.components.AccommodationBookingCard
import com.example.ui.components.AccommodationCard
import com.example.ui.components.SafariTicketCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.KazirangaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    viewModel: KazirangaViewModel,
    modifier: Modifier = Modifier
) {
    val activeSubTab by viewModel.bookingSubTab.collectAsStateWithLifecycle()
    val safariFormState by viewModel.safariFormState.collectAsStateWithLifecycle()
    val hotelFormState by viewModel.hotelFormState.collectAsStateWithLifecycle()
    val safariBookings by viewModel.safariBookings.collectAsStateWithLifecycle()
    val hotelBookings by viewModel.accommodationBookings.collectAsStateWithLifecycle()

    var activeHotelFilter by remember { mutableStateOf(AccommodationCategory.ALL) }
    var selectedHotelForBooking by remember { mutableStateOf<Accommodation?>(null) }

    val filteredHotels = remember(activeHotelFilter, viewModel.accommodations) {
        if (activeHotelFilter == AccommodationCategory.ALL) viewModel.accommodations
        else viewModel.accommodations.filter { it.category == activeHotelFilter }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Sub-Navigation Tabs
        TabRow(
            selectedTabIndex = activeSubTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .testTag("booking_tabs")
        ) {
            Tab(
                selected = activeSubTab == 0,
                onClick = { viewModel.setBookingSubTab(0) },
                text = { Text("Safari Rides", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                icon = { Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("tab_safari_booking")
            )
            Tab(
                selected = activeSubTab == 1,
                onClick = { viewModel.setBookingSubTab(1) },
                text = { Text("Local Stays", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                icon = { Icon(imageVector = Icons.Default.Hotel, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("tab_hotel_booking")
            )
            Tab(
                selected = activeSubTab == 2,
                onClick = { viewModel.setBookingSubTab(2) },
                text = {
                    val total = safariBookings.size + hotelBookings.size
                    Text("My Bookings${if (total > 0) " ($total)" else ""}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                },
                icon = { Icon(imageVector = Icons.Default.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("tab_my_passes")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Notifications / Snack messages
        safariFormState.successMessage?.let { msg ->
            BookingNotice(msg, isSuccess = true, onDismiss = { viewModel.clearMessages() })
            Spacer(modifier = Modifier.height(8.dp))
        }
        safariFormState.errorMessage?.let { err ->
            BookingNotice(err, isSuccess = false, onDismiss = { viewModel.clearMessages() })
            Spacer(modifier = Modifier.height(8.dp))
        }
        hotelFormState.successMessage?.let { msg ->
            BookingNotice(msg, isSuccess = true, onDismiss = { viewModel.clearMessages() })
            Spacer(modifier = Modifier.height(8.dp))
        }
        hotelFormState.errorMessage?.let { err ->
            BookingNotice(err, isSuccess = false, onDismiss = { viewModel.clearMessages() })
            Spacer(modifier = Modifier.height(8.dp))
        }

        // SubTab Content
        when (activeSubTab) {
            0 -> SafariBookingContent(
                formState = safariFormState,
                onUpdate = { transform -> viewModel.updateSafariForm(transform) },
                onSubmit = { viewModel.submitSafariBooking() }
            )
            1 -> LocalStaysContent(
                hotels = filteredHotels,
                activeFilter = activeHotelFilter,
                onFilterChange = { activeHotelFilter = it },
                onBookHotel = { selectedHotelForBooking = it }
            )
            2 -> MyBookingsContent(
                safariBookings = safariBookings,
                hotelBookings = hotelBookings,
                onCancelSafari = { viewModel.cancelSafariBooking(it) },
                onCancelHotel = { viewModel.cancelHotelBooking(it) },
                onBookSafariShortcut = { viewModel.setBookingSubTab(0) }
            )
        }
    }

    // Hotel Reservation Dialog
    selectedHotelForBooking?.let { hotel ->
        HotelReservationDialog(
            accommodation = hotel,
            onDismiss = { selectedHotelForBooking = null },
            onConfirm = { room, checkIn, checkOut, nights, guests, name, email, phone, notes ->
                selectedHotelForBooking = null
                viewModel.startHotelBooking(hotel)
                viewModel.updateHotelForm {
                    copy(
                        selectedAccommodation = hotel,
                        selectedRoomType = room,
                        checkInDate = checkIn,
                        checkOutDate = checkOut,
                        numNights = nights,
                        numGuests = guests,
                        guestName = name,
                        guestEmail = email,
                        guestPhone = phone,
                        specialRequests = notes
                    )
                }
                viewModel.submitHotelBooking()
            }
        )
    }
}

@Composable
fun BookingNotice(message: String, isSuccess: Boolean, onDismiss: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSuccess) SuccessGreen.copy(alpha = 0.15f) else DangerRed.copy(alpha = 0.15f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (isSuccess) SuccessGreen else DangerRed,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSuccess) SuccessGreen else DangerRed,
                    fontWeight = FontWeight.Medium
                )
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(14.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafariBookingContent(
    formState: com.example.ui.viewmodel.SafariBookingFormState,
    onUpdate: (com.example.ui.viewmodel.SafariBookingFormState.() -> com.example.ui.viewmodel.SafariBookingFormState) -> Unit,
    onSubmit: () -> Unit
) {
    // Realtime calculated total
    val baseFeePerPerson = if (formState.isForeignNational) 2000 else 400
    val vehicleFee = if (formState.safariType == "ELEPHANT") 1250 else 2400
    val cameraFee = if (formState.includeCameraPermit) 200 else 0
    val calculatedTotal = (formState.numAdults * baseFeePerPerson) + (formState.numChildren * (baseFeePerPerson / 2)) + vehicleFee + cameraFee

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        // Safari Type Selector (Jeep vs Elephant)
        item {
            Text(
                text = "1. Select Safari Experience",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedCard(
                    onClick = { onUpdate { copy(safariType = "JEEP", timeSlot = "Morning (07:30 AM - 09:30 AM)") } },
                    modifier = Modifier.weight(1f).testTag("select_jeep_safari"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (formState.safariType == "JEEP") ForestGreenContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (formState.safariType == "JEEP") ForestGreenPrimary else Color.LightGray
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = null, tint = ForestGreenPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Jeep Safari", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text("Covers deep ranges, 4x4 open vehicle", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                OutlinedCard(
                    onClick = { onUpdate { copy(safariType = "ELEPHANT", zoneName = "Central Zone (Kohora)", timeSlot = "Slot 1 (05:30 AM - 06:30 AM)") } },
                    modifier = Modifier.weight(1f).testTag("select_elephant_safari"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (formState.safariType == "ELEPHANT") AmberSecondaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (formState.safariType == "ELEPHANT") AmberSecondary else Color.LightGray
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Icon(imageVector = Icons.Default.Pets, contentDescription = null, tint = AmberSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Elephant Ride", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text("Early dawn mist, intimate rhino viewing", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Zone Selector
        item {
            Text(
                text = "2. Choose Park Range",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            val availableZones = if (formState.safariType == "ELEPHANT") {
                listOf("Central Zone (Kohora)", "Western Zone (Bagori)")
            } else {
                listOf("Central Zone (Kohora)", "Western Zone (Bagori)", "Eastern Zone (Agoratoli)", "Burapahar Range (Ghorakati)")
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                availableZones.forEach { zone ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onUpdate { copy(zoneName = zone) } }
                            .testTag("zone_opt_$zone"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (formState.zoneName == zone) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = zone,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (formState.zoneName == zone) FontWeight.Bold else FontWeight.Normal
                            )
                            if (formState.zoneName == zone) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // Date & Slot
        item {
            Text(
                text = "3. Date & Shift",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            val dates = listOf("2026-11-20", "2026-11-21", "2026-11-22", "2026-12-05", "2026-12-10")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(dates) { date ->
                    FilterChip(
                        selected = formState.date == date,
                        onClick = { onUpdate { copy(date = date) } },
                        label = { Text(date, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val slots = if (formState.safariType == "ELEPHANT") {
                listOf("Slot 1 (05:30 AM - 06:30 AM)", "Slot 2 (06:30 AM - 07:30 AM)")
            } else {
                listOf("Morning (07:30 AM - 09:30 AM)", "Afternoon (01:30 PM - 03:30 PM)")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                slots.forEach { slot ->
                    FilterChip(
                        selected = formState.timeSlot == slot,
                        onClick = { onUpdate { copy(timeSlot = slot) } },
                        label = { Text(slot, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Visitors & Permit Options
        item {
            Text(
                text = "4. Visitors & Permits",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Adults (12+ yrs)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("₹$baseFeePerPerson entry per person", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (formState.numAdults > 1) onUpdate { copy(numAdults = numAdults - 1) } },
                        enabled = formState.numAdults > 1
                    ) {
                        Icon(imageVector = Icons.Default.RemoveCircleOutline, contentDescription = "Decrease")
                    }
                    Text("${formState.numAdults}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                    IconButton(
                        onClick = { if (formState.numAdults < 6) onUpdate { copy(numAdults = numAdults + 1) } }
                    ) {
                        Icon(imageVector = Icons.Default.AddCircleOutline, contentDescription = "Increase")
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Children (5-11 yrs)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("50% concession", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (formState.numChildren > 0) onUpdate { copy(numChildren = numChildren - 1) } },
                        enabled = formState.numChildren > 0
                    ) {
                        Icon(imageVector = Icons.Default.RemoveCircleOutline, contentDescription = "Decrease")
                    }
                    Text("${formState.numChildren}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                    IconButton(
                        onClick = { if (formState.numChildren < 4) onUpdate { copy(numChildren = numChildren + 1) } }
                    ) {
                        Icon(imageVector = Icons.Default.AddCircleOutline, contentDescription = "Increase")
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = formState.isForeignNational,
                    onCheckedChange = { onUpdate { copy(isForeignNational = it) } },
                    modifier = Modifier.testTag("foreign_national_checkbox")
                )
                Text("Foreign National visitor rate applies", style = MaterialTheme.typography.bodySmall)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = formState.includeCameraPermit,
                    onCheckedChange = { onUpdate { copy(includeCameraPermit = it) } },
                    modifier = Modifier.testTag("camera_permit_checkbox")
                )
                Text("Add DSLR / Telephoto camera cess (+₹200)", style = MaterialTheme.typography.bodySmall)
            }
        }

        // Lead Passenger Details
        item {
            Text(
                text = "5. Lead Traveler & ID Verification",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = formState.leadName,
                onValueChange = { onUpdate { copy(leadName = it) } },
                label = { Text("Lead Traveler Full Name") },
                modifier = Modifier.fillMaxWidth().testTag("lead_name_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = formState.leadPhone,
                onValueChange = { onUpdate { copy(leadPhone = it) } },
                label = { Text("Mobile Number (10 digits)") },
                modifier = Modifier.fillMaxWidth().testTag("lead_phone_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val idOptions = listOf("Aadhaar Card", "Passport", "Voter ID")
                idOptions.forEach { idType ->
                    FilterChip(
                        selected = formState.idProofType == idType,
                        onClick = { onUpdate { copy(idProofType = idType) } },
                        label = { Text(idType, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = formState.idProofNumber,
                onValueChange = { onUpdate { copy(idProofNumber = it) } },
                label = { Text("${formState.idProofType} Number") },
                modifier = Modifier.fillMaxWidth().testTag("id_number_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Tariff Breakdown & Submit
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Official Tariff Summary",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Forest Entry Fees (${formState.numAdults}A, ${formState.numChildren}C)", style = MaterialTheme.typography.bodySmall)
                        Text("₹${(formState.numAdults * baseFeePerPerson) + (formState.numChildren * (baseFeePerPerson / 2))}", style = MaterialTheme.typography.bodySmall)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (formState.safariType == "ELEPHANT") "Elephant Service & Mahout" else "Registered 4x4 Jeep & Forest Guide", style = MaterialTheme.typography.bodySmall)
                        Text("₹$vehicleFee", style = MaterialTheme.typography.bodySmall)
                    }
                    if (formState.includeCameraPermit) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Camera Cess Permit", style = MaterialTheme.typography.bodySmall)
                            Text("₹$cameraFee", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Payable", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("₹$calculatedTotal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = ForestGreenPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_safari_booking_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                enabled = !formState.isSubmitting
            ) {
                if (formState.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Icon(imageVector = Icons.Default.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Issue Official Safari Pass", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
fun LocalStaysContent(
    hotels: List<Accommodation>,
    activeFilter: AccommodationCategory,
    onFilterChange: (AccommodationCategory) -> Unit,
    onBookHotel: (Accommodation) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        // Category Pills
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(AccommodationCategory.values()) { cat ->
                    FilterChip(
                        selected = activeFilter == cat,
                        onClick = { onFilterChange(cat) },
                        label = { Text(cat.label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }

        items(hotels, key = { it.id }) { hotel ->
            AccommodationCard(
                accommodation = hotel,
                onBookClick = { onBookHotel(hotel) }
            )
        }
    }
}

@Composable
fun MyBookingsContent(
    safariBookings: List<com.example.data.database.SafariBookingEntity>,
    hotelBookings: List<com.example.data.database.AccommodationBookingEntity>,
    onCancelSafari: (Long) -> Unit,
    onCancelHotel: (Long) -> Unit,
    onBookSafariShortcut: () -> Unit
) {
    if (safariBookings.isEmpty() && hotelBookings.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ConfirmationNumber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(54.dp)
                )
                Text(
                    text = "No Active Passes or Stays",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Book an official Kaziranga safari ride or reserve a forest lodge to see your tickets here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Button(
                    onClick = onBookSafariShortcut,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                ) {
                    Text("Book Safari Now")
                }
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            if (safariBookings.isNotEmpty()) {
                item {
                    Text(
                        text = "Official Safari Permits (${safariBookings.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                items(safariBookings, key = { it.id }) { booking ->
                    SafariTicketCard(
                        booking = booking,
                        onCancel = { onCancelSafari(booking.id) }
                    )
                }
            }

            if (hotelBookings.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Accommodation Bookings (${hotelBookings.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                items(hotelBookings, key = { it.id }) { booking ->
                    AccommodationBookingCard(
                        booking = booking,
                        onCancel = { onCancelHotel(booking.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun HotelReservationDialog(
    accommodation: Accommodation,
    onDismiss: () -> Unit,
    onConfirm: (
        room: RoomTypeOption,
        checkIn: String,
        checkOut: String,
        nights: Int,
        guests: Int,
        name: String,
        email: String,
        phone: String,
        notes: String
    ) -> Unit
) {
    var selectedRoom by remember { mutableStateOf(accommodation.roomTypes.first()) }
    var checkInDate by remember { mutableStateOf("2026-11-20") }
    var checkOutDate by remember { mutableStateOf("2026-11-22") }
    var numNights by remember { mutableStateOf(2) }
    var numGuests by remember { mutableStateOf(2) }
    var guestName by remember { mutableStateOf("") }
    var guestEmail by remember { mutableStateOf("") }
    var guestPhone by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Reserve at ${accommodation.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${accommodation.category.label} • ${accommodation.distanceToGate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text("Select Room Type", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    accommodation.roomTypes.forEach { room ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedRoom = room },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedRoom == room) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(room.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                                    Text("₹${room.priceInr} / night", style = MaterialTheme.typography.labelSmall, color = ForestGreenPrimary)
                                }
                                if (selectedRoom == room) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                item {
                    Text("Check-In / Out Dates", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    val stayDates = listOf("2026-11-20 to 2026-11-22 (2N)", "2026-11-22 to 2026-11-24 (2N)", "2026-12-05 to 2026-12-08 (3N)")
                    stayDates.forEach { dt ->
                        val isSelected = checkInDate == dt.substringBefore(" to ")
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                checkInDate = dt.substringBefore(" to ")
                                checkOutDate = dt.substringAfter(" to ").substringBefore(" (")
                                numNights = if (dt.contains("3N")) 3 else 2
                            },
                            label = { Text(dt, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = guestName,
                        onValueChange = { guestName = it },
                        label = { Text("Primary Guest Name") },
                        modifier = Modifier.fillMaxWidth().testTag("hotel_guest_name_input"),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = guestPhone,
                        onValueChange = { guestPhone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth().testTag("hotel_guest_phone_input"),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = guestEmail,
                        onValueChange = { guestEmail = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                error?.let {
                    item {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total (${numNights} nights):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("₹${selectedRoom.priceInr * numNights}", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.bodyMedium, color = ForestGreenPrimary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (guestName.isBlank()) {
                        error = "Please enter primary guest name"
                        return@Button
                    }
                    if (guestPhone.isBlank() || guestPhone.length < 10) {
                        error = "Please enter a valid 10-digit phone number"
                        return@Button
                    }
                    onConfirm(
                        selectedRoom,
                        checkInDate,
                        checkOutDate,
                        numNights,
                        numGuests,
                        guestName,
                        guestEmail,
                        guestPhone,
                        notes
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                modifier = Modifier.testTag("confirm_hotel_reservation_btn")
            ) {
                Text("Confirm Reservation")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
