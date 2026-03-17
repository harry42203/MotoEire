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

// This might be named slightly differently depending on your exact project name!
// Try hitting Alt + Enter if it's red to import your specific Theme.

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Initialize the Database and Repository
        val database = GarageDatabase.getDatabase(this)
        val repository = CarRepository(database.carDao())

        setContent {
            // 2. Wrap the app in your dynamic theme!
            MotoEireTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 3. Launch the main App component
                    MotoEireApp(repository = repository)
                }
            }
        }
    }
}

// A simple way to track which screen we are on without adding complex Navigation libraries
enum class Screen { Dashboard, AddCar }

@Composable
fun MotoEireApp(repository: CarRepository) {
    // Keep track of the current screen. We start on the Dashboard.
    var currentScreen by remember { mutableStateOf(Screen.Dashboard) }

    // Create our factory so Compose knows how to build ViewModels with the Database
    val factory = GarageViewModelFactory(repository)

    // Handle Android system back button when on AddCar screen
    BackHandler(enabled = currentScreen == Screen.AddCar) {
        currentScreen = Screen.Dashboard
    }

    // Swap the screens based on the current state
    when (currentScreen) {
        Screen.Dashboard -> {
            val viewModel: GarageViewModel = viewModel(factory = factory)
            MyGarageScreen(
                viewModel = viewModel,
                onAddCarClick = { currentScreen = Screen.AddCar }
            )
        }
        Screen.AddCar -> {
            val viewModel: AddCarViewModel = viewModel(factory = factory)
            AddCarScreen(
                viewModel = viewModel,
                onNavigateBack = { currentScreen = Screen.Dashboard }
            )
        }
    }
}

// This Factory tells Android how to pass your Repository into your ViewModels
class GarageViewModelFactory(private val repository: CarRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GarageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GarageViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(AddCarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddCarViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}