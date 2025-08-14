package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.selfgrowthfund.sgf.data.local.types.DueMonth
import com.selfgrowthfund.sgf.utils.Dates
import java.util.Date

@Entity(tableName = "deposits")
data class Deposit(
    @PrimaryKey val depositId: String,
    val shareholderId: String,
    val shareholderName: String,
    val dueMonth: DueMonth,
    val paymentDate: String,
    val shareNos: Int,
    val additionalContribution: Double,
    val penalty: Double,
    val totalAmount: Double,
    val paymentStatus: String,
    val modeOfPayment: String,
    val createdAt: Date = Date()
) {
    companion object {
        const val PAYMENT_ON_TIME = "On Time"
        const val PAYMENT_LATE = "Late"

        fun generateNextId(lastId: String?): String {
            return ((lastId?.toIntOrNull() ?: (0 + 1))).toString()
        }

        fun calculatePenalty(dueMonth: DueMonth, paymentDate: Date): Double {
            val dueMonthDate = dueMonth
            val daysLate = Dates.daysBetween(dueMonthDate, paymentDate).coerceAtLeast(0)
            return daysLate * 10.0
        }
    }
}
