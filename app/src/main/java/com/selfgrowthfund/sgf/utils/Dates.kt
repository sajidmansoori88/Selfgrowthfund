package com.selfgrowthfund.sgf.utils

import java.text.SimpleDateFormat
import java.util.*

object Dates {

    // Thread-safe formatters
    private val dueMonthFormat = ThreadLocal.withInitial {
        SimpleDateFormat("MMM-yyyy", Locale.US)
    }

    private val paymentDateFormat = ThreadLocal.withInitial {
        SimpleDateFormat("ddMMyyyy", Locale.US)
    }

    // Current timestamp
    fun now(): Date = Date()

    // Milliseconds conversion
    fun fromMillis(millis: Long): Date = Date(millis)
    fun toMillis(date: Date): Long = date.time

    // Format to "May-2023"
    fun formatToDueMonth(date: Date): String =
        dueMonthFormat.get().format(date)

    // Format to "05052023"
    fun formatToPaymentDate(date: Date): String =
        paymentDateFormat.get().format(date)

    // Parse from "May-2023"
    fun parseDueMonth(dateStr: String): Date? =
        runCatching { dueMonthFormat.get().parse(dateStr) }.getOrNull()

    // Parse from "05052023"
    fun parsePaymentDate(dateStr: String): Date? =
        runCatching { paymentDateFormat.get().parse(dateStr) }.getOrNull()

    // Days between two dates
    fun daysBetween(startDate: Date, endDate: Date): Int {
        val diff = endDate.time - startDate.time
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }

    // Return period calculation
    fun calculateReturnPeriod(investmentDate: Date, returnDate: Date): Int =
        daysBetween(investmentDate, returnDate)
}

// Optional: Extension functions for cleaner usage
fun Date.toDueMonth(): String = Dates.formatToDueMonth(this)
fun Date.toPaymentDate(): String = Dates.formatToPaymentDate(this)
fun Date.daysUntil(other: Date): Int = Dates.daysBetween(this, other)