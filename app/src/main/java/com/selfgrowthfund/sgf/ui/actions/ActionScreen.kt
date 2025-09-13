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
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.data.local.entities.ActionItem
import com.selfgrowthfund.sgf.ui.components.EmptyStateCard
import com.selfgrowthfund.sgf.ui.components.SwipeableActionCard
import com.selfgrowthfund.sgf.ui.theme.GradientBackground
import com.selfgrowthfund.sgf.utils.Result
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ActionScreen(
    viewModel: ActionScreenViewModel,
    currentShareholderId: String,
    modifier: Modifier = Modifier
) {
    val pendingActions by viewModel.pendingActions.collectAsState()
    val responseState by viewModel.responseState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedFilter by remember { mutableStateOf("All") }

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
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
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
                                selected = selectedFilter == "All",
                                onClick = { selectedFilter = "All" },
                                label = { Text("All") },
                                leadingIcon = if (selectedFilter == "All") {
                                    { Icon(Icons.Default.Check, contentDescription = null) }
                                } else null
                            )
                            FilterChip(
                                selected = selectedFilter == "Unresponded",
                                onClick = { selectedFilter = "Unresponded" },
                                label = { Text("Unresponded") },
                                leadingIcon = if (selectedFilter == "Unresponded") {
                                    { Icon(Icons.Default.PendingActions, contentDescription = null) }
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

                        val filteredActions = when (selectedFilter) {
                            "Unresponded" -> pendingActions.filter {
                                !it.responses.containsKey(currentShareholderId)
                            }
                            "Responded" -> pendingActions.filter {
                                it.responses.containsKey(currentShareholderId)
                            }
                            else -> pendingActions
                        }

                        if (filteredActions.isEmpty()) {
                            // Empty State (Pending)
                            EmptyStateCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 32.dp),
                                icon = Icons.Default.Inbox,
                                title = "No actions found",
                                message = "You're all caught up for now 🎉"
                            )
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
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
                        }
                    }
                    1 -> {
                        // ---------- History Tab ----------
                        EmptyStateCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                            icon = Icons.Default.History,
                            title = "History view coming soon",
                            message = "Past actions will appear here once implemented.",
                        )
                    }
                }
            }

            // ---------------- Handle response states ----------------
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
