package com.selfgrowthfund.sgf.ui.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Home : Screen("home")

    // Profile with argument
    object Profile : Screen("profile/{shareholderId}") {
        fun createRoute(shareholderId: String) = "profile/$shareholderId"
    }

    // Core modules
    object Deposits : Screen("deposits")
    object Borrowings : Screen("borrowings")
    object Investments : Screen("investments")
    object Actions : Screen("actions")

    // Dashboards
    object AdminDashboard : Screen("admin_dashboard")
    object TreasurerDashboard : Screen("treasurer_dashboard")

    // ───── Shareholder Management ─────
    object ShareholderList : Screen("shareholder_screen")
    object AddShareholder : Screen("add_shareholder_screen")
    object EditShareholder : Screen("edit_shareholder_screen/{shareholderId}") {
        fun createRoute(shareholderId: String) = "edit_shareholder_screen/$shareholderId"
    }

    // ───── Approval Flows ─────
    object ApprovalQueue : Screen("approval_screen")
    object ApprovalHistory : Screen("approval_history_screen")

    // ───── Session Tracking ─────
    object SessionHistory : Screen("session_history_screen")


    // Reports
    object ReportsDashboard : Screen("reports_dashboard")
    object PenaltyReport : Screen("penalty_report")
    object CashFlowReport : Screen("cash_flow_report")
    object InvestmentReport : Screen("investment_report")
    object BorrowingReport : Screen("borrowing_report")
    object ShareholderSummaryReport : Screen("shareholder_summary_report")
    object FundOverviewReport : Screen("fund_overview_report")

    // ✅ NEW: Borrowing Reports
    object ActiveBorrowingsReport : Screen("active_borrowings_report")
    object ClosedBorrowingsReport : Screen("closed_borrowings_report")

    // Investments flow
    object AddInvestment : Screen("add_investment")
    object InvestmentDetail : Screen("investment_detail/{investmentId}") {
        fun createRoute(investmentId: String) = "investment_detail/$investmentId"
    }
    object AddInvestmentReturn : Screen("add_investment_return/{investmentId}/{investeeType}/{ownershipType}/{investmentType}/{modeOfPayment}/{status}") {
        fun createRoute(
            investmentId: String,
            investeeType: String,
            ownershipType: String,
            investmentType: String,
            modeOfPayment: String,
            status: String
        ) = "add_investment_return/$investmentId/$investeeType/$ownershipType/$investmentType/$modeOfPayment/$status"
    }

    // ✅ NEW: Simplified Investment Return
    object AddReturn : Screen("add_return/{investmentId}") {
        fun createRoute(investmentId: String) = "add_return/$investmentId"
    }

    // ✅ NEW: Apply Investment
    object ApplyInvestment : Screen("apply_investment/{investmentId}") {
        fun createRoute(investmentId: String) = "apply_investment/$investmentId"
    }

    // Deposits flow
    object AddDeposit : Screen("add_deposit?shareholderId={shareholderId}&shareholderName={shareholderName}&role={role}&lastDepositId={lastDepositId}") {
        fun createRoute(
            shareholderId: String,
            shareholderName: String,
            role: String,
            lastDepositId: String? = null
        ) = "add_deposit?shareholderId=$shareholderId&shareholderName=$shareholderName&role=$role&lastDepositId=${lastDepositId ?: ""}"
    }

    // Borrowings flow
    object AddBorrowing : Screen("add_borrowing/{shareholderId}/{shareholderName}") {
        fun createRoute(shareholderId: String, shareholderName: String) = "add_borrowing/$shareholderId/$shareholderName"
    }

    // Transactions / Penalties / Incomes
    object AddTransaction : Screen("add_transaction")
    object AddOtherExpense : Screen("add_other_expense")
    object AddOtherIncome : Screen("add_other_income")
    object AddPenalty : Screen("add_penalty")

    // Member Management
    object ApproveMembers : Screen("approve_members")
    object ManageRoles : Screen("manage_roles")

    // Auth / Security
    object CreatePin : Screen("create_pin")
    object PinEntry : Screen("pin_entry")
    object BiometricSetup : Screen("biometric_setup")
    object AccessDenied : Screen("access_denied")

    // ✅ NEW: Settings & Configuration
    object Settings : Screen("settings")
    object Notifications : Screen("notifications")
    object Help : Screen("help")
    object About : Screen("about")

    // ✅ NEW: Detailed Reports
    object InvestmentReturnsReport : Screen("investment_returns_report")
    object DividendReport : Screen("dividend_report")
    object AuditReport : Screen("audit_report")

    // ✅ NEW: Quick Actions
    object QuickDeposit : Screen("quick_deposit")
    object QuickBorrowing : Screen("quick_borrowing")
    object QuickInvestment : Screen("quick_investment")
}