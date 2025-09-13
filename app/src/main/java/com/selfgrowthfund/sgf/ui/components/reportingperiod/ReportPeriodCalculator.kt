package com.selfgrowthfund.sgf.ui.components.reportingperiod

import java.time.LocalDate

class ReportPeriodCalculator(private val fiscalYearConfig: FiscalYearConfig = FiscalYearConfig()) {
    
    fun getDateRange(period: ReportPeriod, customPeriod: CustomPeriod? = null): Pair<LocalDate, LocalDate> {
        return when (period) {
            ReportPeriod.CURRENT_MONTH -> getCurrentMonthRange()
            ReportPeriod.LAST_3_MONTHS -> getLastNMonthsRange(3)
            ReportPeriod.LAST_6_MONTHS -> getLastNMonthsRange(6)
            ReportPeriod.CURRENT_FY -> getFiscalYearRange(false)
            ReportPeriod.LAST_FY -> getFiscalYearRange(true)
            ReportPeriod.CUSTOM -> getCustomRange(customPeriod)
        }
    }
    
    private fun getCurrentMonthRange(): Pair<LocalDate, LocalDate> {
        val today = LocalDate.now()
        val startDate = today.withDayOfMonth(1)
        return startDate to today
    }
    
    private fun getLastNMonthsRange(months: Int): Pair<LocalDate, LocalDate> {
        val today = LocalDate.now()
        val startDate = today.minusMonths(months.toLong()).withDayOfMonth(1)
        val endDate = today
        return startDate to endDate
    }
    
    private fun getFiscalYearRange(isLastYear: Boolean): Pair<LocalDate, LocalDate> {
        val today = LocalDate.now()
        val currentYear = today.year
        
        val targetYear = if (isLastYear) currentYear - 1 else currentYear
        
        val fyStartDate = LocalDate.of(targetYear, fiscalYearConfig.startMonth, fiscalYearConfig.startDay)
        val fyEndDate = fyStartDate.plusYears(1).minusDays(1)
        
        val actualEndDate = if (!isLastYear && fyEndDate.isAfter(today)) today else fyEndDate
        
        return fyStartDate to actualEndDate
    }
    
    private fun getCustomRange(customPeriod: CustomPeriod?): Pair<LocalDate, LocalDate> {
        return customPeriod?.let { it.startDate to it.endDate } 
            ?: throw IllegalArgumentException("Custom period requires date range")
    }
    
    fun getCurrentFiscalYear(): String {
        val today = LocalDate.now()
        val fyStart = getFiscalYearStartDate(today.year)
        return if (today.isBefore(fyStart)) {
            "${today.year - 1}-${today.year}"
        } else {
            "${today.year}-${today.year + 1}"
        }
    }
    
    private fun getFiscalYearStartDate(year: Int): LocalDate {
        return LocalDate.of(year, fiscalYearConfig.startMonth, fiscalYearConfig.startDay)
    }
}