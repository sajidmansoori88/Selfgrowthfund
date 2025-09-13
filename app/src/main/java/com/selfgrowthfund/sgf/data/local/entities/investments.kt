package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.*
import com.selfgrowthfund.sgf.data.local.converters.AppTypeConverters
import com.selfgrowthfund.sgf.model.enums.EntrySource
import com.selfgrowthfund.sgf.model.enums.*
import java.time.LocalDate

@Entity(tableName = "investments")
@TypeConverters(AppTypeConverters::class)
data class Investment(
    @PrimaryKey(autoGenerate = false)
    val investmentId: String,

    val investeeType: InvesteeType = InvesteeType.External,
    val investeeName: String?,

    val ownershipType: OwnershipType = OwnershipType.Individual,
    val partnerNames: List<String>?,

    val investmentDate: LocalDate,
    val investmentType: InvestmentType = InvestmentType.Other,
    val investmentName: String,

    val amount: Double,
    val expectedProfitPercent: Double,
    val expectedProfitAmount: Double,
    val expectedReturnPeriod: Int,
    val returnDueDate: LocalDate,

    val modeOfPayment: PaymentMode = PaymentMode.OTHER,
    val status: InvestmentStatus = InvestmentStatus.Active,
    val remarks: String? = null,
    val approvalStatus: ApprovalAction = ApprovalAction.PENDING,
    val createdAt: LocalDate = LocalDate.now(),
    val entrySource: EntrySource = EntrySource.USER,
    val enteredBy: String? = null
)