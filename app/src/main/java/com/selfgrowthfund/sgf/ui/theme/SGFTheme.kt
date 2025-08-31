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

    background = Color.Transparent, // gradient background
    onBackground = TextBlack,

    surface = Color.Transparent,
    onSurface = TextBlack,

    error = Color(0xFFD32F2F),
    onError = TextWhite
)

// 🌙 Dark Theme Colors
private val DarkColors = darkColorScheme(
    primary = PrimaryGreen,
    onPrimary = TextBlack,  // Green button → black text for readability

    background = Color.Black,
    onBackground = TextWhite,

    surface = Color(0xFF121212),
    onSurface = TextWhite,

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
        typography = MaterialTheme.typography,
        content = content
    )
}
