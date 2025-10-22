package com.selfgrowthfund.sgf.ui.borrowing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.selfgrowthfund.sgf.data.local.dto.MemberBorrowingStatus
import com.selfgrowthfund.sgf.ui.components.MemberBorrowingStatusCard
import com.selfgrowthfund.sgf.ui.theme.GradientBackground
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberBorrowingStatusScreen(
    navController: NavHostController,
    viewModel: BorrowingViewModel = hiltViewModel()
) {
    val statuses by viewModel.memberStatuses.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Load statuses when screen opens
    LaunchedEffect(Unit) {
        viewModel.loadMemberBorrowingStatuses()
    }

    GradientBackground {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (statuses.isEmpty()) {
                    Text(
                        text = "No active borrowings found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.align(Alignment.Center)
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
                                text = "Member Borrowing Status",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            )
                        }
                        items(statuses) { status ->
                            MemberBorrowingStatusCard(status)
                        }
                    }
                }
            }
        }
    }
}

/* --------------------  CARD COMPONENT -------------------- */

@Composable
fun MemberBorrowingStatusCard(status: MemberBorrowingStatus) {
    val isOverdue = status.nextDueDate?.let {
        runCatching { LocalDate.parse(it).isBefore(LocalDate.now()) }.getOrDefault(false)
    } ?: false

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = status.shareholderName,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("Total Borrowed: ₹${status.totalBorrowed}")
            Text("Total Repaid: ₹${status.totalRepaid}")
            Text("Outstanding: ₹${status.outstanding}")
            if (isOverdue) {
                Text(
                    "⚠️ Overdue",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            } else {
                Text(
                    "Next Due: ${status.nextDueDate ?: "—"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
