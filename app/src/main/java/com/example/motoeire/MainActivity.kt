package com.example.motoeire

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.BackHandler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = GarageDatabase.getDatabase(this)
        val repository = CarRepository(database.carDao())

        setContent {
            MotoEireTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MotoEireApp(repository = repository)
                }
            }
        }
    }
}

@Composable
fun MotoEireApp(repository: CarRepository) {
    // ✅ Use mutableStateOf to make it reactive
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    var selectedCarId by remember { mutableStateOf<Int?>(null) }  // ✅ NEW - Track selected car

    val navigationStack = remember {
        mutableListOf<Screen>(Screen.Dashboard)
    }

    val factory = GarageViewModelFactory(repository)

    // ✅ System back button handler
    BackHandler(enabled = navigationStack.size > 1) {
        navigationStack.removeAt(navigationStack.size - 1)
        currentScreen = navigationStack.last()
    }

    // ✅ Navigation functions
    val navigate = { screen: Screen ->
        navigationStack.add(screen)
        currentScreen = screen
    }

    val goBack = {
        if (navigationStack.size > 1) {
            navigationStack.removeAt(navigationStack.size - 1)
            currentScreen = navigationStack.last()
        }
    }

    // ✅ Swap screens based on current state
    when (currentScreen) {
        Screen.Dashboard -> {
            val viewModel: GarageViewModel = viewModel(factory = factory)
            MyGarageScreen(
                viewModel = viewModel,
                onAddCarClick = { navigate(Screen.AddCar) },
                onCarCardClick = { carId ->  // ✅ NEW
                    selectedCarId = carId
                    navigate(Screen.Details)
                }
            )
        }
        Screen.AddCar -> {
            val viewModel: AddCarViewModel = viewModel(factory = factory)
            AddCarScreen(
                viewModel = viewModel,
                onNavigateBack = { goBack() }
            )
        }
        Screen.EditCar -> {
            // ✅ NEW - EditCarScreen
            if (selectedCarId != null) {
                val detailsViewModel: GarageViewModel = viewModel(factory = factory)
                val editViewModel: AddCarViewModel = viewModel(factory = factory)
                EditCarScreen(
                    carId = selectedCarId!!,
                    viewModel = detailsViewModel,
                    editViewModel = editViewModel,
                    onNavigateBack = { goBack() }
                )
            }
        }
        Screen.Details -> {
            // ✅ UNCOMMENTED - Details screen
            if (selectedCarId != null) {
                val viewModel: GarageViewModel = viewModel(factory = factory)
                DetailsScreen(
                    carId = selectedCarId!!,
                    viewModel = viewModel,
                    onNavigateBack = { goBack() },
                    onEditClick = { navigate(Screen.EditCar) },  // TODO: Implement edit
                    onDeleteClick = { goBack() }  // Go back to dashboard after delete
                )
            }
        }
    }
}

class GarageViewModelFactory(private val repository: CarRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(GarageViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                GarageViewModel(repository) as T
            }
            modelClass.isAssignableFrom(AddCarViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                AddCarViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}