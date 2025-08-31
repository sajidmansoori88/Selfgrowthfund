package com.selfgrowthfund.sgf.data.local.dto

data class ClosedBorrowingSummary(
    val borrowId: String,
    val shareholderName: String,
    val borrowAmount: Double,
    val penaltyPaid: Double,
    val totalPaid: Double,
    val returnPeriodDays: Long
)