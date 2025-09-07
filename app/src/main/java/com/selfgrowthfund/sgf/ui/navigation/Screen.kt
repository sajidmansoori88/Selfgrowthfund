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

    // Reports
    object ReportsDashboard : Screen("reports_dashboard")
    object PenaltyReport : Screen("penalty_report")
    object CashFlowReport : Screen("cash_flow_report")
    object InvestmentReport : Screen("investment_report") // not yet in AppNavGraph
    object BorrowingReport : Screen("borrowing_report")   // not yet in AppNavGraph
    object ShareholderSummaryReport : Screen("shareholder_summary_report") // not yet in AppNavGraph
    object FundOverviewReport : Screen("fund_overview_report")

    // Investments flow
    object AddInvestment : Screen("add_investment")
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

    // Transactions / Penalties / Incomes
    object AddTransaction : Screen("add_transaction")
    object AddExpense : Screen("add_expense")
    object AddIncome : Screen("add_income")
    object AddPenalty : Screen("add_penalty")

    // Auth / Security
    object CreatePin : Screen("create_pin")
    object PinEntry : Screen("pin_entry")
    object BiometricSetup : Screen("biometric_setup")
    object AccessDenied : Screen("access_denied")
}
