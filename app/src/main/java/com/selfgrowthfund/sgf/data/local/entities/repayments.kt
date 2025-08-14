package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.selfgrowthfund.sgf.data.local.types.PaymentMode
import com.selfgrowthfund.sgf.data.local.converters.AppTypeConverters
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Entity(
    tableName = "repayments",
    indices = [Index("borrowId")]
)
@TypeConverters(AppTypeConverters::class)
data class Repayment(
    @PrimaryKey
    @ColumnInfo(name = "repaymentId")
    val repaymentId: String = generateRepaymentId(),

    @ColumnInfo(name = "borrowId")
    val borrowId: String,

    @ColumnInfo(name = "shareholderName")
    val shareholderName: String,

    @ColumnInfo(name = "outstandingBefore")
    val outstandingBefore: Double,

    @ColumnInfo(name = "penaltyDue")
    val penaltyDue: Double,

    @ColumnInfo(name = "repaymentDate")
    val repaymentDate: LocalDateTime,

    @ColumnInfo(name = "principalRepaid")
    val principalRepaid: Double,

    @ColumnInfo(name = "penaltyPaid")
    val penaltyPaid: Double,

    @ColumnInfo(name = "totalAmountPaid")
    val totalAmountPaid: Double = principalRepaid + penaltyPaid,

    @ColumnInfo(name = "modeOfPayment")
    val modeOfPayment: PaymentMode, // ✅ Fixed type

    @ColumnInfo(name = "finalOutstanding")
    val finalOutstanding: Double = outstandingBefore - principalRepaid,

    @ColumnInfo(name = "borrowingStatus")
    val borrowingStatus: String = if (finalOutstanding <= 0.01) "CLOSED" else "ACTIVE",

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "penaltyCalculationNotes")
    val penaltyCalculationNotes: String? = null
) {
    companion object {
        private var lastId = 0
        private val idFormatter = DateTimeFormatter.ofPattern("ddMMyyyy")

        fun generateRepaymentId(): String {
            lastId++
            return "RP${LocalDateTime.now().format(idFormatter)}-${"%04d".format(lastId)}"
        }

        fun create(
            borrowId: String,
            shareholderName: String,
            outstandingBefore: Double,
            repaymentDate: LocalDateTime,
            principalRepaid: Double,
            penaltyPaid: Double,
            modeOfPayment: PaymentMode, // ✅ Corrected
            borrowStartDate: LocalDateTime,
            dueDate: LocalDateTime,
            previousRepayments: List<Repayment>
        ): Repayment {
            val (penaltyAmount, notes) = calculatePenalty(
                borrowStartDate,
                dueDate,
                repaymentDate,
                outstandingBefore,
                previousRepayments
            )

            return Repayment(
                borrowId = borrowId,
                shareholderName = shareholderName,
                outstandingBefore = outstandingBefore,
                penaltyDue = penaltyAmount,
                repaymentDate = repaymentDate,
                principalRepaid = principalRepaid,
                penaltyPaid = penaltyPaid,
                modeOfPayment = modeOfPayment, // ✅ Corrected
                notes = "Processed on ${formatDate(LocalDateTime.now())}",
                penaltyCalculationNotes = notes
            )
        }

        private fun calculatePenalty(
            borrowStartDate: LocalDateTime,
            dueDate: LocalDateTime,
            repaymentDate: LocalDateTime,
            outstandingBefore: Double,
            previousRepayments: List<Repayment>
        ): Pair<Double, String> {
            val gracePeriodEnd = dueDate.plusDays(45)

            if (repaymentDate.isBefore(gracePeriodEnd)) {
                return 0.0 to "No penalty (within grace period until ${formatDate(gracePeriodEnd)})"
            }

            val daysLate = ChronoUnit.DAYS.between(dueDate, repaymentDate).toInt()
            val monthsLate = daysLate / 30
            val penalty = outstandingBefore * 0.01 * monthsLate

            return penalty to "1% monthly penalty for $monthsLate month(s) late ($daysLate days)"
        }

        private fun formatDate(date: LocalDateTime): String {
            return date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
        }
    }
}
