package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = FintechPrimary,
    secondary = FintechSecondary,
    tertiary = FintechTertiary,
    background = FintechBackground,
    surface = FintechSurface,
    error = FintechError,
    onPrimary = Color(0xFF0F0F12),
    onSecondary = Color.White,
    onTertiary = Color(0xFF0F0F12),
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = FintechPrimary,
    secondary = FintechSecondary,
    tertiary = FintechTertiary,
    background = FintechBackground,
    surface = FintechSurface,
    error = FintechError,
    onPrimary = Color(0xFF0F0F12),
    onSecondary = Color.White,
    onTertiary = Color(0xFF0F0F12),
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to true to respect Sophisticated Dark visual intent
  dynamicColor: Boolean = false, // Force custom theme colors
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme


  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
