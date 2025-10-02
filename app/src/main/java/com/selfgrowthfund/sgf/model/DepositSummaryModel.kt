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
    id = depositId ?: provisionalId,    // fallback if depositId is null
    name = shareholderName,
    month = dueMonth,                   // both non-null, safe
    amount = totalAmount,
    status = paymentStatus,
    timestamp = createdAt.toEpochMilli() // Instant is non-null, no safe call needed
)

