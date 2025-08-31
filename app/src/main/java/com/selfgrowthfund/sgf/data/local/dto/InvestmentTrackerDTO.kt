package com.selfgrowthfund.sgf.data.local.dto

import java.time.LocalDate

data class InvestmentTrackerDTO(
    val investmentId: String,
    val investmentName: String,
    val investorName: String?,
    val expectedReturnDate: LocalDate,
    val actualReturnDate: LocalDate?,
    val expectedProfitPercent: Double,
    val actualProfitPercent: Double?
)