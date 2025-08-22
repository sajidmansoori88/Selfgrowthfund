package com.selfgrowthfund.sgf.ui.investmentreturns

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.data.local.entities.Investment
import com.selfgrowthfund.sgf.data.local.entities.InvestmentReturns
import com.selfgrowthfund.sgf.data.repository.InvestmentReturnsRepository
import com.selfgrowthfund.sgf.utils.Dates
import com.selfgrowthfund.sgf.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class InvestmentReturnsViewModel @Inject constructor(
    private val repository: InvestmentReturnsRepository,
    private val dates: Dates
) : ViewModel() {

    private val _addReturnState = MutableStateFlow<Result<Unit>?>(null)
    val addReturnState: StateFlow<Result<Unit>?> = _addReturnState

    val isSubmitting: StateFlow<Boolean> = _addReturnState
        .map { it is Result.Loading }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /**
     * Adds a return for a given investment.
     * Emits Result.Loading, Result.Success, or Result.Error to the UI.
     */
    fun addReturn(
        returnId: String = UUID.randomUUID().toString(),
        investmentId: String,
        amountReceived: Double,
        remarks: String? = null
    ) {
        viewModelScope.launch {
            if (_addReturnState.value is Result.Loading) return@launch

            if (amountReceived <= 0.0) {
                _addReturnState.value = Result.Error(IllegalArgumentException("Amount must be positive"))
                return@launch
            }

            _addReturnState.value = Result.Loading

            val result = repository.addReturn(returnId, investmentId, amountReceived, remarks)

            _addReturnState.value = result
        }
    }

    /**
     * Generates a preview of the return before saving.
     * Useful for showing calculated fields like profit percent or return period.
     */
    fun previewReturn(
        investment: Investment,
        amountReceived: Double,
        remarks: String? = null
    ): InvestmentReturns {
        val localDate = Instant.ofEpochMilli(dates.now())
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        return InvestmentReturns(
            returnId = UUID.randomUUID().toString(),
            investment = investment,
            amountReceived = amountReceived,
            returnDate = localDate,
            remarks = remarks
        )
    }

    /**
     * Clears the current state, useful after showing a toast or navigating away.
     */
    fun clearState() {
        _addReturnState.value = null
    }
}