package com.selfgrowthfund.sgf.ui.borrowing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfgrowthfund.sgf.data.local.entities.Borrowing
import com.selfgrowthfund.sgf.ui.repayments.RepaymentSummary
import com.selfgrowthfund.sgf.ui.repayments.RepaymentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorrowingHistoryScreen(
    onAddBorrowing: () -> Unit,
    onAddRepayment: (String) -> Unit,
    viewModel: BorrowingViewModel = hiltViewModel(),
    repaymentViewModel: RepaymentViewModel = hiltViewModel()
) {
    val allBorrowings by viewModel.allBorrowings.collectAsState()
    val summaries by repaymentViewModel.repaymentSummaries.collectAsState()
    val expanded = remember { mutableStateOf(false) }

    LaunchedEffect(allBorrowings) {
        repaymentViewModel.loadSummaries(allBorrowings)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Borrowing History") })
        },
        floatingActionButton = {
            Box(modifier = Modifier.fillMaxSize()) {
                ExtendedFloatingActionButton(
                    text = { Text("Actions") },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    onClick = { expanded.value = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                )

                DropdownMenu(
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Add Borrowing") },
                        onClick = {
                            expanded.value = false
                            onAddBorrowing()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add Repayment") },
                        onClick = {
                            expanded.value = false
                            allBorrowings.firstOrNull()?.let {
                                onAddRepayment(it.borrowId)
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (allBorrowings.isEmpty()) {
                Text(
                    text = "No borrowings yet",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(allBorrowings) { borrowing ->
                        val summary = summaries[borrowing.borrowId]
                        BorrowingCard(
                            borrowing = borrowing,
                            summary = summary,
                            onRepay = { onAddRepayment(borrowing.borrowId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BorrowingCard(
    borrowing: Borrowing,
    summary: RepaymentSummary?,
    onRepay: () -> Unit
) {
    val principalRepaid = summary?.totalPrincipal ?: 0.0
    val penaltyPaid = summary?.totalPenalty ?: 0.0
    val finalOutstanding = borrowing.amountRequested - principalRepaid

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Borrower: ${borrowing.shareholderName}", style = MaterialTheme.typography.titleMedium)
            Text("Amount Requested: ₹${borrowing.amountRequested}", style = MaterialTheme.typography.bodyMedium)
            Text("Status: ${borrowing.status.label}", style = MaterialTheme.typography.bodySmall)
            Text("Due Date: ${borrowing.dueDate}", style = MaterialTheme.typography.bodySmall)

            if (borrowing.isOverdue()) {
                Text("⚠️ Overdue", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Repaid: ₹$principalRepaid", style = MaterialTheme.typography.bodySmall)
            Text("Penalty Paid: ₹$penaltyPaid", style = MaterialTheme.typography.bodySmall)
            Text("Outstanding: ₹${"%.2f".format(finalOutstanding)}", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = onRepay, modifier = Modifier.align(Alignment.End)) {
                Text("Repay")
            }
        }
    }
}