package com.selfgrowthfund.sgf.data.local.types

enum class DepositStatus(val label: String) {
    Pending("Pending"),
    Approved("Approved"),
    Rejected("Rejected"),
    AutoRejected("Auto-Rejected");

    companion object {
        fun fromString(value: String): DepositStatus =
            entries.firstOrNull { it.label == value } ?: Pending
    }
    fun toDisplayString(): String = label
}
