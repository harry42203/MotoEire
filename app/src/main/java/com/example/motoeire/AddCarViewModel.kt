package com.example.motoeire
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

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
    fun clearError() {
        errorMessage = null
    }
    fun saveCar(onNavigateBack: () -> Unit) {
        errorMessage = null  // Clear previous errors

        when {
            registration.isBlank() -> {
                errorMessage = "Please enter a registration number"
                return
            }
            nctDate == null -> {
                errorMessage = "Please select an NCT renewal date"
                return
            }
            motorTaxDate == null -> {
                errorMessage = "Please select a Motor Tax renewal date"
                return
            }
        }

        viewModelScope.launch {
            try {
                val newCar = Car(
                    nickname = nickname.ifBlank { "My Car" },
                    registrationNumber = registration,
                    insuranceProvider = insuranceProvider,
                    insurancePolicyNumber = policyNumber,
                    nctRenewalDate = nctDate ?: 0L,
                    motorTaxRenewalDate = motorTaxDate ?: 0L
                )
                repository.insertCar(newCar)
                clearFields()
                onNavigateBack()
            } catch (e: Exception) {
                errorMessage = "Failed to save car: ${e.message}"
                Log.e("AddCarViewModel", "Failed to save car", e)
            }
        }
    }
}