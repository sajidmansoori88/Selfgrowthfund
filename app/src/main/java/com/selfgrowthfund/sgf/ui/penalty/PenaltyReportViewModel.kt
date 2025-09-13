package com.selfgrowthfund.sgf.ui.penalty

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.data.local.dao.PenaltyDao
import com.selfgrowthfund.sgf.data.local.entities.Penalty
import com.selfgrowthfund.sgf.model.enums.PenaltyType
import com.selfgrowthfund.sgf.ui.components.reportingperiod.CustomPeriod
import com.selfgrowthfund.sgf.ui.components.reportingperiod.FiscalYearConfig
import com.selfgrowthfund.sgf.ui.components.reportingperiod.ReportPeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class PenaltyReportViewModel @Inject constructor(
    private val penaltyDao: PenaltyDao
) : ViewModel() {

    private val _penalties = MutableStateFlow<List<Penalty>>(emptyList())
    val penalties: StateFlow<List<Penalty>> = _penalties

    fun loadAll() {
        viewModelScope.launch {
            _penalties.value = penaltyDao.getAllPenalties()
        }
    }

    fun filterByType(type: PenaltyType) {
        viewModelScope.launch {
            _penalties.value = penaltyDao.getPenaltiesByType(type.name)
        }
    }

    fun filterByUser(shareholderId: String) {
        viewModelScope.launch {
            _penalties.value = penaltyDao.getPenaltiesByUser(shareholderId)
        }
    }


    fun getMonthRange(period: ReportPeriod, customPeriod: CustomPeriod? = null): List<String> {
        val now = YearMonth.now()
        return when (period) {
            ReportPeriod.CURRENT_MONTH -> listOf(now.toString())
            ReportPeriod.LAST_3_MONTHS -> (0..2).map { now.minusMonths(it.toLong()).toString() }
            ReportPeriod.LAST_6_MONTHS -> (0..5).map { now.minusMonths(it.toLong()).toString() }
            ReportPeriod.CURRENT_FY -> getFiscalYearMonths(false)
            ReportPeriod.LAST_FY -> getFiscalYearMonths(true)
            ReportPeriod.CUSTOM -> getCustomMonthsRange(customPeriod)
        }
    }

    private fun getFiscalYearMonths(isLastYear: Boolean): List<String> {
        val today = LocalDate.now()
        val targetYear = if (isLastYear) today.year - 1 else today.year
        val fiscalYearConfig = FiscalYearConfig() // Or pass as parameter

        val startMonth = fiscalYearConfig.startMonth
        val months = mutableListOf<String>()

        // Get all months in the fiscal year
        for (i in 0 until 12) {
            val monthValue = (startMonth.value - 1 + i) % 12 + 1
            val year = if (monthValue >= startMonth.value) targetYear else targetYear + 1
            val yearMonth = YearMonth.of(year, monthValue)
            months.add(yearMonth.toString())
        }

        return months
    }

    private fun getCustomMonthsRange(customPeriod: CustomPeriod?): List<String> {
        if (customPeriod == null) return emptyList()

        val months = mutableListOf<String>()
        var current = YearMonth.from(customPeriod.startDate)
        val end = YearMonth.from(customPeriod.endDate)

        while (!current.isAfter(end)) {
            months.add(current.toString())
            current = current.plusMonths(1)
        }

        return months
    }

    fun loadByPeriod(period: ReportPeriod) {
        viewModelScope.launch {
            val months = getMonthRange(period)
            val all = months.flatMap { month ->
                penaltyDao.getPenaltiesByMonth(month)
            }
            _penalties.value = all.sortedByDescending { it.date }
        }
    }

    fun getExportRows(): List<List<String>> {
        return penalties.value.map {
            listOf(
                it.date.toString(),
                it.amount.toString(),
                it.type.label,
                it.reason,
                it.recordedBy
            )
        }
    }
    fun loadByCustomPeriod(customPeriod: CustomPeriod) {
        viewModelScope.launch {
            val results = penaltyDao.getPenaltiesBetween(
                startDate = customPeriod.startDate,
                endDate = customPeriod.endDate
            )
            _penalties.value = results.sortedByDescending { it.date }
        }
    }


}