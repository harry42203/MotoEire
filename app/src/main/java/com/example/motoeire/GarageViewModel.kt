package com.example.motoeire

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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