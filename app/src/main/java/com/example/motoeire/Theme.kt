package com.example.motoeire

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun MotoEireTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true, // This enables the wallpaper-based colors!
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Use Dynamic Color if on Android 12+
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme() // Standard M3 Dark
        else -> lightColorScheme() // Standard M3 Light
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}