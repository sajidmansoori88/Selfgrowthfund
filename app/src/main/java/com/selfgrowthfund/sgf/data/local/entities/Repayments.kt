    package com.selfgrowthfund.sgf.data.local.entities

    import androidx.room.*
    import com.selfgrowthfund.sgf.data.local.converters.AppTypeConverters
    import com.selfgrowthfund.sgf.model.enums.ApprovalAction
    import com.selfgrowthfund.sgf.model.enums.ApprovalStage
    import com.selfgrowthfund.sgf.model.enums.BorrowingStatus
    import com.selfgrowthfund.sgf.model.enums.PaymentMode
    import java.time.Instant
    import java.time.LocalDate
    import java.time.format.DateTimeFormatter
    import java.time.temporal.ChronoUnit

    @Entity(
        tableName = "repayments",
        indices = [Index(value = ["borrowId"]), Index(value = ["repaymentDate"])]
    )
    @TypeConverters(AppTypeConverters::class)
    data class Repayment(

        // Primary key: provisionalId generated at submission time
        @PrimaryKey
        @ColumnInfo(name = "provisional_id")
        val provisionalId: String,

        // Admin-assigned repaymentId (nullable until admin finalizes)
        @ColumnInfo(name = "repayment_id")
        val repaymentId: String? = null,

        @ColumnInfo(name = "borrowId")
        val borrowId: String,

        @ColumnInfo(name = "shareholderName")
        val shareholderName: String,

        @ColumnInfo(name = "outstandingBefore")
        val outstandingBefore: Double,

        @ColumnInfo(name = "penaltyDue")
        val penaltyDue: Double,

        @ColumnInfo(name = "repaymentDate")
        val repaymentDate: LocalDate,

        @ColumnInfo(name = "principalRepaid")
        val principalRepaid: Double,

        @ColumnInfo(name = "penaltyPaid")
        val penaltyPaid: Double,

        @ColumnInfo(name = "totalAmountPaid")
        val totalAmountPaid: Double = principalRepaid + penaltyPaid,

        @ColumnInfo(name = "modeOfPayment")
        val modeOfPayment: PaymentMode = PaymentMode.OTHER,

        @ColumnInfo(name = "finalOutstanding")
        val finalOutstanding: Double = outstandingBefore - principalRepaid,

        @ColumnInfo(name = "borrowingStatus")
        val borrowingStatus: BorrowingStatus = if (finalOutstanding <= 0.01)
            BorrowingStatus.CLOSED else BorrowingStatus.ACTIVE,

        @ColumnInfo(name = "penaltyCalculationNotes")
        val penaltyCalculationNotes: String? = null,

        // Notes from creator (separate from approval notes)
        @ColumnInfo(name = "notes")
        val notes: String? = null,

        // Audit
        @ColumnInfo(name = "createdBy")
        val createdBy: String,

        @ColumnInfo(name = "createdAt")
        val createdAt: Instant = Instant.now(),

        // 🔄 Approval workflow (new standard fields)
        @ColumnInfo(name = "approval_status")
        val approvalStatus: ApprovalStage = ApprovalStage.PENDING,

        @ColumnInfo(name = "approved_by")
        val approvedBy: String? = null,

        @ColumnInfo(name = "approval_notes")
        val approvalNotes: String? = null,

        @ColumnInfo(name = "updated_at")
        val updatedAt: Instant = Instant.now(),

        // Optional sync info
        @ColumnInfo(name = "isSynced")
        val isSynced: Boolean = false,

        @ColumnInfo(name = "entry_source")
        val entrySource: String? = null,

        // Legacy for migration (can be dropped after data cleanup)
        @Deprecated("Use approvalStatus: ApprovalStage instead")
        @ColumnInfo(name = "legacy_approval_action")
        val legacyApprovalAction: ApprovalAction? = null
    ) {
        companion object {
            private val displayFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

            fun calculatePenalty(
                borrowStartDate: LocalDate,
                dueDate: LocalDate,
                repaymentDate: LocalDate,
                outstandingBefore: Double,
                previousRepayments: List<Repayment>
            ): Pair<Double, String> {
                val gracePeriodEnd = dueDate.plusDays(45)

                if (repaymentDate.isBefore(gracePeriodEnd)) {
                    return 0.0 to "No penalty (within grace period until ${formatDate(gracePeriodEnd)})"
                }

                val daysLate = ChronoUnit.DAYS.between(dueDate, repaymentDate).toInt()
                val monthsLate = daysLate / 30
                val penalty = outstandingBefore * 0.01 * monthsLate

                return penalty to "1% monthly penalty for $monthsLate month(s) late ($daysLate days)"
            }

            private fun formatDate(date: LocalDate): String {
                return date.format(displayFormatter)
            }
        }
    }
