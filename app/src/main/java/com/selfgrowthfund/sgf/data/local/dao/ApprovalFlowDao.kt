package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.entities.ApprovalFlow
import com.selfgrowthfund.sgf.model.enums.ApprovalType
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface ApprovalFlowDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(flow: ApprovalFlow)

    @Query(
        """
        SELECT * FROM approval_flow
        WHERE entityType = :type
          AND approvedAt BETWEEN :start AND :end
        ORDER BY approvedAt DESC
    """
    )
    suspend fun getFlowsByTypeBetween(
        type: ApprovalType,
        start: Instant,
        end: Instant
    ): List<ApprovalFlow>

    @Query(
        """
        SELECT * FROM approval_flow
        WHERE approvedAt BETWEEN :start AND :end
        ORDER BY approvedAt DESC
    """
    )
    suspend fun getAllFlowsBetween(
        start: Instant,
        end: Instant
    ): List<ApprovalFlow>

    // ✅ Count total approvals for a given entity
    @Query(
        """
    SELECT COUNT(*) FROM approval_flow
    WHERE entityId = :entityId
      AND entityType = :type
      AND approval_action = 'APPROVE'
"""
    )
    suspend fun countApprovedVotes(entityId: String, type: ApprovalType): Int

    // ✅ Count total rejections for a given entity
    @Query(
        """
    SELECT COUNT(*) FROM approval_flow
    WHERE entityId = :entityId
      AND entityType = :type
      AND approval_action = 'REJECT'
"""
    )
    suspend fun countRejectedVotes(entityId: String, type: ApprovalType): Int

    // ✅ Fetch all votes for a specific entity (for PDF export)
    @Query(
        """
    SELECT * FROM approval_flow
    WHERE entityId = :entityId
      AND entityType = :type
    ORDER BY approvedAt ASC
"""
    )
    suspend fun getVotesForEntity(entityId: String, type: ApprovalType): List<ApprovalFlow>

    // ---------- NEW: Pending approvals flow ----------
    @Query("""
        SELECT * FROM approval_flow
        WHERE approvedAt IS NULL
        ORDER BY createdAt DESC
    """)
    fun getPendingApprovals(): Flow<List<ApprovalFlow>>

    @Query("SELECT * FROM approval_flow ORDER BY createdAt DESC")
    fun getAllApprovals(): Flow<List<ApprovalFlow>>

    @Query("""
        SELECT * FROM approval_flow
        WHERE approvedAt IS NOT NULL
        ORDER BY approvedAt DESC
    """)
    fun getCompletedApprovals(): Flow<List<ApprovalFlow>>

    // --- 🔁 SYNC HELPERS ---
    @Query("SELECT * FROM approval_flow WHERE isSynced = 0")
    suspend fun getUnsynced(): List<ApprovalFlow>

    @Query("SELECT * FROM approval_flow ORDER BY createdAt DESC")
    suspend fun getAllApprovalsOnce(): List<ApprovalFlow>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(approvals: List<ApprovalFlow>)

    @Update
    suspend fun update(approvalFlow: ApprovalFlow)

    // --- 🆕 Added for RealtimeSyncRepository compatibility ---

    /** Delete a single approval record */
    @Delete
    suspend fun delete(flow: ApprovalFlow)

    /** Optional: delete all local approvals (e.g., on logout) */
    @Query("DELETE FROM approval_flow")
    suspend fun clearAll()
}
