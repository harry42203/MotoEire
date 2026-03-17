package com.example.motoeire

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GarageViewModel(private val repository: CarRepository) : ViewModel() {

    // Automatically fetches and updates the list whenever the database changes
    val carsList: StateFlow<List<Car>> = repository.allCars
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    fun deleteCar(car: Car) {
        viewModelScope.launch {
            repository.delete(car)
        }
    }
}