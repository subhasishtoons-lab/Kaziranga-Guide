package com.example

import com.example.data.database.AccommodationBookingDao
import com.example.data.database.AccommodationBookingEntity
import com.example.data.database.SafariBookingDao
import com.example.data.database.SafariBookingEntity
import com.example.data.repository.KazirangaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FakeSafariDao : SafariBookingDao {
    val bookings = mutableListOf<SafariBookingEntity>()

    override fun getAllSafariBookings(): Flow<List<SafariBookingEntity>> = flowOf(bookings)

    override suspend fun getBookingById(id: Long): SafariBookingEntity? =
        bookings.find { it.id == id }

    override suspend fun getBookingByReference(ref: String): SafariBookingEntity? =
        bookings.find { it.bookingReference == ref }

    override suspend fun insertSafariBooking(booking: SafariBookingEntity): Long {
        val id = (bookings.size + 1).toLong()
        val copy = booking.copy(id = id)
        bookings.add(copy)
        return id
    }

    override suspend fun updateSafariBooking(booking: SafariBookingEntity) {
        val index = bookings.indexOfFirst { it.id == booking.id }
        if (index != -1) {
            bookings[index] = booking
        }
    }

    override suspend fun cancelSafariBooking(id: Long) {
        val index = bookings.indexOfFirst { it.id == id }
        if (index != -1) {
            bookings[index] = bookings[index].copy(status = "CANCELLED")
        }
    }

    override suspend fun deleteSafariBooking(id: Long) {
        bookings.removeIf { it.id == id }
    }
}

class FakeAccommodationDao : AccommodationBookingDao {
    val bookings = mutableListOf<AccommodationBookingEntity>()

    override fun getAllAccommodationBookings(): Flow<List<AccommodationBookingEntity>> = flowOf(bookings)

    override suspend fun getBookingById(id: Long): AccommodationBookingEntity? =
        bookings.find { it.id == id }

    override suspend fun insertAccommodationBooking(booking: AccommodationBookingEntity): Long {
        val id = (bookings.size + 1).toLong()
        val copy = booking.copy(id = id)
        bookings.add(copy)
        return id
    }

    override suspend fun updateAccommodationBooking(booking: AccommodationBookingEntity) {
        val index = bookings.indexOfFirst { it.id == booking.id }
        if (index != -1) {
            bookings[index] = booking
        }
    }

    override suspend fun cancelAccommodationBooking(id: Long) {
        val index = bookings.indexOfFirst { it.id == id }
        if (index != -1) {
            bookings[index] = bookings[index].copy(status = "CANCELLED")
        }
    }

    override suspend fun deleteAccommodationBooking(id: Long) {
        bookings.removeIf { it.id == id }
    }
}

class KazirangaRepositoryTest {

    private lateinit var repository: KazirangaRepository
    private lateinit var safariDao: FakeSafariDao
    private lateinit var accommodationDao: FakeAccommodationDao

    @Before
    fun setup() {
        safariDao = FakeSafariDao()
        accommodationDao = FakeAccommodationDao()
        repository = KazirangaRepository(safariDao, accommodationDao)
    }

    @Test
    fun testParkZonesContainAllFourRanges() {
        val zones = repository.getParkZones()
        assertEquals(4, zones.size)
        val zoneIds = zones.map { it.id }
        assertTrue(zoneIds.contains("central_kohora"))
        assertTrue(zoneIds.contains("western_bagori"))
        assertTrue(zoneIds.contains("eastern_agoratoli"))
        assertTrue(zoneIds.contains("burapahar_ghorakati"))
    }

    @Test
    fun testTheBigFiveSpeciesAreDocumented() {
        val species = repository.getWildlifeSpecies()
        val bigFive = species.filter { it.isBigFive }
        assertEquals(5, bigFive.size)
        val names = bigFive.map { it.commonName }
        assertTrue(names.contains("Great Indian One-Horned Rhinoceros"))
        assertTrue(names.contains("Royal Bengal Tiger"))
        assertTrue(names.contains("Asian Elephant"))
        assertTrue(names.contains("Wild Water Buffalo"))
        assertTrue(names.contains("Eastern Swamp Deer (Barasingha)"))
    }

    @Test
    fun testFloraCatalogCoversKeyEcosystemSpecies() {
        val flora = repository.getFloraSpecies()
        assertTrue(flora.size >= 5)
        val names = flora.map { it.commonName }
        assertTrue(names.any { it.contains("Elephant Grass") })
        assertTrue(names.any { it.contains("Silk Cotton") })
    }

    @Test
    fun testSafariBookingCalculatesCorrectTariff() = runBlocking {
        val id = repository.bookSafari(
            safariType = "JEEP",
            zoneName = "Central Zone (Kohora)",
            date = "2026-11-20",
            timeSlot = "Morning (07:30 AM - 09:30 AM)",
            numAdults = 2,
            numChildren = 1,
            isForeignNational = false,
            includeCameraPermit = true,
            leadName = "Rohan Sharma",
            leadPhone = "9876543210",
            idProofType = "Aadhaar Card",
            idProofNumber = "1234-5678-9012"
        )

        val booking = safariDao.getBookingById(id)
        assertNotNull(booking)
        // 2 Adults * 400 + 1 Child * 200 + Jeep 2400 + Camera 200 = 800 + 200 + 2400 + 200 = 3600
        assertEquals(3600, booking!!.totalAmountInr)
        assertTrue(booking.bookingReference.startsWith("KZ-SAF-"))
        assertEquals("CONFIRMED", booking.status)
    }

    @Test
    fun testElephantSafariBookingCalculatesCorrectTariff() = runBlocking {
        val id = repository.bookSafari(
            safariType = "ELEPHANT",
            zoneName = "Western Zone (Bagori)",
            date = "2026-11-21",
            timeSlot = "Slot 1 (05:30 AM - 06:30 AM)",
            numAdults = 1,
            numChildren = 0,
            isForeignNational = false,
            includeCameraPermit = false,
            leadName = "Ananya Das",
            leadPhone = "9876543211",
            idProofType = "Passport",
            idProofNumber = "Z9876543"
        )

        val booking = safariDao.getBookingById(id)
        assertNotNull(booking)
        // 1 Adult * 400 + Elephant 1250 = 1650
        assertEquals(1650, booking!!.totalAmountInr)
        assertTrue(booking.bookingReference.startsWith("KZ-SAF-"))
    }

    @Test
    fun testAccommodationBookingCalculatesTotalAmount() = runBlocking {
        val id = repository.bookAccommodation(
            propertyId = "diphlu_river_lodge",
            propertyName = "Diphlu River Lodge",
            roomTypeName = "River-Facing Cottage",
            checkInDate = "2026-11-20",
            checkOutDate = "2026-11-22",
            numNights = 2,
            numGuests = 2,
            pricePerNight = 16500,
            guestName = "Vikram Sen",
            guestEmail = "vikram@example.com",
            guestPhone = "9812345678",
            specialRequests = "Ground floor cottage please"
        )

        val booking = accommodationDao.getBookingById(id)
        assertNotNull(booking)
        assertEquals(33000, booking!!.totalAmountInr)
        assertTrue(booking.bookingReference.startsWith("KZ-HTL-"))
        assertEquals("CONFIRMED", booking.status)
    }
}
