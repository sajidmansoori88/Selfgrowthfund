package com.selfgrowthfund.sgf.data.local.dto

import java.time.LocalDate

data class ClosedBorrowingDTO(
    val borrowId: String,
    val shareholderName: String,
    val approvedAmount: Double,
    val borrowStartDate: LocalDate,
    val closedDate: LocalDate,
    val totalPenaltyPaid: Double,
    val totalPrincipalRepaid: Double
)