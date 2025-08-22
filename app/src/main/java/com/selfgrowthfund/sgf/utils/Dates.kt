package com.selfgrowthfund.sgf.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object Dates {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    private val timestampFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")
        .withZone(ZoneId.systemDefault())

    fun format(date: LocalDate?): String =
        date?.format(dateFormatter) ?: ""

    fun format(instant: Instant?): String =
        instant?.let { timestampFormatter.format(it) } ?: ""

    fun now(): Long = System.currentTimeMillis()

    fun today(): LocalDate = LocalDate.now()
}