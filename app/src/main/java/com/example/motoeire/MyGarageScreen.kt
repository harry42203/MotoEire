package com.example.motoeire

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyGarageScreen(
    viewModel: GarageViewModel,
    onAddCarClick: () -> Unit
) {
    // This collects the Flow from your database and triggers a UI update when it changes
    val cars by viewModel.carsList.collectAsState()

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
                Text(
                    text = "Your garage is empty.\nTap + to add a vehicle.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            // The List of Cars
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(cars) { car ->
                    CarCard(
                        car = car,
                        viewModel = viewModel // Pass the viewModel here
                    )
                }
            }
        }
    }
}

@Composable
fun CarCard(car: Car, viewModel: GarageViewModel) {
    val context = LocalContext.current

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left Side: Nickname and Reg
                Column {
                    Text(text = car.nickname, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(text = car.registrationNumber, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Right Side: Delete Button
                IconButton(onClick = { viewModel.deleteCar(car) }) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete Car",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            // Insurance Info (Kept as standard InfoRow)
            InfoRow(
                icon = Icons.Outlined.Security,
                label = "Insurance",
                value = "${car.insuranceProvider} (Pol: ${car.insurancePolicyNumber})"
            )

            // 2. Replaced old Dates with Smart RenewalRows!
            RenewalRow(
                icon = Icons.Outlined.CalendarMonth,
                label = "Insurance Renewal",
                dateMillis = car.insuranceRenewalDate,
                buttonText = "Renew Insurance",
                url = "https://www.google.com/search?q=car+insurance+renewal",  // Or your specific URL
                context = context
            )

            RenewalRow(
                icon = Icons.Outlined.CalendarMonth,
                label = "NCT Renewal",
                dateMillis = car.nctRenewalDate,
                buttonText = "Book NCT",
                url = "https://www.ncts.ie/?regNo=${car.registrationNumber}",
                context = context
            )

            RenewalRow(
                icon = Icons.Outlined.CalendarMonth,
                label = "Motor Tax",
                dateMillis = car.motorTaxRenewalDate,
                buttonText = "Pay Tax",
                url = "https://www.motortax.ie/OMT/omt.do?regno=${car.registrationNumber}",
                context = context
            )
        }
    }
}

// A reusable helper component for the rows inside the card
@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Helper to convert the Long timestamp back to a readable date
fun formatTimestamp(millis: Long): String {
    if (millis == 0L) return "Not set"
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}

// Defines the three possible states for a renewal date
enum class RenewalStatus {
    OK, DUE_SOON, OVERDUE
}

// Calculates the status based on the saved timestamp
fun checkRenewalStatus(dateMillis: Long): RenewalStatus {
    if (dateMillis == 0L) return RenewalStatus.OK // Ignore if the user hasn't set a date yet

    val todayMillis = System.currentTimeMillis()
    val diffMillis = dateMillis - todayMillis
    val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)

    return when {
        diffDays < 0 -> RenewalStatus.OVERDUE
        diffDays in 0..30 -> RenewalStatus.DUE_SOON
        else -> RenewalStatus.OK
    }
}
// A custom amber color for the 'Due Soon' warning
val WarningAmber = Color(0xFFF57C00)

@Composable
fun RenewalRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    dateMillis: Long,
    buttonText: String,
    url: String,
    context: Context // Needed to launch the browser
) {
    val status = checkRenewalStatus(dateMillis)

    // Dynamically set the text color based on the calculated status
    val valueColor = when (status) {
        RenewalStatus.OVERDUE -> MaterialTheme.colorScheme.error // Standard M3 Red
        RenewalStatus.DUE_SOON -> WarningAmber
        RenewalStatus.OK -> MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween // Pushes the button to the far right edge
    ) {
        // Left side: Icon and Text
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatTimestamp(dateMillis),
                    style = MaterialTheme.typography.bodyLarge,
                    color = valueColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Right side: Action Button (Only visible if Due Soon or Overdue)
        if (status == RenewalStatus.DUE_SOON || status == RenewalStatus.OVERDUE) {
            OutlinedButton(
                onClick = {
                    // Standard Android Intent to open a URL
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                }
            ) {
                Text(buttonText)
            }
        }
    }
}