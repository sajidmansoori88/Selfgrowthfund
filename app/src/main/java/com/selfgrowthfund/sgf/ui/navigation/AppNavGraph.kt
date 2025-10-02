package com.selfgrowthfund.sgf.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.selfgrowthfund.sgf.data.local.entities.InvestmentReturns
import com.selfgrowthfund.sgf.ui.WelcomeScreen
import com.selfgrowthfund.sgf.ui.dashboard.HomeScreen
import com.selfgrowthfund.sgf.ui.auth.*
import com.selfgrowthfund.sgf.ui.deposits.*
import com.selfgrowthfund.sgf.ui.deposits.AddDepositScreen
import com.selfgrowthfund.sgf.model.User
import com.selfgrowthfund.sgf.model.enums.*
import com.selfgrowthfund.sgf.model.reports.BorrowingSummaryViewModel
import com.selfgrowthfund.sgf.session.UserSessionViewModel
import com.selfgrowthfund.sgf.ui.ProfileScreen
import com.selfgrowthfund.sgf.ui.actions.ActionScreen
import com.selfgrowthfund.sgf.ui.actions.ActionScreenViewModel
import com.selfgrowthfund.sgf.ui.addshareholders.AddShareholderScreen
import com.selfgrowthfund.sgf.ui.admin.AdminApprovalHistoryScreen
import com.selfgrowthfund.sgf.ui.admin.AdminApprovalScreen
import com.selfgrowthfund.sgf.ui.admin.AdminSessionHistoryScreen
import com.selfgrowthfund.sgf.ui.admin.AdminShareholderScreen
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
import com.selfgrowthfund.sgf.ui.penalty.AddPenaltyScreen
import com.selfgrowthfund.sgf.ui.repayments.AddRepaymentScreen
import com.selfgrowthfund.sgf.ui.repayments.RepaymentViewModel
import com.selfgrowthfund.sgf.ui.reports.CashFlowReportScreen
import com.selfgrowthfund.sgf.ui.reports.ReportsDashboardScreen
import com.selfgrowthfund.sgf.ui.transactions.AddOtherExpenseScreen
import com.selfgrowthfund.sgf.ui.transactions.AddOtherIncomeScreen
import com.selfgrowthfund.sgf.ui.transactions.TransactionForm
import com.selfgrowthfund.sgf.ui.transactions.TransactionViewModel
import java.time.LocalDate
import com.selfgrowthfund.sgf.ui.admin.AdminDashboardScreen
import com.selfgrowthfund.sgf.ui.admin.AdminDashboardViewModel
import com.selfgrowthfund.sgf.ui.editshareholders.EditShareholderScreen
import com.selfgrowthfund.sgf.ui.investments.InvestmentDetailScreen
import com.selfgrowthfund.sgf.ui.investments.InvestmentListScreen
import com.selfgrowthfund.sgf.ui.reports.ActiveBorrowingReportScreen
import com.selfgrowthfund.sgf.ui.reports.ClosedBorrowingReportScreen
import com.selfgrowthfund.sgf.ui.reports.FundOverviewScreen
import com.selfgrowthfund.sgf.ui.reports.ReportsPlaceholderScreen
import kotlinx.coroutines.flow.flowOf

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
                onDrawerClick = onDrawerClick,
                snackbarHostState = remember { SnackbarHostState() },
                content = { innerPadding ->
                    HomeScreen(
                        modifier = Modifier.padding(innerPadding) // ✅ Pass the padding here
                    )
                },
            )
        }
        // ───── Admin Dashboard ─────
        composable("admin_dashboard") {
            SGFScaffoldWrapper(
                title = "Admin Dashboard",
                onDrawerClick = onDrawerClick,
                snackbarHostState = remember { SnackbarHostState() },
                content = { innerPadding ->
                    AdminDashboardScreen(
                        modifier = Modifier.padding(innerPadding),
                        role = MemberRole.MEMBER_ADMIN,
                        onManageShareholders = { navController.navigate("shareholder_screen") },
                        onViewApprovals = { navController.navigate("approval_screen") },
                        onViewApprovalHistory = { navController.navigate("approval_history_screen") },
                        onViewSessionHistory = { navController.navigate("session_history_screen") },
                        onViewReports = { navController.navigate("reports_screen") }
                    )
                },
            )
        }

        // ───── Shareholder Management ─────
        composable("shareholder_screen") {
            val viewModel: AdminDashboardViewModel = hiltViewModel()

            SGFScaffoldWrapper(
                title = "Shareholders",
                onDrawerClick = onDrawerClick,
                snackbarHostState = remember { SnackbarHostState() },
                content = { innerPadding ->
                    AdminShareholderScreen(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = viewModel,
                        onAddClick = { navController.navigate("add_shareholder_screen") },
                        onModifyClick = { shareholder -> viewModel.modifyShareholder(shareholder) },
                        onDeleteClick = { shareholder -> viewModel.deleteShareholder(shareholder) }
                    )
                }
            )
        }


        composable("add_shareholder_screen") {
            SGFScaffoldWrapper(
                title = "Add Shareholder",
                onDrawerClick = onDrawerClick,
                snackbarHostState = remember { SnackbarHostState() },
                content = { innerPadding ->
                    AddShareholderScreen(
                        modifier = Modifier.padding(innerPadding),
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToEdit = { id -> navController.navigate("edit_shareholder_screen/$id") }
                    )
                },
            )
        }

        composable("edit_shareholder_screen/{shareholderId}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("shareholderId") ?: ""
            SGFScaffoldWrapper(
                title = "Edit Shareholder",
                onDrawerClick = onDrawerClick,
                snackbarHostState = remember { SnackbarHostState() },
                content = { innerPadding ->
                    EditShareholderScreen(
                        modifier = Modifier.padding(innerPadding),
                        shareholderId = id,
                        onNavigateBack = { navController.popBackStack() }
                    )
                },
            )
        }

        // ───── Approval Flows ─────
        composable("approval_screen") {
            val viewModel: AdminDashboardViewModel = hiltViewModel()
            SGFScaffoldWrapper(
                title = "Pending Approvals",
                onDrawerClick = onDrawerClick,
                snackbarHostState = remember { SnackbarHostState() },
                content = { innerPadding ->
                    AdminApprovalScreen(
                        modifier = Modifier
                            .padding(innerPadding), // ← CRITICAL: Add this
                        viewModel = viewModel,
                        onDrawerClick = onDrawerClick // ← Also pass drawer click if needed
                    )
                },
            )
        }

        composable("approval_history_screen") {
            val viewModel: AdminDashboardViewModel = hiltViewModel()
            SGFScaffoldWrapper(
                title = "Approval History",
                onDrawerClick = onDrawerClick,
                snackbarHostState = remember { SnackbarHostState() },
                content = { innerPadding ->
                    AdminApprovalHistoryScreen(
                        modifier = Modifier
                            .padding(innerPadding),
                        viewModel = viewModel,
                        snackbarHostState = remember { SnackbarHostState() })
                },

            )
        }

        // ───── Session Tracking ─────
        composable("session_history_screen") {
            val viewModel: AdminDashboardViewModel = hiltViewModel()
            SGFScaffoldWrapper(
                title = "Session History",
                onDrawerClick = onDrawerClick,
                snackbarHostState = remember { SnackbarHostState() },
                content = { innerPadding ->
                    AdminSessionHistoryScreen(
                        modifier = Modifier
                            .padding(innerPadding),
                        viewModel = viewModel)
                },
            )
        }

        // ───── Reports Placeholder ─────
        composable("reports_screen") {
            SGFScaffoldWrapper(
                title = "Reports",
                onDrawerClick = onDrawerClick,
                snackbarHostState = remember { SnackbarHostState() },
                content = { innerPadding ->
                    ReportsPlaceholderScreen()
                },
            )
        }

        // ───── Treasurer Dashboard ─────
        composable(Screen.TreasurerDashboard.route) {
            SGFScaffoldWrapper(
                title = "Treasurer Dashboard",
                onDrawerClick = onDrawerClick,
                snackbarHostState = remember { SnackbarHostState() },
                content = { innerPadding ->
                    TreasurerDashboardScreen()
                },
            )
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
                },
                { padding ->
                    DepositHistoryScreen(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize(),
                        summaries = summaries
                    )

                },
                snackbarHostState = remember { SnackbarHostState() }
            )
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
                onDrawerClick = onDrawerClick,
                content = { innerPadding ->
                    AddDepositScreen(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding), // ✅ CRITICAL: Pass the padding here
                        onSaveSuccess = { navController.popBackStack() }
                    )
                },
                snackbarHostState = remember { SnackbarHostState() }
            )
        }
        // 🧾 Borrowing
        composable(Screen.Borrowings.route) {
            SGFScaffoldWrapper(
                title = "Borrowings",
                onDrawerClick = onDrawerClick,
                content = {
                    BorrowingHistoryScreen(
                        onAddBorrowing = {
                            navController.navigate("addBorrowing/${currentUser.shareholderId}/${currentUser.name}")
                        },
                        onAddRepayment = { borrowId ->
                            navController.navigate("addRepayment/$borrowId")
                        }
                    )
                },
                snackbarHostState = remember { SnackbarHostState() }
            )
        }

        composable("addBorrowing/{shareholderId}/{shareholderName}") { backStackEntry ->
            val shareholderId = backStackEntry.arguments?.getString("shareholderId") ?: ""
            val shareholderName = backStackEntry.arguments?.getString("shareholderName") ?: ""
            val viewModel: BorrowingViewModel = hiltViewModel()

            SGFScaffoldWrapper(
                title = "Add Borrowing",
                onDrawerClick = onDrawerClick,
                content = { innerPadding ->
                    AddBorrowingScreen(
                        shareholderId = shareholderId,
                        shareholderName = shareholderName,
                        createdBy = "Current User", // Get from auth
                        viewModel = viewModel,
                        onSuccess = { navController.popBackStack() },
                        modifier = Modifier.padding(innerPadding)
                    )
                },
                snackbarHostState = remember { SnackbarHostState() }
            )
        }
        // Active Borrowings Report
        composable(Screen.ActiveBorrowingsReport.route) {
            val viewModel: BorrowingSummaryViewModel = hiltViewModel()

            SGFScaffoldWrapper(
                title = "Active Borrowings",
                onDrawerClick = onDrawerClick,
                content = { innerPadding ->
                    ActiveBorrowingReportScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding) // ✅ Pass the padding here
                    )
                },
                snackbarHostState = remember { SnackbarHostState() }
            )
        }

        // Closed Borrowings Report
        composable(Screen.ClosedBorrowingsReport.route) {
            val viewModel: BorrowingSummaryViewModel = hiltViewModel()

            SGFScaffoldWrapper(
                title = "Closed Borrowings",
                onDrawerClick = onDrawerClick,
                content = { innerPadding ->
                    ClosedBorrowingReportScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding) // ✅ Pass the padding here
                    )
                },
                snackbarHostState = remember { SnackbarHostState() }
            )
        }

        // 💸 Repayment
        composable("addRepayment/{borrowId}") { backStackEntry ->
            val borrowId = backStackEntry.arguments?.getString("borrowId") ?: ""
            val viewModel: RepaymentViewModel = hiltViewModel()

            SGFScaffoldWrapper(
                title = "Add Repayment",
                onDrawerClick = onDrawerClick,
                content = { innerPadding ->
                    AddRepaymentScreen(
                        borrowId = borrowId,
                        viewModel = viewModel,
                        onSuccess = { navController.popBackStack() },
                        modifier = Modifier.padding(innerPadding)
                    )
                },
                snackbarHostState = remember { SnackbarHostState() }
            )
        }

        // 📈 Investments List (entry point)
        composable(Screen.Investments.route) {
            val investmentViewModel: InvestmentViewModel = hiltViewModel()
            val investments by investmentViewModel.investments.collectAsState()

            SGFScaffoldWrapper(
                title = "Investments",
                onDrawerClick = onDrawerClick,
                snackbarHostState = remember { SnackbarHostState() },
                content = { innerPadding ->
                    InvestmentListScreen(
                        investments = investments,
                        onSelectInvestment = { investment ->
                            navController.navigate(
                                Screen.InvestmentDetail.createRoute(investment.provisionalId)
                            )
                        },
                        onApplyInvestment = {
                            navController.navigate(Screen.AddInvestment.route)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            )
        }

// 📊 Investment Detail (by provisionalId)
        composable(
            route = Screen.InvestmentDetail.route,
            arguments = listOf(navArgument("provisionalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val provisionalId = backStackEntry.arguments?.getString("provisionalId") ?: return@composable
            val investmentViewModel: InvestmentViewModel = hiltViewModel()
            val investment by investmentViewModel.getInvestmentByProvisionalId(provisionalId)
                .collectAsState(initial = null)

            investment?.let { inv ->
                val investmentReturnsViewModel: InvestmentReturnsViewModel = hiltViewModel()

                // ✅ directly collect Flow<List<InvestmentReturns>>
                val returns: List<InvestmentReturns> by if (inv.investmentId != null) {
                    investmentReturnsViewModel
                        .getReturnsByInvestmentId(inv.investmentId!!)
                        .collectAsState(initial = emptyList())
                } else {
                    flowOf<List<InvestmentReturns>>(emptyList())
                        .collectAsState(initial = emptyList())
                }

                SGFScaffoldWrapper(
                    title = "Investment Details",
                    onDrawerClick = onDrawerClick,
                    snackbarHostState = remember { SnackbarHostState() },
                    content = { innerPadding ->
                        InvestmentDetailScreen(
                            investment = inv,
                            returns = returns,
                            currentUserRole = currentUser.role,
                            onAddReturn = {
                                inv.investmentId?.let { approvedId ->
                                    navController.navigate(
                                        Screen.AddInvestmentReturn.createRoute(
                                            investmentId = approvedId,
                                            investeeType = inv.investeeType.name,
                                            ownershipType = inv.ownershipType.name,
                                            investmentType = inv.investmentType.name
                                        )
                                    )
                                }
                            },
                            onApplyInvestment = {
                                navController.navigate(Screen.AddInvestment.route)
                            },
                            onDrawerClick = onDrawerClick,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                )
            }
        }

// ➕ Add Investment Screen
        composable(Screen.AddInvestment.route) {
            val investmentViewModel: InvestmentViewModel = hiltViewModel()
            SGFScaffoldWrapper(
                title = "Add Investment",
                onDrawerClick = onDrawerClick,
                content = { innerPadding ->
                    AddInvestmentScreen(
                        viewModel = investmentViewModel,
                        onSuccess = { navController.popBackStack() },
                        modifier = Modifier.padding(innerPadding)
                    )
                },
                snackbarHostState = remember { SnackbarHostState() }
            )
        }

// 💰 Add Investment Return (approved investments only)
        composable(
            route = Screen.AddInvestmentReturn.route,
            arguments = listOf(
                navArgument("investmentId") { type = NavType.StringType },
                navArgument("investeeType") { type = NavType.StringType },
                navArgument("ownershipType") { type = NavType.StringType },
                navArgument("investmentType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val investmentId = backStackEntry.arguments?.getString("investmentId") ?: return@composable
            val investmentViewModel: InvestmentViewModel = hiltViewModel()
            val investment by investmentViewModel.getInvestmentByInvestmentId(investmentId)
                .collectAsState(initial = null)

            val investmentReturnsViewModel: InvestmentReturnsViewModel = hiltViewModel()

            investment?.let { inv ->
                SGFScaffoldWrapper(
                    title = "Add Investment Return",
                    onDrawerClick = onDrawerClick,
                    snackbarHostState = remember { SnackbarHostState() },
                    content = { innerPadding ->
                        InvestmentReturnsEntryScreen(
                            investment = inv,
                            viewModel = investmentReturnsViewModel,
                            currentUserName = currentUser.name,
                            onReturnAdded = { navController.popBackStack() },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                )
            }
        }

        // ✅ Profile
        composable(Screen.Profile.route) { backStackEntry ->
            val shareholderId = backStackEntry.arguments?.getString("shareholderId")
                ?: error("shareholderId is required")

            SGFScaffoldWrapper(
                title = "Profile",
                onDrawerClick = onDrawerClick,
                content = {
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
                },
                snackbarHostState = remember { SnackbarHostState() }
            )
        }


        // 📊 Reports & Actions
        composable(Screen.Actions.route) {
            val viewModel: ActionScreenViewModel = hiltViewModel()
            SGFScaffoldWrapper(
                title = "Actions",
                onDrawerClick = onDrawerClick,
                content = { innerPadding -> // ✅ This gives you the innerPadding
                    ActionScreen(
                        viewModel = viewModel,
                        currentShareholderId = currentUser.shareholderId,
                        modifier = Modifier.padding(innerPadding) // ✅ Pass the padding here
                    )
                },
                snackbarHostState = remember { SnackbarHostState() }
            )
        }

        composable(Screen.ReportsDashboard.route) {
            SGFScaffoldWrapper(
                title = "Reports",
                onDrawerClick = onDrawerClick,
                content = { innerPadding ->
                    ReportsDashboardScreen(viewModel = hiltViewModel(),
                        modifier = Modifier.padding(innerPadding))
                },
                snackbarHostState = remember { SnackbarHostState() }
            )
        }

        composable(Screen.AddOtherExpense.route) {
            SGFScaffoldWrapper(
                title = "Add Expense",
                onDrawerClick = onDrawerClick,
                content = {
                    AddOtherExpenseScreen(user = currentUser)
                },
                snackbarHostState = remember { SnackbarHostState() }
            )
        }

        composable(Screen.AddOtherIncome.route) {
            SGFScaffoldWrapper(
                title = "Add Income",
                onDrawerClick = onDrawerClick,
                content = {
                    AddOtherIncomeScreen(user = currentUser)
                },
                snackbarHostState = remember { SnackbarHostState() }
            )
        }

        composable(Screen.AddPenalty.route) {
            SGFScaffoldWrapper(
                title = "Add Penalty",
                onDrawerClick = onDrawerClick,
                content = {
                    AddPenaltyScreen(user = currentUser)
                },
                snackbarHostState = remember { SnackbarHostState() }
            )
        }

        composable(Screen.PenaltyReport.route) {
            SGFScaffoldWrapper(
                title = "Penalty Report",
                onDrawerClick = onDrawerClick,
                content = {
                    PenaltyReportScreen(user = currentUser)
                },
                snackbarHostState = remember { SnackbarHostState() }
            )
        }

        composable(Screen.CashFlowReport.route) {
            SGFScaffoldWrapper(
                title = "Cash Flow Report",
                onDrawerClick = onDrawerClick,
                content = {
                    CashFlowReportScreen(viewModel = hiltViewModel())
                },
                snackbarHostState = remember { SnackbarHostState() }
            )
        }

        // 🔁 Transactions
        composable("addTransaction") {
            val transactionViewModel: TransactionViewModel = hiltViewModel()
            SGFScaffoldWrapper(
                title = "Add Transaction",
                onDrawerClick = onDrawerClick,
                content = {
                    TransactionForm(onSubmit = { txn ->
                        transactionViewModel.addTransaction(txn)
                        navController.popBackStack()
                    })
                },
                snackbarHostState = remember { SnackbarHostState() }
            )
        }

        composable(Screen.FundOverviewReport.route) {
            SGFScaffoldWrapper(
                title = "Fund Overview",
                onDrawerClick = onDrawerClick,
                content = {
                    FundOverviewScreen(viewModel = hiltViewModel())
                },
                snackbarHostState = remember { SnackbarHostState() }
            )
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