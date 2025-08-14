package com.selfgrowthfund.sgf.utils

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
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

    // Current timestamp as LocalDateTime
    fun nowLocal(): LocalDateTime =
        LocalDateTime.ofInstant(now().toInstant(), ZoneId.systemDefault())

    // Milliseconds conversion
    fun fromMillis(millis: Long): Date = Date(millis)
    fun toMillis(date: Date): Long = date.time

    // Convert Date to LocalDateTime
    fun toLocalDateTime(date: Date): LocalDateTime =
        LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())

    // Convert LocalDateTime to Date
    fun toDate(localDateTime: LocalDateTime): Date =
        Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())

    // Format to "May-2023"
    fun formatToDueMonth(date: Date): String =
        dueMonthFormat.get()!!.format(date)

    // Format to "05052023"
    fun formatToPaymentDate(date: Date): String =
        paymentDateFormat.get()!!.format(date)

    // Parse from "May-2023"
    fun parseDueMonth(dateStr: String): Date? =
        runCatching { dueMonthFormat.get()!!.parse(dateStr) }.getOrNull()

    // Parse from "05052023"
    fun parsePaymentDate(dateStr: String): Date? =
        runCatching { paymentDateFormat.get()!!.parse(dateStr) }.getOrNull()

    // Days between two dates
    fun daysBetween(startDate: Date, endDate: Date): Int {
        val diff = endDate.time - startDate.time
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }

    // Return period calculation
    fun calculateReturnPeriod(investmentDate: Date, returnDate: Date): Int =
        daysBetween(investmentDate, returnDate)
}

