package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "deposits")
data class Deposit(
    @PrimaryKey
    val depositId: String, // Format: D0001, D0002 etc.

    val shareholderId: String,
    val shareholderName: String,

    val dueMonth: String, // Format: MMM-YYYY (May-2025)
    val paymentDate: String, // Format: DDMMYYYY

    val shareNos: Int,
    val shareAmount: Double = 2000.0, // Fixed Rs.2000/share
    val additionalContribution: Double = 0.0,
    val penalty: Double = 0.0, // Rs.5/day after 10th

    val totalAmount: Double, // Calculated: (shareNos*2000) + penalty + additionalContribution

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
                val num = it.removePrefix("D").toInt()
                "D%04d".format(num + 1)
            } ?: "D0001"
        }

        fun calculatePenalty(dueMonth: String, paymentDate: String): Double {
            val (month, year) = dueMonth.split("-")
            val dueDate = "10${month.take(3)}${year}" // 10May2025

            return if (isLatePayment(dueDate, paymentDate)) {
                val daysLate = calculateDaysLate(dueDate, paymentDate)
                daysLate * 5.0 // Rs.5 per day
            } else {
                0.0
            }
        }

        private fun isLatePayment(dueDate: String, paymentDate: String): Boolean {
            // Implement date comparison logic
            return true // Placeholder
        }

        private fun calculateDaysLate(dueDate: String, paymentDate: String): Int {
            // Implement days calculation
            return 0 // Placeholder
        }
    }
}