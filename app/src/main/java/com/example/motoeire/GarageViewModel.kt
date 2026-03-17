    package com.example.motoeire

    import android.util.Log
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.setValue
    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.viewModelScope
    import kotlinx.coroutines.flow.SharingStarted
    import kotlinx.coroutines.flow.StateFlow
    import kotlinx.coroutines.flow.stateIn
    import kotlinx.coroutines.launch
    import androidx.compose.runtime.setValue
    import androidx.compose.runtime.getValue




    class GarageViewModel(private val repository: CarRepository) : ViewModel() {
        var deleteError by mutableStateOf<String?>(null)
        // Automatically fetches and updates the list whenever the database changes
        val carsList: StateFlow<List<Car>> = repository.allCars
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        fun deleteCar(car: Car) {
            viewModelScope.launch {
                try {
                    repository.delete(car)
                    // Optional: Show success toast
                } catch (e: Exception) {
                    deleteError = "Failed to delete car: ${e.message}"
                    Log.e("GarageViewModel", "Error deleting car", e)
                }
            }
        }
        fun updateCar(car: Car) {
            viewModelScope.launch {
                repository.updateCar(car)
            }
        }
    }