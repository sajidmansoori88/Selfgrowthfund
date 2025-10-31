package com.selfgrowthfund.sgf.ui.treasurer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.data.repository.*
import com.selfgrowthfund.sgf.model.enums.ApprovalType
import com.selfgrowthfund.sgf.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TreasurerDashboardViewModel @Inject constructor(
    private val depositRepository: DepositRepository,
    private val borrowingRepository: BorrowingRepository,
    private val repaymentRepository: RepaymentRepository,
    private val investmentRepository: InvestmentRepository,
    private val investmentReturnsRepository: InvestmentReturnsRepository,
    private val approvalFlowRepository: ApprovalFlowRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TreasurerDashboardUiState(isLoading = true))
    val uiState: StateFlow<TreasurerDashboardUiState> = _uiState

    init {
        viewModelScope.launch {
            kotlinx.coroutines.delay(800)
            try {
                loadDashboardData()
            } catch (e: Exception) {
                Timber.e(e, "Error during init: ${e.message}")
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // LOAD DASHBOARD DATA
    // ─────────────────────────────────────────────────────────────
    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // ✅ Use actual repo methods (non-deprecated, available in your project)
                val deposits = depositRepository.getPendingForTreasurer()
                val borrowings = borrowingRepository.getApprovedPendingRelease()
                val repayments = repaymentRepository.getPendingForTreasurer()
                val investments = investmentRepository.getApprovedPendingRelease()
                val returns = investmentReturnsRepository.getPendingForTreasurer()

                // ✅ Member count and quorum calculation
                val totalActiveMembers = borrowingRepository.getActiveMemberCount()
                val quorumRequired = kotlin.math.ceil(totalActiveMembers * (2.0 / 3.0)).toInt()

                // ✅ Approval progress tracking
                val approvalProgressMap = mutableMapOf<String, Int>()
                borrowings.forEach { borrowing ->
                    val count = approvalFlowRepository.countApprovedByEntity(
                        borrowing.provisionalId,
                        ApprovalType.BORROWING
                    )
                    approvalProgressMap[borrowing.provisionalId] = count
                }

                _uiState.update {
                    it.copy(
                        deposits = deposits,
                        borrowings = borrowings,
                        repayments = repayments,
                        investments = investments,
                        returns = returns,
                        totalActiveMembers = totalActiveMembers,
                        quorumRequired = quorumRequired,
                        approvalProgressMap = approvalProgressMap,
                        isLoading = false
                    )
                }

            } catch (e: Exception) {
                Timber.e(e, "Error loading Treasurer dashboard data")
                _uiState.update {
                    it.copy(isLoading = false, message = "Error loading data: ${e.message}")
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // APPROVAL & RELEASE ACTIONS
    // ─────────────────────────────────────────────────────────────

    fun approveDeposit(provisionalId: String, treasurerId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = depositRepository.approveByTreasurer(
                provisionalId,
                treasurerId,
                "Approved by Treasurer"
            )
            if (success) loadDashboardData()
            onResult(success)
        }
    }

    fun releaseBorrowing(
        provisionalId: String,
        treasurerId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val quorumMet = borrowingRepository.isBorrowingApprovalQuorumMet(provisionalId)
                if (!quorumMet) {
                    onResult(false, "Cannot release — 2/3 member approvals not yet received.")
                    return@launch
                }

                when (val result = borrowingRepository.finalizeBorrowing(provisionalId, treasurerId)) {
                    is Result.Success -> {
                        loadDashboardData()
                        onResult(true, "Borrowing released successfully 💸")
                    }
                    is Result.Error -> onResult(false, "Release failed: ${result.exception.message}")
                    else -> { /* no-op */ }
                }
            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")
            }
        }
    }

    fun releaseInvestment(provisionalId: String, treasurerId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            when (investmentRepository.markPaymentReleased(
                provisionalId,
                treasurerId,
                "Investment funds released"
            )) {
                is Result.Success -> {
                    loadDashboardData()
                    onResult(true)
                }
                is Result.Error -> onResult(false)
                else -> { /* no-op */ }
            }
        }
    }

    fun approveRepayment(provisionalId: String, treasurerId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            when (repaymentRepository.approve(
                provisionalId,
                treasurerId,
                "Repayment verified by Treasurer",
                com.selfgrowthfund.sgf.model.enums.ApprovalStage.TREASURER_APPROVED
            )) {
                is Result.Success -> {
                    loadDashboardData()
                    onResult(true)
                }
                is Result.Error -> onResult(false)
                else -> { /* no-op */ }
            }
        }
    }

    fun approveInvestmentReturn(
        provisionalId: String,
        treasurerId: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            when (investmentReturnsRepository.approveByTreasurer(
                provisionalId,
                treasurerId,
                "Investment return verified"
            )) {
                is Result.Success -> {
                    loadDashboardData()
                    onResult(true)
                }
                is Result.Error -> onResult(false)
                else -> { /* no-op */ }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // SHARED UTILITY HELPERS
    // ─────────────────────────────────────────────────────────────

    suspend fun getActiveMemberCount(): Int =
        borrowingRepository.getActiveMemberCount()

    suspend fun getApprovalCount(provisionalId: String): Int =
        approvalFlowRepository.countApprovedByEntity(provisionalId, ApprovalType.BORROWING)
}
