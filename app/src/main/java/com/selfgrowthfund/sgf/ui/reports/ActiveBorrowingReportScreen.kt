package com.selfgrowthfund.sgf.ui.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfgrowthfund.sgf.model.reports.BorrowingSummaryViewModel

@Composable
fun ActiveBorrowingReportScreen(viewModel: BorrowingSummaryViewModel = hiltViewModel()) {
    val borrowings by viewModel.activeBorrowings.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Active / Outstanding Borrowings", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(borrowings) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(item.shareholderName, modifier = Modifier.weight(1f))
                    Text(item.borrowId, modifier = Modifier.weight(1f))
                    Text("₹${item.borrowAmount}", modifier = Modifier.weight(1f))
                    Text("₹${item.principalRepaid}", modifier = Modifier.weight(1f))
                    Text("₹${item.penaltyPaid}", modifier = Modifier.weight(1f))
                    Text("₹${item.outstanding}", modifier = Modifier.weight(1f))
                    Text("₹${item.penaltyDue}", modifier = Modifier.weight(1f))
                    Text("${item.overdueDays} days", modifier = Modifier.weight(1f))
                }
                Divider()
            }
        }
    }
}