package com.selfgrowthfund.sgf.ui.shareholders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.selfgrowthfund.sgf.data.local.entities.Shareholder
import com.selfgrowthfund.sgf.data.repository.ShareholderRepository
import com.selfgrowthfund.sgf.utils.Dates
import com.selfgrowthfund.sgf.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class ShareholderViewModel @Inject constructor(
    private val repository: ShareholderRepository,
    private val dates: Dates
) : ViewModel() {

    // ─────────────── Reactive Streams ───────────────
    val shareholdersFlow: StateFlow<List<Shareholder>> = repository
        .getAllShareholdersStream()
        .map { it.sortedBy { s -> s.fullName } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pagedShareholders: Flow<PagingData<Shareholder>> = repository
        .getPagedShareholdersStream()
        .cachedIn(viewModelScope)

    fun getShareholderById(id: String): Flow<Shareholder?> =
        repository.getShareholderByIdStream(id)

    suspend fun fetchShareholderById(id: String): Shareholder? =
        repository.getShareholderById(id)

    // ─────────────── CRUD Operations ───────────────
    suspend fun deleteShareholder(id: String): Result<Unit> =
        repository.deleteShareholderById(id)

    suspend fun addShareholder(shareholder: Shareholder): Result<Unit> =
        repository.addShareholder(shareholder)

    suspend fun updateShareholder(shareholder: Shareholder): Result<Unit> =
        repository.updateShareholder(shareholder)

    // ─────────────── Submission Flow ───────────────
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
        joinDate: LocalDateTime // ✅ Changed from Date to LocalDateTime
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
                joinDate = joinDate // ✅ LocalDateTime
            )

            _submissionResult.emit(repository.addShareholder(newShareholder))
        }
    }

    // Optional helper to convert Date -> LocalDateTime
    fun convertDateToLocalDateTime(date: java.util.Date): LocalDateTime =
        dates.toLocalDateTime(date)
}
