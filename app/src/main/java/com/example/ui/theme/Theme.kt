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

private val DarkColorScheme = darkColorScheme(
    primary = BotswanaBlue,
    onPrimary = DarkSlate,
    primaryContainer = BotswanaBlueDark,
    onPrimaryContainer = BotswanaBlueLight,
    secondary = GoldenAmber,
    onSecondary = DarkSlate,
    secondaryContainer = GoldenAmberDark,
    background = DarkSlate,
    surface = DarkSurface,
    onBackground = LightSlate,
    onSurface = LightSlate,
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = BotswanaBlueDark,
    onPrimary = LightSurface,
    primaryContainer = BotswanaBlueLight,
    onPrimaryContainer = BotswanaBlueDark,
    secondary = GoldenAmber,
    onSecondary = DarkSlate,
    secondaryContainer = BotswanaBlue,
    background = LightSlate,
    surface = LightSurface,
    onBackground = DarkSlate,
    onSurface = DarkSlate,
    outline = LightBorder
)

@Composable
fun MaranathaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep dynamic color enabled on Android 12+ if desired, but default to our beautiful Botswana identity
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
