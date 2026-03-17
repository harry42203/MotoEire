package com.example.motoeire
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AddCarViewModel(private val repository: CarRepository) : ViewModel() {

    // Holding UI state
    var nickname by mutableStateOf("")
    var registration by mutableStateOf("")
    var insuranceProvider by mutableStateOf("")
    var policyNumber by mutableStateOf("")
    var errorMessage by mutableStateOf<String?>(null)

    // Dates held as Longs (Unix timestamps) to match your Room Entity
    var nctDate by mutableStateOf<Long?>(null)
    var motorTaxDate by mutableStateOf<Long?>(null)
    fun clearFields() {
        nickname = ""
        registration = ""
        insuranceProvider = ""
        policyNumber = ""
        // Reset dates to current time
        nctDate = System.currentTimeMillis()
        motorTaxDate = System.currentTimeMillis()
    }
    fun saveCar(onNavigateBack: () -> Unit) {
        if (registration.isBlank()) {
            errorMessage = "Registration number is required"
            return
        }
        if (nctDate == null || motorTaxDate == null) {
            errorMessage = "Dates are required"
            return
        }
        viewModelScope.launch {
            val newCar = Car(
                nickname = nickname.ifBlank { "My Car" },
                registrationNumber = registration,
                insuranceProvider = insuranceProvider,
                insurancePolicyNumber = policyNumber,
                // Defaulting to 0 if null, though you might want validation here later
                nctRenewalDate = nctDate ?: 0L,
                motorTaxRenewalDate = motorTaxDate ?: 0L
            )
            try {
                repository.insertCar(newCar)
                clearFields()
                onNavigateBack()
            } catch (e: Exception) {
                // Show error toast/snackbar to user
                Log.e("AddCarViewModel", "Failed to save car", e)
            }
        }

    }
}