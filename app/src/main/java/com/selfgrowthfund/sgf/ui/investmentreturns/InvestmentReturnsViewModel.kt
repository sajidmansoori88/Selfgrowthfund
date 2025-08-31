package com.selfgrowthfund.sgf.ui.investmentreturns

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.data.local.entities.Investment
import com.selfgrowthfund.sgf.data.local.entities.InvestmentReturns
import com.selfgrowthfund.sgf.data.local.entities.InvestmentReturnEntry
import com.selfgrowthfund.sgf.data.repository.InvestmentReturnsRepository
import com.selfgrowthfund.sgf.utils.Dates
import com.selfgrowthfund.sgf.utils.IdGenerator
import com.selfgrowthfund.sgf.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
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

    /** Adds a return using a structured entry model */
    fun submitReturn(
        entry: InvestmentReturnEntry,
        lastReturnId: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (_addReturnState.value is Result.Loading) return@launch

            if (entry.amountReceived <= 0.0) {
                _addReturnState.value = Result.Error(IllegalArgumentException("Amount must be positive"))
                onError("Amount must be positive")
                return@launch
            }

            _addReturnState.value = Result.Loading

            val returnEntity = entry.toInvestmentReturn(lastReturnId)
            val result = repository.addReturn(returnEntity)

            _addReturnState.value = result

            when (result) {
                is Result.Success -> onSuccess()
                is Result.Error -> onError(result.exception.message ?: "Submission failed")
                else -> {}
            }
        }
    }

    /** Generates a preview of the return before saving */
    fun previewReturn(entry: InvestmentReturnEntry): InvestmentReturns {
        val localDate = Instant.ofEpochMilli(dates.now())
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        return entry.copy(returnDate = localDate).toInvestmentReturn(lastReturnId = null)
    }

    /** Clears the current state */
    fun clearState() {
        _addReturnState.value = null
    }
}