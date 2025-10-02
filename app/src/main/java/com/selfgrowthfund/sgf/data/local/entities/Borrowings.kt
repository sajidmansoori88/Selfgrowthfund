package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.selfgrowthfund.sgf.model.enums.ApprovalStage
import com.selfgrowthfund.sgf.model.enums.BorrowingStatus
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity(tableName = "borrowings")
data class Borrowing(
    @PrimaryKey
    val borrowId: String = UUID.randomUUID().toString(),

    val shareholderId: String,
    val shareholderName: String,

    val applicationDate: LocalDate,   // via converter
    val amountRequested: Double,

    val borrowEligibility: Double,
    val approvedAmount: Double,

    val borrowStartDate: LocalDate,   // via converter
    val dueDate: LocalDate,           // via converter

    // --- Loan Lifecycle ---
    val status: BorrowingStatus = BorrowingStatus.PENDING,
    val closedDate: LocalDate? = null, // via converter

    // --- Approval Workflow ---
    val approvalStatus: ApprovalStage = ApprovalStage.PENDING,
    val approvedBy: String? = null,
    val notes: String? = null,

    // --- Metadata ---
    val createdBy: String,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {

    constructor(
        shareholderId: String,
        shareholderName: String,
        applicationDate: LocalDate,
        amountRequested: Double,
        borrowEligibility: Double,
        approvedAmount: Double,
        borrowStartDate: LocalDate,
        createdBy: String,
        notes: String? = null
    ) : this(
        borrowId = UUID.randomUUID().toString(),
        shareholderId = shareholderId,
        shareholderName = shareholderName,
        applicationDate = applicationDate,
        amountRequested = amountRequested,
        borrowEligibility = borrowEligibility,
        approvedAmount = minOf(approvedAmount, borrowEligibility),
        borrowStartDate = borrowStartDate,
        dueDate = calculateDueDate(borrowStartDate),
        status = BorrowingStatus.PENDING,
        closedDate = null,
        approvalStatus = ApprovalStage.PENDING,
        approvedBy = null,
        notes = notes,
        createdBy = createdBy,
        createdAt = Instant.now(),
        updatedAt = Instant.now()
    )

    companion object {
        fun calculateDueDate(startDate: LocalDate): LocalDate = startDate.plusDays(45)
        fun calculateEligibility(shareholderAmount: Double): Double = shareholderAmount * 0.9
    }

    // --- Validation ---
    fun validate(): Boolean {
        return approvedAmount <= borrowEligibility &&
                amountRequested > 0 &&
                !borrowStartDate.isBefore(applicationDate)
    }

    // --- Lifecycle Checks ---
    fun isPending(): Boolean = status == BorrowingStatus.PENDING
    fun isApproved(): Boolean = status == BorrowingStatus.APPROVED
    fun isRejected(): Boolean = status == BorrowingStatus.REJECTED
    fun isActive(): Boolean = status == BorrowingStatus.ACTIVE
    fun isClosed(): Boolean = status.isClosed()

    fun isOverdue(today: LocalDate = LocalDate.now()): Boolean =
        status == BorrowingStatus.ACTIVE && today.isAfter(dueDate)
}
