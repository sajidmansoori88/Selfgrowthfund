package com.selfgrowthfund.sgf.utils

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object DateUtils {

    private val formatterDueDate: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH)       // e.g., "10-Aug-2025"
    val formatterPaymentDate: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.getDefault()) // e.g., "11-08-2025"
    private val formatterDisplayDate: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)
    private val formatterMonthYear: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM-yyyy", Locale.ENGLISH)
    private val formatterMonthYearNumeric: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-yyyy", Locale.getDefault())

    fun addDays(date: LocalDate, days: Long): LocalDate = date.plusDays(days)

    fun parseDueMonth(monthYear: String): Pair<String, Int> {
        val (month, year) = monthYear.split("-")
        require(year.toIntOrNull() != null) { "Invalid year format" }
        return month to year.toInt()
    }

    fun isLatePayment(dueMonth: String, paymentDate: String): Boolean {
        return try {
            val dueDate = LocalDate.parse("10-$dueMonth", formatterDueDate)
            val payment = LocalDate.parse(paymentDate, formatterPaymentDate)
            payment.isAfter(dueDate)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid date format")
        }
    }

    fun calculateDaysLate(dueMonth: String, paymentDate: String): Int {
        return try {
            val dueDate = LocalDate.parse("10-$dueMonth", formatterDueDate)
            val payment = LocalDate.parse(paymentDate, formatterPaymentDate)
            ChronoUnit.DAYS.between(dueDate, payment).toInt()
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid date format")
        }
    }

    fun formatForDisplay(date: LocalDate): String {
        return date.format(formatterDisplayDate)
    }

    fun daysBetween(startDate: LocalDate, endDate: LocalDate): Long {
        return ChronoUnit.DAYS.between(startDate, endDate)
    }

    fun getCurrentMonthFormatted(): String {
        return YearMonth.now().format(formatterMonthYear)
    }

    fun generateSelectableDueMonths(monthCount: Int = 5): List<String> {
        val now = YearMonth.now()
        return (0 until monthCount).map { offset ->
            now.minusMonths(offset.toLong()).format(formatterMonthYear)
        }
    }

    fun generateMonthOptions(): List<String> {
        val now = YearMonth.now()
        return (0..5).map { offset ->
            now.plusMonths(offset.toLong()).format(formatterMonthYearNumeric)
        }
    }

    fun now(): Long = Instant.now().toEpochMilli()
}