package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.converters.AppTypeConverters
import org.threeten.bp.LocalDate

@Entity(tableName = "investments")
@TypeConverters(AppTypeConverters::class)
data class Investment(
    @PrimaryKey(autoGenerate = false)
    val investmentId: String,

    val investeeType: String,
    val investeeName: String?,

    val ownershipType: String,
    val partnerNames: List<String>?,

    val investmentDate: LocalDate,
    val investmentType: String,
    val investmentName: String,

    val amount: Double,
    val expectedProfitPercent: Double,
    val expectedProfitAmount: Double,
    val expectedReturnPeriod: Int,
    val returnDueDate: LocalDate,

    val modeOfPayment: String,
    val status: String,
    val remarks: String? = null
) {
    constructor(
        investmentId: String,
        investeeType: String,
        investeeName: String?,
        ownershipType: String,
        partnerNames: List<String>?,
        investmentDate: LocalDate,
        investmentType: String,
        investmentName: String,
        amount: Double,
        expectedProfitPercent: Double,
        expectedReturnPeriod: Int,
        modeOfPayment: String,
        status: String,
        remarks: String? = null
    ) : this(
        investmentId = investmentId,
        investeeType = investeeType,
        investeeName = investeeName,
        ownershipType = ownershipType,
        partnerNames = partnerNames,
        investmentDate = investmentDate,
        investmentType = investmentType,
        investmentName = investmentName,
        amount = amount,
        expectedProfitPercent = expectedProfitPercent,
        expectedProfitAmount = amount * expectedProfitPercent / 100,
        expectedReturnPeriod = expectedReturnPeriod,
        returnDueDate = investmentDate.plusDays(expectedReturnPeriod.toLong()),
        modeOfPayment = modeOfPayment,
        status = status,
        remarks = remarks
    )
}