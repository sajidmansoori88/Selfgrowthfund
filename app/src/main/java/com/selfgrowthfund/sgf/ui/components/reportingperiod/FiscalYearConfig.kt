package com.selfgrowthfund.sgf.ui.components.reportingperiod

import java.time.Month

data class FiscalYearConfig(
    val startMonth: Month = Month.APRIL,
    val startDay: Int = 1
)