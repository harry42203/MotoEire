package com.example.motoeire

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// ✅ NEW - Migration from version 3 to 4 (add display_order column)
private val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE cars ADD COLUMN display_order INTEGER NOT NULL DEFAULT 0")
    }
}

// ✅ Placeholder migrations
private val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Future migrations
    }
}

@Database(
    entities = [Car::class],
    version = 4,  // ✅ UPDATED TO 4
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
                    .addMigrations(MIGRATION_3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}