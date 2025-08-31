package com.selfgrowthfund.sgf.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MetricCard(
    title: String,
    count: Int? = null,
    value: Double? = null,
    highlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = if (highlight)
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        else
            CardDefaults.cardColors()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            count?.let {
                Text(
                    text = "$it",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            value?.let {
                Text(
                    text = "₹%.2f".format(it),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}