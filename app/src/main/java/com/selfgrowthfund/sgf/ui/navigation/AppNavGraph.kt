package com.selfgrowthfund.sgf.ui.navigation

import android.content.Context
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.selfgrowthfund.sgf.data.local.entities.Investment
import com.selfgrowthfund.sgf.data.local.entities.Repayment
import com.selfgrowthfund.sgf.session.UserSessionViewModel
import com.selfgrowthfund.sgf.ui.WelcomeScreen
import com.selfgrowthfund.sgf.ui.auth.*
import com.selfgrowthfund.sgf.ui.dashboard.*
import com.selfgrowthfund.sgf.ui.deposits.*
import com.selfgrowthfund.sgf.ui.deposits.AddDepositScreen
import com.selfgrowthfund.sgf.di.DepositViewModelFactoryEntryPoint
import com.selfgrowthfund.sgf.model.enums.InvesteeType
import com.selfgrowthfund.sgf.model.enums.InvestmentStatus
import com.selfgrowthfund.sgf.model.enums.InvestmentType
import com.selfgrowthfund.sgf.model.enums.OwnershipType
import com.selfgrowthfund.sgf.model.enums.PaymentMode
import com.selfgrowthfund.sgf.ui.ProfileScreen
import com.selfgrowthfund.sgf.ui.actions.ActionScreen
import com.selfgrowthfund.sgf.ui.actions.ActionScreenViewModel
import com.selfgrowthfund.sgf.ui.borrowing.*
import com.selfgrowthfund.sgf.ui.auth.PinEntryScreen
import com.selfgrowthfund.sgf.ui.auth.BiometricSetupScreen
import com.selfgrowthfund.sgf.ui.dashboard.TreasurerDashboardScreen
import com.selfgrowthfund.sgf.ui.penalty.PenaltyReportScreen
import com.selfgrowthfund.sgf.ui.investmentreturns.InvestmentReturnsEntryScreen
import com.selfgrowthfund.sgf.ui.investmentreturns.InvestmentReturnsViewModel
import com.selfgrowthfund.sgf.ui.investments.AddInvestmentScreen
import com.selfgrowthfund.sgf.ui.investments.InvestmentViewModel
import com.selfgrowthfund.sgf.ui.investments.safeValueOf
import com.selfgrowthfund.sgf.ui.penalty.AddPenaltyScreen
import com.selfgrowthfund.sgf.ui.repayments.AddRepaymentScreen
import com.selfgrowthfund.sgf.ui.repayments.RepaymentViewModel
import com.selfgrowthfund.sgf.ui.reports.CashFlowReportScreen
import com.selfgrowthfund.sgf.ui.reports.ReportsDashboardScreen
import com.selfgrowthfund.sgf.ui.transactions.AddExpenseScreen
import com.selfgrowthfund.sgf.ui.transactions.AddIncomeScreen
import com.selfgrowthfund.sgf.ui.transactions.TransactionForm
import com.selfgrowthfund.sgf.ui.transactions.TransactionViewModel
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import java.time.LocalDate

@Composable
fun AppNavGraph(
    navController: NavHostController,
    drawerState: DrawerState,
    scope: CoroutineScope,
    onDrawerClick: () -> Unit
)

 {
    val userSessionViewModel: UserSessionViewModel = hiltViewModel()
    val user = userSessionViewModel.currentUser.collectAsState().value

    NavHost(navController = navController, startDestination = Screen.Welcome.route) {

        // 🔐 Auth & Onboarding (no drawer)
        composable(Screen.Welcome.route) { WelcomeScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.CreatePin.route) { CreatePinScreen(navController) }
        composable(Screen.PinEntry.route) { PinEntryScreen(navController) }
        composable(Screen.BiometricSetup.route) { BiometricSetupScreen(navController) }
        composable(Screen.AccessDenied.route) { AccessDeniedScreen(navController) }

        // 🏠 Dashboards (drawer-enabled)
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                onDrawerClick = onDrawerClick,
                drawerState = drawerState,
                scope = scope
            )
        }

        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(navController = navController, role = user.role)
        }
        composable(Screen.TreasurerDashboard.route) {
            TreasurerDashboardScreen(navController)
        }

        // 💰 Deposits
        composable(Screen.Deposits.route) {
            DepositHistoryScreen(navController = navController,
            drawerState = drawerState,
            scope = scope)
        }
        composable("add_deposit") {
            val context = LocalContext.current.applicationContext as Context
            val factory = EntryPointAccessors.fromApplication(
                context,
                DepositViewModelFactoryEntryPoint::class.java
            ).depositViewModelFactory()

            AddDepositScreen(
                currentUserRole = user.role,
                shareholderId = "SH001",
                shareholderName = "John Doe",
                lastDepositId = "D123",
                onSaveSuccess = { navController.popBackStack() },
                factory = factory,
                modifier = Modifier
            )
        }

        // 🧾 Borrowing
        composable("borrowingHistory") {
            BorrowingHistoryScreen(
                onAddBorrowing = { navController.navigate("addBorrowing") },
                onAddRepayment = { borrowId ->
                    navController.navigate("addRepayment/$borrowId")
                }
            )
        }
        composable("addBorrowing") {
            val borrowingViewModel: BorrowingViewModel = hiltViewModel()
            AddBorrowingScreen(
                shareholderId = "SH001",
                shareholderName = "John Doe",
                createdBy = user.name,
                viewModel = borrowingViewModel,
                onSuccess = { navController.popBackStack() }
            )
        }

        // 💸 Repayment
        composable("addRepayment/{borrowId}") { backStackEntry ->
            val repaymentViewModel: RepaymentViewModel = hiltViewModel()
            val borrowId = backStackEntry.arguments?.getString("borrowId") ?: return@composable

            val borrowing = remember {
                BorrowingContext(
                    borrowId = borrowId,
                    shareholderName = "John Doe",
                    outstandingAmount = 8000.0,
                    borrowStartDate = LocalDate.of(2025, 4, 10),
                    dueDate = LocalDate.of(2025, 7, 10)
                )
            }

            val previousRepayments = remember { emptyList<Repayment>() }

            AddRepaymentScreen(
                borrowId = borrowing.borrowId,
                shareholderName = borrowing.shareholderName,
                outstandingBefore = borrowing.outstandingAmount,
                borrowStartDate = borrowing.borrowStartDate,
                dueDate = borrowing.dueDate,
                previousRepayments = previousRepayments,
                viewModel = repaymentViewModel,
                onSuccess = { navController.popBackStack() }
            )
        }

        // 📈 Investments
        composable("addInvestment") {
            val investmentViewModel: InvestmentViewModel = hiltViewModel()
            AddInvestmentScreen(
                viewModel = investmentViewModel,
                onSuccess = { navController.popBackStack() }
            )
        }

        composable(
            "addInvestmentReturn/{investmentId}/{investeeType}/{ownershipType}/{investmentType}/{modeOfPayment}/{status}"
        ) { backStackEntry ->
            val investmentId = backStackEntry.arguments?.getString("investmentId") ?: return@composable
            val investeeType = backStackEntry.arguments?.getString("investeeType") ?: ""
            val ownershipType = backStackEntry.arguments?.getString("ownershipType") ?: ""
            val investmentType = backStackEntry.arguments?.getString("investmentType") ?: ""
            val modeOfPayment = backStackEntry.arguments?.getString("modeOfPayment") ?: ""
            val status = backStackEntry.arguments?.getString("status") ?: ""

            val investmentReturnsViewModel: InvestmentReturnsViewModel = hiltViewModel()

            val investment = remember {
                Investment(
                    investmentId = investmentId,
                    investeeType = safeValueOf<InvesteeType>(investeeType) ?: InvesteeType.External,
                    investeeName = "John Doe",
                    ownershipType = safeValueOf<OwnershipType>(ownershipType) ?: OwnershipType.Individual,
                    partnerNames = null,
                    investmentDate = LocalDate.now(),
                    investmentType = safeValueOf<InvestmentType>(investmentType) ?: InvestmentType.Other,
                    investmentName = "Seed Capital",
                    amount = 10000.0,
                    expectedProfitPercent = 20.0,
                    expectedProfitAmount = 2000.0,
                    expectedReturnPeriod = 90,
                    returnDueDate = LocalDate.now().plusDays(90),
                    modeOfPayment = safeValueOf<PaymentMode>(modeOfPayment) ?: PaymentMode.OTHER,
                    status = safeValueOf<InvestmentStatus>(status) ?: InvestmentStatus.Active,
                    remarks = null
                )
            }

            InvestmentReturnsEntryScreen(
                investment = investment,
                viewModel = investmentReturnsViewModel,
                currentUserName = user.name,
                onReturnAdded = { navController.popBackStack() }
            )
        }

        // ✅ Profile
        composable("profile/{shareholderId}") { backStackEntry ->
            val shareholderId = backStackEntry.arguments?.getString("shareholderId") ?: ""
            ProfileScreen(
                navController = navController,
                shareholderId = shareholderId,
                drawerState = drawerState,
                scope = scope
            )
        }

        // 📊 Reports & Actions
        composable(Screen.Actions.route) {
            val viewModel: ActionScreenViewModel = hiltViewModel()
            ActionScreen(
                viewModel = viewModel,
                currentShareholderId = user.shareholderId
            )
        }

        composable(Screen.ReportsDashboard.route) {
            ReportsDashboardScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        composable(Screen.AddExpense.route) {
            AddExpenseScreen(navController = navController, user = user)
        }

        composable(Screen.AddIncome.route) {
            AddIncomeScreen(navController = navController, user = user)
        }

        composable(Screen.AddPenalty.route) {
            AddPenaltyScreen(navController = navController, user = user)
        }

        composable(Screen.PenaltyReport.route) {
            PenaltyReportScreen(navController = navController, user = user)
        }

        composable(Screen.CashFlowReport.route) {
            CashFlowReportScreen(viewModel = hiltViewModel())
        }

        // 🔁 Transactions
        composable("addTransaction") {
            val transactionViewModel: TransactionViewModel = hiltViewModel()
            TransactionForm(onSubmit = { txn ->
                transactionViewModel.addTransaction(txn)
                navController.popBackStack()
            })
        }
    }
}

// Temporary mock context — replace with repository call
data class BorrowingContext(
    val borrowId: String,
    val shareholderName: String,
    val outstandingAmount: Double,
    val borrowStartDate: LocalDate,
    val dueDate: LocalDate
)