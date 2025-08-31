package com.selfgrowthfund.sgf.data.local.dto

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class DepositEntrySummaryDTO(
    val shareholderName: String,
    val dueMonth: String,
    val paymentDate: LocalDate,
    val totalAmount: Double,
    val paymentStatus: String,
    val additionalContribution: Double = 0.0,
    val createdAt: Instant? = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant(),
    val depositId: String = "",
    val modeOfPayment: String = "",
    val penalty: Double = 0.0,
    val shareAmount: Double = 0.0,
    val shareholderId: String = "",
    val shareNos: Int = 0
)