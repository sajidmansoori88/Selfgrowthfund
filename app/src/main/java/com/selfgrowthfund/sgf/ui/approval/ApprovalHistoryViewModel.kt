package com.selfgrowthfund.sgf.ui.approval

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.data.repository.ApprovalRepository
import com.selfgrowthfund.sgf.model.ApprovalHistoryEntry
import com.selfgrowthfund.sgf.model.enums.ApprovalType
import com.selfgrowthfund.sgf.ui.admin.ApprovalSummaryRow
import com.selfgrowthfund.sgf.model.enums.ExportType
import com.selfgrowthfund.sgf.model.enums.MemberRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class ApprovalHistoryViewModel(
    private val repository: ApprovalRepository,
    private val role: MemberRole,
    private val shareholderId: String? = null // for filtering member’s own history
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApprovalHistoryUiState())
    val uiState: StateFlow<ApprovalHistoryUiState> = _uiState

    init {
        _uiState.value = _uiState.value.copy(
            availableTypes = allowedApprovalTypes(role)
        )
    }

    fun loadHistory(period: Pair<LocalDate, LocalDate>) {
        _uiState.value = _uiState.value.copy(loading = true, error = null, selectedPeriod = period)

        viewModelScope.launch {
            try {
                val (start, end) = period
                val summary = repository.getApprovalSummary(start, end)

                val history = repository.getApprovalHistory(start, end, _uiState.value.selectedType)
                    .let { entries ->
                        if (role == MemberRole.MEMBER && shareholderId != null) {
                            entries.filter { it.shareholderId == shareholderId }
                        } else {
                            entries
                        }
                    }

                _uiState.value = _uiState.value.copy(
                    summary = summary,
                    history = history,
                    loading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = e.message
                )
            }
        }
    }

    fun onTypeSelected(type: ApprovalType) {
        _uiState.value = _uiState.value.copy(selectedType = type)
        _uiState.value.selectedPeriod?.let { loadHistory(it) }
    }

    fun onExportSelected(export: ExportType) {
        _uiState.value = _uiState.value.copy(selectedExport = export)
    }

    private fun allowedApprovalTypes(role: MemberRole): List<ApprovalType> {
        return when (role) {
            MemberRole.MEMBER_ADMIN -> ApprovalType.entries
            MemberRole.MEMBER_TREASURER -> listOf(
                ApprovalType.DEPOSIT,
                ApprovalType.BORROWING,
                ApprovalType.REPAYMENT,
                ApprovalType.INVESTMENT,
                ApprovalType.INVESTMENT_RETURN
            )
            MemberRole.MEMBER -> listOf(ApprovalType.ALL) // Members just see their own filtered history
        }
    }

    // Future: implement CSV/PDF export
    fun exportHistory() {
        val exportType = _uiState.value.selectedExport
        val history = _uiState.value.history
        // TODO: call repository.exportHistory(history, exportType)
    }
}

data class ApprovalHistoryUiState(
    val availableTypes: List<ApprovalType> = emptyList(),
    val selectedType: ApprovalType = ApprovalType.ALL,
    val selectedPeriod: Pair<LocalDate, LocalDate>? = null,
    val selectedExport: ExportType = ExportType.CSV,
    val summary: List<ApprovalSummaryRow> = emptyList(),
    val history: List<ApprovalHistoryEntry> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)
