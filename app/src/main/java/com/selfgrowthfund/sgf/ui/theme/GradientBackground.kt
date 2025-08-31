package com.selfgrowthfund.sgf.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush

@Composable
fun GradientBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to SecondaryCream,  // Top
                        0.66f to SecondaryCream, // 2/3 cream
                        1.0f to PrimaryGreen     // Bottom 1/3 green
                    )
                )
            )
    ) {
        content()
    }
}
