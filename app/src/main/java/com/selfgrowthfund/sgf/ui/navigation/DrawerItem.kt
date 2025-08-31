package com.selfgrowthfund.sgf.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun DrawerItem(
    label: String,
    badgeCount: Int? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        icon?.let {
            Icon(imageVector = it, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        badgeCount?.let {
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "($it)", style = MaterialTheme.typography.labelSmall)
        }
    }
}