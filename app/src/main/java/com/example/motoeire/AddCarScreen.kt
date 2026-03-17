package com.example.motoeire
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Image  // ✅ NEW
import androidx.compose.material.icons.outlined.PhotoCamera  // ✅ NEW
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCarScreen(
    viewModel: AddCarViewModel,
    onNavigateBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val imageManager = remember { ImageManager(context) }

    // ✅ NEW - Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val savedPath = imageManager.saveImageFromUri(it)
            if (savedPath != null) {
                viewModel.imagePath = savedPath
            }
        }
    }

    // Show snackbar when error occurs
    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }

    // Handle system back button
    BackHandler {
        viewModel.clearFields()
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Vehicle") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearFields()
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

            // ✅ NEW - Image Picker Section
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
                if (viewModel.imagePath != null) {
                    // Show selected image
                    AsyncImage(
                        model = viewModel.imagePath,
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

            // ✅ NEW - Image picker buttons
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

            // ✅ NEW - Clear image button (only show if image selected)
            if (viewModel.imagePath != null) {
                TextButton(
                    onClick = { viewModel.imagePath = null },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Remove Image", color = MaterialTheme.colorScheme.error)
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Expressive Typography
            Text(
                text = "Add Your Vehicle",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            OutlinedTextField(
                value = viewModel.nickname,
                onValueChange = { viewModel.nickname = it },
                label = { Text("Car Nickname") },
                placeholder = { Text("e.g. My Golf, My Car") },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            )
            // Standard Text Inputs
            OutlinedTextField(
                value = viewModel.registration,
                onValueChange = { viewModel.registration = it },
                label = { Text("Registration Number") },
                placeholder = { Text("e.g. 12-D-12345") },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.insuranceProvider,
                onValueChange = { viewModel.insuranceProvider = it },
                label = { Text("Insurance Provider") },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.policyNumber,
                onValueChange = { viewModel.policyNumber = it },
                label = { Text("Policy Number") },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Date Pickers
            DatePickerField(
                label = "Insurance Renewal Date",
                selectedDateMillis = viewModel.insuranceRenewalDate,
                onDateSelected = { viewModel.insuranceRenewalDate = it }
            )

            DatePickerField(
                label = "NCT Renewal Date",
                selectedDateMillis = viewModel.nctDate,
                onDateSelected = { viewModel.nctDate = it }
            )

            DatePickerField(
                label = "Motor Tax Renewal Date",
                selectedDateMillis = viewModel.motorTaxDate,
                onDateSelected = { viewModel.motorTaxDate = it }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
            Button(
                onClick = { viewModel.saveCar(onNavigateBack) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(100)
            ) {
                Text("Save Vehicle", style = MaterialTheme.typography.titleMedium)
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