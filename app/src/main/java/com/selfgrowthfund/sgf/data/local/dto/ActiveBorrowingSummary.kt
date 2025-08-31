package com.selfgrowthfund.sgf.data.local.dto

data class ActiveBorrowingSummary(
    val borrowId: String,
    val shareholderName: String,
    val borrowAmount: Double,
    val principalRepaid: Double,
    val penaltyPaid: Double,
    val outstanding: Double,
    val penaltyDue: Double,
    val overdueDays: Long
)