package com.selfgrowthfund.sgf.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.selfgrowthfund.sgf.data.local.entities.ApprovalFlow
import com.selfgrowthfund.sgf.model.enums.ApprovalType
import java.time.Instant

@Dao
interface ApprovalFlowDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(flow: ApprovalFlow)

    @Query("""
        SELECT * FROM approval_flow
        WHERE entityType = :type
          AND approvedAt BETWEEN :start AND :end
        ORDER BY approvedAt DESC
    """)
    suspend fun getFlowsByTypeBetween(
        type: ApprovalType,
        start: Instant,
        end: Instant
    ): List<ApprovalFlow>

    @Query("""
        SELECT * FROM approval_flow
        WHERE approvedAt BETWEEN :start AND :end
        ORDER BY approvedAt DESC
    """)
    suspend fun getAllFlowsBetween(
        start: Instant,
        end: Instant
    ): List<ApprovalFlow>
}
