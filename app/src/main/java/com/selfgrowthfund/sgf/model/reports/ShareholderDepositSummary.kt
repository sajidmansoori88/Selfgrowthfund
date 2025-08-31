package com.selfgrowthfund.sgf.model.reports

import java.time.LocalDate

data class ShareholderDepositSummary(
    val shareholderId: String,
    val totalDeposits: Double,
    val lastDate: LocalDate?
)

data class ShareholderPenaltySummary(
    val shareholderId: String,
    val totalPenalties: Double
)