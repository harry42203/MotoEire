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

    val navigationStack = remember {
        mutableListOf<Screen>(Screen.Dashboard)
    }

    val factory = GarageViewModelFactory(repository)

    // ✅ System back button handler
    BackHandler(enabled = navigationStack.size > 1) {
        navigationStack.removeAt(navigationStack.size - 1)
        currentScreen = navigationStack.last()  // ✅ Update state to trigger recomposition
    }

    // ✅ Navigation functions
    val navigate = { screen: Screen ->
        navigationStack.add(screen)
        currentScreen = screen  // ✅ KEY FIX - Update state so UI recomposes
    }

    val goBack = {
        if (navigationStack.size > 1) {
            navigationStack.removeAt(navigationStack.size - 1)
            currentScreen = navigationStack.last()  // ✅ Update state to trigger recomposition
        }
    }

    // ✅ Swap screens based on current state
    when (currentScreen) {
        Screen.Dashboard -> {
            val viewModel: GarageViewModel = viewModel(factory = factory)
            MyGarageScreen(
                viewModel = viewModel,
                onAddCarClick = { navigate(Screen.AddCar) }
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
            // TODO: Implement EditCarScreen
        }
        Screen.Details -> {
            // TODO: Implement DetailsScreen
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