package com.selfgrowthfund.sgf.data.local.dto

import java.time.Instant
import java.time.LocalDate

data class DepositEntrySummaryDTO(
    val provisionalId: String = "",
    val depositId: String? = null,
    val shareholderId: String,
    val shareholderName: String,
    val shareNos: Int,
    val shareAmount: Double,
    val additionalContribution: Double,
    val penalty: Double,
    val totalAmount: Double,
    val paymentStatus: String,   // mapped from PaymentStatus.name
    val modeOfPayment: String,   // mapped from PaymentMode.name or "Unknown"
    val dueMonth: String,
    val paymentDate: LocalDate,
    val createdAt: Instant
)
