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
    var insuranceRenewalDate by mutableStateOf<Long?>(null)
    var errorMessage by mutableStateOf<String?>(null)
    var nctDate by mutableStateOf<Long?>(null)
    var motorTaxDate by mutableStateOf<Long?>(null)

    // ✅ NEW - Image path
    var imagePath by mutableStateOf<String?>(null)

    // ✅ NEW - Tax PIN (for edit screen only)
    var taxPin by mutableStateOf("")

    // ✅ NEW - Constants for validation
    companion object {
        const val MAX_NICKNAME_LENGTH = 50
        const val MAX_REGISTRATION_LENGTH = 20
        const val MAX_INSURANCE_PROVIDER_LENGTH = 50
        const val MAX_POLICY_NUMBER_LENGTH = 30
        const val MAX_TAX_PIN_LENGTH = 20
    }

    fun clearFields() {
        nickname = ""
        registration = ""
        insuranceProvider = ""
        policyNumber = ""
        insuranceRenewalDate = null
        nctDate = null
        motorTaxDate = null
        imagePath = null
        taxPin = ""
        errorMessage = null  // ✅ NEW - Clear error too
    }

    fun clearError() {
        errorMessage = null
    }

    fun saveCar(onNavigateBack: () -> Unit) {
        errorMessage = null
        when {
            // ✅ NEW - Validate nickname length
            nickname.isNotBlank() && nickname.length > MAX_NICKNAME_LENGTH -> {
                errorMessage = "Car nickname must be less than $MAX_NICKNAME_LENGTH characters"
                return
            }
            // ✅ NEW - Validate registration length
            registration.isBlank() -> {
                errorMessage = "Please enter a registration number"
                return
            }
            registration.length > MAX_REGISTRATION_LENGTH -> {
                errorMessage = "Registration number must be less than $MAX_REGISTRATION_LENGTH characters"
                return
            }
            // ✅ NEW - Validate insurance provider length
            insuranceProvider.length > MAX_INSURANCE_PROVIDER_LENGTH -> {
                errorMessage = "Insurance provider name must be less than $MAX_INSURANCE_PROVIDER_LENGTH characters"
                return
            }
            // ✅ NEW - Validate policy number length
            policyNumber.length > MAX_POLICY_NUMBER_LENGTH -> {
                errorMessage = "Policy number must be less than $MAX_POLICY_NUMBER_LENGTH characters"
                return
            }
            insuranceRenewalDate == null -> {
                errorMessage = "Please select an insurance renewal date"
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
                    registrationNumber = registration.trim(),  // ✅ NEW - Trim whitespace
                    insuranceProvider = insuranceProvider.trim(),  // ✅ NEW - Trim whitespace
                    insurancePolicyNumber = policyNumber.trim(),  // ✅ NEW - Trim whitespace
                    insuranceRenewalDate = insuranceRenewalDate ?: 0L,
                    nctRenewalDate = nctDate ?: 0L,
                    motorTaxRenewalDate = motorTaxDate ?: 0L,
                    imagePath = imagePath
                )
                repository.insertCar(newCar)
                Log.d("AddCarViewModel", "Car saved successfully")
                clearFields()
                onNavigateBack()
            } catch (e: Exception) {
                errorMessage = "Failed to save car: ${e.message}"
                Log.e("AddCarViewModel", "Failed to save car", e)
            }
        }
    }
}