package com.selfgrowthfund.sgf.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DateUtils {
    private const val DUE_DATE_FORMAT = "dd-MMM-yyyy"       // e.g., "10-Aug-2025"
    private const val PAYMENT_DATE_FORMAT = "dd/MM/yyyy"    // e.g., "11/08/2025"
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
        val dueDateStr = "10-$dueMonth" // e.g., "10-Aug-2025"
        return try {
            val due = SimpleDateFormat(DUE_DATE_FORMAT, Locale.ENGLISH).parse(dueDateStr)
                ?: throw IllegalArgumentException("Invalid due date format")
            val payment = SimpleDateFormat(PAYMENT_DATE_FORMAT, Locale.ENGLISH).parse(paymentDate)
                ?: throw IllegalArgumentException("Invalid payment date format")

            payment.after(due)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid date format")
        }
    }

    fun calculateDaysLate(dueMonth: String, paymentDate: String): Int {
        val dueDateStr = "10-$dueMonth"
        return try {
            val due = SimpleDateFormat(DUE_DATE_FORMAT, Locale.ENGLISH).parse(dueDateStr)
                ?: throw IllegalArgumentException("Invalid due date format")
            val payment = SimpleDateFormat(PAYMENT_DATE_FORMAT, Locale.ENGLISH).parse(paymentDate)
                ?: throw IllegalArgumentException("Invalid payment date format")

            TimeUnit.MILLISECONDS.toDays(payment.time - due.time).toInt()
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid date format")
        }
    }

    fun formatForDisplay(date: Date): String {
        return SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.ENGLISH).format(date)
    }

    fun daysBetween(startDate: Date, endDate: Date): Long {
        return TimeUnit.MILLISECONDS.toDays(endDate.time - startDate.time)
    }

    fun getCurrentMonthFormatted(): String {
        val formatter = SimpleDateFormat("MMM-yyyy", Locale.ENGLISH)
        return formatter.format(Date())
    }

    fun generateSelectableDueMonths(monthCount: Int = 12): List<String> {
        val formatter = SimpleDateFormat("MMM-yyyy", Locale.ENGLISH)
        val calendar = Calendar.getInstance()
        val months = mutableListOf<String>()

        repeat(monthCount) {
            months.add(formatter.format(calendar.time))
            calendar.add(Calendar.MONTH, -1)
        }

        return months
    }
}