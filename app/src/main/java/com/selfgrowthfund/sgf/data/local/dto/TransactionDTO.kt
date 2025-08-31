package com.selfgrowthfund.sgf.data.local.dto

import com.selfgrowthfund.sgf.model.enums.TransactionType
import java.time.LocalDate

data class TransactionDTO(
    val transactionId: String,
    val shareholderId: String,
    val type: TransactionType,
    val amount: Double,
    val date: LocalDate,
    val description: String = "",
    val createdBy: String = "",
    val createdAt: LocalDate = LocalDate.now()
)