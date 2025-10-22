package com.selfgrowthfund.sgf.ui.investments

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.ui.navigation.Screen

@Composable
fun InvestmentRoute(
    rootNavController: NavController,
    currentUserRole: MemberRole,
    onDrawerClick: () -> Unit
) {
    val investmentViewModel: InvestmentViewModel = hiltViewModel()
    val investments = investmentViewModel.investments.collectAsState(initial = emptyList())

    InvestmentListScreen(
        investments = investments.value,
        onSelectInvestment = { selected ->
            rootNavController.navigate("investment_detail/${selected.investmentId}")
        },
        onApplyInvestment = {
            rootNavController.navigate(Screen.AddInvestment.route)
        }
    )
}
