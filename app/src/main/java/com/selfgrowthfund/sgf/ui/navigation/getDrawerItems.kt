package com.selfgrowthfund.sgf.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import com.selfgrowthfund.sgf.model.enums.MemberRole

fun getDrawerItems(role: MemberRole): List<DrawerItemData> {
    return when (role) {
        MemberRole.MEMBER -> listOf(
            DrawerItemData("Home", "home", Icons.Filled.Home),
            DrawerItemData("Deposits", "deposits", Icons.Filled.AccountBalance),
            DrawerItemData("Borrowings", "borrowings", Icons.Filled.Money),
            DrawerItemData("Investments", "investments", Icons.Filled.TrendingUp),
            DrawerItemData("Reports", "reports", Icons.Filled.Assessment),
            DrawerItemData("Actions", "actions", Icons.Filled.Build),
            DrawerItemData("Profile", "profile", Icons.Filled.Person)
        )
        MemberRole.MEMBER_ADMIN -> listOf(
            DrawerItemData("Home", "home", Icons.Filled.Home),
            DrawerItemData("Deposits", "deposits", Icons.Filled.AccountBalance),
            DrawerItemData("Borrowings", "borrowings", Icons.Filled.Money),
            DrawerItemData("Investments", "investments", Icons.Filled.TrendingUp),
            DrawerItemData("Reports", "reports", Icons.Filled.Assessment),
            DrawerItemData("Actions", "actions", Icons.Filled.Build),
            DrawerItemData("Admin Dashboard", "admin_dashboard", Icons.Filled.AdminPanelSettings),
            DrawerItemData("Profile", "profile", Icons.Filled.Person)
        )
        MemberRole.MEMBER_TREASURER -> listOf(
            DrawerItemData("Home", "home", Icons.Filled.Home),
            DrawerItemData("Deposits", "deposits", Icons.Filled.AccountBalance),
            DrawerItemData("Borrowings", "borrowings", Icons.Filled.Money),
            DrawerItemData("Investments", "investments", Icons.Filled.TrendingUp),
            DrawerItemData("Reports", "reports", Icons.Filled.Assessment),
            DrawerItemData("Actions", "actions", Icons.Filled.Build),
            DrawerItemData("Profile", "profile", Icons.Filled.Person)
        )
    }
}