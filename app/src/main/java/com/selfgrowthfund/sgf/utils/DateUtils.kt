package com.selfgrowthfund.sgf.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DateUtils {
    private const val DUE_DATE_FORMAT = "ddMMMyyyy"
    private const val PAYMENT_DATE_FORMAT = "ddMMyyyy"
    private const val DISPLAY_DATE_FORMAT = "dd MMM yyyy"

    fun addDays(date: Date, days: Int): Date {
        val calendar = Calendar.getInstance().apply {
            time = date
            add(Calendar.DAY_OF_YEAR, days)
        }
        return calendar.time
    }

    fun parseDueMonth(monthYear: String): Pair<String, Int> {
        val (month, year) = monthYear.split("-")
        require(year.toIntOrNull() != null) { "Invalid year format" }
        return month to year.toInt()
    }

    fun isLatePayment(dueMonth: String, paymentDate: String): Boolean {
        val dueDate = "10${dueMonth.replace("-", "")}" // e.g., May-2025 → 10May2025
        return try {
            val due = SimpleDateFormat(DUE_DATE_FORMAT, Locale.US).parse(dueDate)
                ?: throw IllegalArgumentException("Invalid due date format")
            val payment = SimpleDateFormat(PAYMENT_DATE_FORMAT, Locale.US).parse(paymentDate)
                ?: throw IllegalArgumentException("Invalid payment date format")

            payment.after(due)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid date format")
        }
    }

    fun calculateDaysLate(dueMonth: String, paymentDate: String): Int {
        val dueDate = "10${dueMonth.replace("-", "")}"
        return try {
            val due = SimpleDateFormat(DUE_DATE_FORMAT, Locale.US).parse(dueDate)
                ?: throw IllegalArgumentException("Invalid due date format")
            val payment = SimpleDateFormat(PAYMENT_DATE_FORMAT, Locale.US).parse(paymentDate)
                ?: throw IllegalArgumentException("Invalid payment date format")

            TimeUnit.MILLISECONDS.toDays(payment.time - due.time).toInt()
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid date format")
        }
    }

    fun formatForDisplay(date: Date): String {
        return SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.US).format(date)
    }

    fun daysBetween(startDate: Date, endDate: Date): Long {
        return TimeUnit.MILLISECONDS.toDays(endDate.time - startDate.time)
    }
}
