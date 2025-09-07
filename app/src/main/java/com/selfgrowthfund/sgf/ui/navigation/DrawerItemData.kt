package com.selfgrowthfund.sgf.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector

data class DrawerItemData(
    val label: String,
    val route: String,
    val icon: ImageVector? = null
)
