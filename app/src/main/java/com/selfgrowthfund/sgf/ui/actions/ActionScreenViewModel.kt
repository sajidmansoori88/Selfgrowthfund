package com.selfgrowthfund.sgf.ui.actions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selfgrowthfund.sgf.data.local.dao.ActionItemDao
import com.selfgrowthfund.sgf.data.local.entities.ActionItem
import com.selfgrowthfund.sgf.data.local.entities.ApprovalFlow
import com.selfgrowthfund.sgf.data.repository.ApprovalFlowRepository
import com.selfgrowthfund.sgf.data.repository.BorrowingRepository
import com.selfgrowthfund.sgf.data.repository.InvestmentRepository
import com.selfgrowthfund.sgf.model.enums.ActionResponse
import com.selfgrowthfund.sgf.model.enums.ApprovalAction
import com.selfgrowthfund.sgf.model.enums.ApprovalStage
import com.selfgrowthfund.sgf.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class ActionScreenViewModel @Inject constructor(
    private val actionDao: ActionItemDao,
    private val approvalFlowRepository: ApprovalFlowRepository,
    private val investmentRepository: InvestmentRepository,
    private val borrowingRepository: BorrowingRepository
) : ViewModel() {

    private val _responseState = MutableStateFlow<Result<Unit>?>(null)
    val responseState: StateFlow<Result<Unit>?> = _responseState

    private val now get() = LocalDateTime.now()

    // ---------- Action Items ----------
    val pendingActions: StateFlow<List<ActionItem>> = actionDao
        .getPendingActions(now)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ---------- Approval Flows ----------
    val pendingApprovalFlows: StateFlow<List<ApprovalFlow>> =
        approvalFlowRepository.getPendingApprovals()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val allApprovalFlows: StateFlow<List<ApprovalFlow>> =
        approvalFlowRepository.getAllApprovals()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val completedApprovals: StateFlow<List<ApprovalFlow>> =
        approvalFlowRepository.getCompletedApprovals()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ---------- Helpers ----------
    fun getPendingCountForUser(shareholderId: String): StateFlow<Int> {
        return actionDao.getPendingActions(now)
            .map { actions ->
                actions.count { it.responses[shareholderId] == null }
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    }


    // ---------- Action Responses ----------
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

    // ===========================================================
    //                  APPROVAL FLOW SECTION
    // ===========================================================

    /** Approve an approval flow item */
    fun approveApproval(flow: ApprovalFlow) {
        viewModelScope.launch {
            val updated = flow.copy(
                action = ApprovalAction.APPROVE,
                approvedAt = Instant.now(),
                remarks = "Approved by ${flow.role.name}"
            )
            approvalFlowRepository.recordApproval(updated)

            // optional: update related entity stage
            when (flow.entityType) {
                com.selfgrowthfund.sgf.model.enums.ApprovalType.INVESTMENT -> {
                    investmentRepository.updateApprovalStage(
                        provisionalId = flow.entityId,
                        newStage = ApprovalStage.APPROVED
                    )
                }
                com.selfgrowthfund.sgf.model.enums.ApprovalType.BORROWING -> {
                    borrowingRepository.updateApprovalStage(
                        borrowId = flow.entityId,
                        newStage = ApprovalStage.APPROVED
                    )
                }
                else -> Unit
            }
        }
    }

    /** Reject an approval flow item */
    fun rejectApproval(flow: ApprovalFlow) {
        viewModelScope.launch {
            val updated = flow.copy(
                action = ApprovalAction.REJECT,
                approvedAt = Instant.now(),
                remarks = "Rejected by ${flow.role.name}"
            )
            approvalFlowRepository.recordApproval(updated)

            // optional: reflect rejection in main entity
            when (flow.entityType) {
                com.selfgrowthfund.sgf.model.enums.ApprovalType.INVESTMENT -> {
                    investmentRepository.updateApprovalStage(
                        provisionalId = flow.entityId,
                        newStage = ApprovalStage.REJECTED
                    )
                }
                com.selfgrowthfund.sgf.model.enums.ApprovalType.BORROWING -> {
                    borrowingRepository.updateApprovalStage(
                        borrowId = flow.entityId,
                        newStage = ApprovalStage.REJECTED
                    )
                }
                else -> Unit
            }
        }
    }

    // ---------- Entity Detail Flows ----------
    fun getInvestmentById(id: String): Flow<com.selfgrowthfund.sgf.data.local.entities.Investment?> =
        investmentRepository.getByProvisionalIdFlow(id)

    fun getBorrowingById(id: String): Flow<com.selfgrowthfund.sgf.data.local.entities.Borrowing?> =
        borrowingRepository.getByBorrowIdFlow(id)
}
