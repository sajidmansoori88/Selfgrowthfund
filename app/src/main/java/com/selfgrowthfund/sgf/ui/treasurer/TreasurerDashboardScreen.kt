package com.selfgrowthfund.sgf.ui.treasurer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.session.UserSessionViewModel
import com.selfgrowthfund.sgf.ui.theme.GradientBackground
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreasurerDashboardScreen(
    viewModel: TreasurerDashboardViewModel = hiltViewModel(),
    userSessionViewModel: UserSessionViewModel = hiltViewModel()
) {
    // ─── Collect states ───────────────────────────────────────────────
    val currentUser by userSessionViewModel.currentUser.collectAsState()
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Deposits", "Borrowings", "Repayments", "Investments", "Returns", "Others")
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // ─── Step 1: Wait for session to load ─────────────────────────────
    if (currentUser.shareholderId.isNullOrEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Loading user session...")
            }
        }
        return
    }

    // ─── Step 2: Restrict non-treasurer roles ─────────────────────────
    if (currentUser.role != MemberRole.MEMBER_TREASURER) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Access Denied — Only Treasurer can access this screen.",
                color = MaterialTheme.colorScheme.error
            )
        }
        return
    }

    val treasurerId = currentUser.shareholderId

    // ─── Step 3: Show loading indicator while dashboard loads ─────────
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Loading Treasurer Dashboard...")
            }
        }
        return
    }

    // ─── Step 4: Main content ─────────────────────────────────────────
    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "Welcome Treasurer ${currentUser.name}",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Summary
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                SummaryRow(
                    deposits = state.deposits.size,
                    borrowings = state.borrowings.size,
                    repayments = state.repayments.size,
                    investments = state.investments.size,
                    returns = state.returns.size
                )
            }

            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        text = { Text(title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }

            // Tab content
            when (selectedTab) {
                // Deposits
                0 -> TreasurerListSection(
                    title = "Pending Deposits",
                    items = state.deposits.map { it.provisionalId to it.shareholderName },
                    onAction = { id ->
                        viewModel.approveDeposit(id, treasurerId) { success ->
                            coroutineScope.launch {
                                showSnack(
                                    snackbarHostState,
                                    if (success) "Deposit approved ✅" else "Action failed ❌"
                                )
                            }
                        }
                    },
                    actionLabel = "Approve"
                )

                // Borrowings
                1 -> TreasurerListSection(
                    title = "Pending Borrowing Releases",
                    items = state.borrowings.map { it.provisionalId to "${it.shareholderName} - ₹${it.approvedAmount}" },
                    onAction = { id ->
                        viewModel.releaseBorrowing(id, treasurerId) { success ->
                            coroutineScope.launch {
                                showSnack(
                                    snackbarHostState,
                                    if (success) "Borrowing released 💸" else "Action failed ❌"
                                )
                            }
                        }
                    },
                    actionLabel = "Release"
                )

                // Repayments
                2 -> TreasurerListSection(
                    title = "Pending Repayments",
                    items = state.repayments.map { it.provisionalId to "Borrow ID: ${it.borrowId}" },
                    onAction = { id ->
                        viewModel.approveRepayment(id, treasurerId) { success ->
                            coroutineScope.launch {
                                showSnack(
                                    snackbarHostState,
                                    if (success) "Repayment verified ✅" else "Action failed ❌"
                                )
                            }
                        }
                    },
                    actionLabel = "Approve"
                )

                // Investments
                3 -> TreasurerListSection(
                    title = "Pending Investments",
                    items = state.investments.map { it.provisionalId to it.investmentName },
                    onAction = { id ->
                        viewModel.releaseInvestment(id, treasurerId) { success ->
                            coroutineScope.launch {
                                showSnack(
                                    snackbarHostState,
                                    if (success) "Investment released 💸" else "Action failed ❌"
                                )
                            }
                        }
                    },
                    actionLabel = "Release"
                )

                // Returns
                4 -> TreasurerListSection(
                    title = "Pending Returns",
                    items = state.returns.map { it.provisionalId to it.investmentId },
                    onAction = { id ->
                        viewModel.approveInvestmentReturn(id, treasurerId) { success ->
                            coroutineScope.launch {
                                showSnack(
                                    snackbarHostState,
                                    if (success) "Return approved ✅" else "Action failed ❌"
                                )
                            }
                        }
                    },
                    actionLabel = "Approve"
                )

                // Others
                5 -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Add Other Income / Expense via respective forms.")
                }
            }
        }
    }
}

// ─── Helper components ───────────────────────────────────────────────
@Composable
private fun TreasurerListSection(
    title: String,
    items: List<Pair<String, String>>,
    onAction: (String) -> Unit,
    actionLabel: String
) {
    Column(Modifier.padding(16.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        if (items.isEmpty()) {
            Text("No items pending", color = MaterialTheme.colorScheme.outline)
        } else {
            LazyColumn {
                items(items) { (id, label) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(label, style = MaterialTheme.typography.bodyLarge)
                                Text("ID: $id", style = MaterialTheme.typography.bodySmall)
                            }
                            Button(onClick = { onAction(id) }) { Text(actionLabel) }
                        }
                    }
                }
            }
        }
    }
}

// Safe snackbar launcher
private fun showSnack(snackbarHostState: SnackbarHostState, message: String) {
    CoroutineScope(Dispatchers.Main).launch {
        snackbarHostState.showSnackbar(message)
    }
}
