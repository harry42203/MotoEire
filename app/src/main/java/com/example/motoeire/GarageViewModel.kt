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

    fun deleteCar(car: Car) {
        viewModelScope.launch {
            try {
                if (!car.imagePath.isNullOrEmpty()) {
                    File(car.imagePath).delete()
                }
                repository.delete(car)
                Log.d("GarageViewModel", "Car deleted successfully")
            } catch (e: Exception) {
                deleteError = "Failed to delete car: ${e.message}"
                Log.e("GarageViewModel", "Error deleting car", e)
            }
        }
    }

    fun updateCar(car: Car) {
        viewModelScope.launch {
            try {
                repository.updateCar(car)
                Log.d("GarageViewModel", "Car updated successfully")
            } catch (e: Exception) {
                deleteError = "Failed to update car: ${e.message}"
                Log.e("GarageViewModel", "Error updating car", e)
            }
        }
    }
}