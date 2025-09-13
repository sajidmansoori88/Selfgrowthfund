package com.selfgrowthfund.sgf.ui.components.reportingperiod

import com.selfgrowthfund.sgf.model.enums.LabelledEnum

enum class ReportPeriod(override val label: String) : LabelledEnum {
    CURRENT_MONTH("Current Month"),
    LAST_3_MONTHS("Last 3 Months"),
    LAST_6_MONTHS("Last 6 Months"),
    CURRENT_FY("Current FY"),
    LAST_FY("Last FY"),
    CUSTOM("Custom Period");

    companion object {
        fun fromLabel(label: String): ReportPeriod =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: CURRENT_MONTH

        fun getAllLabels(): List<String> = entries.map { it.label }
    }
}
