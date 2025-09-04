package com.selfgrowthfund.sgf.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 🌞 Light Theme Colors
private val LightColors = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = TextWhite,

    secondary = AccentLight,
    onSecondary = TextBlack,

    background = Color.Transparent, // gradient background
    onBackground = TextPrimary,

    surface = Surface,
    onSurface = TextPrimary,

    surfaceVariant = AccentLight,

    error = ErrorRed,
    onError = TextWhite
)

// 🌙 Dark Theme Colors
private val DarkColors = darkColorScheme(
    primary = PrimaryGreen,
    onPrimary = TextBlack,

    secondary = AccentDark,
    onSecondary = TextWhite,

    background = Color.Black,
    onBackground = TextWhite,

    surface = Color(0xFF121212),
    onSurface = TextWhite,

    surfaceVariant = AccentDark,

    error = Color(0xFFCF6679),
    onError = TextBlack
)

@Composable
fun SGFTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = SGFTypography,
        content = content
    )
}