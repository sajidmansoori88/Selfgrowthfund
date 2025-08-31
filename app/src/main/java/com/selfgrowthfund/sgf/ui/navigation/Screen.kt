package com.selfgrowthfund.sgf.ui.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Home : Screen("home")

    // ✅ Profile route expects shareholderId as argument
    object Profile : Screen("profile/{shareholderId}") {
        fun createRoute(shareholderId: String) = "profile/$shareholderId"
    }

    object Deposits : Screen("deposits")
    object Borrowings : Screen("borrowings")
    object Investments : Screen("investments")
    object ReportsDashboard : Screen("reportsDashboard")
    object Reports : Screen("reports")
    object Actions : Screen("actions")
    object AdminDashboard : Screen("adminDashboard")
    object TransactionManager : Screen("transactionManager")
    object AddExpense : Screen("addExpense")
    object AddIncome : Screen("addIncome")
    object AddPenalty : Screen("addPenalty")
    object CreatePin : Screen("create_pin")
    object PinEntry : Screen("pin_entry")
    object BiometricSetup : Screen("biometric_setup")
    object AccessDenied : Screen("access_denied")
    object TreasurerDashboard : Screen("treasurer_dashboard")
    object PenaltyReport : Screen("penalty_report")
    object CashFlowReport : Screen("cash_flow_report")
    object InvestmentReport : Screen("investment_report")
    object BorrowingReport : Screen("borrowing_report")
    object ShareholderSummaryReport : Screen("shareholder_summary_report")
    object FundOverviewReport : Screen("fund_overview_report")

}
