package com.selfgrowthfund.sgf.data.local.entities

import  androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.selfgrowthfund.sgf.data.local.types.DueMonth
import com.selfgrowthfund.sgf.model.enums.*
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "deposits",
    indices = [Index(value = ["shareholderId"]), Index(value = ["dueMonth"])]
)
data class Deposit(
    @PrimaryKey
    val depositId: String,

    val shareholderId: String,
    val shareholderName: String,

    val dueMonth: DueMonth,               // e.g., "Aug-2025"
    val paymentDate: LocalDate,         // handled via converters

    val shareNos: Int,
    val shareAmount: Double = 2000.0,
    val additionalContribution: Double = 0.0,
    val penalty: Double = 0.0,

    val totalAmount: Double,
    val paymentStatus: PaymentStatus,   // enum → converter
    val modeOfPayment: PaymentMode,     // ✅ fixed (no Companion)
    val approvalStatus: ApprovalAction = ApprovalAction.PENDING, // enum → converter

    val createdAt: Instant = Instant.now() // handled via converters
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
                val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM-yyyy")
                val dueDate = LocalDate.parse(dueMonth, formatter).withDayOfMonth(10)

                if (paymentDate.isAfter(dueDate)) {
                    val daysLate = java.time.temporal.ChronoUnit.DAYS.between(dueDate, paymentDate).toInt()
                    daysLate * 5.0
                } else {
                    0.0
                }
            } catch (e: Exception) {
                0.0
            }
        }
    }
}
