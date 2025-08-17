package com.selfgrowthfund.sgf.data.local.dto

data class DepositEntrySummaryDTO(
    val depositId: String,
    val shareholderId: String,
    val shareholderName: String,
    val dueMonth: String,
    val paymentDate: String,
    val shareNos: Int,
    val shareAmount: Double,
    val additionalContribution: Double,
    val penalty: Double,
    val totalAmount: Double,
    val paymentStatus: String,
    val modeOfPayment: String,
    val createdAt: Long
)
