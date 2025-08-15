package com.selfgrowthfund.sgf.utils

import android.os.Build
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

class Dates @Inject constructor() {

    private val fallbackFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun format(localDate: LocalDate): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            localDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        } else {
            // Avoid calling LocalDate methods directly on API < 26
            val parts = localDate.toString().split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt() - 1 // Calendar months are 0-based
            val day = parts[2].toInt()

            val date = GregorianCalendar(year, month, day).time
            fallbackFormatter.format(date)
        }
    }

    fun format(timestamp: Long): String {
        val date = Date(timestamp) // Convert Long to Date
        return fallbackFormatter.format(date)
    }

    fun now(): Long = System.currentTimeMillis()

    fun nowDate(): Date = Date(now()) // ✅ Added for Date-based default values
}