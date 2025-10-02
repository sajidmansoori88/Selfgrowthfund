package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.converters.AppTypeConverters
import com.selfgrowthfund.sgf.model.enums.*
import java.time.LocalDate
import java.util.UUID

@Entity(
    tableName = "investments",
    indices = [Index(value = ["investmentId"], unique = true)]
)
@TypeConverters(AppTypeConverters::class)
data class Investment(

    // Primary key: provisionalId (temporary, generated at submission time)
    @PrimaryKey(autoGenerate = false)
    val provisionalId: String = UUID.randomUUID().toString(),

    // Admin-assigned ID (nullable until approval/finalization)
    val investmentId: String? = null,

    // --- Domain fields ---
    val investeeType: InvesteeType = InvesteeType.Shareholder,
    val investeeName: String,          // auto-filled for user, dropdown for treasurer
    val shareholderId: String,         // auto-filled from logged-in user or dropdown
    val ownershipType: OwnershipType = OwnershipType.Individual,
    val partnerNames: String? = null,  // comma-separated

    val investmentDate: LocalDate,
    val investmentType: InvestmentType = InvestmentType.Other,
    val investmentName: String,

    val amount: Double,
    val expectedProfitPercent: Double,
    val expectedProfitAmount: Double,  // could be derived instead of stored
    val expectedReturnPeriod: Int,

    // Nullable: filled only after Treasurer release
    val returnDueDate: LocalDate? = null,

    // Temporary: retained for existing DAO queries
    @Deprecated("Will be removed once Treasurer flow is implemented")
    val status: InvestmentStatus = InvestmentStatus.Active,

    val remarks: String? = null,

    // --- Approval workflow (new standard) ---
    @ColumnInfo(name = "approval_status")
    val approvalStatus: ApprovalStage = ApprovalStage.PENDING,

    @ColumnInfo(name = "approved_by")
    val approvedBy: String? = null,

    @ColumnInfo(name = "approval_notes")
    val approvalNotes: String? = null,

    @ColumnInfo(name = "updated_at")
    val updatedAt: LocalDate = LocalDate.now(),

    // --- Metadata ---
    val createdAt: LocalDate = LocalDate.now(),
    val entrySource: EntrySource = EntrySource.User,
    val enteredBy: String? = null,

    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false,

    // Legacy compatibility for smooth migration
    @Deprecated("Use approvalStatus: ApprovalStage instead")
    val legacyApprovalAction: ApprovalAction? = null
)
