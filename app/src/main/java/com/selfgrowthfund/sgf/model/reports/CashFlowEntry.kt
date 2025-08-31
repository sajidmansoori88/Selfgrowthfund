package com.selfgrowthfund.sgf.model.reports

data class CashFlowEntry(
    val month: String,              // e.g. "Aug 2025"
    val openingBalance: Double,     // Carry-forward from previous month
    val income: Double,             // Deposits, penalties, returns
    val expense: Double,            // Borrowing repayments, fund expenses
    val closingBalance: Double      // opening + income - expense
)