package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.selfgrowthfund.sgf.model.enums.ApprovalAction
import com.selfgrowthfund.sgf.model.enums.ApprovalType
import com.selfgrowthfund.sgf.model.enums.MemberRole
import java.time.Instant

@Entity(tableName = "approval_flow")
data class ApprovalFlow(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    val entityType: ApprovalType,   // e.g. DEPOSIT, BORROWING, REPAYMENT
    val entityId: String,           // link to Deposit.depositId, Borrowing.borrowingId, etc.

    val role: MemberRole,           // TREASURER / ADMIN
    val action: ApprovalAction,     // APPROVED / REJECTED / PENDING
    val approvedBy: String,         // approver name or userId
    val approvedAt: Instant = Instant.now()
)
