package com.selfgrowthfund.sgf.model.reports

data class MonthlyCashFlow(
    val month: String, // format: "YYYY-MM"
    val income: Double,
    val expenses: Double,
    var openingBalance: Double = 0.0,
    var closingBalance: Double = 0.0
)