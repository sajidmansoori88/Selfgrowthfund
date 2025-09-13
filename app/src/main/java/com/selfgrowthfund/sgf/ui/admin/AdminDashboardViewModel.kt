package com.selfgrowthfund.sgf.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.data.repository.ApprovalRepository
import com.selfgrowthfund.sgf.model.ApprovalEntry
import com.selfgrowthfund.sgf.model.ApprovalGroup
import com.selfgrowthfund.sgf.model.ApprovalHistoryEntry
import com.selfgrowthfund.sgf.ui.components.reportingperiod.ReportPeriod
import com.selfgrowthfund.sgf.ui.components.reportingperiod.ReportPeriodCalculator
import com.selfgrowthfund.sgf.model.SessionEntry
import com.selfgrowthfund.sgf.model.User
import com.selfgrowthfund.sgf.ui.components.reportingperiod.CustomPeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val approvalRepository: ApprovalRepository
) : ViewModel() {
    private val periodCalculator = ReportPeriodCalculator()
    private val _shareholders = MutableStateFlow<List<User>>(emptyList())
    val shareholders: StateFlow<List<User>> = _shareholders

    private val _approvalSummary = MutableStateFlow<List<ApprovalSummaryRow>>(emptyList())
    val approvalSummary: StateFlow<List<ApprovalSummaryRow>> = _approvalSummary
    val approvalHistory = MutableStateFlow<List<ApprovalHistoryEntry>>(emptyList())
    val selectedPeriod = MutableStateFlow(ReportPeriod.CURRENT_MONTH)

    val currentDateRange: Pair<LocalDate, LocalDate>
        get() = periodCalculator.getDateRange(selectedPeriod.value)
    val sessionHistory = MutableStateFlow<List<SessionEntry>>(emptyList())
    val customPeriod = MutableStateFlow<CustomPeriod?>(null)

    fun loadApprovalSummary(period: ReportPeriod, start: LocalDate, end: LocalDate) {
        viewModelScope.launch {
            _approvalSummary.value = approvalRepository.getApprovalSummary(start, end)
        }
    }
    fun loadShareholders() {
        viewModelScope.launch {
            // Replace with actual repository call
            _shareholders.value = fetchShareholdersFromBackend()
        }
    }

    fun modifyUser(updatedUser: User) {
        viewModelScope.launch {
            // Call backend to update user
            updateUserInBackend(updatedUser)
            loadShareholders()
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            // Call backend to delete user
            deleteUserFromBackend(user.id)
            loadShareholders()
        }
    }
    private val _approvalGroups = MutableStateFlow<List<ApprovalGroup>>(emptyList())
    val approvalGroups: StateFlow<List<ApprovalGroup>> = _approvalGroups

    fun loadApprovals() {
        viewModelScope.launch {
            _approvalGroups.value = fetchGroupedApprovals()
        }
    }

    fun approve(entry: ApprovalEntry) {
        viewModelScope.launch {
            approveEntryInBackend(entry.id)
            loadApprovals()
        }
    }

    fun reject(entry: ApprovalEntry) {
        viewModelScope.launch {
            rejectEntryInBackend(entry.id)
            loadApprovals()
        }
    }

    fun loadApprovalHistory(period: ReportPeriod, startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            selectedPeriod.value = period
            approvalHistory.value = fetchHistory(startDate, endDate)
        }
    }

    fun exportCSV(period: ReportPeriod, startDate: LocalDate, endDate: LocalDate) {
        // TODO: Implement CSV export with date range
        println("Exporting CSV for $period: $startDate to $endDate")
    }

    fun exportPDF(period: ReportPeriod, startDate: LocalDate, endDate: LocalDate) {
        // TODO: Implement PDF export with date range
        println("Exporting PDF for $period: $startDate to $endDate")
    }

    private suspend fun fetchHistory(startDate: LocalDate, endDate: LocalDate): List<ApprovalHistoryEntry> {
        // TODO: Replace with actual backend call using date range
        return listOf()
    }

    fun loadSessionHistory() {
        viewModelScope.launch {
            sessionHistory.value = fetchSessionData()
        }
    }

    fun exportSessionCSV() {
        // TODO: Implement CSV export logic
    }

    private suspend fun fetchSessionData(): List<SessionEntry> {
        // TODO: Replace with actual backend call
        return listOf()
    }


    // Stubbed backend calls
    private suspend fun fetchGroupedApprovals(): List<ApprovalGroup> = listOf()
    private suspend fun approveEntryInBackend(id: String) {}
    private suspend fun rejectEntryInBackend(id: String) {}
}

// Temporary functions, to be removed once repository is implemented
private suspend fun fetchShareholdersFromBackend(): List<User> {
    // TODO: Replace with actual API call
    return listOf()
}

private suspend fun updateUserInBackend(user: User) {
    // TODO: Replace with actual API call
}

private suspend fun deleteUserFromBackend(userId: String) {
    // TODO: Replace with actual API call
}
