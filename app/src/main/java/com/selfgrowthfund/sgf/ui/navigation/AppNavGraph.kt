package com.selfgrowthfund.sgf.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.selfgrowthfund.sgf.data.local.entities.Investment
import com.selfgrowthfund.sgf.data.local.entities.InvestmentReturns
import com.selfgrowthfund.sgf.data.local.entities.Repayment
import com.selfgrowthfund.sgf.ui.WelcomeScreen
import com.selfgrowthfund.sgf.ui.dashboard.HomeScreen
import com.selfgrowthfund.sgf.ui.auth.*
import com.selfgrowthfund.sgf.ui.deposits.*
import com.selfgrowthfund.sgf.ui.deposits.AddDepositScreen
import com.selfgrowthfund.sgf.model.User
import com.selfgrowthfund.sgf.model.enums.InvesteeType
import com.selfgrowthfund.sgf.model.enums.InvestmentStatus
import com.selfgrowthfund.sgf.model.enums.InvestmentType
import com.selfgrowthfund.sgf.model.enums.OwnershipType
import com.selfgrowthfund.sgf.model.enums.PaymentMode
import com.selfgrowthfund.sgf.session.UserSessionViewModel
import com.selfgrowthfund.sgf.ui.ProfileScreen
import com.selfgrowthfund.sgf.ui.actions.ActionScreen
import com.selfgrowthfund.sgf.ui.actions.ActionScreenViewModel
import com.selfgrowthfund.sgf.ui.borrowing.*
import com.selfgrowthfund.sgf.ui.auth.PinEntryScreen
import com.selfgrowthfund.sgf.ui.auth.BiometricSetupScreen
import com.selfgrowthfund.sgf.ui.components.SGFScaffoldWrapper
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
import java.time.LocalDate
import com.selfgrowthfund.sgf.ui.dashboard.AdminDashboardScreen
import com.selfgrowthfund.sgf.ui.investments.InvestmentDetailScreen
import com.selfgrowthfund.sgf.ui.reports.FundOverviewScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    onDrawerClick: () -> Unit,
    currentUser: User
) {
    val startDestination = "home"

    NavHost(
        navController = navController,
        startDestination = startDestination,
        route = "root"
    ) {

        // 🔐 Auth & Onboarding (no drawer, no scaffold)
        composable(Screen.Welcome.route) { WelcomeScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.CreatePin.route) { CreatePinScreen(navController) }
        composable(Screen.PinEntry.route) { PinEntryScreen(navController) }
        composable(Screen.BiometricSetup.route) { BiometricSetupScreen(navController) }
        composable(Screen.AccessDenied.route) { AccessDeniedScreen(navController) }

        // 🏠 Dashboards
        composable(Screen.Home.route) {
            SGFScaffoldWrapper(
                title = "Home",
                onDrawerClick = onDrawerClick
            ) {
                HomeScreen()
            }
        }

        composable(Screen.AdminDashboard.route) {
            SGFScaffoldWrapper(
                title = "Admin Dashboard",
                onDrawerClick = onDrawerClick
            ) {
                AdminDashboardScreen(role = currentUser.role)
            }
        }

        composable(Screen.TreasurerDashboard.route) {
            SGFScaffoldWrapper(
                title = "Treasurer Dashboard",
                onDrawerClick = onDrawerClick
            ) {
                TreasurerDashboardScreen()
            }
        }

        // 💰 Deposits
        composable(Screen.Deposits.route) {
            val sessionViewModel: UserSessionViewModel = hiltViewModel()
            val currentUser by sessionViewModel.currentUser.collectAsState()
            val viewModel: DepositViewModel = hiltViewModel()
            val summaries by viewModel.depositSummaries.collectAsState()

            SGFScaffoldWrapper(
                title = "Deposits",
                onDrawerClick = onDrawerClick,
                floatingActionButton = {
                    if (currentUser.shareholderId.isNotBlank()) {
                        FloatingActionButton(
                            onClick = {
                                navController.navigate(
                                    "add_deposit?shareholderId=${currentUser.shareholderId}&shareholderName=${currentUser.name}&role=${currentUser.role.name}&lastDepositId=${summaries.lastOrNull()?.depositId ?: ""}"
                                )
                            },
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Deposit")
                        }
                    }
                }
            ) { padding ->
                DepositHistoryScreen(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    summaries = summaries
                )

            }
        }

        composable(
            route = "add_deposit?shareholderId={shareholderId}&shareholderName={shareholderName}&role={role}&lastDepositId={lastDepositId}",
            arguments = listOf(
                navArgument("shareholderId") { type = NavType.StringType },
                navArgument("shareholderName") { type = NavType.StringType },
                navArgument("role") { type = NavType.StringType },
                navArgument("lastDepositId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            // Extract arguments
            val shareholderId = backStackEntry.arguments?.getString("shareholderId") ?: ""
            val shareholderName = backStackEntry.arguments?.getString("shareholderName") ?: ""
            val role = backStackEntry.arguments?.getString("role") ?: ""
            val lastDepositId = backStackEntry.arguments?.getString("lastDepositId")

            // Get ViewModel
            val viewModel: DepositViewModel = hiltViewModel()

            SGFScaffoldWrapper(
                title = "Add Deposit",
                onDrawerClick = onDrawerClick
            ) { innerPadding ->
                AddDepositScreen(
                    navController = navController,
                    modifier = Modifier.padding(innerPadding), // ✅ CRITICAL: Pass the padding here
                    onSaveSuccess = { navController.popBackStack() }
                )
            }
        }
        // 🧾 Borrowing
        composable(Screen.Borrowings.route) {
            SGFScaffoldWrapper(
                title = "Borrowings",
                onDrawerClick = onDrawerClick
            ) {
                BorrowingHistoryScreen(
                    onAddBorrowing = { navController.navigate("addBorrowing") },
                    onAddRepayment = { borrowId ->
                        navController.navigate("addRepayment/$borrowId")
                    }
                )
            }
        }

        composable("addBorrowing") {
            val borrowingViewModel: BorrowingViewModel = hiltViewModel()
            SGFScaffoldWrapper(
                title = "Add Borrowing",
                onDrawerClick = onDrawerClick
            ) {
                AddBorrowingScreen(
                    shareholderId = "SH001",
                    shareholderName = "John Doe",
                    createdBy = currentUser.name,
                    viewModel = borrowingViewModel,
                    onSuccess = { navController.popBackStack() }
                )
            }
        }

        // 💸 Repayment
        composable("addRepayment/{borrowId}") { backStackEntry ->
            val repaymentViewModel: RepaymentViewModel = hiltViewModel()
            val borrowId = backStackEntry.arguments?.getString("borrowId") ?: return@composable

            val shareholderName = "John Doe"
            val outstandingAmount = 8000.0
            val borrowStartDate = LocalDate.of(2025, 4, 10)
            val dueDate = LocalDate.of(2025, 7, 10)
            val previousRepayments = remember { emptyList<Repayment>() }

            SGFScaffoldWrapper(
                title = "Add Repayment",
                onDrawerClick = onDrawerClick
            ) {
                AddRepaymentScreen(
                    borrowId = borrowId,
                    shareholderName = shareholderName,
                    outstandingBefore = outstandingAmount,
                    borrowStartDate = borrowStartDate,
                    dueDate = dueDate,
                    previousRepayments = previousRepayments,
                    viewModel = repaymentViewModel,
                    onSuccess = { navController.popBackStack() }
                )
            }
        }

        // 📈 Investments
        // Investment Detail Screen (from Drawer)
        composable(Screen.Investments.route) {
            val investmentViewModel: InvestmentViewModel = hiltViewModel()
            val investmentReturnsViewModel: InvestmentReturnsViewModel = hiltViewModel()

            // TODO: Replace dummy data with ViewModel data
            val dummyInvestment = Investment(
                investmentId = "INV001",
                investeeType = InvesteeType.External,
                investeeName = "John Doe",
                ownershipType = OwnershipType.Individual,
                partnerNames = null,
                investmentDate = LocalDate.now(),
                investmentType = InvestmentType.Other,
                investmentName = "Seed Capital",
                amount = 10000.0,
                expectedProfitPercent = 20.0,
                expectedProfitAmount = 2000.0,
                expectedReturnPeriod = 90,
                returnDueDate = LocalDate.now().plusDays(90),
                modeOfPayment = PaymentMode.OTHER,
                status = InvestmentStatus.Active,
                remarks = null
            )
            val dummyReturns = emptyList<InvestmentReturns>()

            InvestmentDetailScreen(
                investment = dummyInvestment,
                returns = dummyReturns,
                currentUserRole = currentUser.role,
                onAddReturn = {
                    navController.navigate(
                        Screen.AddInvestmentReturn.createRoute(
                            investmentId = dummyInvestment.investmentId,
                            investeeType = dummyInvestment.investeeType.name,
                            ownershipType = dummyInvestment.ownershipType.name,
                            investmentType = dummyInvestment.investmentType.name,
                            modeOfPayment = dummyInvestment.modeOfPayment.name,
                            status = dummyInvestment.status.name
                        )
                    )
                },
                onApplyInvestment = {
                    navController.navigate(Screen.AddInvestment.route)
                },
                onDrawerClick = onDrawerClick
            )
        }

// Add Investment Screen
        composable(Screen.AddInvestment.route) {
            val investmentViewModel: InvestmentViewModel = hiltViewModel()
            SGFScaffoldWrapper(
                title = "Add Investment",
                onDrawerClick = onDrawerClick
            ) { innerPadding ->
                AddInvestmentScreen(
                    viewModel = investmentViewModel,
                    onSuccess = { navController.popBackStack() },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

// Add Investment Return Screen
        composable(Screen.AddInvestmentReturn.route) { backStackEntry ->
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
                    investeeName = "John Doe", // You might want to get this from arguments or ViewModel
                    ownershipType = safeValueOf<OwnershipType>(ownershipType) ?: OwnershipType.Individual,
                    partnerNames = null,
                    investmentDate = LocalDate.now(),
                    investmentType = safeValueOf<InvestmentType>(investmentType) ?: InvestmentType.Other,
                    investmentName = "Seed Capital", // You might want to get this from arguments or ViewModel
                    amount = 10000.0, // You might want to get this from arguments or ViewModel
                    expectedProfitPercent = 20.0, // You might want to get this from arguments or ViewModel
                    expectedProfitAmount = 2000.0, // You might want to get this from arguments or ViewModel
                    expectedReturnPeriod = 90, // You might want to get this from arguments or ViewModel
                    returnDueDate = LocalDate.now().plusDays(90),
                    modeOfPayment = safeValueOf<PaymentMode>(modeOfPayment) ?: PaymentMode.OTHER,
                    status = safeValueOf<InvestmentStatus>(status) ?: InvestmentStatus.Active,
                    remarks = null
                )
            }

            SGFScaffoldWrapper(
                title = "Add Investment Return",
                onDrawerClick = onDrawerClick
            ) { innerPadding -> // ✅ This gives you the innerPadding
                InvestmentReturnsEntryScreen(
                    investment = investment,
                    viewModel = investmentReturnsViewModel,
                    currentUserName = currentUser.name,
                    onReturnAdded = { navController.popBackStack() },
                    modifier = Modifier.padding(innerPadding) // ✅ CRITICAL: Pass the padding here
                )
            }
        }

        // ✅ Profile
        composable(Screen.Profile.route) { backStackEntry ->
            val shareholderId = backStackEntry.arguments?.getString("shareholderId")
                ?: error("shareholderId is required")

            SGFScaffoldWrapper(
                title = "Profile",
                onDrawerClick = onDrawerClick
            ) {
                ProfileScreen(
                    shareholderId = shareholderId,
                    onLogout = {
                        navController.navigate(Screen.Welcome.route) {
                            // clear backstack so user can’t press back into app
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }


        // 📊 Reports & Actions
        composable(Screen.Actions.route) {
            val viewModel: ActionScreenViewModel = hiltViewModel()
            SGFScaffoldWrapper(
                title = "Actions",
                onDrawerClick = onDrawerClick
            ) { innerPadding -> // ✅ This gives you the innerPadding
                ActionScreen(
                    viewModel = viewModel,
                    currentShareholderId = currentUser.shareholderId,
                    modifier = Modifier.padding(innerPadding) // ✅ Pass the padding here
                )
            }
        }

        composable(Screen.ReportsDashboard.route) {
            SGFScaffoldWrapper(
                title = "Reports",
                onDrawerClick = onDrawerClick
            ) {innerPadding ->
                ReportsDashboardScreen(viewModel = hiltViewModel(),
                    modifier = Modifier.padding(innerPadding))
            }
        }

        composable(Screen.AddExpense.route) {
            SGFScaffoldWrapper(
                title = "Add Expense",
                onDrawerClick = onDrawerClick
            ) {
                AddExpenseScreen(user = currentUser)
            }
        }

        composable(Screen.AddIncome.route) {
            SGFScaffoldWrapper(
                title = "Add Income",
                onDrawerClick = onDrawerClick
            ) {
                AddIncomeScreen(user = currentUser)
            }
        }

        composable(Screen.AddPenalty.route) {
            SGFScaffoldWrapper(
                title = "Add Penalty",
                onDrawerClick = onDrawerClick
            ) {
                AddPenaltyScreen(user = currentUser)
            }
        }

        composable(Screen.PenaltyReport.route) {
            SGFScaffoldWrapper(
                title = "Penalty Report",
                onDrawerClick = onDrawerClick
            ) {
                PenaltyReportScreen(user = currentUser)
            }
        }

        composable(Screen.CashFlowReport.route) {
            SGFScaffoldWrapper(
                title = "Cash Flow Report",
                onDrawerClick = onDrawerClick
            ) {
                CashFlowReportScreen(viewModel = hiltViewModel())
            }
        }

        // 🔁 Transactions
        composable("addTransaction") {
            val transactionViewModel: TransactionViewModel = hiltViewModel()
            SGFScaffoldWrapper(
                title = "Add Transaction",
                onDrawerClick = onDrawerClick
            ) {
                TransactionForm(onSubmit = { txn ->
                    transactionViewModel.addTransaction(txn)
                    navController.popBackStack()
                })
            }
        }

        composable(Screen.FundOverviewReport.route) {
            SGFScaffoldWrapper(
                title = "Fund Overview",
                onDrawerClick = onDrawerClick
            ) {
                FundOverviewScreen(viewModel = hiltViewModel())
            }
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