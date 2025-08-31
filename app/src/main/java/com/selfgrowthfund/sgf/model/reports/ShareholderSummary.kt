package com.selfgrowthfund.sgf.model.reports

data class ShareholderSummary(
    val shareholderId: String,
    val name: String,
    val shares: Int,
    val shareAmount: Double,
    val shareValue: Double,
    val percentContribution: Double,
    val netProfit: Double,
    val absoluteReturn: Double,
    val annualizedReturn: Double
)