package com.selfgrowthfund.sgf.ui.addshareholders

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.data.local.entities.Shareholder
import com.selfgrowthfund.sgf.data.repository.ShareholderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ShareholderListViewModel @Inject constructor(
    private val shareholderRepository: ShareholderRepository
) : ViewModel() {

    val shareholders: Flow<List<Shareholder>> = shareholderRepository.getAllShareholdersStream()
        .catch { e ->
            Timber.tag("ShareholderListViewMode").e(e, "Error fetching shareholders")
            emit(emptyList())
        }

    fun deleteShareholder(id: String) {
        viewModelScope.launch {
            try {
                shareholderRepository.deleteShareholderById(id)
            } catch (e: Exception) {
                Timber.tag("ShareholderListViewMode").e(e, "Error deleting shareholder")
            }
        }
    }
}