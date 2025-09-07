package com.selfgrowthfund.sgf.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DrawerItem(
    item: DrawerItemData,
    textColor: Color,
    badgeCount: Int? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item.icon?.let {
            Icon(
                imageVector = it,
                contentDescription = item.label,
                tint = textColor
            )
            Spacer(modifier = Modifier.width(16.dp))
        }

        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor
        )

        badgeCount?.let {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "($it)",
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.7f)
            )
        }
    }
}
