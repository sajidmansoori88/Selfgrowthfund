package com.selfgrowthfund.sgf.ui.shareholders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.data.local.entities.Shareholder
import com.selfgrowthfund.sgf.data.repository.ShareholderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShareholderListViewModel @Inject constructor(
    private val repository: ShareholderRepository
) : ViewModel() {

    val shareholders: StateFlow<List<Shareholder>> = repository
        .getAllShareholdersStream()
        .map { it.sortedBy { s -> s.fullName } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteShareholder(id: String) {
        viewModelScope.launch {
            repository.deleteShareholderById(id)
        }
    }
}