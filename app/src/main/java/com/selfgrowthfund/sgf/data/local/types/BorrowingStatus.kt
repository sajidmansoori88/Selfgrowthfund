package com.selfgrowthfund.sgf.data.local.types

enum class BorrowingStatus(val label: String) {
    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    ACTIVE("Active"),
    COMPLETED("Completed");

    companion object {
        private val closedStatuses: Set<BorrowingStatus> = setOf(COMPLETED, REJECTED)
        private val activeStatuses: Set<BorrowingStatus> = setOf(PENDING, APPROVED, ACTIVE)

        fun fromLabel(label: String): BorrowingStatus =
            entries.firstOrNull { it.label == label } ?: PENDING

        fun getAll(): List<BorrowingStatus> = entries.toList()

        fun getActive(): Set<BorrowingStatus> = activeStatuses

        fun getClosed(): Set<BorrowingStatus> = closedStatuses

        fun getClosedStatuses(): Set<BorrowingStatus> = closedStatuses
    }

    fun isClosed(): Boolean = this in closedStatuses
}