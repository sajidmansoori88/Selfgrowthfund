package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Entity(tableName = "repayments")
data class Repayment(
    @PrimaryKey
    @ColumnInfo(name = "repaymentId")
    val repaymentId: String = generateRepaymentId(),

    @ColumnInfo(name = "borrowId", index = true)
    val borrowId: String,

    @ColumnInfo(name = "shareholderName")
    val shareholderName: String,

    @ColumnInfo(name = "outstandingBefore")
    val outstandingBefore: Double,

    @ColumnInfo(name = "penaltyDue")
    val penaltyDue: Double,

    @ColumnInfo(name = "repaymentDate")
    val repaymentDate: Date,

    @ColumnInfo(name = "principalRepaid")
    val principalRepaid: Double,

    @ColumnInfo(name = "penaltyPaid")
    val penaltyPaid: Double,

    @ColumnInfo(name = "totalAmountPaid")
    val totalAmountPaid: Double = principalRepaid + penaltyPaid,

    @ColumnInfo(name = "modeOfPayment")
    val modeOfPayment: String,

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
        private val idFormat = SimpleDateFormat("ddMMyyyy", Locale.US)

        fun generateRepaymentId(): String {
            lastId++
            return "RP${idFormat.format(Date())}-${"%04d".format(lastId)}"
        }

        fun create(
            borrowId: String,
            shareholderName: String,
            outstandingBefore: Double,
            repaymentDate: Date,
            principalRepaid: Double,
            penaltyPaid: Double,
            modeOfPayment: String,
            borrowStartDate: Date,
            dueDate: Date,
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
                modeOfPayment = modeOfPayment,
                notes = "Processed on ${formatDate(Date())}",
                penaltyCalculationNotes = notes
            )
        }

        private fun calculatePenalty(
            borrowStartDate: Date,
            dueDate: Date,
            repaymentDate: Date,
            outstandingBefore: Double,
            previousRepayments: List<Repayment>
        ): Pair<Double, String> {
            val gracePeriodEnd = addDays(dueDate, 45)

            if (repaymentDate.before(gracePeriodEnd)) {
                return 0.0 to "No penalty (within grace period until ${formatDate(gracePeriodEnd)})"
            }

            val daysLate = calculateDaysBetween(dueDate, repaymentDate)
            val monthsLate = daysLate / 30
            val penalty = outstandingBefore * 0.01 * monthsLate

            return penalty to "1% monthly penalty for $monthsLate month(s) late ($daysLate days)"
        }

        // Helper functions
        private fun addDays(date: Date, days: Int): Date {
            return Calendar.getInstance().apply {
                time = date
                add(Calendar.DAY_OF_YEAR, days)
            }.time
        }

        private fun calculateDaysBetween(startDate: Date, endDate: Date): Int {
            return TimeUnit.MILLISECONDS.toDays(endDate.time - startDate.time).toInt()
        }

        private fun formatDate(date: Date): String {
            return SimpleDateFormat("dd MMM yyyy", Locale.US).format(date)
        }
    }
}
