package com.selfgrowthfund.sgf.data.local.entities

import com.selfgrowthfund.sgf.model.enums.PaymentMode
import com.selfgrowthfund.sgf.model.enums.ApprovalStage
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class RepaymentEntry(
    val borrowId: String,
    val shareholderName: String,
    val repaymentDate: LocalDate,
    val principalRepaid: Double,
    val penaltyPaid: Double,
    val modeOfPayment: PaymentMode = PaymentMode.OTHER,
    val notes: String? = null,
    val createdBy: String
) {
    fun toRepayment(
        outstandingBefore: Double,
        borrowStartDate: LocalDate,
        dueDate: LocalDate,
        previousRepayments: List<Repayment>
    ): Repayment {
        val provisionalId = UUID.randomUUID().toString() // 🔑 new PK
        val (penaltyDue, penaltyNotes) = Repayment.calculatePenalty(
            borrowStartDate = borrowStartDate,
            dueDate = dueDate,
            repaymentDate = repaymentDate,
            outstandingBefore = outstandingBefore,
            previousRepayments = previousRepayments
        )

        return Repayment(
            provisionalId = provisionalId,
            repaymentId = null, // Assigned by Admin later
            borrowId = borrowId,
            shareholderName = shareholderName,
            outstandingBefore = outstandingBefore,
            penaltyDue = penaltyDue,
            repaymentDate = repaymentDate,
            principalRepaid = principalRepaid,
            penaltyPaid = penaltyPaid,
            modeOfPayment = modeOfPayment,
            notes = notes,
            penaltyCalculationNotes = penaltyNotes,
            createdBy = createdBy,
            createdAt = Instant.now(),
            approvalStatus = ApprovalStage.PENDING,
            approvedBy = null,
            approvalNotes = null,
            updatedAt = Instant.now(),
            isSynced = false,
            entrySource = "USER"
        )
    }
}
