package com.selfgrowthfund.sgf.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.selfgrowthfund.sgf.data.local.entities.Investment
import com.selfgrowthfund.sgf.ui.deposits.DepositHistoryScreen
import com.selfgrowthfund.sgf.ui.deposits.DepositSummaryScreen
import com.selfgrowthfund.sgf.ui.investmentreturns.InvestmentReturnsEntryScreen
import com.selfgrowthfund.sgf.ui.investmentreturns.InvestmentReturnsViewModel
import com.selfgrowthfund.sgf.ui.theme.SGFTheme
import dagger.hilt.android.AndroidEntryPoint
import org.threeten.bp.LocalDate

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SGFApp()
        }
    }
}

@Composable
fun SGFApp() {
    SGFTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            val navController = rememberNavController()
            SGFNavHost(navController)
        }
    }
}

@Composable
fun SGFNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "depositSummary") {
        composable("depositSummary") {
            DepositSummaryScreen()
        }
        composable("depositHistory") {
            DepositHistoryScreen()
        }
        composable("investmentReturnEntry") {
            // Replace with actual investment and viewModel injection
            val dummyInvestment = Investment(
                investmentId = "INV001",
                investeeType = "Individual",
                investeeName = "John Doe",
                ownershipType = "Sole",
                partnerNames = null,
                investmentDate = LocalDate.of(2025, 8, 1),
                investmentType = "Bond",
                investmentName = "SGF Bonds",
                amount = 10000.0,
                expectedProfitPercent = 12.5,
                expectedProfitAmount = 1250.0,
                expectedReturnPeriod = 90,
                returnDueDate = LocalDate.of(2025, 10, 30),
                modeOfPayment = "Bank Transfer",
                status = "Active",
                remarks = "Test investment"
            )
            val viewModel = hiltViewModel<InvestmentReturnsViewModel>()
            InvestmentReturnsEntryScreen(
                investment = dummyInvestment,
                viewModel = viewModel,
                onReturnAdded = { navController.popBackStack() }
            )
        }
    }
}