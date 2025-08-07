package com.selfgrowthfund.sgf.utils

import java.text.SimpleDateFormat
import java.util.*

object Dates {
    // For current timestamp
    fun now() = Date()

    // For conversion (if storing milliseconds in DB)
    fun fromMillis(millis: Long) = Date(millis)
    fun toMillis(date: Date) = date.time

    // For your MMM-YYYY format (May-2023)
    fun formatToDueMonth(date: Date): String {
        return SimpleDateFormat("MMM-yyyy", Locale.US).format(date)
    }

    // For your DDMMYYYY format (05052023)
    fun formatToPaymentDate(date: Date): String {
        return SimpleDateFormat("ddMMyyyy", Locale.US).format(date)
    }

    // Add this for easier date calculations
    fun daysBetween(startDate: Date, endDate: Date): Int {
        val diff = endDate.time - startDate.time
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }

    // Add this for your actual return period calculation
    fun calculateReturnPeriod(investmentDate: Date, returnDate: Date): Int {
        return daysBetween(investmentDate, returnDate)
    }
}