package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = lightColorScheme(
    primary = LavenderAccent,
    onPrimary = Color.White,
    secondary = LavenderLight,
    onSecondary = Color.White,
    tertiary = LavenderLight,
    background = DarkBg,
    onBackground = PureWhite,
    surface = DarkCard,
    onSurface = PureWhite,
    surfaceVariant = BorderDark,
    onSurfaceVariant = TextMutedDark
)

private val LightColorScheme = lightColorScheme(
    primary = LavenderAccent,
    onPrimary = Color.White,
    secondary = LavenderLight,
    onSecondary = Color.White,
    tertiary = LavenderDark,
    background = LightBg,
    onBackground = PureWhite,
    surface = LightCard,
    onSurface = PureWhite,
    surfaceVariant = BorderLight,
    onSurfaceVariant = TextMutedLight
)

@Composable
fun NoteifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
