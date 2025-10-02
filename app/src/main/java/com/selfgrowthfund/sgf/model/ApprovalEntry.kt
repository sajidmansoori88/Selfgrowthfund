package com.selfgrowthfund.sgf.model

import com.selfgrowthfund.sgf.model.enums.ApprovalAction
import com.selfgrowthfund.sgf.model.enums.ApprovalType
import java.time.Instant

data class ApprovalEntry(
    val entityId: String,              // 🔄 renamed from id → entityId (matches repo logic)
    val type: ApprovalType,
    val approvalAction: ApprovalAction,
    val submitterName: String,
    val notes: String? = null,         // ✅ add notes for remarks/justifications
    val createdAt: Instant = Instant.now()
)

data class ApprovalGroup(
    val title: String,
    val entries: List<ApprovalEntry>
)
