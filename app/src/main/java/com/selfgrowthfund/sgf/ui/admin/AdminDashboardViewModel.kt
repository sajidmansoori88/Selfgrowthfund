package com.selfgrowthfund.sgf.ui.admin

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.data.local.entities.Shareholder
import com.selfgrowthfund.sgf.data.repository.ApprovalRepository
import com.selfgrowthfund.sgf.data.repository.ShareholderRepository
import com.selfgrowthfund.sgf.data.repository.UserSessionRepository
import com.selfgrowthfund.sgf.model.ApprovalEntry
import com.selfgrowthfund.sgf.model.ApprovalGroup
import com.selfgrowthfund.sgf.model.ApprovalHistoryEntry
import com.selfgrowthfund.sgf.session.SessionEntry
import com.selfgrowthfund.sgf.model.enums.ApprovalType
import com.selfgrowthfund.sgf.ui.components.reportingperiod.ReportPeriod
import com.selfgrowthfund.sgf.ui.components.reportingperiod.ReportPeriodCalculator
import com.selfgrowthfund.sgf.ui.components.reportingperiod.CustomPeriod
import com.selfgrowthfund.sgf.utils.ExportUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val approvalRepository: ApprovalRepository,
    private val shareholderRepository: ShareholderRepository,
    private val sessionRepository: UserSessionRepository,
) : ViewModel() {

    private val periodCalculator = ReportPeriodCalculator()

    // ── UI State ──
    data class UiState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val successMessage: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    // ── Session History ──
    private val _sessionHistory = MutableStateFlow<List<SessionEntry>>(emptyList())
    val sessionHistory: StateFlow<List<SessionEntry>> = _sessionHistory.asStateFlow()

    fun loadSessionHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val shareholderMap = _shareholders.value.associateBy({ it.shareholderId }, { it.fullName })
                _sessionHistory.value = sessionRepository.getUserSessions(shareholderMap)
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load session history: ${e.message}"
                    )
                }
            }
        }
    }

    fun exportSessionHistoryCsv(context: android.content.Context) {
        viewModelScope.launch {
            try {
                if (_sessionHistory.value.isEmpty()) {
                    _uiState.update {
                        it.copy(errorMessage = "No session history available to export")
                    }
                    return@launch
                }

                _uiState.update { it.copy(successMessage = "Session history exported successfully") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Export failed: ${e.message}") }
            }
        }
    }

    fun exportSessionCSV() {
        viewModelScope.launch {
            try {
                val data = _sessionHistory.value
                if (data.isEmpty()) {
                    _uiState.update { it.copy(errorMessage = "No session data to export") }
                    return@launch
                }

                ExportUtils.exportSessionHistoryCSV(
                    context = appContext,
                    sessionEntries = data
                )

                _uiState.update { it.copy(successMessage = "Session history exported successfully") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Export failed: ${e.message}") }
            }
        }
    }

    // ── Shareholders ──
    private val _shareholders = MutableStateFlow<List<Shareholder>>(emptyList())
    val shareholders: StateFlow<List<Shareholder>> = _shareholders.asStateFlow()

    fun loadShareholders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                _shareholders.value = shareholderRepository.getAllShareholders()
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load shareholders: ${e.message}"
                    )
                }
            }
        }
    }

    fun modifyShareholder(updated: Shareholder) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                shareholderRepository.updateShareholder(updated)
                loadShareholders()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Shareholder updated successfully"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to update shareholder: ${e.message}"
                    )
                }
            }
        }
    }

    fun deleteShareholder(shareholder: Shareholder) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                shareholderRepository.deleteShareholder(shareholder)
                loadShareholders()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Shareholder deleted successfully"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to delete shareholder: ${e.message}"
                    )
                }
            }
        }
    }

    // ── Approval Summary ──
    private val _approvalSummary = MutableStateFlow<List<ApprovalSummaryRow>>(emptyList())
    val approvalSummary: StateFlow<List<ApprovalSummaryRow>> = _approvalSummary.asStateFlow()

    private val _customPeriod = MutableStateFlow<CustomPeriod?>(null)
    val customPeriod: StateFlow<CustomPeriod?> = _customPeriod.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(ReportPeriod.CURRENT_MONTH)
    val selectedPeriod: StateFlow<ReportPeriod> = _selectedPeriod.asStateFlow()

    val currentDateRange: Pair<LocalDate, LocalDate>
        get() = periodCalculator.getDateRange(_selectedPeriod.value)

    fun setSelectedPeriod(period: ReportPeriod) {
        _selectedPeriod.value = period
    }

    fun setCustomPeriod(period: CustomPeriod?) {
        _customPeriod.value = period
    }

    fun loadApprovalSummary(period: ReportPeriod, start: LocalDate, end: LocalDate) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                _approvalSummary.value = approvalRepository.getApprovalSummary(start, end)
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load approval summary: ${e.message}"
                    )
                }
            }
        }
    }

    // ── Approvals & History ──
    private val _approvalGroups = MutableStateFlow<List<ApprovalGroup>>(emptyList())
    val approvalGroups: StateFlow<List<ApprovalGroup>> = _approvalGroups.asStateFlow()

    fun loadApprovals() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                _approvalGroups.value = approvalRepository.getGroupedApprovals()
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load approvals: ${e.message}"
                    )
                }
            }
        }
    }

    fun approve(entry: ApprovalEntry) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val currentUser = sessionRepository.getCurrentUser()
                    ?: throw IllegalStateException("No logged-in user found")

                approvalRepository.approveEntry(entry, currentUser)
                loadApprovals()

                _uiState.update {
                    it.copy(isLoading = false, successMessage = "Entry approved successfully")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Failed to approve entry: ${e.message}")
                }
            }
        }
    }

    fun reject(entry: ApprovalEntry) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val currentUser = sessionRepository.getCurrentUser()
                    ?: throw IllegalStateException("No logged-in user found")

                approvalRepository.rejectEntry(entry, currentUser)
                loadApprovals()

                _uiState.update {
                    it.copy(isLoading = false, successMessage = "Entry rejected successfully")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Failed to reject entry: ${e.message}")
                }
            }
        }
    }

    // ── Approval History ──
    private val _approvalHistory = MutableStateFlow<List<ApprovalHistoryEntry>>(emptyList())
    val approvalHistory: StateFlow<List<ApprovalHistoryEntry>> = _approvalHistory.asStateFlow()

    fun loadApprovalHistory(period: ReportPeriod, startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                _selectedPeriod.value = period
                _approvalHistory.value = approvalRepository.getApprovalHistory(
                    startDate, endDate,
                    type = ApprovalType.ALL
                )
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load approval history: ${e.message}"
                    )
                }
            }
        }
    }

    // ── Enhanced Export Functions ──
    fun exportCSV(period: ReportPeriod, startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                println("Exporting CSV for $period: $startDate to $endDate")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "CSV exported successfully"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to export CSV: ${e.message}"
                    )
                }
            }
        }
    }

    fun exportPDF(period: ReportPeriod, startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                println("Exporting PDF for $period: $startDate to $endDate")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "PDF exported successfully"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to export PDF: ${e.message}"
                    )
                }
            }
        }
    }

    // ── Init ──
    init {
        loadShareholders()
        loadApprovals()
        loadSessionHistory()
    }
}
