package com.example.expensetracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun ExpenseTrackerTheme(
    themePreset: ThemePreset = themePresets[0],
    content: @Composable () -> Unit
) {
    val colorScheme = themePresetToColorScheme(themePreset)

    PlatformStatusBarEffect(themePreset.primary)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
expect fun PlatformStatusBarEffect(statusBarColor: Color)
