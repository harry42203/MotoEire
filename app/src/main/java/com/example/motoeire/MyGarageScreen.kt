package com.example.motoeire

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedCars by viewModel.selectedCars.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var sortedCars by remember(cars) { mutableStateOf(cars) }

    // ✅ Handle back button to exit selection mode
    BackHandler(enabled = isSelectionMode) {
        viewModel.toggleSelectionMode()
    }

    // ✅ Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    "Delete ${selectedCars.size} car${if (selectedCars.size > 1) "s" else ""}?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSelectedCars()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                // ✅ Selection mode top bar
                TopAppBar(
                    title = {
                        Text(
                            "${selectedCars.size} selected",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.toggleSelectionMode() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Exit selection"
                            )
                        }
                    },
                    actions = {
                        // ✅ Select all button
                        IconButton(
                            onClick = { viewModel.selectAllCars(cars.map { it.id }) }
                        ) {
                            Text("All", modifier = Modifier.padding(horizontal = 8.dp))
                        }
                        // ✅ Delete button
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            enabled = selectedCars.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete selected",
                                tint = if (selectedCars.isNotEmpty())
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            } else {
                // ✅ Normal top bar
                TopAppBar(
                    title = {
                        Text(
                            "My Garage",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
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
            }
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                FloatingActionButton(
                    onClick = onAddCarClick,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Car")
                }
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
                ViewMode.GRID -> GridViewLayout(
                    cars = sortedCars,
                    paddingValues = paddingValues,
                    onCarCardClick = onCarCardClick,
                    isSelectionMode = isSelectionMode,
                    selectedCars = selectedCars,
                    viewModel = viewModel
                )
                ViewMode.CARD -> CardViewLayout(
                    cars = sortedCars,
                    paddingValues = paddingValues,
                    onCarCardClick = onCarCardClick,
                    isSelectionMode = isSelectionMode,
                    selectedCars = selectedCars,
                    viewModel = viewModel,
                    onOrderChanged = { newOrder ->
                        viewModel.updateCarOrder(newOrder)
                        sortedCars = newOrder
                    }
                )
                ViewMode.LIST -> ListViewLayout(
                    cars = sortedCars,
                    paddingValues = paddingValues,
                    onCarCardClick = onCarCardClick,
                    isSelectionMode = isSelectionMode,
                    selectedCars = selectedCars,
                    viewModel = viewModel,
                    onOrderChanged = { newOrder ->
                        viewModel.updateCarOrder(newOrder)
                        sortedCars = newOrder
                    }
                )
            }
        }
    }
}

@Composable
fun GridViewLayout(
    cars: List<Car>,
    paddingValues: PaddingValues,
    onCarCardClick: (Int) -> Unit,
    isSelectionMode: Boolean,
    selectedCars: Set<Int>,
    viewModel: GarageViewModel
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
                onClick = {
                    if (isSelectionMode) {
                        viewModel.toggleCarSelection(car.id)
                    } else {
                        onCarCardClick(car.id)
                    }
                },
                onLongPress = {
                    if (!isSelectionMode) {
                        viewModel.toggleSelectionMode()
                        viewModel.toggleCarSelection(car.id)
                    }
                },
                isSelectionMode = isSelectionMode,
                isSelected = selectedCars.contains(car.id)
            )
        }
    }
}

@Composable
fun CardViewLayout(
    cars: List<Car>,
    paddingValues: PaddingValues,
    onCarCardClick: (Int) -> Unit,
    isSelectionMode: Boolean,
    selectedCars: Set<Int>,
    viewModel: GarageViewModel,
    onOrderChanged: (List<Car>) -> Unit
) {
    var reorderedCars by remember(cars) { mutableStateOf(cars) }
    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(reorderedCars, key = { car -> car.id }) { car ->
            val index = reorderedCars.indexOf(car)

            CarWideCardWithSelection(
                car = car,
                index = index,
                totalItems = reorderedCars.size,
                onClick = {
                    if (isSelectionMode) {
                        viewModel.toggleCarSelection(car.id)
                    } else {
                        onCarCardClick(car.id)
                    }
                },
                onLongPress = {
                    if (!isSelectionMode) {
                        viewModel.toggleSelectionMode()
                        viewModel.toggleCarSelection(car.id)
                    }
                },
                isSelectionMode = isSelectionMode,
                isSelected = selectedCars.contains(car.id),
                isDragging = draggedItemIndex == index,
                onDragStart = { draggedItemIndex = index },
                onDragEnd = {
                    draggedItemIndex = null
                    onOrderChanged(reorderedCars)
                },
                onReorder = { fromIndex, toIndex ->
                    // ✅ FIX - Add bounds checking
                    if (toIndex >= 0 && toIndex < reorderedCars.size) {
                        val newList = reorderedCars.toMutableList()
                        val item = newList.removeAt(fromIndex)
                        newList.add(toIndex, item)
                        reorderedCars = newList
                        draggedItemIndex = toIndex
                    }
                }
            )
        }
    }
}

@Composable
fun ListViewLayout(
    cars: List<Car>,
    paddingValues: PaddingValues,
    onCarCardClick: (Int) -> Unit,
    isSelectionMode: Boolean,
    selectedCars: Set<Int>,
    viewModel: GarageViewModel,
    onOrderChanged: (List<Car>) -> Unit
) {
    var reorderedCars by remember(cars) { mutableStateOf(cars) }
    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(reorderedCars, key = { car -> car.id }) { car ->
            val index = reorderedCars.indexOf(car)

            CarListItemWithSelection(
                car = car,
                index = index,
                totalItems = reorderedCars.size,
                onClick = {
                    if (isSelectionMode) {
                        viewModel.toggleCarSelection(car.id)
                    } else {
                        onCarCardClick(car.id)
                    }
                },
                onLongPress = {
                    if (!isSelectionMode) {
                        viewModel.toggleSelectionMode()
                        viewModel.toggleCarSelection(car.id)
                    }
                },
                isSelectionMode = isSelectionMode,
                isSelected = selectedCars.contains(car.id),
                isDragging = draggedItemIndex == index,
                onDragStart = { draggedItemIndex = index },
                onDragEnd = {
                    draggedItemIndex = null
                    onOrderChanged(reorderedCars)
                },
                onReorder = { fromIndex, toIndex ->
                    // ✅ FIX - Add bounds checking
                    if (toIndex >= 0 && toIndex < reorderedCars.size) {
                        val newList = reorderedCars.toMutableList()
                        val item = newList.removeAt(fromIndex)
                        newList.add(toIndex, item)
                        reorderedCars = newList
                        draggedItemIndex = toIndex
                    }
                }
            )
        }
    }
}

// ✅ Wide Card with drag support (only in selection mode)
@Composable
fun CarWideCardWithSelection(
    car: Car,
    index: Int,
    totalItems: Int,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    isDragging: Boolean = false,
    onDragStart: () -> Unit = {},
    onDragEnd: () -> Unit = {},
    onReorder: (Int, Int) -> Unit = { _, _ -> }
) {
    val statuses = getIndividualRenewalStatuses(
        insuranceDate = car.insuranceRenewalDate,
        nctDate = car.nctRenewalDate,
        motorTaxDate = car.motorTaxRenewalDate
    )

    var dragOffset by remember { mutableStateOf(0f) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                when {
                    isDragging -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    else -> Color.Transparent
                }
            )
            .pointerInput(isSelectionMode) {
                if (isSelectionMode) {
                    detectDragGestures(
                        onDragStart = {
                            onDragStart()
                            dragOffset = 0f
                        },
                        onDragEnd = {
                            onDragEnd()
                        },
                        onDrag = { change, dragAmount ->
                            dragOffset += dragAmount.y
                            if (dragOffset > 100 && index < totalItems - 1) {
                                onReorder(index, index + 1)
                                dragOffset = 0f
                            } else if (dragOffset < -100 && index > 0) {
                                onReorder(index, index - 1)
                                dragOffset = 0f
                            }
                        }
                    )
                }
            }
            .combinedClickable(
                enabled = true,
                indication = ripple(),
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
                onLongClick = onLongPress
            ),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = if (isDragging) 8.dp else 0.dp
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
                // ✅ Selection checkbox
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = null,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .size(24.dp)
                    )
                }

                Text(
                    text = car.nickname,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = if (isSelectionMode) 36.dp else 0.dp)
                )

                Text(
                    text = car.registrationNumber,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.align(Alignment.BottomStart)
                )

                RenewalStatusColumn(
                    statuses = statuses,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }
    }
}

// ✅ Gallery card with selection
@Composable
fun CarGalleryCard(
    car: Car,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    isSelectionMode: Boolean,
    isSelected: Boolean
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
            .background(
                if (isSelected)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                else
                    Color.Transparent
            )
            .combinedClickable(
                enabled = true,
                indication = ripple(),
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
                onLongClick = onLongPress
            ),
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
                // ✅ Selection checkbox
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = null,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .size(24.dp)
                    )
                }

                Text(
                    text = car.nickname,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = if (isSelectionMode) 36.dp else 0.dp)
                )

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

// ✅ List item with drag support (only in selection mode)
@Composable
fun CarListItemWithSelection(
    car: Car,
    index: Int,
    totalItems: Int,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    isDragging: Boolean = false,
    onDragStart: () -> Unit = {},
    onDragEnd: () -> Unit = {},
    onReorder: (Int, Int) -> Unit = { _, _ -> }
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

    var dragOffset by remember { mutableStateOf(0f) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isDragging -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    else -> Color.Transparent
                }
            )
            .pointerInput(isSelectionMode) {
                if (isSelectionMode) {
                    detectDragGestures(
                        onDragStart = {
                            onDragStart()
                            dragOffset = 0f
                        },
                        onDragEnd = {
                            onDragEnd()
                        },
                        onDrag = { change, dragAmount ->
                            dragOffset += dragAmount.y
                            if (dragOffset > 50 && index < totalItems - 1) {
                                onReorder(index, index + 1)
                                dragOffset = 0f
                            } else if (dragOffset < -50 && index > 0) {
                                onReorder(index, index - 1)
                                dragOffset = 0f
                            }
                        }
                    )
                }
            }
            .combinedClickable(
                enabled = true,
                indication = ripple(),
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
                onLongClick = onLongPress
            ),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = if (isDragging) 8.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ✅ Selection checkbox + Car info
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = null,
                        modifier = Modifier.size(24.dp)
                    )
                }

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

data class RenewalStatuses(
    val nctStatus: RenewalStatus,
    val taxStatus: RenewalStatus,
    val insuranceStatus: RenewalStatus
)

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

@Composable
fun RenewalStatusColumn(
    statuses: RenewalStatuses,
    modifier: Modifier = Modifier
) {
    val issues = listOfNotNull(
        if (statuses.nctStatus != RenewalStatus.OK) "NCT" to statuses.nctStatus else null,
        if (statuses.taxStatus != RenewalStatus.OK) "Tax" to statuses.taxStatus else null,
        if (statuses.insuranceStatus != RenewalStatus.OK) "Insurance" to statuses.insuranceStatus else null
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.End
    ) {
        if (issues.isEmpty()) {
            AllOkBadge()
        } else {
            issues.forEach { (label, status) ->
                RenewalStatusBadge(label, status)
            }
        }
    }
}

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