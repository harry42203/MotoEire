package com.example.motoeire

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.material3.ripple
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
import coil.compose.AsyncImage
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyGarageScreen(
    viewModel: GarageViewModel,
    viewMode: ViewMode = ViewMode.GRID,
    onAddCarClick: () -> Unit,
    onCarCardClick: (carId: Int) -> Unit,
    onSettingsClick: () -> Unit
) {
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
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    }
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
            when (viewMode) {
                ViewMode.GRID -> GridViewLayout(cars, paddingValues, onCarCardClick)
                ViewMode.CARD -> CardViewLayout(cars, paddingValues, onCarCardClick)
                ViewMode.LIST -> ListViewLayout(cars, paddingValues, onCarCardClick)
            }
        }
    }
}

@Composable
fun GridViewLayout(
    cars: List<Car>,
    paddingValues: PaddingValues,
    onCarCardClick: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(cars, key = { car -> car.id }) { car ->
            CarGalleryCard(
                car = car,
                onClick = { onCarCardClick(car.id) }
            )
        }
    }
}

@Composable
fun CardViewLayout(
    cars: List<Car>,
    paddingValues: PaddingValues,
    onCarCardClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(cars, key = { car -> car.id }) { car ->
            CarWideCard(car = car, onClick = { onCarCardClick(car.id) })
        }
    }
}

// ✅ Wide Card Component for Card View (16:9)
@Composable
fun CarWideCard(
    car: Car,
    onClick: () -> Unit
) {
    val statuses = getIndividualRenewalStatuses(
        insuranceDate = car.insuranceRenewalDate,
        nctDate = car.nctRenewalDate,
        motorTaxDate = car.motorTaxRenewalDate
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                enabled = true,
                indication = ripple(),
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() },
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box {
            if (car.imagePath != null) {
                AsyncImage(
                    model = car.imagePath,
                    contentDescription = "Car photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.25f)
            ) {}

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = car.nickname,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.TopStart)
                )

                Text(
                    text = car.registrationNumber,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.align(Alignment.BottomStart)
                )

                // ✅ Show only issues or all ok
                RenewalStatusColumn(
                    statuses = statuses,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }
    }
}

@Composable
fun ListViewLayout(
    cars: List<Car>,
    paddingValues: PaddingValues,
    onCarCardClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(cars, key = { car -> car.id }) { car ->
            CarListItem(car = car, onClick = { onCarCardClick(car.id) })
        }
    }
}

@Composable
fun CarListItem(
    car: Car,
    onClick: () -> Unit
) {
    val renewalStatus = try {
        getWorstRenewalStatus(
            insuranceDate = car.insuranceRenewalDate,
            nctDate = car.nctRenewalDate,
            motorTaxDate = car.motorTaxRenewalDate
        )
    } catch (e: Exception) {
        RenewalStatus.OK
    }

    val statusColor = when (renewalStatus) {
        RenewalStatus.OK -> Color(0xFF4CAF50)
        RenewalStatus.DUE_SOON -> Color(0xFFF57C00)
        RenewalStatus.OVERDUE -> Color(0xFFD32F2F)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                enabled = true,
                indication = ripple(),
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() },
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = car.nickname,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = car.registrationNumber,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                color = statusColor,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = when (renewalStatus) {
                        RenewalStatus.OK -> "✓"
                        RenewalStatus.DUE_SOON -> "!"
                        RenewalStatus.OVERDUE -> "⚠"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// ✅ Beautiful gallery card with image and status badges
@Composable
fun CarGalleryCard(
    car: Car,
    onClick: () -> Unit
) {
    val statuses = getIndividualRenewalStatuses(
        insuranceDate = car.insuranceRenewalDate,
        nctDate = car.nctRenewalDate,
        motorTaxDate = car.motorTaxRenewalDate
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                enabled = true,
                indication = ripple(),
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() },
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box {
            if (car.imagePath != null) {
                AsyncImage(
                    model = car.imagePath,
                    contentDescription = "Car photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {}
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.3f)
            ) {}

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Text(
                    text = car.nickname,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.TopStart)
                )

                // ✅ Show only issues or all ok
                RenewalStatusColumn(
                    statuses = statuses,
                    modifier = Modifier.align(Alignment.TopEnd)
                )

                Text(
                    text = car.registrationNumber,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.align(Alignment.BottomStart)
                )
            }
        }
    }
}

// ✅ NEW - Data class for individual renewal statuses
data class RenewalStatuses(
    val nctStatus: RenewalStatus,
    val taxStatus: RenewalStatus,
    val insuranceStatus: RenewalStatus
)

// ✅ NEW - Get individual statuses for each renewal type
fun getIndividualRenewalStatuses(
    insuranceDate: Long,
    nctDate: Long,
    motorTaxDate: Long
): RenewalStatuses {
    return RenewalStatuses(
        nctStatus = checkRenewalStatus(nctDate),
        taxStatus = checkRenewalStatus(motorTaxDate),
        insuranceStatus = checkRenewalStatus(insuranceDate)
    )
}

// ✅ UPDATED - Only show issues (DUE_SOON or OVERDUE) or display "All OK"
@Composable
fun RenewalStatusColumn(
    statuses: RenewalStatuses,
    modifier: Modifier = Modifier
) {
    // Get only the items that have issues
    val issues = listOfNotNull(
        if (statuses.nctStatus != RenewalStatus.OK) "NCT" to statuses.nctStatus else null,
        if (statuses.taxStatus != RenewalStatus.OK) "Tax" to statuses.taxStatus else null,
        if (statuses.insuranceStatus != RenewalStatus.OK) "Insurance" to statuses.insuranceStatus else null
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (issues.isEmpty()) {
            // ✅ Show "All OK" if no issues
            AllOkBadge()
        } else {
            // ✅ Show only the issues
            issues.forEach { (label, status) ->
                RenewalStatusBadge(label, status)
            }
        }
    }
}

// ✅ NEW - "All OK" badge when everything is good
@Composable
fun AllOkBadge() {
    Surface(
        color = Color(0xFF4CAF50).copy(alpha = 0.9f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "✓",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "All OK",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ✅ Individual status badge with label and icon
@Composable
fun RenewalStatusBadge(
    label: String,
    status: RenewalStatus
) {
    val (backgroundColor, statusText) = when (status) {
        RenewalStatus.OK -> Color(0xFF4CAF50).copy(alpha = 0.9f) to "✓"
        RenewalStatus.DUE_SOON -> Color(0xFFF57C00).copy(alpha = 0.9f) to "!"
        RenewalStatus.OVERDUE -> Color(0xFFD32F2F).copy(alpha = 0.9f) to "⚠"
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ✅ Status badge component (kept for backwards compatibility)
@Composable
fun StatusBadge(
    status: RenewalStatus,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (status) {
        RenewalStatus.OK -> Color(0xFF4CAF50).copy(alpha = 0.9f)
        RenewalStatus.DUE_SOON -> Color(0xFFF57C00).copy(alpha = 0.9f)
        RenewalStatus.OVERDUE -> Color(0xFFD32F2F).copy(alpha = 0.9f)
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

    return when {
        RenewalStatus.OVERDUE in statuses -> RenewalStatus.OVERDUE
        RenewalStatus.DUE_SOON in statuses -> RenewalStatus.DUE_SOON
        else -> RenewalStatus.OK
    }
}

fun formatTimestamp(millis: Long): String {
    if (millis == 0L) return "Not set"
    val formatter = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
    return formatter.format(java.util.Date(millis))
}

enum class RenewalStatus {
    OK, DUE_SOON, OVERDUE
}

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