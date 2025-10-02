package com.selfgrowthfund.sgf.data.local.dto

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class RepaymentSummaryDTO(
    val provisionalId: String,
    val repaymentId: String? = null,
    val borrowId: String,
    val shareholderName: String,
    val repaymentDate: LocalDate,
    val principalRepaid: Double,
    val penaltyPaid: Double,
    val totalAmountPaid: Double,
    val modeOfPayment: String,
    val finalOutstanding: Double,
    val approvalStatus: String,
    val createdAt: Instant? = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
)
