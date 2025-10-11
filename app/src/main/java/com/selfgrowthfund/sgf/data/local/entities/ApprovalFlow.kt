package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.selfgrowthfund.sgf.model.enums.ApprovalAction
import com.selfgrowthfund.sgf.model.enums.ApprovalType
import com.selfgrowthfund.sgf.model.enums.MemberRole
import java.time.Instant

/**
 * Tracks all approval or rejection actions made by members, treasurer, or admin
 * for any entity (Deposit, Borrowing, Investment, etc.).
 *
 * Used for multi-stage approval flow and audit (PDF export).
 */
@Entity(tableName = "approval_flow")
data class ApprovalFlow(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val entityType: ApprovalType,
    val entityId: String,

    val role: MemberRole,

    @ColumnInfo(name = "approval_action")
    val action: ApprovalAction,   // APPROVE, REJECT, or PENDING

    val approvedBy: String,
    val remarks: String? = null,

    val createdAt: Instant = Instant.now(),  // when this record was created
    val approvedAt: Instant? = null          // null = not yet acted upon
)

