package com.selfgrowthfund.sgf.ui.repayment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.data.repository.RepaymentRepository
import com.selfgrowthfund.sgf.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class RepaymentViewModel(
    private val repository: RepaymentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RepaymentListUiState())
    val uiState: StateFlow<RepaymentListUiState> = _uiState

    fun loadRepayments(borrowId: String) {
        viewModelScope.launch {
            repository.getAllByBorrowId(borrowId)
                .onStart {
                    _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                }
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _uiState.value = RepaymentListUiState(
                                isLoading = false,
                                repayments = result.data
                            )
                        }
                        is Result.Error -> {
                            _uiState.value = RepaymentListUiState(
                                isLoading = false,
                                error = result.exception
                            )
                        }

                        Result.Loading -> TODO()
                    }
                }
        }
    }
}