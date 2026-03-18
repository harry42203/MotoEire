package com.example.motoeire

// ✅ Represents each screen in your app
sealed class Screen {
    data object Dashboard : Screen()
    data object AddCar : Screen()
    data object EditCar : Screen()  // Easy to add new screens!
    data object Details : Screen()  // Future screens
    data object Settings : Screen()
    data object Notifications : Screen()
}

// ✅ Navigation actions
sealed class NavigationAction {
    data object Back : NavigationAction()
    data class NavigateTo(val screen: Screen) : NavigationAction()
}

// ✅ Navigation state holder
class NavigationStack {
    private val stack = mutableListOf<Screen>()

    fun push(screen: Screen) {
        stack.add(screen)
    }

    fun pop(): Boolean {
        return if (stack.size > 1) {
            stack.removeAt(stack.size - 1)
            true
        } else {
            false
        }
    }

    fun current(): Screen {
        return stack.lastOrNull() ?: Screen.Dashboard
    }

    fun canGoBack(): Boolean {
        return stack.size > 1
    }
}