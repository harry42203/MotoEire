package com.example.motoeire

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import coil.compose.AsyncImage
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    carId: Int,
    viewModel: GarageViewModel,
    onNavigateBack: () -> Unit,
    onEditClick: () -> Unit,  // ✅ Navigate to edit screen
    onDeleteClick: () -> Unit  // ✅ Delete and navigate back
) {
    val cars by viewModel.carsList.collectAsState()
    val car = cars.find { it.id == carId }
    val context = LocalContext.current

    // ✅ NEW - Delete confirmation dialog state
    var showDeleteDialog by remember { mutableStateOf(false) }

    // ✅ NEW - Clipboard feedback snackbar
    val snackbarHostState = remember { SnackbarHostState() }

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

    // ✅ NEW - Delete confirmation dialog
    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            carName = car.nickname,
            onConfirmDelete = {
                viewModel.deleteCar(car)
                showDeleteDialog = false
                onDeleteClick()
            },
            onDismiss = {
                showDeleteDialog = false
            }
        )
    }

    // ✅ NEW - Handle system back button
    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(car.nickname) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                actions = {
                    // ✅ Edit button
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit Car"
                        )
                    }
                    // ✅ Delete button - Now shows confirmation
                    IconButton(onClick = {
                        showDeleteDialog = true  // Show dialog instead of immediate delete
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete Car",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }  // ✅ NEW
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ✅ Car Image (large)
            if (car.imagePath != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    AsyncImage(
                        model = car.imagePath,
                        contentDescription = "Car photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No Image",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ✅ Content section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Registration and Provider Card
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // ✅ NEW - Make Registration copyable
                        CopyableDetailRow(
                            label = "Registration",
                            value = car.registrationNumber,
                            snackbarHostState = snackbarHostState,
                            context = context
                        )
                        DetailRow(label = "Insurance Provider", value = car.insuranceProvider)
                        // ✅ NEW - Make Policy Number copyable
                        CopyableDetailRow(
                            label = "Policy Number",
                            value = car.insurancePolicyNumber,
                            snackbarHostState = snackbarHostState,
                            context = context
                        )
                    }
                }

                // ✅ Insurance Renewal Section
                Text(
                    text = "Insurance Renewal",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                RenewalDetailCard(
                    icon = Icons.Outlined.CalendarMonth,
                    label = "Insurance Renewal Date",
                    dateMillis = car.insuranceRenewalDate,
                    buttonText = "Renew Insurance",
                    url = "https://www.google.com/search?q=car+insurance+renewal",
                    context = context
                )

                // ✅ NCT Renewal Section
                Text(
                    text = "NCT Renewal",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                RenewalDetailCard(
                    icon = Icons.Outlined.CalendarMonth,
                    label = "NCT Renewal Date",
                    dateMillis = car.nctRenewalDate,
                    buttonText = "Book NCT",
                    url = "https://www.ncts.ie/?regNo=${car.registrationNumber}",
                    context = context
                )

                // ✅ Motor Tax Renewal Section
                Text(
                    text = "Motor Tax",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                // ✅ NEW - Tax PIN Display (if available)
                if (!car.taxPin.isNullOrEmpty()) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CopyableDetailRow(
                                label = "Tax PIN",
                                value = car.taxPin!!,
                                snackbarHostState = snackbarHostState,
                                context = context
                            )
                        }
                    }
                }

                RenewalDetailCard(
                    icon = Icons.Outlined.CalendarMonth,
                    label = "Motor Tax Renewal Date",
                    dateMillis = car.motorTaxRenewalDate,
                    buttonText = "Pay Tax",
                    url = "https://www.motortax.ie/OMT/omt.do?regno=${car.registrationNumber}",
                    context = context
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ✅ NEW - Delete Confirmation Dialog
@Composable
fun DeleteConfirmationDialog(
    carName: String,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Vehicle?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Are you sure you want to delete \"$carName\"?",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "This action cannot be undone. All vehicle information including renewal dates and photos will be permanently removed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Delete")
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        titleContentColor = MaterialTheme.colorScheme.onSurface
    )
}

// ✅ Helper composable for displaying details
@Composable
fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ✅ NEW - Copyable Detail Row with clipboard functionality
@Composable
fun CopyableDetailRow(
    label: String,
    value: String,
    snackbarHostState: SnackbarHostState,
    context: Context
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = value.isNotEmpty()) {
                // ✅ Copy to clipboard
                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                        as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Copied", value)
                clipboard.setPrimaryClip(clip)
            }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Outlined.ContentCopy,
                contentDescription = "Copy to clipboard",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ✅ Enhanced renewal card for details screen
@Composable
fun RenewalDetailCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    dateMillis: Long,
    buttonText: String,
    url: String,
    context: Context
) {
    val status = checkRenewalStatus(dateMillis)
    val statusColor = when (status) {
        RenewalStatus.OK -> Color(0xFF4CAF50)  // Green
        RenewalStatus.DUE_SOON -> Color(0xFFF57C00)  // Amber
        RenewalStatus.OVERDUE -> Color(0xFFD32F2F)  // Red
    }

    val statusLabel = when (status) {
        RenewalStatus.OK -> "In Date"
        RenewalStatus.DUE_SOON -> "Due Soon"
        RenewalStatus.OVERDUE -> "Overdue"
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (status == RenewalStatus.OK)
                MaterialTheme.colorScheme.surface
            else
                statusColor.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    color = statusColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = formatTimestamp(dateMillis),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )

            // ✅ Action button (always visible on details screen)
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(buttonText)
            }
        }
    }
}