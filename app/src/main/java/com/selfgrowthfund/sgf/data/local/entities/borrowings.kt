package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.selfgrowthfund.sgf.data.local.types.BorrowingStatus
import java.time.LocalDateTime

@Entity(tableName = "borrowings")
data class Borrowing(
    @PrimaryKey
    @ColumnInfo(name = "borrowId")
    val borrowId: String,

    @ColumnInfo(name = "shareholderId")
    val shareholderId: String,

    @ColumnInfo(name = "shareholderName")
    val shareholderName: String,

    @ColumnInfo(name = "applicationDate")
    val applicationDate: LocalDateTime,

    @ColumnInfo(name = "amountRequested")
    val amountRequested: Double,

    @ColumnInfo(name = "consentingMember1Id")
    val consentingMember1Id: String?,

    @ColumnInfo(name = "consentingMember1Name")
    val consentingMember1Name: String?,

    @ColumnInfo(name = "consentingMember2Id")
    val consentingMember2Id: String?,

    @ColumnInfo(name = "consentingMember2Name")
    val consentingMember2Name: String?,

    @ColumnInfo(name = "borrowEligibility")
    val borrowEligibility: Double,

    @ColumnInfo(name = "approvedAmount")
    val approvedAmount: Double,

    @ColumnInfo(name = "borrowStartDate")
    val borrowStartDate: LocalDateTime,

    @ColumnInfo(name = "dueDate")
    val dueDate: LocalDateTime, // borrowStartDate + 45 days

    @ColumnInfo(name = "status")
    val status: BorrowingStatus = BorrowingStatus.PENDING,

    @ColumnInfo(name = "closedDate")
    val closedDate: LocalDateTime? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "createdBy")
    val createdBy: String,

    @ColumnInfo(name = "createdAt")
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    constructor(
        shareholderId: String,
        shareholderName: String,
        applicationDate: LocalDateTime,
        amountRequested: Double,
        consentingMember1Id: String?,
        consentingMember1Name: String?,
        consentingMember2Id: String?,
        consentingMember2Name: String?,
        borrowEligibility: Double,
        approvedAmount: Double,
        borrowStartDate: LocalDateTime,
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
        closedDate = null,
        notes = notes,
        createdBy = createdBy
    )

    companion object {
        fun calculateDueDate(startDate: LocalDateTime): LocalDateTime =
            startDate.plusDays(45)

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
                !borrowStartDate.isBefore(applicationDate)
    }

    fun isActive(): Boolean = status == BorrowingStatus.ACTIVE
    fun isPending(): Boolean = status == BorrowingStatus.PENDING
    fun isCompleted(): Boolean = status == BorrowingStatus.COMPLETED
    fun isRejected(): Boolean = status == BorrowingStatus.REJECTED
}
