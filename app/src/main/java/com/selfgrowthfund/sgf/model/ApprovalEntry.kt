package com.selfgrowthfund.sgf.model

data class ApprovalEntry(
    val id: String,
    val type: String, // Deposit, Borrowing, etc.
    val submitterName: String,
    val date: String
)

data class ApprovalGroup(
    val title: String,
    val entries: List<ApprovalEntry>
)