package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.ActionItemDao
import com.selfgrowthfund.sgf.data.local.entities.ActionItem
import com.selfgrowthfund.sgf.model.enums.ActionResponse
import com.selfgrowthfund.sgf.model.enums.ActionType
import com.selfgrowthfund.sgf.utils.Result
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ActionItemRepository (Realtime Firestore <-> Room Sync)
 *
 * - All local writes mark isSynced = false.
 * - realtimeSyncRepository.pushAllUnsynced() automatically syncs with Firestore.
 * - Firestore listeners handled globally by RealtimeSyncRepository.
 */
@Singleton
class ActionItemRepository @Inject constructor(
    private val dao: ActionItemDao,
    private val realtimeSyncRepository: RealtimeSyncRepository
) {

    // =============================
    // CRUD Operations
    // =============================

    suspend fun addAction(action: ActionItem): Result<Unit> = try {
        dao.insertAction(action.copy(isSynced = false, updatedAt = Instant.now()))
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun updateAction(action: ActionItem): Result<Unit> = try {
        dao.updateAction(action.copy(isSynced = false, updatedAt = Instant.now()))
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun deleteAction(action: ActionItem): Result<Unit> = try {
        dao.deleteAction(action)
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun completeAction(action: ActionItem): Result<Unit> = try {
        dao.updateAction(
            action.copy(
                response = ActionResponse.COMPLETED,
                isSynced = false,
                updatedAt = Instant.now()
            )
        )
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // =============================
    // Queries
    // =============================

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
