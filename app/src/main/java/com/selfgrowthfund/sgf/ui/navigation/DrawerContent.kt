package com.selfgrowthfund.sgf.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.selfgrowthfund.sgf.session.UserSessionViewModel
import com.selfgrowthfund.sgf.model.reports.ShareholderSummaryViewModel
import com.selfgrowthfund.sgf.ui.actions.ActionScreenViewModel

@Composable
fun DrawerContent(
    navController: NavHostController,
    onItemClick: () -> Unit
) {
    val userSessionViewModel: UserSessionViewModel = hiltViewModel()
    val user by userSessionViewModel.currentUser.collectAsState()

    val summaryViewModel: ShareholderSummaryViewModel = hiltViewModel()
    val summaries by summaryViewModel.summaries.collectAsState()
    val userSummary = summaries.find { it.shareholderId == user.shareholderId }

    val actionViewModel: ActionScreenViewModel = hiltViewModel()
    val pendingCount by actionViewModel
        .getPendingCountForUser(user.shareholderId)
        .collectAsState()

    val textColor = MaterialTheme.colorScheme.onPrimaryContainer

    Column(modifier = Modifier.padding(16.dp)) {
        // Header
        Text("Self Growth Fund", style = MaterialTheme.typography.titleLarge, color = textColor)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Welcome, ${user.name}", style = MaterialTheme.typography.bodyLarge, color = textColor)
        Text("Role: ${user.role.label}", style = MaterialTheme.typography.bodyMedium, color = textColor)

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()

        // ✅ Centralized role-based drawer items
        val drawerItems = remember(user) {
            getDrawerItems(user.role, user.shareholderId)
        }

        drawerItems.forEach { item ->
            DrawerItem(
                item = item,
                textColor = textColor,
                badgeCount = if (item.route == Screen.Actions.route) pendingCount else null,
                onClick = {
                    navController.navigate(item.route) {
                        launchSingleTop = true
                    }
                    onItemClick()
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()

        // ✅ Summary
        Text("Your Summary", style = MaterialTheme.typography.titleSmall, color = textColor)
        Text("Contribution: ₹${userSummary?.shareAmount ?: "-"}", color = textColor)
        Text("Value: ₹${userSummary?.shareValue ?: "-"}", color = textColor)
        Text("Growth: ${"%.2f".format(userSummary?.absoluteReturn ?: 0.0)}%", color = textColor)

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()

        // ✅ Logout
        DrawerItem(
            item = DrawerItemData("Logout", Screen.Welcome.route, null),
            textColor = textColor,
            onClick = {
                userSessionViewModel.clearSession()
                navController.navigate(Screen.Welcome.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
                onItemClick()
            }
        )

    }
}

@Preview(showBackground = true, widthDp = 300, heightDp = 600)
@Composable
fun PreviewDrawerContent() {
    val navController = rememberNavController()

    // Fake user (normally comes from UserSessionViewModel)
    val fakeUser = com.selfgrowthfund.sgf.model.User(
        id = "U001",
        name = "Sajid",
        role = com.selfgrowthfund.sgf.model.enums.MemberRole.MEMBER_TREASURER,
        shareholderId = "SH001"
    )

    // Fake summary with all required fields
    val fakeSummary = com.selfgrowthfund.sgf.model.reports.ShareholderSummary(
        shareholderId = "SH001",
        name = "Sajid",
        shareAmount = 5000.0,
        shareValue = 5500.0,
        absoluteReturn = 10.0,
        annualizedReturn = 12.5,
        percentContribution = 15.0,
        shares = 50,
        lastContributionAmount = 1000.0,
        lastContributionDate = java.time.LocalDate.now().minusMonths(1),
        nextDue = java.time.LocalDate.now().plusMonths(1),
        outstandingBorrowing = 2000.0,
        netProfit = 750.0
    )

    // Fake pending count
    val pendingCount = 3

    val textColor = MaterialTheme.colorScheme.onPrimaryContainer
    val drawerItems = getDrawerItems(fakeUser.role, fakeUser.shareholderId)

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Self Growth Fund", style = MaterialTheme.typography.titleLarge, color = textColor)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Welcome, ${fakeUser.name}", style = MaterialTheme.typography.bodyLarge, color = textColor)
        Text("Role: ${fakeUser.role.label}", style = MaterialTheme.typography.bodyMedium, color = textColor)

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()

        drawerItems.forEach { item ->
            DrawerItem(
                item = item,
                textColor = textColor,
                badgeCount = if (item.route == Screen.Actions.route) pendingCount else null,
                onClick = { /* no-op for preview */ }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()

        Text("Your Summary", style = MaterialTheme.typography.titleSmall, color = textColor)
        Text("Contribution: ₹${fakeSummary.shareAmount}", color = textColor)
        Text("Value: ₹${fakeSummary.shareValue}", color = textColor)
        Text("Growth: ${"%.2f".format(fakeSummary.absoluteReturn)}%", color = textColor)

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()

        DrawerItem(
            item = DrawerItemData("Logout", Screen.Welcome.route, null),
            textColor = textColor,
            onClick = { /* no-op for preview */ }
        )
    }
}

