package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
      AND approval_action = 'APPROVED'
"""
    )
    suspend fun countApprovedVotes(entityId: String, type: ApprovalType): Int

    // ✅ Count total rejections for a given entity
    @Query(
        """
    SELECT COUNT(*) FROM approval_flow
    WHERE entityId = :entityId
      AND entityType = :type
      AND approval_action = 'REJECTED'
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
    /**
     * Returns a Flow of ApprovalFlow entries that are pending:
     * we consider pending those with no approvedAt timestamp yet.
     *
     * This is a safe way to get "pending" approvals without relying
     * on specific enum column names that may differ across schema versions.
     */
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

}

