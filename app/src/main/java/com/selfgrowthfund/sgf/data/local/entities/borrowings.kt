package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "borrowings")
data class Borrowing(
    @PrimaryKey val borrowId: String,
    val shareholderId: String,
    val shareholderName: String,
    val applicationDate: Date,
    val amountRequested: Double,
    val consentingMember1Id: String?,
    val consentingMember1Name: String?,
    val consentingMember2Id: String?,
    val consentingMember2Name: String?,
    val borrowEligibility: Double,
    val approvedAmount: Double,
    val borrowStartDate: Date,
    val dueDate: Date, // borrowStartDate + 45 days
    val status: String = "Pending",
    val closedDate: Date? = null, // Added field to track when borrowing was closed
    val notes: String? = null,
    val createdBy: String,
    val createdAt: Date = Date()
) {
    // Secondary constructor for new entries
    constructor(
        shareholderId: String,
        shareholderName: String,
        applicationDate: Date,
        amountRequested: Double,
        consentingMember1Id: String?,
        consentingMember1Name: String?,
        consentingMember2Id: String?,
        consentingMember2Name: String?,
        borrowEligibility: Double,
        approvedAmount: Double,
        borrowStartDate: Date,
        createdBy: String,
        notes: String? = null
    ) : this(
        borrowId = "",
        shareholderId = shareholderId,
        shareholderName = shareholderName,
        applicationDate = applicationDate,
        amountRequested = amountRequested,
        consentingMember1Id = consentingMember1Id,
        consentingMember1Name = consentingMember1Name,
        consentingMember2Id = consentingMember2Id,
        consentingMember2Name = consentingMember2Name,
        borrowEligibility = borrowEligibility,
        approvedAmount = minOf(approvedAmount, borrowEligibility),
        borrowStartDate = borrowStartDate,
        dueDate = calculateDueDate(borrowStartDate),
        status = BorrowingStatus.PENDING,
        closedDate = null, // Initially null for new entries
        notes = notes,
        createdBy = createdBy
    )

    companion object {
        private fun calculateDueDate(startDate: Date): Date {
            val calendar = Calendar.getInstance().apply {
                time = startDate
                add(Calendar.DAY_OF_YEAR, 45)
            }
            return calendar.time
        }

        fun calculateEligibility(
            shareholderAmount: Double,
            consentingMember1Amount: Double?,
            consentingMember2Amount: Double?
        ): Double {
            val base = shareholderAmount * 0.9
            val consenting = listOfNotNull(
                consentingMember1Amount?.times(0.9),
                consentingMember2Amount?.times(0.9)
            ).sum()
            return base + consenting
        }
    }

    fun validate(): Boolean {
        return approvedAmount <= borrowEligibility &&
                amountRequested > 0 &&
                !borrowStartDate.before(applicationDate)
    }

    fun isActive(): Boolean = status == BorrowingStatus.ACTIVE
    fun isPending(): Boolean = status == BorrowingStatus.PENDING
    fun isCompleted(): Boolean = status == BorrowingStatus.COMPLETED
    fun isRejected(): Boolean = status == BorrowingStatus.REJECTED
}

object BorrowingStatus {
    const val PENDING = "Pending"
    const val APPROVED = "Approved"
    const val REJECTED = "Rejected"
    const val ACTIVE = "Active"
    const val COMPLETED = "Completed"

    fun getAllStatuses(): List<String> = listOf(PENDING, APPROVED, REJECTED, ACTIVE, COMPLETED)
    fun getActiveStatuses(): List<String> = listOf(PENDING, APPROVED, ACTIVE)
    fun getClosedStatuses(): List<String> = listOf(COMPLETED, REJECTED)
}