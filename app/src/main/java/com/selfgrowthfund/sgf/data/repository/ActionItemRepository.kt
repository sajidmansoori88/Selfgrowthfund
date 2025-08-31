package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.ActionItemDao
import com.selfgrowthfund.sgf.data.local.entities.ActionItem
import com.selfgrowthfund.sgf.model.enums.ActionResponse
import com.selfgrowthfund.sgf.model.enums.ActionType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class ActionItemRepository(private val dao: ActionItemDao) {

    suspend fun addAction(action: ActionItem) = dao.insertAction(action)

    suspend fun updateAction(action: ActionItem) = dao.updateAction(action)

    suspend fun deleteAction(action: ActionItem) = dao.deleteAction(action)

    suspend fun getActionById(actionId: String): ActionItem? = dao.getActionById(actionId)

    fun getAllActions(): Flow<List<ActionItem>> = dao.getAllActions()

    fun getPendingActions(now: LocalDateTime): Flow<List<ActionItem>> = dao.getPendingActions(now)

    fun getActionsCreatedBy(userId: String): Flow<List<ActionItem>> = dao.getActionsCreatedBy(userId)

    fun getActionsByType(type: ActionType): Flow<List<ActionItem>> =
        dao.getActionsByType(type.label)

    fun getActionsByResponse(response: ActionResponse): Flow<List<ActionItem>> =
        dao.getActionsByResponse(response.label)

    fun getPendingActionCount(now: LocalDateTime): Flow<Int> =
        dao.getPendingActionCount(ActionResponse.PENDING.label, now)
}