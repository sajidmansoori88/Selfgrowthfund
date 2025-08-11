package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity(
    tableName = "deposits",
    indices = [Index(value = ["shareholderId"]), Index(value = ["dueMonth"])]
)
data class Deposit(
    @PrimaryKey
    val depositId: String, // Format: D0001, D0002, etc.

    val shareholderId: String,
    val shareholderName: String,

    val dueMonth: String, // Format: MMM-yyyy (e.g., "Aug-2025")
    val paymentDate: String, // Format: dd/MM/yyyy (e.g., "11/08/2025")

    val shareNos: Int,
    val shareAmount: Double = 2000.0, // Fixed Rs.2000/share
    val additionalContribution: Double = 0.0,
    val penalty: Double = 0.0, // Rs.5/day after 10th

    val totalAmount: Double, // Calculated: (shareNos * shareAmount) + penalty + additionalContribution

    val paymentStatus: String, // "On-time" or "Late"
    val modeOfPayment: String, // "Cash" or "Online"

    val createdAt: Long = System.currentTimeMillis()
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

        fun calculatePenalty(dueMonth: String, paymentDate: String): Double {
            return try {
                val dueFormatter = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH)
                val payFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

                val dueDate = dueFormatter.parse("10-$dueMonth")
                val actualDate = payFormatter.parse(paymentDate)

                if (actualDate != null && dueDate != null && actualDate.after(dueDate)) {
                    val diffMillis = actualDate.time - dueDate.time
                    val daysLate = (diffMillis / (1000 * 60 * 60 * 24)).toInt()
                    daysLate * 5.0
                } else {
                    0.0
                }
            } catch (e: Exception) {
                0.0 // Graceful fallback for invalid input
            }
        }
    }
}