package com.selfgrowthfund.sgf.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.model.reports.CashFlowEntry

@Composable
fun CashFlowChart(data: List<CashFlowEntry>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        data.forEach { entry ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(entry.month, style = MaterialTheme.typography.bodySmall)
                Text("Opening: ₹%.2f".format(entry.openingBalance), style = MaterialTheme.typography.bodySmall)
                Text("Closing: ₹%.2f".format(entry.closingBalance), style = MaterialTheme.typography.bodySmall)
            }
            Divider()
        }
    }
}