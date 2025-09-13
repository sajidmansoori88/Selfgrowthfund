package com.selfgrowthfund.sgf.ui.actions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.data.local.dao.ActionItemDao
import com.selfgrowthfund.sgf.data.local.entities.ActionItem
import com.selfgrowthfund.sgf.model.enums.ActionResponse
import com.selfgrowthfund.sgf.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class ActionScreenViewModel @Inject constructor(
    private val actionDao: ActionItemDao
) : ViewModel() {

    private val _responseState = MutableStateFlow<Result<Unit>?>(null)
    val responseState: StateFlow<Result<Unit>?> = _responseState

    private val now get() = LocalDateTime.now()

    val pendingActions: StateFlow<List<ActionItem>> = actionDao
        .getPendingActions(now)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun getPendingCountForUser(shareholderId: String): StateFlow<Int> {
        return actionDao.getPendingActions(now)
            .map { actions ->
                actions.count { it.responses[shareholderId] == null }
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    }

    // ---- Existing generic method ----
    fun submitResponse(actionId: String, shareholderId: String, response: ActionResponse) {
        viewModelScope.launch {
            _responseState.value = Result.Loading

            val action = actionDao.getActionById(actionId)
            if (action == null) {
                _responseState.value = Result.Error(Exception("Action not found"))
                return@launch
            }

            val updatedResponses = action.responses.toMutableMap()
            updatedResponses[shareholderId] = response

            val updatedAction = action.copy(responses = updatedResponses)
            actionDao.updateAction(updatedAction)

            _responseState.value = Result.Success(Unit)
        }
    }

    // ---- Convenience wrappers for cleaner screen code ----
    fun approve(action: ActionItem, shareholderId: String) {
        submitResponse(action.actionId, shareholderId, ActionResponse.APPROVE)
    }

    fun reject(action: ActionItem, shareholderId: String) {
        submitResponse(action.actionId, shareholderId, ActionResponse.REJECT)
    }


    fun clearState() {
        _responseState.value = null
    }

    fun checkFinalization(action: ActionItem): Boolean {
        val totalResponses = action.responses.size
        val quorum = 3 // TODO: make dynamic
        val deadlinePassed = action.deadline?.isBefore(now) == true
        return totalResponses >= quorum || deadlinePassed
    }
}
