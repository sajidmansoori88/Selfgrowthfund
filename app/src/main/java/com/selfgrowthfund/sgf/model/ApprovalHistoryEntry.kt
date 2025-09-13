package com.selfgrowthfund.sgf.model

data class ApprovalHistoryEntry(
    val id: String,
    val type: String,
    val approvedBy: String,
    val date: String,
    val status: String // Approved, Rejected
)