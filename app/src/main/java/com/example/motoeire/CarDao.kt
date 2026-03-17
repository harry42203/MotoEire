package com.example.motoeire
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CarDao {

    // Suspend function so it runs on a background thread
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCar(car: Car)

    // Room automatically runs queries returning Flow on a background thread.
    // It will emit a new list every time the database changes.
    @Query("SELECT * FROM cars ORDER BY id DESC")
    fun getAllCars(): Flow<List<Car>>

    @Query("DELETE FROM cars WHERE id = :carId")
    suspend fun deleteCar(carId: Int)

    @Delete
    suspend fun delete(car: Car)
}