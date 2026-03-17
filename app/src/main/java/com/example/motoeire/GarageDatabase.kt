package com.example.motoeire
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Car::class],
    version = 3,  // ✅ UPDATED FROM 2 TO 3
    exportSchema = false
)
abstract class GarageDatabase : RoomDatabase() {

    abstract fun carDao(): CarDao

    companion object {
        @Volatile
        private var INSTANCE: GarageDatabase? = null

        fun getDatabase(context: Context): GarageDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GarageDatabase::class.java,
                    "garage_database"
                )
                    .fallbackToDestructiveMigration() // ✅ Handles migration automatically
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}