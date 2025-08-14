package com.selfgrowthfund.sgf.model.enums

interface LabelledEnum {
    val label: String
}

enum class DepositStatus(override val label: String) : LabelledEnum {
    Pending("Pending"),
    Approved("Approved"),
    Rejected("Rejected"),
    AutoRejected("Auto-Rejected")
}

enum class PaymentMode(override val label: String) : LabelledEnum {
    CASH("Cash"),
    BANK_TRANSFER("Bank Transfer"),
    ONLINE_PAYMENT("Online Payment"),
    CHEQUE("Cheque"),
    OTHER("Other Payment Method");

    companion object {
        fun fromLabel(label: String): PaymentMode {
            return entries.firstOrNull { it.label == label } ?: OTHER
        }

        fun getAllLabels(): List<String> = entries.map { it.label }
    }
}
enum class BorrowingStatus(override val label: String) : LabelledEnum {
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

enum class ShareholderStatus(override val label: String) : LabelledEnum {
    Active("Active"),
    Inactive("Inactive");

    companion object {
        fun fromString(value: String): ShareholderStatus =
            entries.firstOrNull { it.label.equals(value, ignoreCase = true) } ?: Inactive
    }
}

enum class PaymentStatus(override val label: String) : LabelledEnum {
    ON_TIME("On Time"),
    LATE("Late"),
    PENDING("Pending");

    fun toDisplayString(): String = label

    companion object {
        fun fromString(value: String): PaymentStatus =
            entries.firstOrNull { it.label.equals(value, ignoreCase = true) } ?: PENDING
    }
}

enum class InvesteeType(override val label: String) : LabelledEnum {
    Shareholder("Shareholder"),
    External("External");
}

enum class OwnershipType(override val label: String) : LabelledEnum {
    Individual("Individual"),
    Joint("Joint"),
    GroupOwned("Group-owned");
}

enum class InvestmentStatus(override val label: String) : LabelledEnum {
    Active("Active"),
    Closed("Closed"),
    Sold("Sold"),
    WrittenOff("Written-Off");
}