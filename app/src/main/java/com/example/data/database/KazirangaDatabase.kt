package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SafariBookingEntity::class,
        AccommodationBookingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KazirangaDatabase : RoomDatabase() {
    abstract fun safariBookingDao(): SafariBookingDao
    abstract fun accommodationBookingDao(): AccommodationBookingDao

    companion object {
        @Volatile
        private var INSTANCE: KazirangaDatabase? = null

        fun getDatabase(context: Context): KazirangaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KazirangaDatabase::class.java,
                    "kaziranga_park_db"
                ).fallbackToDestructiveMigration()
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
