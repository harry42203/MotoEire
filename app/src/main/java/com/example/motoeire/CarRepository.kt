package com.example.motoeire
import kotlinx.coroutines.flow.Flow

class CarRepository(private val carDao: CarDao) {

    // Room executes all queries which return Flow on a background dispatcher,
    // so we don't need to wrap this in withContext(Dispatchers.IO).
    val allCars: Flow<List<Car>> = carDao.getAllCars()

    suspend fun insertCar(car: Car) {
        carDao.insertCar(car)
    }

    suspend fun deleteCar(carId: Int) {
        carDao.deleteCar(carId)
    }

    suspend fun delete(car: Car) = carDao.delete(car)

    suspend fun updateCar(car: Car) {
        carDao.updateCar(car)
    }
    suspend fun deleteAllCars() {
        carDao.deleteAllCars()
    }
}