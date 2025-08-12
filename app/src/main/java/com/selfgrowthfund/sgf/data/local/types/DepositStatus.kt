package com.selfgrowthfund.sgf.data.local.types

enum class DepositStatus(val label: String) {
    Pending("Pending"),
    Approved("Approved"),
    Rejected("Rejected"),
    AutoRejected("Auto-Rejected")
}
