package com.selfgrowthfund.sgf.data.local.entities

import com.selfgrowthfund.sgf.model.enums.BorrowingStatus
import java.time.Instant
import java.time.LocalDate

data class BorrowingEntry(
    val shareholderId: String,
    val shareholderName: String,
    val applicationDate: LocalDate = LocalDate.now(),
    val amountRequested: Double,
    val borrowEligibility: Double = 0.0,
    val approvedAmount: Double = 0.0,
    val borrowStartDate: LocalDate = LocalDate.now(),
    val dueDate: LocalDate,
    val status: BorrowingStatus = BorrowingStatus.PENDING,
    val closedDate: LocalDate? = null,
    val notes: String? = null,
    val createdBy: String
) {
    fun toBorrowing(borrowId: String): Borrowing {
        return Borrowing(
            borrowId = borrowId,
            shareholderId = shareholderId,
            shareholderName = shareholderName,
            applicationDate = applicationDate,
            amountRequested = amountRequested,
            borrowEligibility = borrowEligibility,
            approvedAmount = approvedAmount,
            borrowStartDate = borrowStartDate,
            dueDate = dueDate,
            status = status,
            closedDate = closedDate,
            notes = notes,
            createdBy = createdBy,
            createdAt = Instant.now()
        )
    }
}