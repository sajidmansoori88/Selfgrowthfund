package com.selfgrowthfund.sgf.ui.deposits

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfgrowthfund.sgf.data.local.dto.DepositEntrySummaryDTO
import com.selfgrowthfund.sgf.ui.components.DepositSummaryCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositSummaryScreen(viewModel: DepositViewModel = hiltViewModel()) {
    val summaries by viewModel.depositSummaries.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Deposit Summary") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        if (summaries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No deposit summaries available.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(summaries) { summary ->
                    DepositSummaryCard(summary) // ✅ uses shared component
                }
            }
        }
    }
}

@Composable
fun DepositSummaryCardCompact(summaryDTO: DepositEntrySummaryDTO) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Shareholder: ${summaryDTO.shareholderName}")
            Text("Due Month: ${summaryDTO.dueMonth}")
            Text("Payment Date: ${summaryDTO.paymentDate}")
            Text("Total Amount: ₹${summaryDTO.totalAmount}", fontWeight = FontWeight.Bold)
            Text("Payment Status: ${summaryDTO.paymentStatus}")
            Text("Mode: ${summaryDTO.modeOfPayment}")
        }
    }
}