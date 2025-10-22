package com.selfgrowthfund.sgf.data.local.dto

import java.time.LocalDate

data class MemberBorrowingStatus(
    val shareholderId: String,
    val shareholderName: String,
    val totalBorrowed: Double,
    val totalRepaid: Double,
    val nextDueDate: String?,
) {
    val outstanding: Double get() = totalBorrowed - totalRepaid
    val isOverdue: Boolean get() = nextDueDate?.let {
        LocalDate.parse(it).isBefore(LocalDate.now())
    } ?: false
}
