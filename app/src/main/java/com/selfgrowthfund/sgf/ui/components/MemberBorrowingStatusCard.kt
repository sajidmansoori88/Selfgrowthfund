package com.selfgrowthfund.sgf.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.data.local.dto.MemberBorrowingStatus

@Composable
fun MemberBorrowingStatusCard(status: MemberBorrowingStatus) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (status.isOverdue)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(status.shareholderName, style = MaterialTheme.typography.titleMedium)
            Text("Total Borrowed: ₹${status.totalBorrowed}")
            Text("Total Repaid: ₹${status.totalRepaid}")
            Text("Outstanding: ₹${status.outstanding}")
            if (status.isOverdue) {
                Text("⚠️ Overdue", color = MaterialTheme.colorScheme.error)
            } else {
                Text("Next Due: ${status.nextDueDate ?: "—"}")
            }
        }
    }
}
