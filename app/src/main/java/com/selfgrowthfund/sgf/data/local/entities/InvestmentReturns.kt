package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.converters.AppTypeConverters
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
    indices = [Index("investmentId")]
)
@TypeConverters(AppTypeConverters::class)
data class InvestmentReturns(
    @PrimaryKey
    val returnId: String,

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

    val entrySource: EntrySource = EntrySource.ADMIN,
    val enteredBy: String? = null
) {
    constructor(
        returnId: String,
        investment: Investment,
        amountReceived: Double,
        modeOfPayment: PaymentMode,
        returnDate: LocalDate = LocalDate.now(),
        remarks: String? = null,
        entrySource: EntrySource = EntrySource.ADMIN,
        enteredBy: String? = null
    ) : this(
        returnId = returnId,
        investmentId = investment.investmentId,
        investmentName = investment.investmentName,
        expectedReturnPeriod = investment.expectedReturnPeriod,
        actualReturnPeriod = calculateDaysBetween(investment.investmentDate, returnDate),
        amountInvested = investment.amount,
        amountReceived = amountReceived,
        expectedProfitPercent = investment.expectedProfitPercent,
        actualProfitPercent = calculateActualProfitPercent(investment.amount, amountReceived),
        expectedProfitAmount = investment.expectedProfitAmount,
        actualProfitAmount = amountReceived - investment.amount,
        profitPercentVariance = calculateActualProfitPercent(
            investment.amount,
            amountReceived
        ) - investment.expectedProfitPercent,
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