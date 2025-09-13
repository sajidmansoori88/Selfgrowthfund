package com.selfgrowthfund.sgf.ui.admin.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.model.ApprovalHistoryEntry

@Composable
fun ApprovalHistoryRow(entry: ApprovalHistoryEntry) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Type: ${entry.type}", style = MaterialTheme.typography.titleMedium)
            Text("Approved by: ${entry.approvedBy}", style = MaterialTheme.typography.bodyMedium)
            Text("Date: ${entry.date}", style = MaterialTheme.typography.bodySmall)
            Text("Status: ${entry.status}", style = MaterialTheme.typography.bodySmall)
        }
    }
}