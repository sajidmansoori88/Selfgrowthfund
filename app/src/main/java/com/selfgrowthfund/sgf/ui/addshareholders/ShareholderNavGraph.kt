package com.selfgrowthfund.sgf.ui.addshareholders

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.selfgrowthfund.sgf.ui.addshareholders.AddShareholderScreen

@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.shareholderNavGraph(navController: NavController) {
    composable("addShareholder") { navBackStackEntry ->
        val viewModel = hiltViewModel<AddShareholderViewModel>()
        AddShareholderScreen(viewModel = viewModel, navController = navController)
    }

    // Placeholder until ShareholderListScreen is created
    composable("shareholderList") {
        androidx.compose.material3.Text("Shareholder list coming soon")
    }
}