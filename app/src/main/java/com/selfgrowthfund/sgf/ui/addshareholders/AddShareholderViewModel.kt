package com.selfgrowthfund.sgf.ui.addshareholders

import  androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.data.local.entities.ShareholderEntry
import com.selfgrowthfund.sgf.data.repository.ShareholderRepository
import com.selfgrowthfund.sgf.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddShareholderViewModel @Inject constructor(
    private val repository: ShareholderRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun addShareholder(input: ShareholderEntry, onResult: (Boolean, String?) -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.syncShareholderToFirestore(input)
            _isLoading.value = false
            when (result) {
                is Result.Success -> onResult(true, null)
                is Result.Error -> onResult(false, result.exception.message)
                Result.Loading -> TODO()
            }
        }
    }
}