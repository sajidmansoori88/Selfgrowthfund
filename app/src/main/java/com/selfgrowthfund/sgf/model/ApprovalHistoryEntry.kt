package com.selfgrowthfund.sgf.model

import com.selfgrowthfund.sgf.model.enums.ApprovalAction
import com.selfgrowthfund.sgf.model.enums.ApprovalType

data class ApprovalHistoryEntry(
    val id: String,
    val type: ApprovalType,
    val approvedBy: String,       // approver name or userId
    val date: String,             // consider LocalDate later
    val status: ApprovalAction,
    val shareholderId: String? = null
)