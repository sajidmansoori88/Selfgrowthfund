package com.selfgrowthfund.sgf.ui.components.reportingperiod

import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class CustomPeriod(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val label: String = "Custom: ${startDate.format(DateTimeFormatter.ISO_DATE)} to ${endDate.format(DateTimeFormatter.ISO_DATE)}"
) {
    init {
        require(!startDate.isAfter(endDate)) { "Start date must be before end date" }
        require(startDate.isAfter(LocalDate.now().minusYears(5))) { "Date range too far in past" }
        require(endDate.isBefore(LocalDate.now().plusDays(1))) { "End date cannot be in future" }
    }
}