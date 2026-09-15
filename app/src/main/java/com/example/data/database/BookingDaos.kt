package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SafariBookingDao {
    @Query("SELECT * FROM safari_bookings ORDER BY bookedAtTimestamp DESC")
    fun getAllSafariBookings(): Flow<List<SafariBookingEntity>>

    @Query("SELECT * FROM safari_bookings WHERE id = :id")
    suspend fun getBookingById(id: Long): SafariBookingEntity?

    @Query("SELECT * FROM safari_bookings WHERE bookingReference = :ref")
    suspend fun getBookingByReference(ref: String): SafariBookingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSafariBooking(booking: SafariBookingEntity): Long

    @Update
    suspend fun updateSafariBooking(booking: SafariBookingEntity)

    @Query("UPDATE safari_bookings SET status = 'CANCELLED' WHERE id = :id")
    suspend fun cancelSafariBooking(id: Long)

    @Query("DELETE FROM safari_bookings WHERE id = :id")
    suspend fun deleteSafariBooking(id: Long)
}

@Dao
interface AccommodationBookingDao {
    @Query("SELECT * FROM accommodation_bookings ORDER BY bookedAtTimestamp DESC")
    fun getAllAccommodationBookings(): Flow<List<AccommodationBookingEntity>>

    @Query("SELECT * FROM accommodation_bookings WHERE id = :id")
    suspend fun getBookingById(id: Long): AccommodationBookingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccommodationBooking(booking: AccommodationBookingEntity): Long

    @Update
    suspend fun updateAccommodationBooking(booking: AccommodationBookingEntity)

    @Query("UPDATE accommodation_bookings SET status = 'CANCELLED' WHERE id = :id")
    suspend fun cancelAccommodationBooking(id: Long)

    @Query("DELETE FROM accommodation_bookings WHERE id = :id")
    suspend fun deleteAccommodationBooking(id: Long)
}
