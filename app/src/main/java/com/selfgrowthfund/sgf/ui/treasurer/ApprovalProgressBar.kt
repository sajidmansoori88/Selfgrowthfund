package com.selfgrowthfund.sgf.ui.treasurer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * 🔹 Displays 2/3 approval progress for a borrowing item.
 */
@Composable
fun ApprovalProgressBar(
    approvedCount: Int,
    requiredCount: Int,
    modifier: Modifier = Modifier
) {
    val progress = (approvedCount.toFloat() / requiredCount.coerceAtLeast(1)).coerceIn(0f, 1f)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$approvedCount / $requiredCount approvals",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
