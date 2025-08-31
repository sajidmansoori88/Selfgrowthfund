package com.selfgrowthfund.sgf.data.local.dto

import java.time.LocalDate

data class ActiveBorrowingDTO(
    val borrowId: String,
    val shareholderName: String,
    val approvedAmount: Double,
    val dueDate: LocalDate,
    val totalPrincipalRepaid: Double,
    val totalPenaltyPaid: Double,
    val totalPenaltyAccrued: Double
)