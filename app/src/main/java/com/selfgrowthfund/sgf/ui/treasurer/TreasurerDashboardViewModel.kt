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
import javax.inject.Inject

@HiltViewModel
class TreasurerDashboardViewModel @Inject constructor(
    private val depositRepository: DepositRepository,
    private val borrowingRepository: BorrowingRepository,
    private val repaymentRepository: RepaymentRepository,
    private val investmentRepository: InvestmentRepository,
    private val investmentReturnsRepository: InvestmentReturnsRepository,
    private val approvalFlowRepository: ApprovalFlowRepository // ✅ Added dependency
) : ViewModel() {

    private val _uiState = MutableStateFlow(TreasurerDashboardUiState(isLoading = true))
    val uiState: StateFlow<TreasurerDashboardUiState> = _uiState

    init {
        // Temporarily disabled auto-load for crash diagnosis
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            try {
                loadDashboardData()
            } catch (e: Exception) {
                android.util.Log.e("TreasurerVM", "Error during init: ${e.message}", e)
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
                val deposits = depositRepository.getPendingForTreasurer()
                val borrowings = borrowingRepository.getApprovedPendingRelease()
                val repayments = repaymentRepository.getPendingForTreasurer()
                val investments = investmentRepository.getApprovedPendingRelease()
                val returns = investmentReturnsRepository.getPendingForTreasurer()

                val totalActiveMembers = borrowingRepository.shareholderDao.getActiveMemberCount()
                val quorumRequired = kotlin.math.ceil(totalActiveMembers * (2.0 / 3.0)).toInt()

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
                        isLoading = false,
                        totalActiveMembers = totalActiveMembers,
                        quorumRequired = quorumRequired,
                        approvalProgressMap = approvalProgressMap
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, message = "Error loading data: ${e.message}")
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // APPROVE / RELEASE ACTIONS
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
                // ✅ Step 1: Check 2/3 quorum
                val quorumMet = borrowingRepository.isBorrowingApprovalQuorumMet(provisionalId)
                if (!quorumMet) {
                    onResult(false, "Cannot release — 2/3 member approvals not yet received.")
                    return@launch
                }

                // ✅ Step 2: Finalize borrowing
                when (val result = borrowingRepository.finalizeBorrowing(provisionalId, treasurerId)) {
                    is Result.Success<*> -> onResult(true, "Borrowing released successfully 💸")
                    is Result.Error -> onResult(false, "Release failed: ${result.exception.message}")
                    else -> onResult(false, "Unexpected result type") // ✅ exhaustive fix
                }
            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")
            }
        }
    }

    fun releaseInvestment(provisionalId: String, treasurerId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = investmentRepository.markPaymentReleased(
                provisionalId,
                treasurerId,
                "Investment funds released"
            )
            if (success) loadDashboardData()
            onResult(success)
        }
    }

    fun approveRepayment(provisionalId: String, treasurerId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repaymentRepository.approveByTreasurer(
                provisionalId,
                treasurerId,
                "Repayment verified by Treasurer"
            )
            if (success) loadDashboardData()
            onResult(success)
        }
    }

    fun approveInvestmentReturn(
        provisionalId: String,
        treasurerId: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val success = investmentReturnsRepository.approveByTreasurer(
                provisionalId,
                treasurerId,
                "Investment return verified"
            )
            if (success) loadDashboardData()
            onResult(success)
        }
    }

    // ─────────────────────────────────────────────────────────────
    // SHARED UTILITY HELPERS (for BorrowingListSection)
    // ─────────────────────────────────────────────────────────────
    suspend fun getActiveMemberCount(): Int =
        borrowingRepository.shareholderDao.getActiveMemberCount()

    suspend fun getApprovalCount(provisionalId: String): Int =
        approvalFlowRepository.countApprovedByEntity(provisionalId, ApprovalType.BORROWING)
}
