// shareholderNavGraph.kt
package com.selfgrowthfund.sgf.ui.addshareholders

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfgrowthfund.sgf.ui.editshareholders.EditShareholderScreen

// shareholderNavGraph.kt
fun NavGraphBuilder.shareholderNavGraph(navController: NavController) {
    // Add Shareholder Screen
    composable("addShareholder") {
        AddShareholderScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToEdit = { shareholderId ->
                navController.navigate("editShareholder/$shareholderId")
            }
        )
    }

    // Edit Shareholder Screen
    composable("editShareholder/{shareholderId}") {
        val shareholderId = it.arguments?.getString("shareholderId") ?: ""
        EditShareholderScreen(
            shareholderId = shareholderId,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // Shareholder List Screen (if you have it)
    composable("shareholderList") {
        ShareholderListScreen(navController = navController)
    }
}
