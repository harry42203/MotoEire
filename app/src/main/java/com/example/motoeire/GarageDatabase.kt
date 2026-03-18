package com.example.motoeire

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// ✅ Define migrations for each schema change
private val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Example: Add a new column with a default value
        // db.execSQL("ALTER TABLE cars ADD COLUMN new_field TEXT DEFAULT 'default_value'")
        // This is just a placeholder - modify as needed for your changes
    }
}

private val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Future migrations go here
    }
}

@Database(
    entities = [Car::class],
    version = 3,  // Keep at current version
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
                    // ✅ Add migrations here as you make schema changes
                    .addMigrations(MIGRATION_3_4, MIGRATION_4_5)
                    // ✅ Keep this as a fallback, but only use it for dev
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}