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
import androidx.navigation.NavHostController
import androidx.compose.runtime.livedata.observeAsState
import com.selfgrowthfund.sgf.data.local.entities.Borrowing
import com.selfgrowthfund.sgf.ui.repayments.RepaymentSummary
import com.selfgrowthfund.sgf.ui.repayments.RepaymentViewModel
import com.selfgrowthfund.sgf.ui.theme.GradientBackground

@OptIn(ExperimentalMaterial3Api::class)
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
                Text(
                    "⚠️ Overdue",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorrowingHistoryScreen(
    onAddBorrowing: () -> Unit,
    onAddRepayment: (String) -> Unit,
    navController: NavHostController,
    viewModel: BorrowingViewModel = hiltViewModel(),
    repaymentViewModel: RepaymentViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val allBorrowings by viewModel.allBorrowings.collectAsState()
    val summaries by repaymentViewModel.repaymentSummaries.collectAsState()
    val expanded = remember { mutableStateOf(false) }

    val refreshTrigger = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<Boolean>("refreshBorrowings")
        ?.observeAsState()

    // Refresh once when loaded
    LaunchedEffect(Unit) {
        viewModel.refreshAllBorrowings()
    }

    // Update repayment summaries when borrowings change
    LaunchedEffect(allBorrowings) {
        repaymentViewModel.loadSummaries(allBorrowings)
    }

    // React to refresh flag from ApplyBorrowingScreen
    LaunchedEffect(refreshTrigger?.value) {
        if (refreshTrigger?.value == true) {
            viewModel.refreshAllBorrowings()
            snackbarHostState.showSnackbar("New borrowing added ✅")
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("refreshBorrowings", false)
        }
    }

    GradientBackground {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { expanded.value = !expanded.value },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Actions")
                }
            }
        ) { paddingValues ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (allBorrowings.isEmpty()) {
                    Text(
                        text = "No borrowings yet. Tap + to apply.",
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.Center),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
                    ) {
                        item {
                            Text(
                                text = "Borrowing History",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            )
                        }

                        items(allBorrowings.sortedByDescending { it.createdAt }) { borrowing ->
                            val summary = summaries[borrowing.borrowId ?: borrowing.provisionalId]
                            BorrowingCard(
                                borrowing = borrowing,
                                summary = summary as? RepaymentSummary,
                                onRepay = {
                                    onAddRepayment(
                                        borrowing.borrowId ?: borrowing.provisionalId
                                    )
                                }
                            )
                        }
                    }
                }

                DropdownMenu(
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 24.dp, bottom = 72.dp)
                ) {
                    DropdownMenuItem(
                        text = { Text("Borrow") },
                        onClick = {
                            expanded.value = false
                            onAddBorrowing()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Repay") },
                        onClick = {
                            expanded.value = false
                            allBorrowings.firstOrNull()?.let {
                                onAddRepayment(it.borrowId ?: it.provisionalId)
                            }
                        }
                    )
                }
            }
        }
    }
}


