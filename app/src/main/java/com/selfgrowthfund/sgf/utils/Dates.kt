package com.selfgrowthfund.sgf.utils

import com.selfgrowthfund.sgf.data.local.types.DueMonth
import java.text.SimpleDateFormat
import java.util.*
import java.time.LocalDateTime
import java.time.ZoneId

object Dates {

    private val dueMonthFormat = ThreadLocal.withInitial {
        SimpleDateFormat("MMM-yyyy", Locale.US)
    }

    private val paymentDateFormat = ThreadLocal.withInitial {
        SimpleDateFormat("ddMMyyyy", Locale.US)
    }

    fun now(): Date = Date()
    fun fromMillis(millis: Long): Date = Date(millis)
    fun toMillis(date: Date): Long = date.time

    fun formatToDueMonth(date: Date): String = dueMonthFormat.get()!!.format(date)
    fun formatToPaymentDate(date: Date): String = paymentDateFormat.get()!!.format(date)

    fun parseDueMonth(dateStr: String): Date? =
        runCatching { dueMonthFormat.get()!!.parse(dateStr) }.getOrNull()

    fun parsePaymentDate(dateStr: String): Date? =
        runCatching { paymentDateFormat.get()!!.parse(dateStr) }.getOrNull()

    fun daysBetween(startDate: DueMonth, endDate: Date): Int =
        ((endDate.time - startDate.time) / (1000 * 60 * 60 * 24)).toInt()

    fun calculateReturnPeriod(investmentDate: Date, returnDate: Date): Int =
        daysBetween(investmentDate, returnDate)

    fun toLocalDateTime(date: Date): LocalDateTime =
        LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
}

fun Date.toDueMonth(): String = Dates.formatToDueMonth(this)
fun Date.toPaymentDate(): String = Dates.formatToPaymentDate(this)
fun Date.daysUntil(other: Date): Int = Dates.daysBetween(this, other)
fun Date.toLocalDateTime(): LocalDateTime = Dates.toLocalDateTime(this)
