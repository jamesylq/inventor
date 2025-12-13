package com.example.inventor.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

val LightColors = lightColorScheme(
    primary = Color(0xFFDCA75B),
    onPrimary = Color(0xFF6B4023),
    primaryContainer = Color(0xFFF0DEB8),
    onPrimaryContainer = Color(0xFFA45E0A),
//    secondary = Color(0xFF625B71),
//    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFEBD1A0),
    onSurface = Color(0xFF111111),
//    error = Color(0xFFB3261E),
//    onError = Color(0xFFFFFFFF)
)



@Composable
fun InventorTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content
    )
}