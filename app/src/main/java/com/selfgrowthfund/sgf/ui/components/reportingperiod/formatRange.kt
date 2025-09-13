package com.selfgrowthfund.sgf.ui.components.reportingperiod

import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun Pair<LocalDate, LocalDate>.formatRange(): String {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    return "${first.format(formatter)} - ${second.format(formatter)}"
}

// Extension for easy period display
fun ReportPeriod.getDisplayName(customPeriod: CustomPeriod? = null): String {
    return if (this == ReportPeriod.CUSTOM) {
        customPeriod?.label ?: this.label
    } else {
        this.label
    }
}