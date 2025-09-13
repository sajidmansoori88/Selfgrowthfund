package com.selfgrowthfund.sgf.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.model.ApprovalEntry
import com.selfgrowthfund.sgf.ui.components.EmptyStateCard
import com.selfgrowthfund.sgf.ui.components.SwipeableActionCard
import com.selfgrowthfund.sgf.ui.theme.GradientBackground
import kotlinx.coroutines.launch

@Composable
fun AdminApprovalScreen(
    modifier: Modifier = Modifier,
    viewModel: AdminDashboardViewModel,
    onDrawerClick: () -> Unit = {}
) {
    val approvalGroups by viewModel.approvalGroups.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    GradientBackground {
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (approvalGroups.isEmpty()) {
                // 🔹 Empty state when no approvals
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateCard(
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.History,
                        title = "No pending approvals",
                        message = "All requests have been reviewed.",
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    approvalGroups.forEach { group ->
                        item {
                            Text(
                                group.title,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(group.entries) { entry: ApprovalEntry ->
                            SwipeableActionCard(
                                entry = entry,
                                onApprove = {
                                    viewModel.approve(entry)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Approved: ${entry.type}")
                                    }
                                },
                                onReject = {
                                    viewModel.reject(entry)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Rejected: ${entry.type}")
                                    }
                                }
                            ) { approval ->
                                // 🔹 Slot-based content for Admin approvals
                                Text(
                                    "Type: ${approval.type}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "Submitted by: ${approval.submitterName}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "Date: ${approval.date}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(24.dp))
}
