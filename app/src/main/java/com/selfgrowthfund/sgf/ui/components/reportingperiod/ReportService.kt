package com.selfgrowthfund.sgf.ui.components.reportingperiod

import java.time.LocalDate

class ReportService(private val fiscalYearConfig: FiscalYearConfig = FiscalYearConfig()) {
    private val periodCalculator = ReportPeriodCalculator(fiscalYearConfig)
    private var currentPeriod: ReportPeriod = ReportPeriod.CURRENT_MONTH
    private var customPeriod: CustomPeriod? = null
    
    fun setPeriod(period: ReportPeriod, customPeriod: CustomPeriod? = null) {
        if (period == ReportPeriod.CUSTOM && customPeriod == null) {
            throw IllegalArgumentException("Custom period requires date range")
        }
        this.currentPeriod = period
        this.customPeriod = customPeriod
    }
    
    fun getCurrentDateRange(): Pair<LocalDate, LocalDate> {
        return periodCalculator.getDateRange(currentPeriod, customPeriod)
    }
    
    fun getPeriodLabel(): String {
        return if (currentPeriod == ReportPeriod.CUSTOM) {
            customPeriod?.label ?: currentPeriod.label
        } else {
            currentPeriod.label
        }
    }
    
    fun getCurrentPeriod(): ReportPeriod = currentPeriod
    fun getCustomPeriod(): CustomPeriod? = customPeriod
}