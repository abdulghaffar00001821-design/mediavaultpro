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
    primary = AccentIndigo,
    onPrimary = Color.White,
    secondary = AccentCyan,
    onSecondary = Color.Black,
    tertiary = AccentGreen,
    onTertiary = Color.White,
    background = MainBgDark,
    onBackground = TextLight,
    surface = CardBgDark,
    onSurface = TextLight,
    surfaceVariant = HeaderFooterDark,
    onSurfaceVariant = TextMuted,
    outline = BorderDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = AccentIndigo,
    onPrimary = Color.White,
    secondary = AccentCyan,
    onSecondary = Color.Black,
    tertiary = AccentGreen,
    onTertiary = Color.White,
    background = MainBgLight,
    onBackground = TextDark,
    surface = CardBgLight,
    onSurface = TextDark,
    surfaceVariant = HeaderFooterLight,
    onSurfaceVariant = TextDarkMuted,
    outline = BorderLight
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
