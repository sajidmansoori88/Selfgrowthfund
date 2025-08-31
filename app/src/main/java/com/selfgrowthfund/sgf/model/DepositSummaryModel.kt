package com.selfgrowthfund.sgf.model

import com.selfgrowthfund.sgf.data.local.dto.DepositEntrySummaryDTO

data class DepositSummaryModel(
    val id: String,
    val name: String,
    val month: String,
    val amount: Double,
    val status: String,
    val timestamp: Long
)

fun DepositEntrySummaryDTO.toDomain(): DepositSummaryModel = DepositSummaryModel(
    id = depositId,
    name = shareholderName,
    month = dueMonth,
    amount = totalAmount,
    status = paymentStatus,
    timestamp = createdAt?.toEpochMilli() ?: System.currentTimeMillis()
)
