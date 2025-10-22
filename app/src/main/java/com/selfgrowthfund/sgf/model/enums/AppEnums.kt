package com.selfgrowthfund.sgf.model.enums

import androidx.compose.ui.graphics.Color

interface LabelledEnum {
    val label: String
}

// --- Deposit specific (legacy) ---
enum class DepositStatus(override val label: String) : LabelledEnum {
    Pending("Pending"),
    Approved("Approved"),
    Rejected("Rejected"),
    AutoRejected("Auto-Rejected");

    companion object {
        fun fromLabel(label: String): DepositStatus =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: Pending
    }
}

// --- Unified Approval Stage (new, richer workflow) ---
enum class ApprovalStage(override val label: String) : LabelledEnum {
    PENDING("Pending"),
    TREASURER_APPROVED("Treasurer Approved"),
    ADMIN_APPROVED("Admin Approved"),
    APPROVED("Approved"),
    REJECTED("Rejected");

    companion object {
        fun fromLabel(label: String): ApprovalStage =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: PENDING
    }
}

// --- Old ApprovalAction (kept for compatibility) ---
enum class ApprovalAction(override val label: String) : LabelledEnum {
    APPROVE("Approve"),
    REJECT("Reject"),
    PENDING("Pending Review");

    companion object {
        fun fromLabel(label: String): ApprovalAction =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: PENDING
    }
}

// --- Bridge Converters ---
fun ApprovalAction.toStage(): ApprovalStage = when (this) {
    ApprovalAction.PENDING -> ApprovalStage.PENDING
    ApprovalAction.APPROVE -> ApprovalStage.APPROVED
    ApprovalAction.REJECT -> ApprovalStage.REJECTED
}

fun ApprovalStage.toAction(): ApprovalAction = when (this) {
    ApprovalStage.PENDING -> ApprovalAction.PENDING
    ApprovalStage.APPROVED,
    ApprovalStage.TREASURER_APPROVED,
    ApprovalStage.ADMIN_APPROVED -> ApprovalAction.APPROVE
    ApprovalStage.REJECTED -> ApprovalAction.REJECT
}

// --- Other enums remain unchanged ---
enum class PaymentMode(override val label: String) : LabelledEnum {
    CASH("Cash"),
    BANK_TRANSFER("Bank Transfer"),
    ONLINE_PAYMENT("Online Payment"),
    CHEQUE("Cheque"),
    OTHER("Other Payment Method");

    companion object {
        fun fromLabel(label: String): PaymentMode =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: OTHER

        fun getAllLabels(): List<String> = entries.map { it.label }
    }
}

enum class BorrowingStatus(override val label: String) : LabelledEnum {
    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    ACTIVE("Active"),
    CLOSED("Closed"),
    OVERDUE("Overdue");

    companion object {
        private val closedStatuses: Set<BorrowingStatus> = setOf(CLOSED, REJECTED)
        private val activeStatuses: Set<BorrowingStatus> = setOf(PENDING, APPROVED, ACTIVE)

        fun fromLabel(label: String): BorrowingStatus =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: PENDING

        fun getAll(): List<BorrowingStatus> = entries.toList()
        fun getActive(): Set<BorrowingStatus> = activeStatuses
        fun getClosed(): Set<BorrowingStatus> = closedStatuses
    }

    fun isClosed(): Boolean = this in getClosed()
}


enum class ShareholderStatus(override val label: String) : LabelledEnum {
    Active("Active"),
    Inactive("Inactive"),
    Exited("Exited");

    companion object {
        fun fromLabel(label: String): ShareholderStatus =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: Inactive
    }
}

enum class PaymentStatus(override val label: String) : LabelledEnum {
    ON_TIME("On Time"),
    EARLY("Early"),
    LATE("Late"),
    PENDING("Pending");

    fun toDisplayString(): String = label

    companion object {
        fun fromLabel(label: String): PaymentStatus =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: PENDING
    }

}

enum class InvesteeType(override val label: String) : LabelledEnum {
    Shareholder("Shareholder"),
    External("External");

    companion object {
        fun fromLabel(label: String): InvesteeType =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: External
    }
}

enum class OwnershipType(override val label: String) : LabelledEnum {
    Individual("Individual"),
    Joint("Joint"),
    GroupOwned("Group-owned");

    companion object {
        fun fromLabel(label: String): OwnershipType =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: Individual
    }
}

enum class InvestmentType(override val label: String) : LabelledEnum {
    Trading("Trading"),
    Property("Property"),
    Microenterprise("Microenterprise"),
    Machinery("Machinery"),
    Other("Other");

    companion object {
        fun fromLabel(label: String): InvestmentType =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: Other
    }
}

enum class InvestmentStatus(override val label: String) : LabelledEnum {
    Active("Active"),
    Closed("Closed"),
    Sold("Sold"),
    WrittenOff("Written-Off");

    companion object {
        fun fromLabel(label: String): InvestmentStatus =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: Active
    }
}

enum class MemberRole(override val label: String) : LabelledEnum {
    MEMBER_ADMIN("Member Admin"),
    MEMBER_TREASURER("Member Treasurer"),
    MEMBER("Member");

    companion object {
        fun fromLabel(label: String): MemberRole =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: MEMBER

        fun getAllLabels(): List<String> = entries.map { it.label }
    }
}

enum class EntrySource(override val label: String) : LabelledEnum {
    User("Shareholder"),
    MemberAdmin ("Member Admin"),
    MemberTreasurer("Member Treasurer"),
    SYSTEM("System"),
    MIGRATION("Migration");

    companion object {
        fun fromLabel(label: String): EntrySource =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: User
    }
}

enum class TransactionType(override val label: String) : LabelledEnum {
    Deposit("Deposit"),
    Borrowing("Borrowing"),
    Repayment("Repayment"),
    Investment("Investment"),
    InvestmentReturn("Investment Return"),
    Expense("Expense"),
    Income("Income");

    companion object {
        fun fromLabel(label: String): TransactionType =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: Expense

        fun getAllLabels(): List<String> = entries.map { it.label }
    }
}

enum class ActionType(override val label: String) : LabelledEnum {
    BORROWING_APPROVAL("Borrowing Approval"),
    INVESTMENT_CONSENT("Investment Consent"),
    EXPENSE_REVIEW("Expense Review");

    companion object {
        fun fromLabel(label: String): ActionType =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: BORROWING_APPROVAL
    }
}

enum class ActionResponse(override val label: String) : LabelledEnum {
    APPROVE("Approve"),
    REJECT("Reject"),
    CONSENT("Consent"),
    DISSENT("Dissent"),
    PENDING("Pending");

    companion object {
        fun fromLabel(label: String): ActionResponse =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: PENDING
    }
}

enum class PenaltyType(override val label: String) : LabelledEnum {
    SHARE_DEPOSIT("Share Deposit"),
    BORROWING("Borrowing"),
    INVESTMENT("Investment"),
    OTHER("Other");

    companion object {
        fun fromLabel(label: String): PenaltyType =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: OTHER

        fun getAllLabels(): List<String> = entries.map { it.label }
    }

    fun getDisplayColor(): Color = when (this) {
        SHARE_DEPOSIT -> Color(0xFF4CAF50)
        BORROWING -> Color(0xFF2196F3)
        INVESTMENT -> Color(0xFFFF9800)
        OTHER -> Color.Gray
    }
}

enum class ApprovalType(override val label: String) : LabelledEnum {
    ALL("All"),
    DEPOSIT("Deposit"),
    BORROWING("Borrowing"),
    REPAYMENT("Repayment"),
    INVESTMENT("Investment"),
    INVESTMENT_RETURN("Investment Return"),
    OTHER_INCOME("Other Income"),
    OTHER_EXPENSE("Other Expense");

    companion object {
        fun fromLabel(label: String): ApprovalType =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: ALL

        fun getAllLabels(): List<String> = entries.map { it.label }
    }
}

enum class ExportType(val label: String) {
    CSV("CSV"),
    PDF("PDF");

    companion object {
        fun getAll() = entries
        fun fromLabel(label: String) = entries.firstOrNull { it.label == label } ?: CSV
    }
}
