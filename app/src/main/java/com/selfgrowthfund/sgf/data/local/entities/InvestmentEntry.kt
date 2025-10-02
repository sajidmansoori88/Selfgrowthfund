package com.selfgrowthfund.sgf.data.local.entities

import com.selfgrowthfund.sgf.model.enums.*
import java.time.LocalDate
import java.util.UUID

data class InvestmentEntry(
    val investeeType: InvesteeType = InvesteeType.Shareholder,
    val investeeName: String,                 // required now
    val shareholderId: String,                // required now
    val ownershipType: OwnershipType = OwnershipType.Individual,
    val partnerNames: String? = null,         // comma-separated

    val investmentDate: LocalDate,
    val investmentType: InvestmentType = InvestmentType.Other,
    val investmentName: String,

    val amount: Double,
    val expectedProfitPercent: Double,
    val expectedReturnPeriod: Int,

    val modeOfPayment: PaymentMode = PaymentMode.OTHER,
    val status: InvestmentStatus = InvestmentStatus.Active,
    val remarks: String? = null,

    val entrySource: EntrySource = EntrySource.User,
    val enteredBy: String? = null
) {
    fun toInvestment(): Investment {
        val expectedProfitAmount = amount * expectedProfitPercent / 100
        val returnDueDate = investmentDate.plusDays(expectedReturnPeriod.toLong())

        return Investment(
            provisionalId = UUID.randomUUID().toString(),  // always generated
            investmentId = null,                           // will be set on approval
            investeeType = investeeType,
            investeeName = investeeName,
            shareholderId = shareholderId,
            ownershipType = ownershipType,
            partnerNames = partnerNames,
            investmentDate = investmentDate,
            investmentType = investmentType,
            investmentName = investmentName,
            amount = amount,
            expectedProfitPercent = expectedProfitPercent,
            expectedProfitAmount = expectedProfitAmount,
            expectedReturnPeriod = expectedReturnPeriod,
            returnDueDate = returnDueDate,
            status = status,
            remarks = remarks,
            approvalStatus = ApprovalStage.PENDING,
            entrySource = entrySource,
            enteredBy = enteredBy
        )
    }
}
