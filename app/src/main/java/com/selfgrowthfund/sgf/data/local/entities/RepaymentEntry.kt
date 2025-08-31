package com.selfgrowthfund.sgf.data.local.entities

import com.selfgrowthfund.sgf.model.enums.PaymentMode
import com.selfgrowthfund.sgf.utils.IdGenerator
import java.time.LocalDate

data class RepaymentEntry(
    val borrowId: String,
    val shareholderName: String,
    val repaymentDate: LocalDate,
    val principalRepaid: Double,
    val penaltyPaid: Double,
    val modeOfPayment: PaymentMode = PaymentMode.OTHER,
    val notes: String? = null
) {
    fun toRepayment(
        lastRepaymentId: String?,
        outstandingBefore: Double,
        borrowStartDate: LocalDate,
        dueDate: LocalDate,
        previousRepayments: List<Repayment>
    ): Repayment {
        val newId = IdGenerator.nextRepaymentId(lastRepaymentId)
        val (penaltyDue, penaltyNotes) = Repayment.calculatePenalty(
            borrowStartDate = borrowStartDate,
            dueDate = dueDate,
            repaymentDate = repaymentDate,
            outstandingBefore = outstandingBefore,
            previousRepayments = previousRepayments
        )

        return Repayment(
            repaymentId = newId,
            borrowId = borrowId,
            shareholderName = shareholderName,
            outstandingBefore = outstandingBefore,
            penaltyDue = penaltyDue,
            repaymentDate = repaymentDate,
            principalRepaid = principalRepaid,
            penaltyPaid = penaltyPaid,
            modeOfPayment = modeOfPayment,
            notes = notes,
            penaltyCalculationNotes = penaltyNotes
        )
    }
}