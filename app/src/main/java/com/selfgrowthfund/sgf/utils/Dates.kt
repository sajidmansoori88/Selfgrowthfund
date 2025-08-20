package com.selfgrowthfund.sgf.utils

import android.os.Build
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

class Dates @Inject constructor() {

    private val fallbackFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    fun format(localDate: LocalDate): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            localDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        } else {
            val parts = localDate.toString().split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt() - 1
            val day = parts[2].toInt()
            val date = GregorianCalendar(year, month, day).time
            fallbackFormatter.format(date)
        }
    }

    fun format(date: Date): String {
        return fallbackFormatter.format(date)
    }

    fun format(timestamp: Long): String {
        return format(Date(timestamp))
    }

    fun now(): Long = System.currentTimeMillis()

    fun nowDate(): Date = Date(now())
}