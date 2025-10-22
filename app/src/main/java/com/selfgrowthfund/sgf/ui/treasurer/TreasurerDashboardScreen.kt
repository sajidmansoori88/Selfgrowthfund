package com.selfgrowthfund.sgf.ui.treasurer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

/**
 * TreasurerDashboardScreen.kt
 *
 * - Uses a guarded `userReady` flag so transient/partial emissions from the user session
 *   won't prematurely show "Access Denied" or an early spinner and block the UI.
 * - Keeps previous UI structure intact.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreasurerDashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: TreasurerDashboardViewModel = hiltViewModel(),
    userSessionViewModel: UserSessionViewModel = hiltViewModel(),

) {
    // ─── Collect states ───────────────────────────────────────────────
    val currentUser by userSessionViewModel.currentUser.collectAsState()
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Deposits", "Borrowings", "Repayments", "Investments", "Returns")
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // ─── Safe guard: ensure we only proceed when user is populated and Treasurer ───
    // This prevents brief intermediate states from returning early and blocking the UI.
    val userReady by remember(currentUser.shareholderId, currentUser.role) {
        derivedStateOf {
            val idReady = currentUser.shareholderId.isNotBlank()
            val roleReady = currentUser.role != null
            idReady && roleReady
        }
    }

    if (!userReady) {
        // show initializing UI but do not return "Access Denied" yet
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Initializing session...")
            }
        }
        return
    }

    // After userReady is true, only allow Treasurer role to proceed
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
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "Welcome Treasurer ${currentUser.name}",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(16.dp)
            )

            // Summary
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                1 -> Column {
                    ApprovalSummaryCard(uiState = state) // existing UI expects uiState
                    TreasurerBorrowingListSection(
                        title = "Pending Borrowing Releases",
                        borrowings = state.borrowings,
                        onRelease = { id ->
                            viewModel.releaseBorrowing(id, treasurerId) { success, message ->
                                coroutineScope.launch {
                                    showSnack(snackbarHostState, message)
                                }
                            }
                        },
                        viewModel = viewModel
                    )
                }

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

@Composable
private fun TreasurerBorrowingListSection(
    title: String,
    borrowings: List<com.selfgrowthfund.sgf.data.local.entities.Borrowing>,
    onRelease: (String) -> Unit,
    viewModel: TreasurerDashboardViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val approvalCounts = remember { mutableStateMapOf<String, Int>() }
    val totalMembers = remember { mutableIntStateOf(0) }

    // Load total active members once
    LaunchedEffect(Unit) {
        totalMembers.intValue = viewModel.getActiveMemberCount()
    }

    Column(Modifier.padding(16.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        if (borrowings.isEmpty()) {
            Text("No borrowings pending", color = MaterialTheme.colorScheme.outline)
        } else {
            LazyColumn {
                items(borrowings) { borrowing ->
                    val provisionalId = borrowing.provisionalId
                    val approvalCount = approvalCounts[provisionalId] ?: 0
                    val total = totalMembers.intValue
                    val quorum = kotlin.math.ceil(total * (2.0 / 3.0)).toInt()
                    val quorumMet = approvalCount >= quorum

                    // Load approval count per borrowing
                    LaunchedEffect(provisionalId) {
                        coroutineScope.launch {
                            val count = viewModel.getApprovalCount(provisionalId)
                            approvalCounts[provisionalId] = count
                        }
                    }

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
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "${borrowing.shareholderName} - ₹${borrowing.amountRequested}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text("ID: $provisionalId", style = MaterialTheme.typography.bodySmall)

                                // ✅ Approval progress display
                                if (total > 0) {
                                    Text(
                                        "$approvalCount / $total approvals • ${if (quorumMet) "Quorum Met ✅" else "Awaiting"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (quorumMet)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.outline
                                    )
                                    LinearProgressIndicator(
                                        progress = approvalCount.toFloat() / total,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp)
                                    )
                                }
                            }

                            Button(
                                onClick = { onRelease(provisionalId) },
                                enabled = quorumMet
                            ) {
                                Text(if (quorumMet) "Release" else "Wait")
                            }
                        }
                    }
                }
            }
        }
    }
}

// Safe snackbar launcher
private fun showSnack(
    snackbarHostState: SnackbarHostState,
    message: String,
    actionLabel: String? = null,
    duration: SnackbarDuration = SnackbarDuration.Long
) {
    CoroutineScope(Dispatchers.Main).launch {
        snackbarHostState.showSnackbar(
            message = message,
            actionLabel = actionLabel,
            withDismissAction = true,
            duration = duration
        )
    }
}

@Composable
fun ApprovalSummaryCard(uiState: TreasurerDashboardUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Borrowing Approval Summary",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(8.dp))
            Text("Active Members: ${uiState.totalActiveMembers}")
            Text("Quorum Required (2/3): ${uiState.quorumRequired}")
            Text("Pending Borrowings: ${uiState.borrowings.size}")

            Spacer(Modifier.height(12.dp))

            val total = uiState.totalActiveMembers
            val quorum = uiState.quorumRequired
            val approvalsMet = uiState.approvalProgressMap.values.sum()
            val progress = if (quorum > 0) approvalsMet.toFloat() / quorum else 0f
            val quorumMet = approvalsMet >= quorum && quorum > 0

            LinearProgressIndicator(
                progress = progress.coerceIn(0f, 1f),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(4.dp))
            Text(
                text = if (quorumMet) "Quorum Met ✅" else "Awaiting Approvals ⏳",
                color = if (quorumMet) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
