package com.selfgrowthfund.sgf.model.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.data.local.dao.BorrowingDao
import com.selfgrowthfund.sgf.data.local.dto.ActiveBorrowingDTO
import com.selfgrowthfund.sgf.data.local.dto.ClosedBorrowingDTO
import com.selfgrowthfund.sgf.data.local.dto.ClosedBorrowingSummary
import com.selfgrowthfund.sgf.data.local.dto.ActiveBorrowingSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class BorrowingSummaryViewModel @Inject constructor(
    private val borrowingDao: BorrowingDao
) : ViewModel() {

    // ─────────────── Active Borrowings ───────────────
    private val _activeBorrowings = MutableStateFlow<List<ActiveBorrowingSummary>>(emptyList())
    val activeBorrowings: StateFlow<List<ActiveBorrowingSummary>> = _activeBorrowings

    // ─────────────── Closed Borrowings ───────────────
    private val _closedBorrowings = MutableStateFlow<List<ClosedBorrowingSummary>>(emptyList())
    val closedBorrowings: StateFlow<List<ClosedBorrowingSummary>> = _closedBorrowings

    // ─────────────── Fiscal Year Filter ───────────────
    private val _selectedFiscalYear = MutableStateFlow<String?>(null)
    val selectedFiscalYear: StateFlow<String?> = _selectedFiscalYear

    fun setFiscalYear(year: String?) {
        _selectedFiscalYear.value = year
        refreshClosedBorrowings()
    }

    init {
        refreshActiveBorrowings()
        refreshClosedBorrowings()
    }

    private fun refreshActiveBorrowings() {
        viewModelScope.launch {
            val rawList = borrowingDao.getActiveBorrowingSummary()
            val today = LocalDate.now()

            val summaries = rawList.map { dto ->
                val outstanding = dto.approvedAmount - dto.totalPrincipalRepaid
                val penaltyDue = dto.totalPenaltyAccrued - dto.totalPenaltyPaid
                val overdueDays = maxOf(ChronoUnit.DAYS.between(dto.dueDate, today), 0)

                ActiveBorrowingSummary(
                    borrowId = dto.borrowId,
                    shareholderName = dto.shareholderName,
                    borrowAmount = dto.approvedAmount,
                    principalRepaid = dto.totalPrincipalRepaid,
                    penaltyPaid = dto.totalPenaltyPaid,
                    outstanding = outstanding,
                    penaltyDue = penaltyDue,
                    overdueDays = overdueDays
                )
            }

            _activeBorrowings.value = summaries.sortedByDescending { it.outstanding }
        }
    }

    private fun refreshClosedBorrowings() {
        viewModelScope.launch {
            val rawClosed = borrowingDao.getClosedBorrowingSummary()
            val selectedYear = _selectedFiscalYear.value

            val filteredClosed = rawClosed.filter { dto ->
                selectedYear?.let { fy ->
                    val (startYear, endYear) = fy.split("-").map { it.toInt() }
                    val start = LocalDate.of(startYear, 4, 1)
                    val end = LocalDate.of(endYear, 3, 31)
                    dto.closedDate in start..end
                } ?: true
            }

            val summaries = filteredClosed.map { dto ->
                val totalPaid = dto.totalPrincipalRepaid + dto.totalPenaltyPaid
                val returnPeriodDays = ChronoUnit.DAYS.between(dto.borrowStartDate, dto.closedDate)

                ClosedBorrowingSummary(
                    borrowId = dto.borrowId,
                    shareholderName = dto.shareholderName,
                    borrowAmount = dto.approvedAmount,
                    penaltyPaid = dto.totalPenaltyPaid,
                    totalPaid = totalPaid,
                    returnPeriodDays = returnPeriodDays
                )
            }

            _closedBorrowings.value = summaries.sortedByDescending { it.returnPeriodDays }
        }
    }

    fun getFiscalYears(): List<String> {
        val currentYear = LocalDate.now().year
        return (2023..currentYear + 1).map { "$it-${it + 1}" }.reversed()
    }
}