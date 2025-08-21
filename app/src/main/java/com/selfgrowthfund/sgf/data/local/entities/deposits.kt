package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.selfgrowthfund.sgf.data.local.converters.AppTypeConverters
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit
import org.threeten.bp.Instant

@Entity(
    tableName = "deposits",
    indices = [Index(value = ["shareholderId"]), Index(value = ["dueMonth"])]
)
@TypeConverters(AppTypeConverters::class)
data class Deposit(
    @PrimaryKey
    val depositId: String,

    val shareholderId: String,
    val shareholderName: String,

    val dueMonth: String, // Format: MMM-yyyy (e.g., "Aug-2025")
    val paymentDate: LocalDate, // Actual payment date

    val shareNos: Int,
    val shareAmount: Double = 2000.0,
    val additionalContribution: Double = 0.0,
    val penalty: Double = 0.0,

    val totalAmount: Double,
    val paymentStatus: String,
    val modeOfPayment: String,

    val createdAt: Instant = Instant.now()
) {
    companion object {
        const val PAYMENT_ON_TIME = "On-time"
        const val PAYMENT_LATE = "Late"
        const val MODE_CASH = "Cash"
        const val MODE_ONLINE = "Online"

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
    }
}