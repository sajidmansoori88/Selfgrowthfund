package com.selfgrowthfund.sgf.ui.shareholders

import androidx.compose.foundation.rememberOverscrollEffect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.data.local.entities.Shareholder
import com.selfgrowthfund.sgf.data.repository.ShareholderRepository
import com.selfgrowthfund.sgf.model.enums.MemberRole
import com.selfgrowthfund.sgf.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ShareholderViewModel @Inject constructor(
    private val repository: ShareholderRepository
) : ViewModel() {

    val shareholders: StateFlow<List<Shareholder>> = repository
        .getAllShareholdersStream()
        .map { it.sortedBy { s -> s.fullName } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getShareholderById(id: String): Flow<Shareholder?> =
        repository.getShareholderByIdStream(id)

    suspend fun fetchShareholderById(id: String): Shareholder? =
        repository.getShareholderById(id)

    suspend fun deleteShareholder(id: String): Result<Unit> =
        repository.deleteShareholderById(id)

    suspend fun addShareholder(shareholder: Shareholder): Result<Unit> =
        repository.addShareholder(shareholder)

    suspend fun updateShareholder(shareholder: Shareholder): Result<Unit> =
        repository.updateShareholder(shareholder)

    private val _submissionResult = MutableStateFlow<Result<Unit>?>(null)
    val submissionResult: StateFlow<Result<Unit>?> = _submissionResult

    fun clearSubmissionResult() {
        _submissionResult.value = null
    }

    fun submitShareholder(
        fullName: String,
        mobileNumber: String,
        address: String,
        shareBalanceInput: String,
        joinDate: Date
    ) {
        val shareBalance = shareBalanceInput.toDoubleOrNull()
        if (fullName.isBlank() || mobileNumber.isBlank() || address.isBlank() || shareBalance == null || shareBalance <= 0.0) {
            _submissionResult.value = Result.Error(Exception("Validation failed"))
            return
        }

        viewModelScope.launch {
            val lastId = repository.getLastShareholderId()
            val newId = Shareholder.generateNextId(lastId)

            val newShareholder = Shareholder(
                shareholderId = newId,
                fullName = fullName,
                mobileNumber = mobileNumber,
                address = address,
                shareBalance = shareBalance,
                joinDate = Date(),
                role = MemberRole.MEMBER
            )

            val result = repository.addShareholder(newShareholder)
            _submissionResult.emit(result)
        }
    }
}