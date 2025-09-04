package com.selfgrowthfund.sgf.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.selfgrowthfund.sgf.ui.theme.GradientBackground
import com.selfgrowthfund.sgf.ui.theme.Surface
import com.selfgrowthfund.sgf.ui.theme.TextPrimary
import com.selfgrowthfund.sgf.ui.theme.TextWhite

@Composable
fun GradientScaffold(
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()

    GradientBackground {
        // Use Surface to ensure proper text contrast
        Surface(color = Color.Transparent) {
            if (floatingActionButton != null) {
                Scaffold(
                    topBar = topBar,
                    bottomBar = bottomBar,
                    floatingActionButton = floatingActionButton,
                    containerColor = Color.Transparent,
                    contentColor = if (isDarkTheme) TextWhite else TextPrimary,
                    content = content
                )
            } else {
                Scaffold(
                    topBar = topBar,
                    bottomBar = bottomBar,
                    containerColor = Color.Transparent,
                    contentColor = if (isDarkTheme) TextWhite else TextPrimary,
                    content = content
                )
            }
        }
    }
}