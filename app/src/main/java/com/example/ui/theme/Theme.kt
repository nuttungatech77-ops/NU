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
    primary = NuPrimaryGold,
    secondary = NuAccentGold,
    tertiary = NuGlowBlue,
    background = NuDeepBlueBg,
    surface = NuCardSlate,
    onPrimary = NuDeepBlueBg,
    onSecondary = NuDeepBlueBg,
    onBackground = NuTextWhite,
    onSurface = NuTextWhite
  )

private val LightColorScheme =
  lightColorScheme(
    primary = NuLightPrimary,
    secondary = NuLightGold,
    tertiary = NuAccentGold,
    background = NuLightBg,
    surface = NuLightCard,
    onPrimary = NuTextWhite,
    onSecondary = NuTextWhite,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force Dark theme by default for the futuristic experience
  dynamicColor: Boolean = false, // Disable dynamic colors so our premium golden branding shines
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
