package com.example.motoeire

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class GarageViewModel(private val repository: CarRepository) : ViewModel() {
    var deleteError by mutableStateOf<String?>(null)

    // ✅ FIXED - Use .stateIn() to properly convert Flow to StateFlow
    val carsList: StateFlow<List<Car>> = repository.allCars
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ✅ NEW - Track if delete operation was successful
    var deleteSuccess by mutableStateOf(false)
        private set

    fun deleteCar(car: Car) {
        viewModelScope.launch {
            try {
                deleteError = null
                deleteSuccess = false

                // ✅ ENHANCED - Delete image file with better error handling
                deleteImageFile(car.imagePath)

                // Delete car from database
                repository.delete(car)

                deleteSuccess = true
                Log.d("GarageViewModel", "Car and associated image deleted successfully")
            } catch (e: Exception) {
                deleteError = "Failed to delete car: ${e.message}"
                deleteSuccess = false
                Log.e("GarageViewModel", "Error deleting car", e)
            }
        }
    }

    // ✅ ENHANCED - updateCar with optional oldImagePath for cleanup
    fun updateCar(car: Car, oldImagePath: String? = null) {
        viewModelScope.launch {
            try {
                deleteError = null
                deleteSuccess = false

                // ✅ NEW - Delete old image if user changed it to a different one
                if (oldImagePath != null && oldImagePath != car.imagePath && oldImagePath.isNotEmpty()) {
                    deleteImageFile(oldImagePath)
                    Log.d("GarageViewModel", "Old image file cleaned up during update")
                }

                // Update car in database
                repository.updateCar(car)

                deleteSuccess = true
                Log.d("GarageViewModel", "Car updated successfully")
            } catch (e: Exception) {
                deleteError = "Failed to update car: ${e.message}"
                deleteSuccess = false
                Log.e("GarageViewModel", "Error updating car", e)
            }
        }
    }

    // ✅ NEW - Helper function to delete image file safely
    private fun deleteImageFile(imagePath: String?) {
        if (imagePath.isNullOrEmpty()) return

        try {
            val file = File(imagePath)

            // Check if file exists
            if (!file.exists()) {
                Log.w("GarageViewModel", "Image file doesn't exist (may have been deleted): $imagePath")
                return
            }

            // Check if it's actually a file (not a directory)
            if (!file.isFile) {
                Log.w("GarageViewModel", "Path is not a file: $imagePath")
                return
            }

            // Attempt to delete
            val deleted = file.delete()

            if (deleted) {
                Log.d("GarageViewModel", "Image file deleted successfully: $imagePath")
            } else {
                // File might be locked or permission issue
                Log.w("GarageViewModel", "Failed to delete image file (may be in use): $imagePath")
            }
        } catch (e: SecurityException) {
            Log.e("GarageViewModel", "Permission denied when deleting image: $imagePath", e)
        } catch (e: Exception) {
            Log.e("GarageViewModel", "Error deleting image file: ${e.message}", e)
        }
    }

    // ✅ NEW - Clear delete error
    fun clearDeleteError() {
        deleteError = null
        deleteSuccess = false
    }
    // ✅ NEW - Multi-select state
    private val _selectedCars = MutableStateFlow<Set<Int>>(emptySet())
    val selectedCars: StateFlow<Set<Int>> = _selectedCars.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    // ✅ NEW - Toggle selection mode
    fun toggleSelectionMode() {
        _isSelectionMode.value = !_isSelectionMode.value
        if (!_isSelectionMode.value) {
            _selectedCars.value = emptySet()
        }
    }

    // ✅ NEW - Toggle car selection
    fun toggleCarSelection(carId: Int) {
        val current = _selectedCars.value.toMutableSet()
        if (current.contains(carId)) {
            current.remove(carId)
        } else {
            current.add(carId)
        }
        _selectedCars.value = current
    }

    // ✅ NEW - Select all cars
    fun selectAllCars(carIds: List<Int>) {
        _selectedCars.value = carIds.toSet()
    }

    // ✅ NEW - Deselect all cars
    fun deselectAllCars() {
        _selectedCars.value = emptySet()
    }

    // ✅ NEW - Delete selected cars
    fun deleteSelectedCars() {
        viewModelScope.launch {
            try {
                val carIds = _selectedCars.value.toList()
                if (carIds.isNotEmpty()) {
                    repository.deleteCars(carIds)
                    _selectedCars.value = emptySet()
                    _isSelectionMode.value = false
                    deleteSuccess = true
                }
            } catch (e: Exception) {
                deleteError = "Failed to delete cars: ${e.message}"
                Log.e("GarageViewModel", "Error deleting cars", e)
            }
        }
    }

    // ✅ NEW - Update car order
    fun updateCarOrder(cars: List<Car>) {
        viewModelScope.launch {
            try {
                cars.forEachIndexed { index, car ->
                    val updatedCar = car.copy(displayOrder = index)
                    repository.updateCar(updatedCar)
                }
            } catch (e: Exception) {
                deleteError = "Failed to update order: ${e.message}"
                Log.e("GarageViewModel", "Error updating car order", e)
            }
        }
    }

    // ✅ NEW - Delete all car images (called during app data reset)
    fun deleteAllCarImages() {
        viewModelScope.launch {
            try {
                val cars = carsList.value
                var deletedCount = 0
                var failedCount = 0

                for (car in cars) {
                    if (!car.imagePath.isNullOrEmpty()) {
                        try {
                            val file = File(car.imagePath)
                            if (file.exists() && file.delete()) {
                                deletedCount++
                                Log.d("GarageViewModel", "Cleaned up image for car: ${car.nickname}")
                            } else {
                                failedCount++
                            }
                        } catch (e: Exception) {
                            failedCount++
                            Log.e("GarageViewModel", "Error deleting image for ${car.nickname}", e)
                        }
                    }
                }

                Log.d("GarageViewModel", "Image cleanup completed: $deletedCount deleted, $failedCount failed")
            } catch (e: Exception) {
                Log.e("GarageViewModel", "Error in deleteAllCarImages", e)
            }
        }
    }
}