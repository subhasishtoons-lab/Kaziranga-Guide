package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AccommodationBookingEntity
import com.example.data.database.KazirangaDatabase
import com.example.data.database.SafariBookingEntity
import com.example.data.model.*
import com.example.data.repository.KazirangaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppNavDestination(val label: String) {
    HOME("Overview"),
    FLORA_FAUNA("Wildlife & Flora"),
    MAP("Interactive Map"),
    TRAVEL_GUIDE("Travel & Tips"),
    BOOKING("Safaris & Stays")
}

data class SafariBookingFormState(
    val safariType: String = "JEEP", // "JEEP" or "ELEPHANT"
    val zoneName: String = "Central Zone (Kohora)",
    val date: String = "2026-11-20",
    val timeSlot: String = "Morning (07:30 AM - 09:30 AM)",
    val numAdults: Int = 2,
    val numChildren: Int = 0,
    val isForeignNational: Boolean = false,
    val includeCameraPermit: Boolean = true,
    val leadName: String = "",
    val leadPhone: String = "",
    val idProofType: String = "Aadhaar Card",
    val idProofNumber: String = "",
    val isSubmitting: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

data class AccommodationBookingFormState(
    val selectedAccommodation: Accommodation? = null,
    val selectedRoomType: RoomTypeOption? = null,
    val checkInDate: String = "2026-11-20",
    val checkOutDate: String = "2026-11-22",
    val numNights: Int = 2,
    val numGuests: Int = 2,
    val guestName: String = "",
    val guestEmail: String = "",
    val guestPhone: String = "",
    val specialRequests: String = "",
    val isSubmitting: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class KazirangaViewModel(application: Application) : AndroidViewModel(application) {
    private val database = KazirangaDatabase.getDatabase(application)
    private val repository = KazirangaRepository(
        safariDao = database.safariBookingDao(),
        accommodationDao = database.accommodationBookingDao()
    )

    // Current navigation tab
    private val _currentDestination = MutableStateFlow(AppNavDestination.HOME)
    val currentDestination: StateFlow<AppNavDestination> = _currentDestination.asStateFlow()

    fun navigateTo(dest: AppNavDestination) {
        _currentDestination.value = dest
    }

    // Static Park Data
    val parkZones: List<ParkZone> = repository.getParkZones()
    val allWildlife: List<WildlifeSpecies> = repository.getWildlifeSpecies()
    val allFlora: List<FloraSpecies> = repository.getFloraSpecies()
    val spottingTips: List<SpottingTip> = repository.getSpottingTips()
    val mapLocations: List<MapLocation> = repository.getMapLocations()
    val accommodations: List<Accommodation> = repository.getAccommodations()
    val travelSections: List<TravelSection> = repository.getTravelGuideSections()

    // Wildlife Filtering & Search
    private val _selectedWildlifeCategory = MutableStateFlow(WildlifeCategory.ALL)
    val selectedWildlifeCategory: StateFlow<WildlifeCategory> = _selectedWildlifeCategory.asStateFlow()

    private val _speciesSearchQuery = MutableStateFlow("")
    val speciesSearchQuery: StateFlow<String> = _speciesSearchQuery.asStateFlow()

    private val _selectedSpecies = MutableStateFlow<WildlifeSpecies?>(null)
    val selectedSpecies: StateFlow<WildlifeSpecies?> = _selectedSpecies.asStateFlow()

    private val _selectedFlora = MutableStateFlow<FloraSpecies?>(null)
    val selectedFlora: StateFlow<FloraSpecies?> = _selectedFlora.asStateFlow()

    private val _selectedZone = MutableStateFlow<ParkZone?>(null)
    val selectedZone: StateFlow<ParkZone?> = _selectedZone.asStateFlow()

    // Interactive Map State
    private val _selectedMapLocation = MutableStateFlow<MapLocation?>(null)
    val selectedMapLocation: StateFlow<MapLocation?> = _selectedMapLocation.asStateFlow()

    private val _mapFilterType = MutableStateFlow<MapLocationType?>(null)
    val mapFilterType: StateFlow<MapLocationType?> = _mapFilterType.asStateFlow()

    // Booking Forms & Lists
    private val _safariFormState = MutableStateFlow(SafariBookingFormState())
    val safariFormState: StateFlow<SafariBookingFormState> = _safariFormState.asStateFlow()

    private val _hotelFormState = MutableStateFlow(AccommodationBookingFormState())
    val hotelFormState: StateFlow<AccommodationBookingFormState> = _hotelFormState.asStateFlow()

    // Room DB Flow
    val safariBookings: StateFlow<List<SafariBookingEntity>> = repository.safariBookings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val accommodationBookings: StateFlow<List<AccommodationBookingEntity>> = repository.accommodationBookings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Booking sub-tab: 0 = Safari, 1 = Stay, 2 = My Passes
    private val _bookingSubTab = MutableStateFlow(0)
    val bookingSubTab: StateFlow<Int> = _bookingSubTab.asStateFlow()

    fun setBookingSubTab(tab: Int) {
        _bookingSubTab.value = tab
    }

    // Setters
    fun setWildlifeCategory(cat: WildlifeCategory) {
        _selectedWildlifeCategory.value = cat
    }

    fun setSpeciesSearchQuery(query: String) {
        _speciesSearchQuery.value = query
    }

    fun selectSpecies(species: WildlifeSpecies?) {
        _selectedSpecies.value = species
    }

    fun selectFlora(flora: FloraSpecies?) {
        _selectedFlora.value = flora
    }

    fun selectZone(zone: ParkZone?) {
        _selectedZone.value = zone
    }

    fun selectMapLocation(loc: MapLocation?) {
        _selectedMapLocation.value = loc
    }

    fun setMapFilterType(type: MapLocationType?) {
        _mapFilterType.value = type
    }

    // Safari Form Updates
    fun updateSafariForm(transform: SafariBookingFormState.() -> SafariBookingFormState) {
        _safariFormState.value = _safariFormState.value.transform()
    }

    fun submitSafariBooking() {
        val state = _safariFormState.value
        if (state.leadName.isBlank()) {
            _safariFormState.value = state.copy(errorMessage = "Please enter the lead traveler name.")
            return
        }
        if (state.leadPhone.isBlank() || state.leadPhone.length < 10) {
            _safariFormState.value = state.copy(errorMessage = "Please enter a valid 10-digit mobile number.")
            return
        }
        if (state.idProofNumber.isBlank()) {
            _safariFormState.value = state.copy(errorMessage = "Please enter your ID proof number for the forest permit.")
            return
        }

        _safariFormState.value = state.copy(isSubmitting = true, errorMessage = null)
        viewModelScope.launch {
            try {
                repository.bookSafari(
                    safariType = state.safariType,
                    zoneName = state.zoneName,
                    date = state.date,
                    timeSlot = state.timeSlot,
                    numAdults = state.numAdults,
                    numChildren = state.numChildren,
                    isForeignNational = state.isForeignNational,
                    includeCameraPermit = state.includeCameraPermit,
                    leadName = state.leadName,
                    leadPhone = state.leadPhone,
                    idProofType = state.idProofType,
                    idProofNumber = state.idProofNumber
                )
                _safariFormState.value = SafariBookingFormState(
                    successMessage = "Safari permit successfully confirmed! View your pass in 'My Bookings'."
                )
                _bookingSubTab.value = 2 // Switch to My Bookings tab
            } catch (e: Exception) {
                _safariFormState.value = state.copy(
                    isSubmitting = false,
                    errorMessage = "Booking error: ${e.localizedMessage}"
                )
            }
        }
    }

    fun cancelSafariBooking(id: Long) {
        viewModelScope.launch {
            repository.cancelSafariBooking(id)
        }
    }

    // Hotel Form Updates
    fun startHotelBooking(accommodation: Accommodation) {
        _hotelFormState.value = AccommodationBookingFormState(
            selectedAccommodation = accommodation,
            selectedRoomType = accommodation.roomTypes.firstOrNull()
        )
        _currentDestination.value = AppNavDestination.BOOKING
        _bookingSubTab.value = 1 // Hotel tab
    }

    fun updateHotelForm(transform: AccommodationBookingFormState.() -> AccommodationBookingFormState) {
        _hotelFormState.value = _hotelFormState.value.transform()
    }

    fun submitHotelBooking() {
        val state = _hotelFormState.value
        val hotel = state.selectedAccommodation ?: return
        val room = state.selectedRoomType ?: hotel.roomTypes.first()

        if (state.guestName.isBlank()) {
            _hotelFormState.value = state.copy(errorMessage = "Please enter primary guest name.")
            return
        }
        if (state.guestPhone.isBlank() || state.guestPhone.length < 10) {
            _hotelFormState.value = state.copy(errorMessage = "Please enter a valid 10-digit contact number.")
            return
        }

        _hotelFormState.value = state.copy(isSubmitting = true, errorMessage = null)
        viewModelScope.launch {
            try {
                repository.bookAccommodation(
                    propertyId = hotel.id,
                    propertyName = hotel.name,
                    roomTypeName = room.name,
                    checkInDate = state.checkInDate,
                    checkOutDate = state.checkOutDate,
                    numNights = state.numNights,
                    numGuests = state.numGuests,
                    pricePerNight = room.priceInr,
                    guestName = state.guestName,
                    guestEmail = state.guestEmail,
                    guestPhone = state.guestPhone,
                    specialRequests = state.specialRequests
                )
                _hotelFormState.value = AccommodationBookingFormState(
                    successMessage = "Reservation confirmed with ${hotel.name}! View voucher in 'My Bookings'."
                )
                _bookingSubTab.value = 2 // Switch to My Bookings tab
            } catch (e: Exception) {
                _hotelFormState.value = state.copy(
                    isSubmitting = false,
                    errorMessage = "Reservation error: ${e.localizedMessage}"
                )
            }
        }
    }

    fun cancelHotelBooking(id: Long) {
        viewModelScope.launch {
            repository.cancelAccommodationBooking(id)
        }
    }

    fun clearMessages() {
        _safariFormState.value = _safariFormState.value.copy(successMessage = null, errorMessage = null)
        _hotelFormState.value = _hotelFormState.value.copy(successMessage = null, errorMessage = null)
    }
}
