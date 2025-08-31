package com.selfgrowthfund.sgf.model.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.data.local.dao.InvestmentDao
import com.selfgrowthfund.sgf.data.local.dto.InvestmentTrackerDTO
import com.selfgrowthfund.sgf.data.local.dto.InvestmentTrackerSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class InvestmentTrackerViewModel @Inject constructor(
    private val investmentDao: InvestmentDao
) : ViewModel() {

    private val _investments = MutableStateFlow<List<InvestmentTrackerSummary>>(emptyList())
    val investments: StateFlow<List<InvestmentTrackerSummary>> = _investments

    private val _selectedFiscalYear = MutableStateFlow<String?>(null)
    val selectedFiscalYear: StateFlow<String?> = _selectedFiscalYear

    fun setFiscalYear(year: String?) {
        _selectedFiscalYear.value = year
        refreshInvestments()
    }

    init {
        refreshInvestments()
    }

    private fun refreshInvestments() {
        viewModelScope.launch {
            val rawList = investmentDao.getInvestmentTrackerSummary()
            val selectedYear = _selectedFiscalYear.value

            val filtered = rawList.filter { dto ->
                selectedYear?.let { fy ->
                    val (startYear, endYear) = fy.split("-").map { it.toInt() }
                    val start = LocalDate.of(startYear, 4, 1)
                    val end = LocalDate.of(endYear, 3, 31)
                    dto.expectedReturnDate in start..end
                } ?: true
            }

            val summaries = filtered.map { dto ->
                val status = when {
                    dto.actualReturnDate == null -> "Pending"
                    dto.actualReturnDate.isBefore(dto.expectedReturnDate) -> "Early"
                    dto.actualReturnDate.isEqual(dto.expectedReturnDate) -> "On-Time"
                    dto.actualReturnDate.isAfter(dto.expectedReturnDate) -> "Delayed"
                    dto.actualProfitPercent != null && dto.actualProfitPercent >= dto.expectedProfitPercent -> "Achieved"
                    else -> "Returned"
                }

                InvestmentTrackerSummary(
                    investmentId = dto.investmentId,
                    investmentName = dto.investmentName,
                    investorName = dto.investorName ?: "—",
                    expectedReturnDate = dto.expectedReturnDate,
                    actualReturnDate = dto.actualReturnDate,
                    expectedProfitPercent = dto.expectedProfitPercent,
                    actualProfitPercent = dto.actualProfitPercent ?: 0.0,
                    status = status
                )
            }

            _investments.value = summaries.sortedByDescending { it.expectedReturnDate }
        }
    }

    fun getFiscalYears(): List<String> {
        val currentYear = LocalDate.now().year
        return (2023..currentYear + 1).map { "$it-${it + 1}" }.reversed()
    }
}