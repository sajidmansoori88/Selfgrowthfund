package com.selfgrowthfund.sgf.utils.mappers

import com.selfgrowthfund.sgf.data.local.dto.DepositEntrySummaryDTO
import com.selfgrowthfund.sgf.model.DepositSummaryModel

fun DepositEntrySummaryDTO.toDomain(): DepositSummaryModel = DepositSummaryModel(
    id = depositId ?: provisionalId,   // ✅ ensures non-null
    name = shareholderName,
    month = dueMonth,
    amount = totalAmount,
    status = paymentStatus,
    timestamp = createdAt.toEpochMilli()
)
