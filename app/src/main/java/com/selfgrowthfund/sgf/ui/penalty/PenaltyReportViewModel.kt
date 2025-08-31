package com.selfgrowthfund.sgf.ui.penalty

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.data.local.dao.PenaltyDao
import com.selfgrowthfund.sgf.data.local.entities.Penalty
import com.selfgrowthfund.sgf.model.enums.PenaltyType
import com.selfgrowthfund.sgf.model.enums.ReportPeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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

    fun getMonthRange(period: ReportPeriod): List<String> {
        val now = YearMonth.now()
        return when (period) {
            ReportPeriod.CURRENT_MONTH -> listOf(now.toString())
            ReportPeriod.LAST_MONTH -> listOf(now.minusMonths(1).toString())
            ReportPeriod.LAST_3_MONTHS -> (0..2).map { now.minusMonths(it.toLong()).toString() }
            ReportPeriod.LAST_6_MONTHS -> (0..5).map { now.minusMonths(it.toLong()).toString() }
            ReportPeriod.LAST_9_MONTHS -> (0..8).map { now.minusMonths(it.toLong()).toString() }
            ReportPeriod.LAST_12_MONTHS -> (0..11).map { now.minusMonths(it.toLong()).toString() }
        }
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

}