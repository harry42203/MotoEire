package com.example.motoeire

import android.content.Context
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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.platform.LocalContext

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
    // ✅ FIXED - Create SettingsDataStore inside @Composable
    val context = LocalContext.current
    val settingsDataStore = remember { SettingsDataStore(context) }
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(settingsDataStore, repository, context)
    )
    val settings by settingsViewModel.userSettings.collectAsState()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    var selectedCarId by remember { mutableStateOf<Int?>(null) }

    val navigationStack = remember {
        mutableListOf<Screen>(Screen.Dashboard)
    }

    val factory = GarageViewModelFactory(repository)

    // ✅ Apply theme based on settings
    val useDarkMode = when (settings.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    BackHandler(enabled = navigationStack.size > 1) {
        navigationStack.removeAt(navigationStack.size - 1)
        currentScreen = navigationStack.last()
    }

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

    MotoEireTheme(darkTheme = useDarkMode) {
        when (currentScreen) {
            Screen.Dashboard -> {
                val viewModel: GarageViewModel = viewModel(factory = factory)
                MyGarageScreen(
                    viewModel = viewModel,
                    viewMode = settings.viewMode,
                    onAddCarClick = { navigate(Screen.AddCar) },
                    onCarCardClick = { carId ->
                        selectedCarId = carId
                        navigate(Screen.Details)
                    },
                    onSettingsClick = { navigate(Screen.Settings) }
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
                if (selectedCarId != null) {
                    val viewModel: GarageViewModel = viewModel(factory = factory)
                    DetailsScreen(
                        carId = selectedCarId!!,
                        viewModel = viewModel,
                        onNavigateBack = { goBack() },
                        onEditClick = { navigate(Screen.EditCar) },
                        onDeleteClick = { goBack() }
                    )
                }
            }
            Screen.Settings -> {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateBack = { goBack() },
                    onNavigateToNotifications = { navigate(Screen.Notifications) }  // ✅ ADD THIS
                )
            }
            Screen.Notifications -> {  // ✅ ADD THIS ENTIRE BLOCK
                NotificationsScreen(
                    viewModel = settingsViewModel,
                    onNavigateBack = { goBack() }
                )
            }
        }
    }
}

class SettingsViewModelFactory(
    private val settingsDataStore: SettingsDataStore,
    private val repository: CarRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                SettingsViewModel(settingsDataStore, repository, context) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
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