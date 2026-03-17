package com.example.motoeire

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCarScreen(
    carId: Int,
    viewModel: GarageViewModel,
    editViewModel: AddCarViewModel,  // ✅ Separate ViewModel for editing
    onNavigateBack: () -> Unit
) {
    val cars by viewModel.carsList.collectAsState()
    val car = cars.find { it.id == carId }

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val imageManager = remember { ImageManager(context) }

    // ✅ Initialize form with car data on first load
    LaunchedEffect(car) {
        if (car != null && editViewModel.registration.isEmpty()) {
            editViewModel.nickname = car.nickname
            editViewModel.registration = car.registrationNumber
            editViewModel.insuranceProvider = car.insuranceProvider
            editViewModel.policyNumber = car.insurancePolicyNumber
            editViewModel.insuranceRenewalDate = car.insuranceRenewalDate
            editViewModel.nctDate = car.nctRenewalDate
            editViewModel.motorTaxDate = car.motorTaxRenewalDate
            editViewModel.imagePath = car.imagePath
        }
    }

    // ✅ Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val savedPath = imageManager.saveImageFromUri(it)
            if (savedPath != null) {
                editViewModel.imagePath = savedPath
            }
        }
    }

    // Show snackbar when error occurs
    LaunchedEffect(editViewModel.errorMessage) {
        editViewModel.errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
            editViewModel.clearError()
        }
    }

    // Handle system back button
    BackHandler {
        editViewModel.clearFields()
        onNavigateBack()
    }

    if (car == null) {
        // Car not found
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Car not found")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Vehicle") },
                navigationIcon = {
                    IconButton(onClick = {
                        editViewModel.clearFields()
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Spacer(modifier = Modifier.height(8.dp))

            // ✅ Image Picker Section
            Text(
                text = "Car Photo",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Image preview or placeholder
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                if (editViewModel.imagePath != null) {
                    // Show selected image
                    AsyncImage(
                        model = editViewModel.imagePath,
                        contentDescription = "Car photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Show placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // ✅ Image picker buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gallery")
                }

                OutlinedButton(
                    onClick = { /* TODO: Add camera functionality */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Camera")
                }
            }

            // ✅ Clear image button (only show if image selected)
            if (editViewModel.imagePath != null) {
                TextButton(
                    onClick = { editViewModel.imagePath = null },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Remove Image", color = MaterialTheme.colorScheme.error)
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Expressive Typography
            Text(
                text = "Edit Vehicle Details",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            OutlinedTextField(
                value = editViewModel.nickname,
                onValueChange = { newValue ->
                    // ✅ NEW - Limit input to max length
                    if (newValue.length <= AddCarViewModel.MAX_NICKNAME_LENGTH) {
                        editViewModel.nickname = newValue
                    }
                },
                label = { Text("Car Nickname") },
                placeholder = { Text("e.g. My Golf, My Car") },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
                supportingText = {  // ✅ NEW - Show character count
                    Text(
                        text = "${editViewModel.nickname.length}/${AddCarViewModel.MAX_NICKNAME_LENGTH}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (editViewModel.nickname.length > AddCarViewModel.MAX_NICKNAME_LENGTH - 10)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )

// Registration Number
            OutlinedTextField(
                value = editViewModel.registration,
                onValueChange = { newValue ->
                    // ✅ NEW - Limit input to max length
                    if (newValue.length <= AddCarViewModel.MAX_REGISTRATION_LENGTH) {
                        editViewModel.registration = newValue
                    }
                },
                label = { Text("Registration Number") },
                placeholder = { Text("e.g. 12-D-12345") },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
                supportingText = {  // ✅ NEW - Show character count
                    Text(
                        text = "${editViewModel.registration.length}/${AddCarViewModel.MAX_REGISTRATION_LENGTH}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (editViewModel.registration.length > AddCarViewModel.MAX_REGISTRATION_LENGTH - 5)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )

            OutlinedTextField(
                value = editViewModel.insuranceProvider,
                onValueChange = { newValue ->
                    // ✅ NEW - Limit input to max length
                    if (newValue.length <= AddCarViewModel.MAX_INSURANCE_PROVIDER_LENGTH) {
                        editViewModel.insuranceProvider = newValue
                    }
                },
                label = { Text("Insurance Provider") },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
                supportingText = {  // ✅ NEW - Show character count
                    Text(
                        text = "${editViewModel.insuranceProvider.length}/${AddCarViewModel.MAX_INSURANCE_PROVIDER_LENGTH}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )

            OutlinedTextField(
                value = editViewModel.policyNumber,
                onValueChange = { newValue ->
                    // ✅ NEW - Limit input to max length
                    if (newValue.length <= AddCarViewModel.MAX_POLICY_NUMBER_LENGTH) {
                        editViewModel.policyNumber = newValue
                    }
                },
                label = { Text("Policy Number") },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
                supportingText = {  // ✅ NEW - Show character count
                    Text(
                        text = "${editViewModel.policyNumber.length}/${AddCarViewModel.MAX_POLICY_NUMBER_LENGTH}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )

            // Date Pickers
            DatePickerField(
                label = "Insurance Renewal Date",
                selectedDateMillis = editViewModel.insuranceRenewalDate,
                onDateSelected = { editViewModel.insuranceRenewalDate = it }
            )

            DatePickerField(
                label = "NCT Renewal Date",
                selectedDateMillis = editViewModel.nctDate,
                onDateSelected = { editViewModel.nctDate = it }
            )

            DatePickerField(
                label = "Motor Tax Renewal Date",
                selectedDateMillis = editViewModel.motorTaxDate,
                onDateSelected = { editViewModel.motorTaxDate = it }
            )

            Spacer(modifier = Modifier.weight(1f))

            // ✅ Update Button (instead of Save)
            Button(
                onClick = {
                    // ✅ NEW - Add validation for length limits
                    when {
                        editViewModel.nickname.isNotBlank() && editViewModel.nickname.length > AddCarViewModel.MAX_NICKNAME_LENGTH -> {
                            editViewModel.errorMessage = "Car nickname must be less than ${AddCarViewModel.MAX_NICKNAME_LENGTH} characters"
                            return@Button
                        }
                        editViewModel.registration.isBlank() -> {
                            editViewModel.errorMessage = "Please enter a registration number"
                            return@Button
                        }
                        editViewModel.registration.length > AddCarViewModel.MAX_REGISTRATION_LENGTH -> {
                            editViewModel.errorMessage = "Registration number must be less than ${AddCarViewModel.MAX_REGISTRATION_LENGTH} characters"
                            return@Button
                        }
                        editViewModel.insuranceProvider.length > AddCarViewModel.MAX_INSURANCE_PROVIDER_LENGTH -> {
                            editViewModel.errorMessage = "Insurance provider name must be less than ${AddCarViewModel.MAX_INSURANCE_PROVIDER_LENGTH} characters"
                            return@Button
                        }
                        editViewModel.policyNumber.length > AddCarViewModel.MAX_POLICY_NUMBER_LENGTH -> {
                            editViewModel.errorMessage = "Policy number must be less than ${AddCarViewModel.MAX_POLICY_NUMBER_LENGTH} characters"
                            return@Button
                        }
                        editViewModel.insuranceRenewalDate == null -> {
                            editViewModel.errorMessage = "Please select an insurance renewal date"
                            return@Button
                        }
                        editViewModel.nctDate == null -> {
                            editViewModel.errorMessage = "Please select an NCT renewal date"
                            return@Button
                        }
                        editViewModel.motorTaxDate == null -> {
                            editViewModel.errorMessage = "Please select a Motor Tax renewal date"
                            return@Button
                        }
                    }

                    // Update the car in the database
                    val updatedCar = car.copy(
                        nickname = editViewModel.nickname.ifBlank { "My Car" }.trim(),  // ✅ NEW - Trim whitespace
                        registrationNumber = editViewModel.registration.trim(),  // ✅ NEW - Trim whitespace
                        insuranceProvider = editViewModel.insuranceProvider.trim(),  // ✅ NEW - Trim whitespace
                        insurancePolicyNumber = editViewModel.policyNumber.trim(),  // ✅ NEW - Trim whitespace
                        insuranceRenewalDate = editViewModel.insuranceRenewalDate ?: 0L,
                        nctRenewalDate = editViewModel.nctDate ?: 0L,
                        motorTaxRenewalDate = editViewModel.motorTaxDate ?: 0L,
                        imagePath = editViewModel.imagePath
                    )

                    // Save to database
                    viewModel.updateCar(updatedCar, oldImagePath = car.imagePath)
                    editViewModel.clearFields()
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(100)
            ) {
                Text("Update Vehicle", style = MaterialTheme.typography.titleMedium)
            }

            // Privacy Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = "Privacy Shield",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Your vehicle data is completely private\nand stored locally on your device.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}