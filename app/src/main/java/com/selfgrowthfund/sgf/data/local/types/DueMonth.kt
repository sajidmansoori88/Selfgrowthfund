package com.selfgrowthfund.sgf.data.local.types

import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Immutable class representing a due month with year.
 */
@ConsistentCopyVisibility
data class DueMonth private constructor(
    private val yearMonth: YearMonth
) {
    companion object {
        private val formatter = DateTimeFormatter.ofPattern("MMM-yyyy")
        private val DEFAULT_MONTH = YearMonth.now().plusMonths(1) // Default: next month

        fun fromYearMonth(yearMonth: YearMonth): DueMonth = DueMonth(yearMonth)

        fun parse(
            value: String,
            fallbackToDefault: Boolean = true
        ): DueMonth {
            return try {
                DueMonth(YearMonth.parse(value, formatter))
            } catch (e: DateTimeParseException) {
                if (fallbackToDefault) DEFAULT else throw e
            }
        }

        // For Room type converter
        fun fromString(value: String): DueMonth =
            try {
                DueMonth(YearMonth.parse(value, formatter))
            } catch (_: DateTimeParseException) {
                DEFAULT
            }

        val DEFAULT: DueMonth = DueMonth(DEFAULT_MONTH)
    }

    fun getYearMonth(): YearMonth = yearMonth

    override fun toString(): String = yearMonth.format(formatter)

    fun isPast(): Boolean = yearMonth.isBefore(YearMonth.now())
    fun isCurrent(): Boolean = yearMonth == YearMonth.now()
}
