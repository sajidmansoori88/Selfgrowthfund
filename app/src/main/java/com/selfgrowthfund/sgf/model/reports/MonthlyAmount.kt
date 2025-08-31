package com.selfgrowthfund.sgf.model.reports

data class MonthlyAmount(
    val month: String, // format: "YYYY-MM"
    val total: Double
)