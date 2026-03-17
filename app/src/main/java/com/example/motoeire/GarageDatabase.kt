package com.example.motoeire
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Car::class], version = 2, exportSchema = false)
abstract class GarageDatabase : RoomDatabase() {

    abstract fun carDao(): CarDao

    companion object {
        @Volatile
        private var INSTANCE: GarageDatabase? = null

        fun getDatabase(context: Context): GarageDatabase {
            // If the INSTANCE is not null, return it. Otherwise, create the database.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GarageDatabase::class.java,
                    "garage_database"
                )
                    .fallbackToDestructiveMigration() // Useful for prototyping; wipes DB on schema change
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}