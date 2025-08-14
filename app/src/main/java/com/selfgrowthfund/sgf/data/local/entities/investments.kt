package com.selfgrowthfund.sgf.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.selfgrowthfund.sgf.data.local.converters.AppTypeConverters
import java.time.LocalDateTime

@Entity(tableName = "investments")
@TypeConverters(AppTypeConverters::class)
data class Investment(
    @PrimaryKey
    val investmentId: String,

    val investeeType: String,
    val investeeName: String?,
    val ownershipType: String,
    val partnerNames: String?,

    val investmentDate: LocalDateTime,
    val investmentType: String,
    val investmentName: String,

    val amount: Double,
    val expectedProfitPercent: Double,
    val expectedProfitAmount: Double,
    val expectedReturnPeriod: Int,

    val returnDueDate: LocalDateTime,

    val modeOfPayment: String,
    val status: String,
    val remarks: String?
)
