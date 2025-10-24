package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.types.DueMonth
import com.selfgrowthfund.sgf.model.enums.*
import com.selfgrowthfund.sgf.data.local.converters.AppTypeConverters
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID

@Entity(
    tableName = "deposits",
    indices = [
        Index(value = ["shareholderId"], name = "index_deposits_shareholderId"),
        Index(value = ["dueMonth"], name = "index_deposits_dueMonth")
    ]
)
@TypeConverters(AppTypeConverters::class)
data class Deposit(

    // --- Primary Keys ---
    @PrimaryKey(autoGenerate = false)
    val provisionalId: String = UUID.randomUUID().toString(), // always generated locally
    val depositId: String? = null, // permanent ID (set after Admin approval)

    // --- Shareholder Info ---
    val shareholderId: String,
    val shareholderName: String,

    // --- Payment Details ---
    val dueMonth: DueMonth,                  // e.g., "Aug-2025"
    val paymentDate: LocalDate,              // via converter
    val shareNos: Int,
    val shareAmount: Double = 2000.0,
    val additionalContribution: Double = 0.0,
    val penalty: Double = 0.0,
    val totalAmount: Double,
    val paymentStatus: PaymentStatus,        // enum → converter
    val modeOfPayment: PaymentMode?,         // enum → converter

    // --- Approval Workflow ---
    val approvalStatus: ApprovalStage = ApprovalStage.PENDING, // 👈 multi-stage workflow
    val approvedBy: String? = null,          // Admin/Treasurer ID
    val notes: String? = null,               // rejection reason, comments, etc.

    // --- Metadata ---
    val isSynced: Boolean = false,           // for offline sync
    val entrySource: EntrySource = EntrySource.User, // who entered: USER, TREASURER, ADMIN, SYSTEM
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
) {
    companion object {
        fun generateNextId(lastId: String?): String {
            return lastId?.let {
                val num = it.removePrefix("D").toIntOrNull() ?: 0
                "D%04d".format(num + 1)
            } ?: "D0001"
        }

        fun calculatePenalty(dueMonth: String, paymentDate: LocalDate): Double {
            return try {
                val formatter = DateTimeFormatter.ofPattern("MMM-yyyy")
                val dueDate = LocalDate.parse(dueMonth, formatter).withDayOfMonth(10)

                if (paymentDate.isAfter(dueDate)) {
                    val daysLate = ChronoUnit.DAYS.between(dueDate, paymentDate).toInt()
                    daysLate * 5.0
                } else {
                    0.0
                }
            } catch (e: Exception) {
                0.0
            }
        }

        fun calculateTotalAmount(
            shareNos: Int,
            shareAmount: Double,
            additionalContribution: Double,
            penalty: Double
        ): Double {
            return (shareNos * shareAmount) + additionalContribution + penalty
        }
    }
}
