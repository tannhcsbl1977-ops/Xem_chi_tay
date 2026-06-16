package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = GoldAccent,
    secondary = SageSecondary,
    tertiary = SlateGray,
    background = Color(0xFF1E1C1A), // Dark mahogany tint
    surface = Color(0xFF2C2926), // Darker sandy card
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFEBE1D5),
    onSurface = Color(0xFFEBE1D5),
    surfaceVariant = Color(0xFF252220),
    outline = Color(0xFF3F3B36)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = GoldAccent,
    secondary = SageSecondary,
    tertiary = SlateGray,
    background = CreamBg,
    surface = PureWhite,
    onPrimary = PureWhite,
    onSecondary = PureWhite,
    onTertiary = CharcoalText,
    onBackground = CharcoalText,
    onSurface = CharcoalText,
    surfaceVariant = SandBg,
    outline = FineBorder
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+ (false is better for our branded look)
  dynamicColor: Boolean = false,
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
