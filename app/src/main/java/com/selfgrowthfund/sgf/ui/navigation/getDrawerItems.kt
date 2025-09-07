package com.selfgrowthfund.sgf.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.selfgrowthfund.sgf.model.enums.MemberRole

fun getDrawerItems(role: MemberRole, shareholderId: String): List<DrawerItemData> {
    return when (role) {
        MemberRole.MEMBER -> listOf(
            DrawerItemData("Home", Screen.Home.route, Icons.Filled.Home),
            DrawerItemData("Deposits", Screen.Deposits.route, Icons.Filled.AccountBalance),
            DrawerItemData("Borrowings", Screen.Borrowings.route, Icons.Filled.Money),
            DrawerItemData("Investments", Screen.Investments.route, Icons.Filled.TrendingUp),
            DrawerItemData("Reports", Screen.ReportsDashboard.route, Icons.Filled.Assessment),
            DrawerItemData("Actions", Screen.Actions.route, Icons.Filled.Build),
            DrawerItemData("Profile", Screen.Profile.createRoute(shareholderId), Icons.Filled.Person)
        )
        MemberRole.MEMBER_ADMIN -> listOf(
            DrawerItemData("Home", Screen.Home.route, Icons.Filled.Home),
            DrawerItemData("Deposits", Screen.Deposits.route, Icons.Filled.AccountBalance),
            DrawerItemData("Borrowings", Screen.Borrowings.route, Icons.Filled.Money),
            DrawerItemData("Investments", Screen.Investments.route, Icons.Filled.TrendingUp),
            DrawerItemData("Reports", Screen.ReportsDashboard.route, Icons.Filled.Assessment),
            DrawerItemData("Actions", Screen.Actions.route, Icons.Filled.Build),
            DrawerItemData("Admin Dashboard", Screen.AdminDashboard.route, Icons.Filled.AdminPanelSettings),
            DrawerItemData("Profile", Screen.Profile.createRoute(shareholderId), Icons.Filled.Person)
        )
        MemberRole.MEMBER_TREASURER -> listOf(
            DrawerItemData("Home", Screen.Home.route, Icons.Filled.Home),
            DrawerItemData("Deposits", Screen.Deposits.route, Icons.Filled.AccountBalance),
            DrawerItemData("Borrowings", Screen.Borrowings.route, Icons.Filled.Money),
            DrawerItemData("Investments", Screen.Investments.route, Icons.Filled.TrendingUp),
            DrawerItemData("Reports", Screen.ReportsDashboard.route, Icons.Filled.Assessment),
            DrawerItemData("Actions", Screen.Actions.route, Icons.Filled.Build),
            DrawerItemData("Transaction Manager", Screen.AddTransaction.route, Icons.Filled.AttachMoney),
            DrawerItemData("Treasury Dashboard", Screen.TreasurerDashboard.route, Icons.Filled.AccountBalanceWallet),
            DrawerItemData("Profile", Screen.Profile.createRoute(shareholderId), Icons.Filled.Person)
        )
    }
}
