package com.selfgrowthfund.sgf.ui.deposits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.data.local.entities.Deposit
import com.selfgrowthfund.sgf.data.repository.DepositRepository
import com.selfgrowthfund.sgf.model.enums.MemberRole
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

class DepositHistoryViewModel(
    private val repository: DepositRepository,
    val currentUserRole: MemberRole,
    private val currentUserId: String
) : ViewModel() {

    // ---------------- UI State ----------------
    private val _visibleMonthLimit = MutableStateFlow(6)
    val visibleMonthLimit: StateFlow<Int> = _visibleMonthLimit

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _loadError = MutableStateFlow<String?>(null)
    val loadError: StateFlow<String?> = _loadError

    // ---------------- Data State ----------------
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

    fun resetFilters() {
        _shareholderFilter.value = "All"
        _statusFilter.value = "All"
        _visibleMonthLimit.value = 6
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
            _isLoading.value = true
            _loadError.value = null
            try {
                val deposits = repository.getAllDeposits()
                _allDeposits.value = deposits
            } catch (e: Exception) {
                _loadError.value = e.message
            }
            _isLoading.value = false
        }
    }

    fun showMoreMonths() {
        if (_visibleMonthLimit.value < 12) {
            _visibleMonthLimit.value += 3
        }
    }

    private fun getRecentMonths(limit: Int): List<String> {
        val formatter = DateTimeFormatter.ofPattern("MMM-yyyy", Locale.getDefault())
        val now = YearMonth.now()
        return (0 until limit).map { offset ->
            now.minusMonths(offset.toLong()).format(formatter)
        }
    }
}