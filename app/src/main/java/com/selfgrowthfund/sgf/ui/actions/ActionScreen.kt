package com.selfgrowthfund.sgf.ui.actions

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.data.local.entities.ActionItem
import com.selfgrowthfund.sgf.data.local.entities.ApprovalFlow
import com.selfgrowthfund.sgf.model.enums.ApprovalType
import com.selfgrowthfund.sgf.ui.components.*
import com.selfgrowthfund.sgf.ui.theme.GradientBackground
import com.selfgrowthfund.sgf.utils.Result
import kotlinx.coroutines.launch
import kotlin.math.pow

// ---------------- Helper for XIRR ----------------
private fun calculateXirr(amount: Double, expectedReturnAmount: Double, durationDays: Int): Double {
    if (amount <= 0.0 || durationDays <= 0) return 0.0
    val totalValue = amount + expectedReturnAmount
    val ratio = totalValue / amount
    val annualFactor = 365.0 / durationDays.toDouble()
    return (ratio.pow(annualFactor) - 1) * 100.0
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ActionScreen(
    viewModel: ActionScreenViewModel,
    currentShareholderId: String,
    modifier: Modifier = Modifier
) {
    // ---------- State Flows ----------
    val pendingActions by viewModel.pendingActions.collectAsState()
    val pendingApprovals by viewModel.pendingApprovalFlows.collectAsState()
    val allApprovalFlows by viewModel.allApprovalFlows.collectAsState()
    val responseState by viewModel.responseState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedFilter by remember { mutableStateOf("Unresponded") }

    GradientBackground {
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ---------------- TabRow ----------------
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onBackground,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[selectedTab])
                            .height(3.dp),
                        height = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Pending",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedTab == 0)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "History",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedTab == 1)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                )
            }

            // ---------------- Animated Tab Content ----------------
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "TabSwitchAnimation"
            ) { targetTab ->
                when (targetTab) {
                    0 -> {
                        // ---------- Pending Tab ----------
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FilterChip(
                                selected = selectedFilter == "Unresponded",
                                onClick = { selectedFilter = "Unresponded" },
                                label = { Text("Unresponded") },
                                leadingIcon = if (selectedFilter == "Unresponded") {
                                    { Icon(Icons.Default.Check, contentDescription = null) }
                                } else null
                            )
                            FilterChip(
                                selected = selectedFilter == "Responded",
                                onClick = { selectedFilter = "Responded" },
                                label = { Text("Responded") },
                                leadingIcon = if (selectedFilter == "Responded") {
                                    { Icon(Icons.Default.Done, contentDescription = null) }
                                } else null
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // ---------------- Combined Filter Logic ----------------
                        val filteredPair: Pair<List<ActionItem>, List<ApprovalFlow>> =
                            when (selectedFilter) {
                                "Responded" -> {
                                    val respondedActions = pendingActions.filter {
                                        it.responses.containsKey(currentShareholderId)
                                    }
                                    val respondedApprovals =
                                        allApprovalFlows.filter { it.approvedAt != null }
                                    respondedActions to respondedApprovals
                                }

                                else -> {
                                    val unrespondedActions = pendingActions.filter {
                                        !it.responses.containsKey(currentShareholderId)
                                    }
                                    val unrespondedApprovals = pendingApprovals
                                    unrespondedActions to unrespondedApprovals
                                }
                            }

                        val filteredActions = filteredPair.first
                        val filteredApprovals = filteredPair.second

                        // ---------------- Empty State ----------------
                        if (filteredActions.isEmpty() && filteredApprovals.isEmpty()) {
                            EmptyStateCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 32.dp),
                                icon = Icons.Default.Inbox,
                                title = "No actions or approvals found",
                                message = "You're all caught up for now 🎉"
                            )
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                // ---------- Existing Action Items ----------
                                if (filteredActions.isNotEmpty()) {
                                    Text(
                                        "Pending Actions",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )

                                    filteredActions.forEach { action: ActionItem ->
                                        SwipeableActionCard(
                                            entry = action,
                                            onApprove = {
                                                viewModel.approve(action, currentShareholderId)
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("Approved: ${action.title}")
                                                }
                                            },
                                            onReject = {
                                                viewModel.reject(action, currentShareholderId)
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("Rejected: ${action.title}")
                                                }
                                            }
                                        ) { a ->
                                            Text(
                                                a.title,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                a.description,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }

                                // ---------- Pending / Responded Approval Flows ----------
                                if (filteredApprovals.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(30.dp))

                                    filteredApprovals.forEach { approval: ApprovalFlow ->
                                        when (approval.entityType) {
                                            ApprovalType.INVESTMENT -> {
                                                val investment by viewModel
                                                    .getInvestmentById(approval.entityId)
                                                    .collectAsState(initial = null)

                                                val amount = investment?.amount ?: 0.0
                                                val expectedPercent =
                                                    investment?.expectedProfitPercent ?: 0.0
                                                val expectedReturnAmount =
                                                    (amount * expectedPercent) / 100.0
                                                val xirr = calculateXirr(
                                                    amount,
                                                    expectedReturnAmount,
                                                    investment?.expectedReturnPeriod ?: 0
                                                )

                                                val isRespondedView = selectedFilter == "Responded"

                                                InvestmentApprovalCard(
                                                    investeeName = investment?.investeeName
                                                        ?: "Loading...",
                                                    dateOfApplication = investment?.createdAt.toString(),
                                                    amount = amount,
                                                    expectedReturnAmount = expectedReturnAmount,
                                                    expectedReturnPercent = expectedPercent,
                                                    expectedReturnDate = investment?.returnDueDate.toString(),
                                                    returnDays = investment?.expectedReturnPeriod
                                                        ?: 0,
                                                    xirr = xirr,
                                                    statusLabel = when {
                                                        approval.approvedAt == null -> "Pending"
                                                        approval.action.name == "APPROVE" -> "Approved"
                                                        approval.action.name == "REJECT" -> "Rejected"
                                                        else -> "Pending"
                                                    },
                                                    onReject = if (isRespondedView) null else {
                                                        {
                                                            viewModel.rejectApproval(approval)
                                                            coroutineScope.launch {
                                                                snackbarHostState.showSnackbar("Rejected Investment")
                                                            }
                                                        }
                                                    }
                                                )

                                            }

                                            ApprovalType.BORROWING -> {
                                                val borrowing by viewModel
                                                    .getBorrowingById(approval.entityId)
                                                    .collectAsState(initial = null)

                                                val isRespondedView = selectedFilter == "Responded"

                                                BorrowingApprovalCard(
                                                    borrowerName = borrowing?.shareholderName
                                                        ?: "Loading...",
                                                    dateOfApplication = borrowing?.applicationDate.toString(),
                                                    borrowEligibility = borrowing?.borrowEligibility
                                                        ?: 0.0,
                                                    amountRequested = borrowing?.amountRequested
                                                        ?: 0.0,
                                                    statusLabel = when {
                                                        approval.approvedAt == null -> "Pending"
                                                        approval.action.name == "APPROVE" -> "Approved"
                                                        approval.action.name == "REJECT" -> "Rejected"
                                                        else -> "Pending"
                                                    },
                                                    onReject = if (isRespondedView) null else {
                                                        {
                                                            viewModel.rejectApproval(approval)
                                                            coroutineScope.launch {
                                                                snackbarHostState.showSnackbar("Rejected Borrowing")
                                                            }
                                                        }
                                                    }
                                                )
                                            }

                                            else -> {
                                                Text("Unhandled approval type: ${approval.entityType}")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // ---------- History Tab ----------
                        val completedApprovals by viewModel.completedApprovals.collectAsState()

                        if (completedApprovals.isEmpty()) {
                            EmptyStateCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 32.dp),
                                icon = Icons.Default.History,
                                title = "No completed approvals yet",
                                message = "Once an application is fully approved or rejected, it will appear here."
                            )
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                completedApprovals.forEach { approval ->
                                    when (approval.entityType) {
                                        ApprovalType.INVESTMENT -> {
                                            val investment by viewModel
                                                .getInvestmentById(approval.entityId)
                                                .collectAsState(initial = null)

                                            val amount = investment?.amount ?: 0.0
                                            val expectedPercent =
                                                investment?.expectedProfitPercent ?: 0.0
                                            val expectedReturnAmount =
                                                (amount * expectedPercent) / 100.0
                                            val xirr = calculateXirr(
                                                amount,
                                                expectedReturnAmount,
                                                investment?.expectedReturnPeriod ?: 0
                                            )

                                            InvestmentApprovalCard(
                                                investeeName = investment?.investeeName
                                                    ?: "Loading...",
                                                dateOfApplication = investment?.createdAt.toString(),
                                                amount = amount,
                                                expectedReturnAmount = expectedReturnAmount,
                                                expectedReturnPercent = expectedPercent,
                                                expectedReturnDate = investment?.returnDueDate.toString(),
                                                returnDays = investment?.expectedReturnPeriod ?: 0,
                                                xirr = xirr,
                                                statusLabel = when (approval.action.name) {
                                                    "APPROVE" -> "Approved"
                                                    "REJECT" -> "Rejected"
                                                    else -> "Pending"
                                                },
                                                onApprove = null,
                                                onReject = null
                                            )
                                        }

                                        ApprovalType.BORROWING -> {
                                            val borrowing by viewModel
                                                .getBorrowingById(approval.entityId)
                                                .collectAsState(initial = null)

                                            BorrowingApprovalCard(
                                                borrowerName = borrowing?.shareholderName
                                                    ?: "Loading...",
                                                dateOfApplication = borrowing?.applicationDate.toString(),
                                                borrowEligibility = borrowing?.borrowEligibility
                                                    ?: 0.0,
                                                amountRequested = borrowing?.amountRequested ?: 0.0,
                                                statusLabel = when (approval.action.name) {
                                                    "APPROVE" -> "Approved"
                                                    "REJECT" -> "Rejected"
                                                    else -> "Pending"
                                                },
                                                onApprove = null,
                                                onReject = null
                                            )
                                        }

                                        // ✅ Handle all other types gracefully (won’t show, but keeps compiler happy)
                                        else -> { /* No-op: other approval types not shown on ActionScreen */
                                        }
                                    }
                                }
                            }
                        }


                        // ---------------- Response State Handling ----------------
                        when (responseState) {
                            is Result.Success -> {
                                LaunchedEffect(responseState) {
                                    viewModel.clearState()
                                }
                            }

                            is Result.Error -> {
                                Text(
                                    text = "Error: ${(responseState as Result.Error).exception.message}",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            else -> {}
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}
