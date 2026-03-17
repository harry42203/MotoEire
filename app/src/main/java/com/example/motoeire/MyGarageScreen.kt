package com.example.motoeire

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyGarageScreen(
    viewModel: GarageViewModel,
    onAddCarClick: () -> Unit,
    onCarCardClick: (carId: Int) -> Unit
) {
    val cars by viewModel.carsList.collectAsState()  // ✅ Collects StateFlow

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        "My Garage",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCarClick,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Car")
            }
        }
    ) { paddingValues ->

        if (cars.isEmpty()) {
            // Friendly Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your garage is empty.",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap the + button to add a vehicle.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            // ✅ Grid layout for gallery
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cars, key = { car -> car.id }) { car ->  // ✅ Add key for better recomposition
                    CarGalleryCard(
                        car = car,
                        onClick = { onCarCardClick(car.id) }
                    )
                }
            }
        }
    }
}

// ✅ NEW - Beautiful gallery card with image and status badge
@Composable
fun CarGalleryCard(
    car: Car,
    onClick: () -> Unit
) {
    val renewalStatus = getWorstRenewalStatus(
        insuranceDate = car.insuranceRenewalDate,
        nctDate = car.nctRenewalDate,
        motorTaxDate = car.motorTaxRenewalDate
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },  // ✅ FIXED - Changed from onClick to .clickable
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box {
            // ✅ Background image
            if (car.imagePath != null) {
                AsyncImage(
                    model = car.imagePath,
                    contentDescription = "Car photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {}
            }

            // ✅ Dark overlay for text readability
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.3f)
            ) {}

            // ✅ Car name - Top left
            Text(
                text = car.nickname,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            )

            // ✅ Status badge - Top right
            StatusBadge(
                status = renewalStatus,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            )

            // ✅ Registration at bottom
            Text(
                text = car.registrationNumber,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            )
        }
    }
}

// ✅ NEW - Status badge component
@Composable
fun StatusBadge(
    status: RenewalStatus,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (status) {
        RenewalStatus.OK -> Color(0xFF4CAF50).copy(alpha = 0.9f)  // Green
        RenewalStatus.DUE_SOON -> Color(0xFFF57C00).copy(alpha = 0.9f)  // Amber
        RenewalStatus.OVERDUE -> Color(0xFFD32F2F).copy(alpha = 0.9f)  // Red
    }

    val label = when (status) {
        RenewalStatus.OK -> "In Date"
        RenewalStatus.DUE_SOON -> "Due"
        RenewalStatus.OVERDUE -> "Overdue"
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// ✅ NEW - Helper function to get worst status across all renewal dates
fun getWorstRenewalStatus(
    insuranceDate: Long,
    nctDate: Long,
    motorTaxDate: Long
): RenewalStatus {
    val statuses = listOf(
        checkRenewalStatus(insuranceDate),
        checkRenewalStatus(nctDate),
        checkRenewalStatus(motorTaxDate)
    )

    // Priority: OVERDUE > DUE_SOON > OK
    return when {
        RenewalStatus.OVERDUE in statuses -> RenewalStatus.OVERDUE
        RenewalStatus.DUE_SOON in statuses -> RenewalStatus.DUE_SOON
        else -> RenewalStatus.OK
    }
}

// Helper to convert the Long timestamp back to a readable date
fun formatTimestamp(millis: Long): String {
    if (millis == 0L) return "Not set"
    val formatter = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
    return formatter.format(java.util.Date(millis))
}

// Defines the three possible states for a renewal date
enum class RenewalStatus {
    OK, DUE_SOON, OVERDUE
}

// Calculates the status based on the saved timestamp
fun checkRenewalStatus(dateMillis: Long): RenewalStatus {
    if (dateMillis == 0L) return RenewalStatus.OK

    val todayMillis = System.currentTimeMillis()
    val diffMillis = dateMillis - todayMillis
    val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)

    return when {
        diffDays < 0 -> RenewalStatus.OVERDUE
        diffDays in 0..30 -> RenewalStatus.DUE_SOON
        else -> RenewalStatus.OK
    }
}