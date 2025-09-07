package com.selfgrowthfund.sgf.ui.actions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.selfgrowthfund.sgf.ui.components.ActionCard
import com.selfgrowthfund.sgf.utils.Result

@Composable
fun ActionScreen(
    viewModel: ActionScreenViewModel,
    currentShareholderId: String,
    modifier: Modifier = Modifier
) {
    val pendingActions by viewModel.pendingActions.collectAsState()
    val responseState by viewModel.responseState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedFilter by remember { mutableStateOf("All") }

    LaunchedEffect(Unit) {
        viewModel.pendingActions.collect {}
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Tab Row with proper spacing
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                modifier = Modifier.padding(vertical = 16.dp) // Added vertical padding
            ) {
                Text(
                    "Pending",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )
            }
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                modifier = Modifier.padding(vertical = 16.dp) // Added vertical padding
            ) {
                Text(
                    "History",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )
            }
        }
        if (selectedTab == 0) {
            // Filter chips for Pending tab
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = selectedFilter == "All",
                    onClick = { selectedFilter = "All" },
                    label = { Text("All") },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                FilterChip(
                    selected = selectedFilter == "Unresponded",
                    onClick = { selectedFilter = "Unresponded" },
                    label = { Text("Unresponded") },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                FilterChip(
                    selected = selectedFilter == "Responded",
                    onClick = { selectedFilter = "Responded" },
                    label = { Text("Responded") },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            val filteredActions = when (selectedFilter) {
                "Unresponded" -> pendingActions.filter { !it.responses.containsKey(currentShareholderId) }
                "Responded" -> pendingActions.filter { it.responses.containsKey(currentShareholderId) }
                else -> pendingActions
            }

            if (filteredActions.isEmpty()) {
                // Centered empty state with proper spacing
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No actions found",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
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
            }
        } else {
            // History tab content with proper spacing
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "History view coming soon",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        // Handle response states
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

        // Bottom spacer for better scrolling
        Spacer(modifier = Modifier.height(32.dp))
    }
}