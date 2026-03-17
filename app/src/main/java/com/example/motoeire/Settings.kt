package com.example.motoeire

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

enum class ViewMode {
    GRID, CARD, LIST
}

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val viewMode: ViewMode = ViewMode.GRID
)