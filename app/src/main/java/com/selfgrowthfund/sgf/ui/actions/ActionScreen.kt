package com.selfgrowthfund.sgf.ui.actions

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.selfgrowthfund.sgf.data.local.entities.ActionItem
import com.selfgrowthfund.sgf.model.enums.ActionResponse
import com.selfgrowthfund.sgf.ui.components.ActionCard
import com.selfgrowthfund.sgf.utils.Result

@Composable
fun ActionScreen(
    viewModel: ActionScreenViewModel,
    currentShareholderId: String
) {
    val pendingActions by viewModel.pendingActions.collectAsState()
    val responseState by viewModel.responseState.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var selectedFilter by remember { mutableStateOf("All") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("Pending")
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("History")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = selectedFilter == "All",
                    onClick = { selectedFilter = "All" },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = selectedFilter == "Unresponded",
                    onClick = { selectedFilter = "Unresponded" },
                    label = { Text("Unresponded") }
                )
                FilterChip(
                    selected = selectedFilter == "Responded",
                    onClick = { selectedFilter = "Responded" },
                    label = { Text("Responded") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val filteredActions = when (selectedFilter) {
                "Unresponded" -> pendingActions.filter { !it.responses.containsKey(currentShareholderId) }
                "Responded" -> pendingActions.filter { it.responses.containsKey(currentShareholderId) }
                else -> pendingActions
            }

            if (filteredActions.isEmpty()) {
                Text("No actions found", style = MaterialTheme.typography.bodyMedium)
            } else {
                filteredActions.forEach { action ->
                    ActionCard(
                        action = action,
                        currentShareholderId = currentShareholderId,
                        onRespond = { response ->
                            viewModel.submitResponse(action.actionId, currentShareholderId, response)
                        }
                    )
                }
            }
        } else {
            Text("History view coming soon", style = MaterialTheme.typography.bodyMedium)
        }

        when (responseState) {
            is Result.Success -> {
                LaunchedEffect(responseState) {
                    viewModel.clearState()
                    // TODO: Show snackbar or toast
                }
            }
            is Result.Error -> {
                Text(
                    text = "Error: ${(responseState as Result.Error).exception.message}",
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {}
        }
    }
}