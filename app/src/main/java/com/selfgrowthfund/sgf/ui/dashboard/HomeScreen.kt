package com.selfgrowthfund.sgf.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.session.UserSessionViewModel
import com.selfgrowthfund.sgf.ui.navigation.DrawerContent
import com.selfgrowthfund.sgf.ui.theme.SGFTheme
import com.selfgrowthfund.sgf.model.ShareholderSummary
import com.selfgrowthfund.sgf.model.ShareholderSummaryViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(navController: NavHostController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                navController = navController,
                onItemClick = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        HomeScreenContentWrapper(navController = navController)
    }
}

@Composable
fun HomeScreenContentWrapper(navController: NavHostController) {
    val userSessionViewModel: UserSessionViewModel = hiltViewModel()
    val user by userSessionViewModel.currentUser.collectAsState()

    val summaryViewModel: ShareholderSummaryViewModel = hiltViewModel()
    val summary by summaryViewModel.summary.collectAsState()

    LaunchedEffect(user.id) {
        summaryViewModel.loadSummary(user)
    }

    HomeScreenContent(
        name = user.name,
        role = user.role.name,
        summary = summary
    )
}

@Composable
fun HomeScreenContent(
    name: String,
    role: String,
    summary: ShareholderSummary
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text("Welcome, $name", style = MaterialTheme.typography.headlineMedium)
        Text("Role: $role", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(32.dp))

        Text("Total Share Contribution", style = MaterialTheme.typography.titleMedium)
        Text("₹${summary.totalShareContribution}", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Current Value", style = MaterialTheme.typography.titleMedium)
        Text("₹${summary.currentValue}", style = MaterialTheme.typography.bodyLarge)
        Text("Growth: ${"%.2f".format(summary.growthPercent)}%", color = MaterialTheme.colorScheme.primary)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Last Contribution", style = MaterialTheme.typography.titleMedium)
        Text("₹6,000.00 (${summary.lastContribution.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))})")

        Spacer(modifier = Modifier.height(16.dp))

        Text("Next Due Contribution", style = MaterialTheme.typography.titleMedium)
        Text(summary.nextDue.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))

        Spacer(modifier = Modifier.height(24.dp))

        if (summary.outstandingBorrowing > 0) {
            Text(
                text = "⚠️ Outstanding Borrowing: ₹${summary.outstandingBorrowing}",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val fakeSummary = ShareholderSummary(
        totalShareContribution = 30000,
        currentValue = 250000.0,
        growthPercent = 733.33,
        lastContribution = LocalDate.of(2025, 8, 15),
        nextDue = LocalDate.of(2025, 9, 10),
        outstandingBorrowing = 7000.0
    )

    SGFTheme {
        HomeScreenContent(
            name = "John",
            role = MemberRole.MEMBER.name,
            summary = fakeSummary
        )
    }
}