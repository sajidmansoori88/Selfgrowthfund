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
        Text("Self Growth Fund", style = MaterialTheme.typography.titleLarge, color = textColor)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Welcome, ${user.name}", style = MaterialTheme.typography.bodyLarge, color = textColor)
        Text("Role: ${user.role.label}", style = MaterialTheme.typography.bodyMedium, color = textColor)

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()

        DrawerItem(
            label = "Home",
            textColor = textColor,
            onClick = {
                navController.navigate(Screen.Home.route)
                onItemClick()
            }
        )

        DrawerItem(
            label = "Profile",
            textColor = textColor,
            onClick = {
                navController.navigate(Screen.Profile.createRoute(user.shareholderId))
                onItemClick()
            }
        )

        DrawerItem(
            label = "Deposits",
            textColor = textColor,
            onClick = {
                navController.navigate(Screen.Deposits.route)
                onItemClick()
            }
        )

        DrawerItem(
            label = "Borrowings",
            textColor = textColor,
            onClick = {
                navController.navigate(Screen.Borrowings.route)
                onItemClick()
            }
        )

        DrawerItem(
            label = "Investments",
            textColor = textColor,
            onClick = {
                navController.navigate(Screen.Investments.route)
                onItemClick()
            }
        )

        DrawerItem(
            label = "Reports Dashboard",
            textColor = textColor,
            onClick = {
                navController.navigate(Screen.ReportsDashboard.route)
                onItemClick()
            }
        )

        DrawerItem(
            label = "Reports",
            textColor = textColor,
            onClick = {
                navController.navigate(Screen.Reports.route)
                onItemClick()
            }
        )

        DrawerItem(
            label = "Actions",
            badgeCount = pendingCount,
            textColor = textColor,
            onClick = {
                navController.navigate(Screen.Actions.route)
                onItemClick()
            }
        )

        if (user.role == MemberRole.MEMBER_ADMIN) {
            DrawerItem(
                label = "Admin Dashboard",
                textColor = textColor,
                onClick = {
                    navController.navigate(Screen.AdminDashboard.route)
                    onItemClick()
                }
            )

            DrawerItem(
                label = "Transaction",
                textColor = textColor,
                onClick = {
                    navController.navigate(Screen.TransactionManager.route)
                    onItemClick()
                }
            )
        }

        if (user.role == MemberRole.MEMBER_TREASURER) {
            DrawerItem(
                label = "Record Expense",
                textColor = textColor,
                onClick = {
                    navController.navigate(Screen.AddExpense.route)
                    onItemClick()
                }
            )

            DrawerItem(
                label = "Record Income",
                textColor = textColor,
                onClick = {
                    navController.navigate(Screen.AddIncome.route)
                    onItemClick()
                }
            )

            DrawerItem(
                label = "Record Penalty",
                textColor = textColor,
                onClick = {
                    navController.navigate(Screen.AddPenalty.route)
                    onItemClick()
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()

        Text("Your Summary", style = MaterialTheme.typography.titleSmall, color = textColor)
        Text("Contribution: ₹${userSummary?.shareAmount ?: "-"}", color = textColor)
        Text("Value: ₹${userSummary?.shareValue ?: "-"}", color = textColor)
        Text("Growth: ${"%.2f".format(userSummary?.absoluteReturn ?: 0.0)}%", color = textColor)

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()

        DrawerItem(
            label = "Logout",
            textColor = textColor,
            onClick = {
                userSessionViewModel.clearSession()
                navController.navigate(Screen.Login.route)
                onItemClick()
            }
        )
    }
}