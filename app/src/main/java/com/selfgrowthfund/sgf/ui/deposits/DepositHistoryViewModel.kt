package com.selfgrowthfund.sgf.ui.deposits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.data.local.entities.Deposit
import com.selfgrowthfund.sgf.data.repository.DepositRepository
import com.selfgrowthfund.sgf.model.enums.MemberRole
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DepositHistoryViewModel(
    private val repository: DepositRepository,
    val currentUserRole: MemberRole,
    private val currentUserId: String
) : ViewModel() {

    private val _visibleMonthLimit = MutableStateFlow(6)
    val visibleMonthLimit: StateFlow<Int> = _visibleMonthLimit

    private val _allDeposits = MutableStateFlow<List<Deposit>>(emptyList())
    val allDeposits: StateFlow<List<Deposit>> = _allDeposits

    val visibleMonths: StateFlow<List<String>> = visibleMonthLimit.map { limit ->
        getRecentMonths(limit)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, getRecentMonths(6))

    private val _shareholderFilter = MutableStateFlow("All")
    val shareholderFilter: StateFlow<String> = _shareholderFilter

    fun setShareholderFilter(name: String) {
        _shareholderFilter.value = name
    }

    private val _statusFilter = MutableStateFlow("All")
    val statusFilter: StateFlow<String> = _statusFilter

    fun setStatusFilter(status: String) {
        _statusFilter.value = status
    }

    val filteredDeposits: StateFlow<List<Deposit>> = combine(
        allDeposits,
        visibleMonths,
        statusFilter,
        shareholderFilter
    ) { deposits, months, status, shareholder ->
        deposits.filter { entry ->
            entry.dueMonth in months &&
                    (status == "All" || entry.paymentStatus == status) &&
                    (
                            currentUserRole != MemberRole.MEMBER_ADMIN && entry.shareholderId == currentUserId ||
                                    currentUserRole == MemberRole.MEMBER_ADMIN && (shareholder == "All" || entry.shareholderName == shareholder)
                            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun loadDeposits() {
        viewModelScope.launch {
            val deposits = repository.getAllDeposits()
            _allDeposits.value = deposits
        }
    }

    fun showMoreMonths() {
        if (_visibleMonthLimit.value < 12) {
            _visibleMonthLimit.value += 3
        }
    }

    private fun getRecentMonths(limit: Int): List<String> {
        val formatter = SimpleDateFormat("MMM-yyyy", Locale.getDefault())
        val cal = Calendar.getInstance()
        return (0 until limit).map {
            val month = formatter.format(cal.time)
            cal.add(Calendar.MONTH, -1)
            month
        }
    }
}