package com.selfgrowthfund.sgf.data.repository

import com.selfgrowthfund.sgf.data.local.dao.ApprovalFlowDao
import com.selfgrowthfund.sgf.data.local.entities.ApprovalFlow
import com.selfgrowthfund.sgf.model.enums.ApprovalType
import com.selfgrowthfund.sgf.utils.Result
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ApprovalFlowRepository — Realtime Sync Enabled
 *
 * - Writes mark isSynced = false (for pushAllUnsynced)
 * - Firestore ↔ Room sync handled via RealtimeSyncRepository
 */
@Singleton
class ApprovalFlowRepository @Inject constructor(
    private val dao: ApprovalFlowDao,
    private val realtimeSyncRepository: RealtimeSyncRepository
) {

    private val collectionName = "approvals"

    // ========== Core Operations ==========

    suspend fun recordApproval(flow: ApprovalFlow): Result<Unit> = try {
        dao.insert(flow.copy(isSynced = false))
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun update(flow: ApprovalFlow): Result<Unit> = try {
        dao.update(flow.copy(isSynced = false))
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun delete(flow: ApprovalFlow): Result<Unit> = try {
        dao.delete(flow)
        realtimeSyncRepository.pushAllUnsynced()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // ========== Queries ==========

    suspend fun getFlowsByTypeBetween(
        type: ApprovalType,
        start: Instant,
        end: Instant
    ): List<ApprovalFlow> = dao.getFlowsByTypeBetween(type, start, end)

    suspend fun getAllFlowsBetween(
        start: Instant,
        end: Instant
    ): List<ApprovalFlow> = dao.getAllFlowsBetween(start, end)

    fun getAllApprovals(): Flow<List<ApprovalFlow>> = dao.getAllApprovals()

    suspend fun countApprovedByEntity(entityId: String, type: ApprovalType): Int =
        dao.countApprovedVotes(entityId, type)

    suspend fun countRejectedByEntity(entityId: String, type: ApprovalType): Int =
        dao.countRejectedVotes(entityId, type)

    suspend fun getVotesForEntity(entityId: String, type: ApprovalType): List<ApprovalFlow> =
        dao.getVotesForEntity(entityId, type)

    fun getPendingApprovals(): Flow<List<ApprovalFlow>> = dao.getPendingApprovals()
    fun getCompletedApprovals(): Flow<List<ApprovalFlow>> = dao.getCompletedApprovals()

    // ========== Realtime Sync Initialization ==========

    /**
     * Starts realtime Firestore listener for "approvals" collection.
     * Automatically upserts incoming changes into local Room DB.
     */
    fun startRealtimeSync() {
        // RealtimeSyncRepository already has a global listener on "approvals"
        // so no need for per-collection start — but we provide a local hook if needed later
    }

    /**
     * Manually triggers sync of unsynced ApprovalFlow records.
     * Use this after bulk inserts or initial login.
     */
    suspend fun pushUnsynced() {
        realtimeSyncRepository.pushAllUnsynced()
    }
}
