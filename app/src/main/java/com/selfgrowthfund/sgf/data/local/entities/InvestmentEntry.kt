package com.selfgrowthfund.sgf.data.local.entities


import com.selfgrowthfund.sgf.model.enums.*
import com.selfgrowthfund.sgf.utils.IdGenerator
import java.time.LocalDate

data class InvestmentEntry(
    val investeeType: InvesteeType = InvesteeType.External,
    val investeeName: String?,
    val ownershipType: OwnershipType = OwnershipType.Individual,
    val partnerNames: List<String>?,

    val investmentDate: LocalDate,
    val investmentType: InvestmentType = InvestmentType.Other,
    val investmentName: String,

    val amount: Double,
    val expectedProfitPercent: Double,
    val expectedReturnPeriod: Int,

    val modeOfPayment: PaymentMode = PaymentMode.OTHER,
    val status: InvestmentStatus = InvestmentStatus.Active,
    val remarks: String? = null,

    val entrySource: EntrySource = EntrySource.USER,
    val enteredBy: String? = null
) {
    fun toInvestment(lastInvestmentId: String?): Investment {
        val newId = IdGenerator.nextInvestmentId(lastInvestmentId)
        val expectedProfitAmount = amount * expectedProfitPercent / 100
        val returnDueDate = investmentDate.plusDays(expectedReturnPeriod.toLong())

        return Investment(
            investmentId = newId,
            investeeType = investeeType,
            investeeName = investeeName,
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
            modeOfPayment = modeOfPayment,
            status = status,
            remarks = remarks,
            entrySource = entrySource,
            enteredBy = enteredBy
        )
    }
}