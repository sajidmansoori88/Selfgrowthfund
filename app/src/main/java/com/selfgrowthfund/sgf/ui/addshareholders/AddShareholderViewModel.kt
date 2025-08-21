package com.selfgrowthfund.sgf.ui.addshareholders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.data.local.entities.ShareholderEntry
import com.selfgrowthfund.sgf.data.repository.ShareholderRepository
import com.selfgrowthfund.sgf.utils.Result
import com.selfgrowthfund.sgf.utils.mappers.toShareholder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddShareholderViewModel @Inject constructor(
    private val shareholderRepository: ShareholderRepository
) : ViewModel() {

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _saveSuccess = MutableStateFlow<Boolean?>(null)
    val saveSuccess: StateFlow<Boolean?> = _saveSuccess

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun addShareholder(entry: ShareholderEntry) {
        viewModelScope.launch {
            _isSaving.value = true
            _errorMessage.value = null
            _saveSuccess.value = null

            try {
                val lastId = shareholderRepository.getLastShareholderId()
                val shareholder = entry.toShareholder(lastId)
                val result = shareholderRepository.addShareholder(shareholder)

                when (result) {
                    is Result.Success -> _saveSuccess.value = true
                    is Result.Error -> _errorMessage.value = result.exception.message
                    Result.Loading -> {} // Optional: handle loading state
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isSaving.value = false
            }
        }
    }
}