package com.selfgrowthfund.sgf.ui.penalty

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.data.local.dao.PenaltyDao
import com.selfgrowthfund.sgf.data.local.entities.Penalty
import dagger.hilt.android.lifecycle.HiltViewModel

import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PenaltyViewModel @Inject constructor(
    private val penaltyDao: PenaltyDao
) : ViewModel() {

    fun insertPenalty(penalty: Penalty) {
        viewModelScope.launch {
            penaltyDao.insert(penalty)
        }
    }
}