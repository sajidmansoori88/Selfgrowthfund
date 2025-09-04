package com.selfgrowthfund.sgf.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.ColorScheme

val SGFLightColorScheme: ColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = TextWhite,
    primaryContainer = AccentLight,         // Used for drawer background
    onPrimaryContainer = AccentDark,        // Used for drawer text
    surface = Surface,
    onSurface = TextPrimary,
    error = ErrorRed,
    onError = TextWhite,
    secondary = SecondaryCream,
    onSecondary = TextPrimary,
    background = Surface,
    onBackground = TextPrimary
)

val SGFDarkColorScheme: ColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    onPrimary = TextWhite,
    primaryContainer = AccentDark,
    onPrimaryContainer = AccentLight,
    surface = TextPrimary,
    onSurface = TextWhite,
    error = ErrorRed,
    onError = TextWhite,
    secondary = TextSecondary,
    onSecondary = TextWhite,
    background = TextPrimary,
    onBackground = TextWhite
)