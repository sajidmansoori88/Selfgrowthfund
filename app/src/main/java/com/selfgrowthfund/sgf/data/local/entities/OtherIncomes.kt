package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.converters.AppTypeConverters
import com.selfgrowthfund.sgf.model.enums.ApprovalAction
import com.selfgrowthfund.sgf.model.enums.ApprovalStage
import com.selfgrowthfund.sgf.model.enums.PaymentMode
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "other_incomes")
@TypeConverters(AppTypeConverters::class)
data class OtherIncome(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val date: LocalDate,
    val amount: Double,
    val remarks: String, // replaces 'source' and 'description'
    val modeOfPayment: PaymentMode = PaymentMode.OTHER,

    // --- Approval workflow ---
    @ColumnInfo(name = "approval_status")
    val approvalStatus: ApprovalStage = ApprovalStage.PENDING,

    @ColumnInfo(name = "approved_by")
    val approvedBy: String? = null,

    @ColumnInfo(name = "approval_notes")
    val approvalNotes: String? = null,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant = Instant.now(),

    // --- Metadata ---
    val recordedBy: String, // shareholderId or adminId
    val createdAt: Instant = Instant.now(),

    val isSynced: Boolean = false,

    // Legacy compatibility
    @Deprecated("Use approvalStatus: ApprovalStage instead")
    val legacyApprovalAction: ApprovalAction? = null
)
