package com.selfgrowthfund.sgf.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.selfgrowthfund.sgf.R
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.model.reports.ShareholderSummary
import com.selfgrowthfund.sgf.model.reports.ShareholderSummaryViewModel
import com.selfgrowthfund.sgf.session.UserSessionViewModel
import com.selfgrowthfund.sgf.ui.components.GradientScaffold
import com.selfgrowthfund.sgf.ui.components.SGFScaffoldWrapper
import com.selfgrowthfund.sgf.ui.navigation.DrawerContent
import com.selfgrowthfund.sgf.ui.theme.SGFTheme
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    drawerState: DrawerState,
    scope: CoroutineScope,
    onDrawerClick: () -> Unit
) {
    val userSessionViewModel: UserSessionViewModel = hiltViewModel()
    val user by userSessionViewModel.currentUser.collectAsState()

    val summaryViewModel: ShareholderSummaryViewModel = hiltViewModel()
    val summaries by summaryViewModel.summaries.collectAsState()

    LaunchedEffect(Unit) {
        summaryViewModel.loadAllSummaries()
    }

    val currentSummary = summaries.find { it.shareholderId == user.shareholderId }

    SGFScaffoldWrapper(
        title = "Self Growth Fund",
        drawerState = drawerState,
        scope = scope,
        drawerContent = {
            DrawerContent(
                navController = navController,
                onItemClick = { scope.launch { drawerState.close() } }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (currentSummary != null) {
                HomeScreenContent(
                    name = user.name,
                    role = user.role.name,
                    summary = currentSummary
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun HomeScreenContent(
    name: String,
    role: String,
    summary: ShareholderSummary
) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd-MM-yyyy") }

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
        Text("₹${summary.shareAmount}", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Current Value", style = MaterialTheme.typography.titleMedium)
        Text("₹${summary.shareValue}", style = MaterialTheme.typography.bodyLarge)
        Text(
            text = "Growth: ${"%.2f".format(summary.absoluteReturn)}%",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Last Contribution", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "₹${summary.lastContributionAmount} (${summary.lastContributionDate.format(formatter)})",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Next Due Contribution", style = MaterialTheme.typography.titleMedium)
        Text(summary.nextDue.format(formatter), style = MaterialTheme.typography.bodyLarge)

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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Home Screen Preview")
@Composable
fun HomeScreenPreview() {
    SGFTheme {
        val fakeSummary = ShareholderSummary(
            shareholderId = "SH001",
            name = "Sajid Mansoori",
            shares = 15,
            shareAmount = 30000.0,
            shareValue = 250000.0,
            percentContribution = 12.5,
            netProfit = 220000.0,
            absoluteReturn = 733.33,
            annualizedReturn = 120.0,
            lastContributionAmount = 6000.0,
            lastContributionDate = LocalDate.of(2025, 8, 10),
            nextDue = LocalDate.of(2025, 9, 10),
            outstandingBorrowing = 7000.0
        )

        GradientScaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Self Growth Fund",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    },
                    navigationIcon = {
                        Image(
                            painter = painterResource(id = R.drawable.sgf_logo),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .size(45.dp) // Adjust size as needed
                                .padding(8.dp),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                )
            },
            content = { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    HomeScreenContent(
                        name = "Sajid",
                        role = MemberRole.MEMBER.name,
                        summary = fakeSummary
                    )
                }
            }
        )
    }
}