package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.selfgrowthfund.sgf.model.enums.ApprovalAction
import com.selfgrowthfund.sgf.model.enums.ApprovalStage
import com.selfgrowthfund.sgf.model.enums.PaymentMode
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "other_expenses")
data class OtherExpense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val date: LocalDate,
    val amount: Double,
    val remarks: String, // replaces 'description' and 'category'
    val modeOfPayment: PaymentMode = PaymentMode.OTHER,

    // --- Approval workflow ---
    val approvalStatus: ApprovalStage = ApprovalStage.PENDING,
    val approvedBy: String? = null,
    val approvalNotes: String? = null,
    val updatedAt: Instant = Instant.now(),

    // --- Metadata ---
    val recordedBy: String, // shareholderId or adminId
    val createdAt: Instant = Instant.now(),
    val isSynced: Boolean = false,

    // Legacy compatibility
    @Deprecated("Use approvalStatus: ApprovalStage instead")
    val legacyApprovalAction: ApprovalAction? = null
)
