package com.example.motoeire

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CarDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCar(car: Car)

    @Query("SELECT * FROM cars ORDER BY display_order ASC, id DESC")
    fun getAllCars(): Flow<List<Car>>

    @Query("DELETE FROM cars WHERE id = :carId")
    suspend fun deleteCar(carId: Int)

    @Delete
    suspend fun delete(car: Car)

    @Update
    suspend fun updateCar(car: Car)

    @Query("DELETE FROM cars")
    suspend fun deleteAllCars()

    // ✅ NEW - Delete multiple cars
    @Query("DELETE FROM cars WHERE id IN (:carIds)")
    suspend fun deleteCars(carIds: List<Int>)
}