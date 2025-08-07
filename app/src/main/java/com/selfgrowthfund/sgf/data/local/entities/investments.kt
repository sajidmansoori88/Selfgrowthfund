package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "investments")
data class Investment(
    @PrimaryKey(autoGenerate = false)
    val investmentId: String, // Format: IN0001, IN0002, etc.

    val investeeType: String, // "Shareholder", "External", or "Group"
    val investeeName: String?, // Nullable for Group type

    val ownershipType: String, // "Individual", "Joint", or "Group-owned"
    val partnerNames: List<String>?, // Nullable unless OwnershipType = Joint

    val investmentDate: Date,
    val investmentType: String, // "Cash", "Property", etc.
    val investmentName: String,

    val amount: Double,
    val expectedProfitPercent: Double,
    val expectedProfitAmount: Double, // Calculated field
    val expectedReturnPeriod: Int, // in days
    val returnDueDate: Date, // Calculated field

    val modeOfPayment: String, // "Cash", "UPI", etc.
    val status: String, // "Active", "Closed", etc.
    val remarks: String? = null // Optional
) {
    // Secondary constructor for auto-generating calculated fields
    constructor(
        investmentId: String,
        investeeType: String,
        investeeName: String?,
        ownershipType: String,
        partnerNames: List<String>?,
        investmentDate: Date,
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
        returnDueDate = Date(investmentDate.time + expectedReturnPeriod * 24 * 60 * 60 * 1000L),
        modeOfPayment = modeOfPayment,
        status = status,
        remarks = remarks
    )
}