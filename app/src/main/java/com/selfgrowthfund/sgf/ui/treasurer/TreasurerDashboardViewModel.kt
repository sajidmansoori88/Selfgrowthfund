package com.selfgrowthfund.sgf.ui.treasurer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.data.repository.*
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
) : ViewModel() {

    private val _uiState = MutableStateFlow(TreasurerDashboardUiState(isLoading = true))
    val uiState: StateFlow<TreasurerDashboardUiState> = _uiState

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val deposits = depositRepository.getPendingForTreasurer()
                val borrowings = borrowingRepository.getApprovedPendingRelease()
                val repayments = repaymentRepository.getPendingForTreasurer()
                val investments = investmentRepository.getApprovedPendingRelease()
                val returns = investmentReturnsRepository.getPendingForTreasurer()

                _uiState.update {
                    it.copy(
                        deposits = deposits,
                        borrowings = borrowings,
                        repayments = repayments,
                        investments = investments,
                        returns = returns,
                        isLoading = false,
                        message = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, message = "Error loading data: ${e.message}")
                }
            }
        }
    }

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

    fun releaseBorrowing(provisionalId: String, treasurerId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = borrowingRepository.markPaymentReleased(
                provisionalId,
                treasurerId,
                "Borrowing funds released"
            )
            if (success) loadDashboardData()
            onResult(success)
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

    fun approveInvestmentReturn(provisionalId: String, treasurerId: String, onResult: (Boolean) -> Unit) {
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
}
