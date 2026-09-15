package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "safari_bookings")
data class SafariBookingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookingReference: String,
    val safariType: String, // "JEEP" or "ELEPHANT"
    val zoneName: String,   // "Kohora (Central)", "Bagori (Western)", etc.
    val date: String,       // e.g. "2026-11-15"
    val timeSlot: String,   // e.g. "07:30 AM - 09:30 AM"
    val numAdults: Int,
    val numChildren: Int,
    val isForeignNational: Boolean,
    val includeCameraPermit: Boolean,
    val leadGuestName: String,
    val leadGuestPhone: String,
    val idProofType: String, // "Aadhaar", "Passport", "Voter ID"
    val idProofNumber: String,
    val totalAmountInr: Int,
    val status: String,     // "CONFIRMED", "COMPLETED", "CANCELLED"
    val bookedAtTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "accommodation_bookings")
data class AccommodationBookingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookingReference: String,
    val propertyId: String,
    val propertyName: String,
    val roomTypeName: String,
    val checkInDate: String,
    val checkOutDate: String,
    val numNights: Int,
    val numGuests: Int,
    val guestName: String,
    val guestEmail: String,
    val guestPhone: String,
    val totalAmountInr: Int,
    val specialRequests: String,
    val status: String, // "CONFIRMED", "PENDING_CONFIRMATION", "CANCELLED"
    val bookedAtTimestamp: Long = System.currentTimeMillis()
)
