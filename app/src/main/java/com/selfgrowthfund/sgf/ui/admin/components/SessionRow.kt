package com.selfgrowthfund.sgf.ui.admin.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.model.SessionEntry

@Composable
fun SessionRow(index: Int, entry: SessionEntry) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("${index + 1}", modifier = Modifier.width(24.dp))
        Text(entry.shareholderId, modifier = Modifier.width(50.dp))
        Text(entry.name, modifier = Modifier.weight(1f))
        Text("${entry.currentMonthSessions}", modifier = Modifier.width(80.dp))
        Text("${entry.lifetimeSessions}", modifier = Modifier.width(80.dp))
    }
}

@Composable
fun SessionHeaderRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Sr.", modifier = Modifier.width(24.dp))
        Text("ID", modifier = Modifier.width(100.dp))
        Text("Name", modifier = Modifier.weight(1f))
        Text("Month", modifier = Modifier.width(80.dp))
        Text("Lifetime", modifier = Modifier.width(80.dp))
    }
}