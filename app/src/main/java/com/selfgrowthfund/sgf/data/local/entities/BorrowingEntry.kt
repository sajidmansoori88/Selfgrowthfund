package com.selfgrowthfund.sgf.data.local.entities

import com.selfgrowthfund.sgf.model.enums.BorrowingStatus
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class BorrowingEntry(
    val shareholderId: String,
    val shareholderName: String,
    val amountRequested: Double,
    val borrowEligibility: Double = 0.0,
    val approvedAmount: Double = 0.0,
    val borrowStartDate: LocalDate = LocalDate.now(),
    val notes: String? = null,
    val createdBy: String,
    val applicationDate: LocalDate = LocalDate.now(),
) {

    fun toProvisionalBorrowing(): Borrowing {
        val start = borrowStartDate
        val due = Borrowing.calculateDueDate(start)

        return Borrowing(
            provisionalId = UUID.randomUUID().toString(),   // ✅ auto provisional ID
            borrowId = null,                                 // ✅ stays null until approval
            shareholderId = shareholderId,
            shareholderName = shareholderName,
            applicationDate = applicationDate,
            amountRequested = amountRequested,
            borrowEligibility = borrowEligibility,
            approvedAmount = approvedAmount,
            borrowStartDate = start,
            dueDate = due,
            status = BorrowingStatus.PENDING,
            closedDate = null,
            approvalStatus = com.selfgrowthfund.sgf.model.enums.ApprovalStage.PENDING,
            approvedBy = null,
            notes = notes,
            createdBy = createdBy,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }
}
