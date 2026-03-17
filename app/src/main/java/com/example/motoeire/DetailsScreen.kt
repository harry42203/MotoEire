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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
                    // ✅ Delete button
                    IconButton(onClick = {
                        viewModel.deleteCar(car)
                        onDeleteClick()
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete Car",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
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
                        DetailRow(label = "Registration", value = car.registrationNumber)
                        DetailRow(label = "Insurance Provider", value = car.insuranceProvider)
                        DetailRow(label = "Policy Number", value = car.insurancePolicyNumber)
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

// ✅ Helper composable for displaying details
@Composable
fun DetailRow(
    label: String,
    value: String
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
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
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