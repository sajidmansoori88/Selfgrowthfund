package com.selfgrowthfund.sgf.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.session.UserSessionViewModel
import com.selfgrowthfund.sgf.model.ShareholderSummaryViewModel
import com.selfgrowthfund.sgf.ui.actions.ActionScreenViewModel

@Composable
fun DrawerContent(
    navController: NavHostController,
    onItemClick: () -> Unit
) {
    val userSessionViewModel: UserSessionViewModel = hiltViewModel()
    val user by userSessionViewModel.currentUser.collectAsState()

    val summaryViewModel: ShareholderSummaryViewModel = hiltViewModel()
    val summary by summaryViewModel.summary.collectAsState()

    val actionViewModel: ActionScreenViewModel = hiltViewModel()
    val pendingCount by actionViewModel
        .getPendingCountForUser(user.shareholderId)
        .collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Self Growth Fund", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Welcome, ${user.name}", style = MaterialTheme.typography.bodyLarge)
        Text("Role: ${user.role.label}", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()

        DrawerItem("Home") {
            navController.navigate(Screen.Home.route)
            onItemClick()
        }

        DrawerItem("Profile") {
            navController.navigate(Screen.Profile.createRoute(user.shareholderId))
            onItemClick()
        }


        DrawerItem("Deposits") {
            navController.navigate(Screen.Deposits.route)
            onItemClick()
        }

        DrawerItem("Borrowings") {
            navController.navigate(Screen.Borrowings.route)
            onItemClick()
        }

        DrawerItem("Investments") {
            navController.navigate(Screen.Investments.route)
            onItemClick()
        }

        DrawerItem("Reports Dashboard") {
            navController.navigate(Screen.ReportsDashboard.route)
            onItemClick()
        }

        DrawerItem("Reports") {
            navController.navigate(Screen.Reports.route)
            onItemClick()
        }

        DrawerItem(label = "Actions", badgeCount = pendingCount) {
            navController.navigate(Screen.Actions.route)
            onItemClick()
        }

        if (user.role == MemberRole.MEMBER_ADMIN) {
            DrawerItem("Admin Dashboard") {
                navController.navigate(Screen.AdminDashboard.route)
                onItemClick()
            }

            DrawerItem("Transaction") {
                navController.navigate(Screen.TransactionManager.route)
                onItemClick()
            }
        }

        if (user.role == MemberRole.MEMBER_TREASURER) {
            DrawerItem("Record Expense") { navController.navigate(Screen.AddExpense.route) }
            DrawerItem("Record Income") { navController.navigate(Screen.AddIncome.route) }
        }

        if (user.role == MemberRole.MEMBER_TREASURER) {
            DrawerItem("Record Penalty") {
                navController.navigate(Screen.AddPenalty.route)
                onItemClick()
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()

        Text("Your Summary", style = MaterialTheme.typography.titleSmall)
        Text("Contribution: ₹${summary.totalShareContribution}")
        Text("Value: ₹${summary.currentValue}")
        Text("Growth: ${"%.2f".format(summary.growthPercent)}%")

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()

        DrawerItem("Logout") {
            userSessionViewModel.clearSession()
            navController.navigate(Screen.Login.route)
            onItemClick()
        }

    }
}