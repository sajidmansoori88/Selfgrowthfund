package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.converters.AppTypeConverters
import com.selfgrowthfund.sgf.model.enums.ApprovalAction
import com.selfgrowthfund.sgf.model.enums.ApprovalStage
import com.selfgrowthfund.sgf.model.enums.EntrySource
import com.selfgrowthfund.sgf.model.enums.PaymentMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Entity(
    tableName = "investment_returns",
    foreignKeys = [
        ForeignKey(
            entity = Investment::class,
            parentColumns = ["investmentId"],
            childColumns = ["investmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("investmentId"), Index("provisionalId")]
)
@TypeConverters(AppTypeConverters::class)
data class InvestmentReturns(

    // --- Identifiers ---
    @PrimaryKey(autoGenerate = false)
    val provisionalId: String = java.util.UUID.randomUUID().toString(),

    // --- Final ID (assigned after Admin approval) ---
    val returnId: String? = null, // Final ID after Admin approval

    // --- Investment Info ---
    val investmentId: String,
    val investmentName: String,

    val expectedReturnPeriod: Int,
    val actualReturnPeriod: Int,

    val amountInvested: Double,
    val amountReceived: Double,

    val expectedProfitPercent: Double,
    val actualProfitPercent: Double,

    val expectedProfitAmount: Double,
    val actualProfitAmount: Double,

    val profitPercentVariance: Double,
    val profitAmountVariance: Double,

    val returnDate: LocalDate,
    val modeOfPayment: PaymentMode,
    val remarks: String? = null,

    // --- Approval workflow ---
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
    val entrySource: EntrySource = EntrySource.MemberAdmin,
    val enteredBy: String? = null,

    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false,

    // Legacy compatibility
    @Deprecated("Use approvalStatus instead")
    val legacyApprovalAction: ApprovalAction? = null
)
 {
    constructor(
        returnId: String,
        investment: Investment,
        amountReceived: Double,
        modeOfPayment: PaymentMode,
        returnDate: LocalDate = LocalDate.now(),
        remarks: String? = null,
        entrySource: EntrySource = EntrySource.MemberAdmin,
        enteredBy: String? = null
    ) : this(
        returnId = returnId,
        investmentId = investment.investmentId ?: error("Investment must be approved before returns"),
        investmentName = investment.investmentName,
        expectedReturnPeriod = investment.expectedReturnPeriod,
        actualReturnPeriod = calculateDaysBetween(investment.investmentDate, returnDate),
        amountInvested = investment.amount,
        amountReceived = amountReceived,
        expectedProfitPercent = investment.expectedProfitPercent,
        actualProfitPercent = calculateActualProfitPercent(investment.amount, amountReceived),
        expectedProfitAmount = investment.expectedProfitAmount,
        actualProfitAmount = amountReceived - investment.amount,
        profitPercentVariance = calculateActualProfitPercent(investment.amount, amountReceived) -
                investment.expectedProfitPercent,
        profitAmountVariance = (amountReceived - investment.amount) - investment.expectedProfitAmount,
        returnDate = returnDate,
        modeOfPayment = modeOfPayment,
        remarks = remarks,
        entrySource = entrySource,
        enteredBy = enteredBy
    )

    companion object {
        private fun calculateDaysBetween(startDate: LocalDate, endDate: LocalDate): Int {
            return ChronoUnit.DAYS.between(startDate, endDate).toInt()
        }

        private fun calculateActualProfitPercent(
            amountInvested: Double,
            amountReceived: Double
        ): Double {
            return ((amountReceived - amountInvested) / amountInvested) * 100
        }
    }
}
